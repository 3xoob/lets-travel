package com.letstravel.service;

import com.letstravel.domain.Feedback;
import com.letstravel.domain.Travel;
import com.letstravel.domain.User;
import com.letstravel.domain.enums.SubscriptionStatus;
import com.letstravel.dto.feedback.FeedbackRequest;
import com.letstravel.dto.feedback.FeedbackResponse;
import com.letstravel.exception.BusinessException;
import com.letstravel.exception.EntityNotFoundException;
import com.letstravel.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final TravelRepository travelRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ManagerProfileRepository managerProfileRepository;

    @Transactional
    public FeedbackResponse submitFeedback(FeedbackRequest req, String email) {
        User traveler = userRepository.findByEmail(email)
            .orElseThrow(() -> new EntityNotFoundException("User", null));
        Travel travel = travelRepository.findById(req.travelId())
            .orElseThrow(() -> new EntityNotFoundException("Travel", req.travelId()));

        // Must have completed subscription
        boolean hasCompleted = subscriptionRepository
            .findByTravelIdAndTravelerId(travel.getId(), traveler.getId())
            .map(s -> s.getStatus() == SubscriptionStatus.ACTIVE
                      && travel.getEndDate().isBefore(LocalDate.now()))
            .orElse(false);
        if (!hasCompleted) {
            throw new BusinessException("You can only review travels you have completed", HttpStatus.FORBIDDEN);
        }

        Feedback feedback = feedbackRepository
            .findByTravelIdAndTravelerId(travel.getId(), traveler.getId())
            .orElse(Feedback.builder().travel(travel).traveler(traveler).build());

        feedback.setRating(req.rating());
        feedback.setComment(req.comment());
        feedback = feedbackRepository.save(feedback);

        // Recalculate travel average rating and update manager profile
        recalculate(travel);

        return toResponse(feedback);
    }

    private void recalculate(Travel travel) {
        Double avg = feedbackRepository.findAverageRatingByTravelId(travel.getId()).orElse(0.0);
        // Update manager's average rating across all their travels
        managerProfileRepository.findByUserId(travel.getManager().getId()).ifPresent(mp -> {
            List<Travel> managerTravels = travelRepository.findByManagerAndStatusIn(
                travel.getManager(),
                List.of(com.letstravel.domain.enums.TravelStatus.PUBLISHED,
                        com.letstravel.domain.enums.TravelStatus.COMPLETED));
            double overallAvg = managerTravels.stream()
                .mapToDouble(t -> feedbackRepository.findAverageRatingByTravelId(t.getId()).orElse(0.0))
                .average()
                .orElse(0.0);
            mp.setAverageRating(BigDecimal.valueOf(overallAvg).setScale(2, RoundingMode.HALF_UP));
            managerProfileRepository.save(mp);
        });
    }

    @Transactional(readOnly = true)
    public Page<FeedbackResponse> getForTravel(Long travelId, Pageable pageable) {
        return feedbackRepository.findByTravelId(travelId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<FeedbackResponse> getForManager(String managerEmail, Pageable pageable) {
        User manager = userRepository.findByEmail(managerEmail)
            .orElseThrow(() -> new EntityNotFoundException("User", null));
        return feedbackRepository.findByManagerId(manager.getId(), pageable).map(this::toResponse);
    }

    public FeedbackResponse toResponse(Feedback f) {
        return new FeedbackResponse(
            f.getId(), f.getTravel().getId(),
            f.getTravel().getTitle(),
            f.getTraveler().getFirstName() + " " + f.getTraveler().getLastName().charAt(0) + ".",
            f.getRating(), f.getComment(), f.getCreatedAt());
    }
}
