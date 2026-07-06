package com.letstravel.repository;

import com.letstravel.domain.Subscription;
import com.letstravel.domain.enums.SubscriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByTravelIdAndTravelerId(Long travelId, Long travelerId);

    Page<Subscription> findByTravelerId(Long travelerId, Pageable pageable);

    Page<Subscription> findByTravelId(Long travelId, Pageable pageable);

    long countByTravelIdAndStatus(Long travelId, SubscriptionStatus status);

    @Query("SELECT s FROM Subscription s WHERE s.status = 'PENDING' AND s.subscribedAt < :cutoff")
    List<Subscription> findExpiredPending(@Param("cutoff") LocalDateTime cutoff);

    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.traveler.id = :travelerId AND s.status = 'ACTIVE' AND s.travel.endDate < CURRENT_DATE")
    long countCompletedByTravelerId(@Param("travelerId") Long travelerId);

    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.traveler.id = :travelerId AND s.status = 'ACTIVE' AND s.travel.startDate >= CURRENT_DATE")
    long countUpcomingByTravelerId(@Param("travelerId") Long travelerId);

    List<Subscription> findByTravelerIdAndStatus(Long travelerId, SubscriptionStatus status);

    Optional<Subscription> findByPaymentId(Long paymentId);
}
