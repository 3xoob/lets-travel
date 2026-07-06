package com.letstravel.service;

import com.letstravel.domain.Travel;
import com.letstravel.neo4j.node.*;
import com.letstravel.neo4j.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class Neo4jSyncService {

    private final UserNodeRepository userNodeRepository;
    private final TravelNodeRepository travelNodeRepository;
    private final CategoryNodeRepository categoryNodeRepository;
    private final DestinationNodeRepository destinationNodeRepository;
    private final TagNodeRepository tagNodeRepository;

    private static final Map<String, String> COUNTRY_REGIONS;

    static {
        COUNTRY_REGIONS = new HashMap<>();
        COUNTRY_REGIONS.put("France", "europe");
        COUNTRY_REGIONS.put("Germany", "europe");
        COUNTRY_REGIONS.put("Spain", "europe");
        COUNTRY_REGIONS.put("Italy", "europe");
        COUNTRY_REGIONS.put("Portugal", "europe");
        COUNTRY_REGIONS.put("Netherlands", "europe");
        COUNTRY_REGIONS.put("Japan", "asia");
        COUNTRY_REGIONS.put("China", "asia");
        COUNTRY_REGIONS.put("Thailand", "asia");
        COUNTRY_REGIONS.put("India", "asia");
        COUNTRY_REGIONS.put("Vietnam", "asia");
        COUNTRY_REGIONS.put("Indonesia", "asia");
        COUNTRY_REGIONS.put("USA", "americas");
        COUNTRY_REGIONS.put("Brazil", "americas");
        COUNTRY_REGIONS.put("Mexico", "americas");
        COUNTRY_REGIONS.put("Argentina", "americas");
        COUNTRY_REGIONS.put("Colombia", "americas");
        COUNTRY_REGIONS.put("Canada", "americas");
        COUNTRY_REGIONS.put("Egypt", "africa");
        COUNTRY_REGIONS.put("Kenya", "africa");
        COUNTRY_REGIONS.put("Morocco", "africa");
        COUNTRY_REGIONS.put("South Africa", "africa");
        COUNTRY_REGIONS.put("Tanzania", "africa");
        COUNTRY_REGIONS.put("Nigeria", "africa");
        COUNTRY_REGIONS.put("Australia", "oceania");
        COUNTRY_REGIONS.put("New Zealand", "oceania");
        COUNTRY_REGIONS.put("Fiji", "oceania");
        COUNTRY_REGIONS.put("Papua New Guinea", "oceania");
        COUNTRY_REGIONS.put("Turkey", "europe");
        COUNTRY_REGIONS.put("Greece", "europe");
        COUNTRY_REGIONS.put("UAE", "asia");
        COUNTRY_REGIONS.put("Saudi Arabia", "asia");
    }

    @Transactional
    public void upsertTravel(Travel travel) {
        try {
            CategoryNode category = categoryNodeRepository
                .findById(travel.getCategory().name())
                .orElseGet(() -> categoryNodeRepository.save(new CategoryNode(travel.getCategory().name())));

            String destKey = travel.getDestinationCity() + ":" + travel.getDestinationCountry();
            String region = COUNTRY_REGIONS.getOrDefault(travel.getDestinationCountry(), "other");
            DestinationNode destination = destinationNodeRepository
                .findById(destKey)
                .orElseGet(() -> destinationNodeRepository.save(
                    DestinationNode.builder()
                        .key(destKey)
                        .city(travel.getDestinationCity())
                        .country(travel.getDestinationCountry())
                        .region(region)
                        .build()));

            Set<TagNode> tagNodes = new HashSet<>();
            if (travel.getTags() != null) {
                for (String tag : travel.getTags()) {
                    TagNode tn = tagNodeRepository.findById(tag.toLowerCase())
                        .orElseGet(() -> tagNodeRepository.save(new TagNode(tag.toLowerCase())));
                    tagNodes.add(tn);
                }
            }

            TravelNode travelNode = travelNodeRepository.findById(travel.getId())
                .orElse(TravelNode.builder().id(travel.getId()).build());
            travelNode.setTitle(travel.getTitle());
            travelNode.setCategory(category);
            travelNode.setDestination(destination);
            travelNode.setTags(tagNodes);
            travelNodeRepository.save(travelNode);
        } catch (Exception e) {
            log.error("Failed to sync travel {} to Neo4j: {}", travel.getId(), e.getMessage());
        }
    }

    @Transactional
    public void deleteTravel(Long travelId) {
        try {
            travelNodeRepository.deleteById(travelId);
        } catch (Exception e) {
            log.error("Failed to delete travel {} from Neo4j: {}", travelId, e.getMessage());
        }
    }

    @Transactional
    public void deleteUserNode(Long userId) {
        try {
            userNodeRepository.deleteById(userId);
        } catch (Exception e) {
            log.error("Failed to delete user {} from Neo4j: {}", userId, e.getMessage());
        }
    }

    @Transactional
    public void upsertSubscription(Long userId, String email, Long travelId) {
        try {
            UserNode userNode = userNodeRepository.findById(userId)
                .orElseGet(() -> userNodeRepository.save(
                    UserNode.builder().id(userId).email(email).build()));
            travelNodeRepository.findById(travelId).ifPresent(travelNode -> {
                userNode.getSubscribedTravels().add(travelNode);
                userNodeRepository.save(userNode);
            });
        } catch (Exception e) {
            log.error("Failed to sync subscription to Neo4j: {}", e.getMessage());
        }
    }
}
