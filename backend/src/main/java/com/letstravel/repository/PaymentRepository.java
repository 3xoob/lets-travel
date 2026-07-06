package com.letstravel.repository;

import com.letstravel.domain.Payment;
import com.letstravel.domain.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByProviderRef(String providerRef);

    Optional<Payment> findByProviderIntentId(String intentId);

    @Query(value = """
        SELECT CAST(EXTRACT(YEAR FROM p.paid_at) AS INTEGER) AS year,
               CAST(EXTRACT(MONTH FROM p.paid_at) AS INTEGER) AS month,
               COALESCE(SUM(p.amount), 0) AS income,
               COUNT(DISTINCT s.travel_id) AS trip_count
        FROM payments p
        JOIN subscriptions s ON s.payment_id = p.id
        JOIN travels t ON t.id = s.travel_id
        WHERE p.status = 'COMPLETED'
          AND t.manager_id = :managerId
          AND p.paid_at >= NOW() - INTERVAL '1 month' * :months
        GROUP BY year, month
        ORDER BY year, month
        """, nativeQuery = true)
    List<Object[]> findMonthlyIncomeByManager(@Param("managerId") Long managerId, @Param("months") int months);

    @Query(value = """
        SELECT CAST(EXTRACT(YEAR FROM p.paid_at) AS INTEGER) AS year,
               CAST(EXTRACT(MONTH FROM p.paid_at) AS INTEGER) AS month,
               COALESCE(SUM(p.amount), 0) AS income,
               COUNT(DISTINCT s.travel_id) AS trip_count
        FROM payments p
        JOIN subscriptions s ON s.payment_id = p.id
        WHERE p.status = 'COMPLETED'
          AND p.paid_at >= NOW() - INTERVAL '1 month' * :months
        GROUP BY year, month
        ORDER BY year, month
        """, nativeQuery = true)
    List<Object[]> findMonthlyIncomeAll(@Param("months") int months);

    @Query(value = """
        SELECT COALESCE(SUM(p.amount), 0)
        FROM payments p
        JOIN subscriptions s ON s.payment_id = p.id
        WHERE p.status = 'COMPLETED'
          AND s.traveler_id = :travelerId
        """, nativeQuery = true)
    BigDecimal sumTotalSpendByTravelerId(@Param("travelerId") Long travelerId);
}
