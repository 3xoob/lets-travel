package com.letstravel.dto.analytics;

import com.letstravel.dto.feedback.FeedbackResponse;
import com.letstravel.dto.travel.TravelSummary;

import java.util.List;

public record AdminDashboardResponse(
    List<IncomeByMonthDto> incomeByMonth,
    List<ManagerRankDto> topManagers,
    List<TravelSummary> topTravels,
    List<FeedbackResponse> recentFeedback,
    long openReports,
    long totalUsers,
    long totalTravels
) {}
