package com.letstravel.service;

import com.letstravel.domain.Travel;
import com.letstravel.domain.enums.TravelStatus;
import com.letstravel.dto.travel.TravelSummary;
import com.letstravel.exception.EntityNotFoundException;
import com.letstravel.repository.TravelRepository;
import com.letstravel.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    private final Neo4jClient neo4jClient;
    private final TravelRepository travelRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String CACHE_PREFIX = "lt:reco:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    public List<TravelSummary> getRecommendations(String email) {
        userRepository.findByEmail(email)
            .orElseThrow(() -> new EntityNotFoundException("User", null));

        // Check Redis cache
        String cacheKey = CACHE_PREFIX + email;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            // Parse cached IDs and load travels
            String[] ids = cached.split(",");
            List<Long> travelIds = Arrays.stream(ids).map(Long::parseLong).toList();
            return loadTravelsByIds(travelIds);
        }

        // Query Neo4j for recommendations
        String cypher = """
            MATCH (u:User {email: $email})-[:SUBSCRIBED_TO]->(past:Travel)
            WITH u,
                 collect(DISTINCT [(past)-[:HAS_CATEGORY]->(c) | c.name]) AS likedCats,
                 collect(DISTINCT [(past)-[:IN_DESTINATION]->(d) | d.region]) AS likedRegions,
                 collect(DISTINCT [(past)-[:HAS_TAG]->(t) | t.name]) AS likedTags
            MATCH (rec:Travel)
            WHERE NOT (u)-[:SUBSCRIBED_TO]->(rec)
            OPTIONAL MATCH (rec)-[:HAS_CATEGORY]->(rc)
            OPTIONAL MATCH (rec)-[:IN_DESTINATION]->(rd)
            OPTIONAL MATCH (rec)-[:HAS_TAG]->(rt)
            WITH rec,
                 CASE WHEN rc.name IN likedCats[0] THEN 2 ELSE 0 END +
                 CASE WHEN rd.region IN likedRegions[0] THEN 2 ELSE 0 END +
                 size([t IN coalesce(likedTags[0],[]) WHERE t IN collect(rt.name)]) AS score
            WHERE score > 0
            RETURN rec.id AS travelId, score
            ORDER BY score DESC LIMIT 10
            """;

        try {
            List<Long> travelIds = neo4jClient.query(cypher)
                .bind(email).to("email")
                .fetchAs(Long.class)
                .mappedBy((typeSystem, record) -> record.get("travelId").asLong())
                .all()
                .stream().toList();

            if (!travelIds.isEmpty()) {
                String idStr = travelIds.stream().map(String::valueOf).collect(Collectors.joining(","));
                redisTemplate.opsForValue().set(cacheKey, idStr, CACHE_TTL);
                return loadTravelsByIds(travelIds);
            }
        } catch (Exception e) {
            log.warn("Neo4j recommendation query failed, using fallback: {}", e.getMessage());
        }

        // Cold start fallback: top-rated travels
        return getTrending();
    }

    public List<TravelSummary> getTrending() {
        return travelRepository.findTopPublishedByEnrollment(PageRequest.of(0, 10))
            .stream()
            .filter(t -> t.getStartDate().isAfter(LocalDate.now()))
            .map(TravelService::toSummary)
            .toList();
    }

    public void invalidateCache(String email) {
        redisTemplate.delete(CACHE_PREFIX + email);
    }

    private List<TravelSummary> loadTravelsByIds(List<Long> ids) {
        return ids.stream()
            .map(id -> travelRepository.findById(id).orElse(null))
            .filter(t -> t != null
                && t.getStatus() == TravelStatus.PUBLISHED
                && t.getStartDate().isAfter(LocalDate.now()))
            .map(TravelService::toSummary)
            .toList();
    }
}
