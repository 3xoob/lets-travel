package com.letstravel.repository;

import com.letstravel.domain.ManagerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ManagerProfileRepository extends JpaRepository<ManagerProfile, Long> {

    Optional<ManagerProfile> findByUserId(Long userId);

    Optional<ManagerProfile> findByUserEmail(String email);

    @Query(value = """
        SELECT mp.* FROM manager_profiles mp
        ORDER BY (mp.total_income * 0.4 + mp.average_rating * 40 + mp.total_trips * 2) DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<ManagerProfile> findTopManagersByScore(@Param("limit") int limit);
}
