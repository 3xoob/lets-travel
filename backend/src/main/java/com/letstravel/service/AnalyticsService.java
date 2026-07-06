package com.letstravel.service;

import com.letstravel.domain.enums.ReportStatus;
import com.letstravel.domain.enums.TravelStatus;
import com.letstravel.dto.analytics.*;
import com.letstravel.dto.feedback.FeedbackResponse;
import com.letstravel.dto.travel.TravelSummary;
import com.letstravel.exception.EntityNotFoundException;
import com.letstravel.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsService {

    private final PaymentRepository paymentRepository;
    private final ManagerProfileRepository managerProfileRepository;
    private final TravelRepository travelRepository;
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final FeedbackRepository feedbackRepository;
    private final FeedbackService feedbackService;
    private final SubscriptionRepository subscriptionRepository;

    public AdminDashboardResponse getAdminDashboard(int months) {
        List<IncomeByMonthDto> income = getAdminIncomeByMonth(months);
        List<ManagerRankDto> topManagers = getTopManagers(10);
        List<TravelSummary> topTravels = travelRepository
            .findTopPublishedByEnrollment(PageRequest.of(0, 10))
            .stream().map(TravelService::toSummary).toList();
        List<FeedbackResponse> recentFeedback = feedbackRepository
            .findRecentFeedback(PageRequest.of(0, 20))
            .getContent().stream().map(feedbackService::toResponse).toList();
        long openReports = reportRepository.countByStatus(ReportStatus.OPEN);
        long totalUsers = userRepository.count();
        long totalTravels = travelRepository.count();
        return new AdminDashboardResponse(income, topManagers, topTravels,
            recentFeedback, openReports, totalUsers, totalTravels);
    }

    public List<IncomeByMonthDto> getAdminIncomeByMonth(int months) {
        return paymentRepository.findMonthlyIncomeAll(months).stream()
            .map(row -> new IncomeByMonthDto(
                ((Number) row[0]).intValue(),
                ((Number) row[1]).intValue(),
                new BigDecimal(row[2].toString()).setScale(2, RoundingMode.HALF_UP),
                ((Number) row[3]).longValue()))
            .toList();
    }

    public List<ManagerRankDto> getTopManagers(int limit) {
        return managerProfileRepository.findTopManagersByScore(limit).stream()
            .map(mp -> {
                BigDecimal score = mp.getTotalIncome().multiply(BigDecimal.valueOf(0.4))
                    .add(mp.getAverageRating().multiply(BigDecimal.valueOf(40)))
                    .add(BigDecimal.valueOf(mp.getTotalTrips() * 2))
                    .setScale(2, RoundingMode.HALF_UP);
                return new ManagerRankDto(
                    mp.getUser().getId(),
                    mp.getUser().getFirstName(),
                    mp.getUser().getLastName(),
                    mp.getAverageRating(),
                    mp.getTotalIncome(),
                    mp.getTotalTrips(),
                    mp.getReportCount(),
                    score);
            }).toList();
    }

    public ManagerDashboardResponse getManagerDashboard(String managerEmail) {
        var manager = userRepository.findByEmail(managerEmail)
            .orElseThrow(() -> new EntityNotFoundException("User", null));
        var profile = managerProfileRepository.findByUserId(manager.getId())
            .orElseThrow(() -> new EntityNotFoundException("ManagerProfile", manager.getId()));

        List<Object[]> currentMonthData = paymentRepository.findMonthlyIncomeByManager(manager.getId(), 1);
        List<Object[]> prevMonthData = paymentRepository.findMonthlyIncomeByManager(manager.getId(), 2);

        BigDecimal currentIncome = currentMonthData.isEmpty() ? BigDecimal.ZERO
            : new BigDecimal(currentMonthData.get(currentMonthData.size() - 1)[2].toString());
        BigDecimal prevIncome = prevMonthData.size() < 2 ? BigDecimal.ZERO
            : new BigDecimal(prevMonthData.get(0)[2].toString());

        long activeTravels = travelRepository.countByManagerAndStatus(manager, TravelStatus.PUBLISHED);
        long totalTravels = travelRepository.findByManager(manager, PageRequest.of(0, 1)).getTotalElements();

        long totalSubscribers = travelRepository.findByManagerAndStatusIn(manager,
                List.of(TravelStatus.PUBLISHED, TravelStatus.COMPLETED))
            .stream()
            .mapToLong(t -> subscriptionRepository.countByTravelIdAndStatus(
                t.getId(), com.letstravel.domain.enums.SubscriptionStatus.ACTIVE))
            .sum();

        List<ManagerDashboardResponse.TravelWithStats> travelStats =
            travelRepository.findByManager(manager, PageRequest.of(0, 20)).stream()
                .map(t -> {
                    long subs = subscriptionRepository.countByTravelIdAndStatus(
                        t.getId(), com.letstravel.domain.enums.SubscriptionStatus.ACTIVE);
                    BigDecimal income = subs > 0
                        ? t.getPrice().multiply(BigDecimal.valueOf(subs))
                        : BigDecimal.ZERO;
                    BigDecimal avgRating = BigDecimal.valueOf(
                        feedbackRepository.findAverageRatingByTravelId(t.getId()).orElse(0.0));
                    return new ManagerDashboardResponse.TravelWithStats(
                        TravelService.toSummary(t), subs, income, avgRating);
                }).toList();

        return new ManagerDashboardResponse(currentIncome, prevIncome,
            activeTravels, totalTravels, totalSubscribers,
            profile.getAverageRating(), travelStats);
    }

    public List<IncomeByMonthDto> getManagerIncomeByMonth(String managerEmail, int months) {
        var manager = userRepository.findByEmail(managerEmail)
            .orElseThrow(() -> new EntityNotFoundException("User", null));
        return paymentRepository.findMonthlyIncomeByManager(manager.getId(), months).stream()
            .map(row -> new IncomeByMonthDto(
                ((Number) row[0]).intValue(),
                ((Number) row[1]).intValue(),
                new BigDecimal(row[2].toString()).setScale(2, RoundingMode.HALF_UP),
                ((Number) row[3]).longValue()))
            .toList();
    }
}
