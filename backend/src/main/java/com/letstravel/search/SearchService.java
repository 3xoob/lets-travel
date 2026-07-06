package com.letstravel.search;

import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.json.JsonData;
import com.letstravel.dto.travel.TravelSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final ElasticsearchTravelRepository travelRepository;

    public Page<TravelSummary> search(
            String q,
            String category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            LocalDate startAfter,
            LocalDate endBefore,
            Pageable pageable) {

        BoolQuery.Builder boolQuery = new BoolQuery.Builder();

        // Status filter: only PUBLISHED
        boolQuery.filter(TermQuery.of(t -> t.field("status").value("PUBLISHED"))._toQuery());

        // Full-text search
        if (q != null && !q.isBlank()) {
            boolQuery.must(MultiMatchQuery.of(m -> m
                .query(q)
                .fields("title^3", "description", "destinationCity^2", "destinationCountry^2", "managerName", "tags")
            )._toQuery());
        }

        // Category filter
        if (category != null && !category.isBlank()) {
            boolQuery.filter(TermQuery.of(t -> t.field("category").value(category.toUpperCase()))._toQuery());
        }

        // Price range
        if (minPrice != null || maxPrice != null) {
            RangeQuery.Builder rangeBuilder = new RangeQuery.Builder().field("price");
            if (minPrice != null) rangeBuilder.gte(JsonData.of(minPrice));
            if (maxPrice != null) rangeBuilder.lte(JsonData.of(maxPrice));
            boolQuery.filter(rangeBuilder.build()._toQuery());
        }

        // Date range
        if (startAfter != null) {
            boolQuery.filter(RangeQuery.of(r -> r.field("startDate")
                .gte(JsonData.of(startAfter.toString())))._toQuery());
        }
        if (endBefore != null) {
            boolQuery.filter(RangeQuery.of(r -> r.field("endDate")
                .lte(JsonData.of(endBefore.toString())))._toQuery());
        }

        NativeQuery nativeQuery = NativeQuery.builder()
            .withQuery(boolQuery.build()._toQuery())
            .withPageable(pageable)
            .build();

        SearchHits<TravelDocument> hits = elasticsearchOperations.search(nativeQuery, TravelDocument.class);

        List<TravelSummary> results = hits.getSearchHits().stream()
            .map(SearchHit::getContent)
            .map(this::toSummary)
            .collect(Collectors.toList());

        return new PageImpl<>(results, pageable, hits.getTotalHits());
    }

    public List<String> autocomplete(String q) {
        if (q == null || q.length() < 2) return List.of();
        String lower = q.toLowerCase();

        // phrase_prefix works on text fields; prefix works on keyword fields
        BoolQuery innerShould = BoolQuery.of(b -> b
            .should(MatchPhrasePrefixQuery.of(m -> m.field("title").query(q))._toQuery())
            .should(PrefixQuery.of(p -> p.field("destinationCity").value(lower))._toQuery())
            .should(PrefixQuery.of(p -> p.field("destinationCountry").value(lower))._toQuery())
            .minimumShouldMatch("1")
        );

        BoolQuery outerQuery = BoolQuery.of(b -> b
            .filter(TermQuery.of(t -> t.field("status").value("PUBLISHED"))._toQuery())
            .must(innerShould._toQuery())
        );

        NativeQuery nativeQuery = NativeQuery.builder()
            .withQuery(outerQuery._toQuery())
            .withMaxResults(8)
            .build();

        SearchHits<TravelDocument> hits = elasticsearchOperations.search(nativeQuery, TravelDocument.class);
        List<String> suggestions = new ArrayList<>();
        for (SearchHit<TravelDocument> hit : hits.getSearchHits()) {
            suggestions.add(hit.getContent().getTitle());
        }
        return suggestions;
    }

    private TravelSummary toSummary(TravelDocument doc) {
        return new TravelSummary(
            Long.parseLong(doc.getId()),
            doc.getTitle(),
            doc.getDestinationCity(),
            doc.getDestinationCountry(),
            com.letstravel.domain.enums.TravelCategory.valueOf(doc.getCategory()),
            doc.getTags(),
            doc.getStartDate(),
            doc.getEndDate(),
            doc.getPrice(),
            doc.getCapacity(),
            doc.getCurrentEnrollment(),
            doc.getImageUrls(),
            com.letstravel.domain.enums.TravelStatus.valueOf(doc.getStatus()),
            doc.getAverageRating()
        );
    }
}
