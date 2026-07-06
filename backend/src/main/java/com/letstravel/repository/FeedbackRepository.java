package com.letstravel.repository;

import com.letstravel.domain.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    Optional<Feedback> findByTravelIdAndTravelerId(Long travelId, Long travelerId);

    Page<Feedback> findByTravelId(Long travelId, Pageable pageable);

    List<Feedback> findByTravelIdIn(List<Long> travelIds);

    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.travel.id = :travelId")
    Optional<Double> findAverageRatingByTravelId(@Param("travelId") Long travelId);

    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.travel.id = :travelId")
    long countByTravelId(@Param("travelId") Long travelId);

    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.traveler.id = :travelerId")
    long countByTravelerId(@Param("travelerId") Long travelerId);

    @Query("SELECT f FROM Feedback f JOIN f.travel t WHERE t.manager.id = :managerId ORDER BY f.createdAt DESC")
    Page<Feedback> findByManagerId(@Param("managerId") Long managerId, Pageable pageable);

    @Query("SELECT f FROM Feedback f ORDER BY f.createdAt DESC")
    Page<Feedback> findRecentFeedback(Pageable pageable);
}
