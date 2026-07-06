package com.letstravel.event;

import com.letstravel.search.ElasticsearchTravelRepository;
import com.letstravel.search.TravelDocument;
import com.letstravel.service.Neo4jSyncService;
import com.letstravel.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class TravelIndexEventListener {

    private final ElasticsearchTravelRepository elasticsearchTravelRepository;
    private final FeedbackRepository feedbackRepository;
    private final Neo4jSyncService neo4jSyncService;

    public TravelIndexEventListener(
            ElasticsearchTravelRepository elasticsearchTravelRepository,
            FeedbackRepository feedbackRepository,
            @Lazy Neo4jSyncService neo4jSyncService) {
        this.elasticsearchTravelRepository = elasticsearchTravelRepository;
        this.feedbackRepository = feedbackRepository;
        this.neo4jSyncService = neo4jSyncService;
    }

    @Async
    @EventListener
    public void onTravelIndexEvent(TravelIndexEvent event) {
        if (TravelIndexEvent.DELETE.equals(event.operation())) {
            elasticsearchTravelRepository.deleteById(event.travel().getId().toString());
            neo4jSyncService.deleteTravel(event.travel().getId());
        } else {
            double avgRating = feedbackRepository
                .findAverageRatingByTravelId(event.travel().getId())
                .orElse(0.0);
            long feedbackCount = feedbackRepository.countByTravelId(event.travel().getId());
            TravelDocument doc = TravelDocument.fromTravel(event.travel(), avgRating, feedbackCount);
            elasticsearchTravelRepository.save(doc);
            neo4jSyncService.upsertTravel(event.travel());
        }
    }
}
