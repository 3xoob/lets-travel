package com.letstravel.repository;

import com.letstravel.domain.Travel;
import com.letstravel.domain.User;
import com.letstravel.domain.enums.TravelStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TravelRepository extends JpaRepository<Travel, Long> {

    Page<Travel> findByStatus(TravelStatus status, Pageable pageable);

    Page<Travel> findByManager(User manager, Pageable pageable);

    Page<Travel> findByManagerAndStatus(User manager, TravelStatus status, Pageable pageable);

    List<Travel> findByManagerAndStatusIn(User manager, List<TravelStatus> statuses);

    @Query("SELECT t FROM Travel t WHERE t.status = 'PUBLISHED' ORDER BY t.currentEnrollment DESC")
    List<Travel> findTopPublishedByEnrollment(Pageable pageable);

    @Query("SELECT COUNT(t) FROM Travel t WHERE t.manager = :manager AND t.status = :status")
    long countByManagerAndStatus(@Param("manager") User manager, @Param("status") TravelStatus status);
}
