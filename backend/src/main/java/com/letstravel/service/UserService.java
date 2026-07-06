package com.letstravel.service;

import com.letstravel.domain.ManagerProfile;
import com.letstravel.domain.User;
import com.letstravel.domain.enums.UserRole;
import com.letstravel.dto.manager.ManagerProfileResponse;
import com.letstravel.dto.travel.TravelSummary;
import com.letstravel.dto.traveler.TravelerStatsResponse;
import com.letstravel.dto.user.ChangeRoleRequest;
import com.letstravel.dto.user.UpdateProfileRequest;
import com.letstravel.dto.user.UserDto;
import com.letstravel.exception.BusinessException;
import com.letstravel.exception.EntityNotFoundException;
import com.letstravel.repository.*;
import com.letstravel.search.ElasticsearchTravelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ManagerProfileRepository managerProfileRepository;
    private final TravelRepository travelRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final FeedbackRepository feedbackRepository;
    private final ReportRepository reportRepository;
    private final PaymentRepository paymentRepository;
    private final Neo4jSyncService neo4jSyncService;
    private final ElasticsearchTravelRepository elasticsearchTravelRepository;

    public UserDto getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new EntityNotFoundException("User", null));
        return AuthService.toUserDto(user);
    }

    @Transactional
    public UserDto updateProfile(String email, UpdateProfileRequest req) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new EntityNotFoundException("User", null));
        if (req.firstName() != null) user.setFirstName(req.firstName());
        if (req.lastName() != null) user.setLastName(req.lastName());
        if (req.avatarUrl() != null) user.setAvatarUrl(req.avatarUrl());
        return AuthService.toUserDto(userRepository.save(user));
    }

    @Transactional
    public UserDto changeRole(Long userId, ChangeRoleRequest req) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User", userId));
        if (req.role() == UserRole.MANAGER && user.getRole() != UserRole.MANAGER) {
            managerProfileRepository.findByUserId(userId).orElseGet(() -> {
                ManagerProfile mp = ManagerProfile.builder().user(user).build();
                return managerProfileRepository.save(mp);
            });
        }
        user.setRole(req.role());
        return AuthService.toUserDto(userRepository.save(user));
    }

    public Page<UserDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(AuthService::toUserDto);
    }

    public ManagerProfileResponse getManagerProfile(Long managerId) {
        User manager = userRepository.findById(managerId)
            .orElseThrow(() -> new EntityNotFoundException("Manager", managerId));
        if (manager.getRole() != UserRole.MANAGER && manager.getRole() != UserRole.ADMIN) {
            throw new BusinessException("User is not a manager", HttpStatus.BAD_REQUEST);
        }
        ManagerProfile profile = managerProfileRepository.findByUserId(managerId)
            .orElse(ManagerProfile.builder().user(manager).build());

        List<TravelSummary> recentTravels = travelRepository
            .findTopPublishedByEnrollment(PageRequest.of(0, 6))
            .stream()
            .filter(t -> t.getManager().getId().equals(managerId))
            .map(TravelService::toSummary)
            .toList();

        return new ManagerProfileResponse(
            manager.getId(), manager.getFirstName(), manager.getLastName(),
            profile.getBio(), profile.getSpecialties(),
            profile.getTotalIncome(), profile.getTotalTrips(),
            profile.getAverageRating(), profile.getReportCount(),
            recentTravels);
    }

    public TravelerStatsResponse getTravelerStats(String email) {
        User traveler = userRepository.findByEmail(email)
            .orElseThrow(() -> new EntityNotFoundException("User", null));
        long pastTrips = subscriptionRepository.countCompletedByTravelerId(traveler.getId());
        long upcomingTrips = subscriptionRepository.countUpcomingByTravelerId(traveler.getId());
        BigDecimal totalSpend = paymentRepository.sumTotalSpendByTravelerId(traveler.getId());
        if (totalSpend == null) totalSpend = BigDecimal.ZERO;
        long cancellations = subscriptionRepository.findByTravelerIdAndStatus(
            traveler.getId(), com.letstravel.domain.enums.SubscriptionStatus.CANCELLED).size();
        long reviewsGiven = feedbackRepository.countByTravelerId(traveler.getId());
        long reportsFiled = reportRepository.countByStatus(com.letstravel.domain.enums.ReportStatus.OPEN);
        return new TravelerStatsResponse(pastTrips, upcomingTrips, totalSpend,
            cancellations, reviewsGiven, reportsFiled);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> exportMyData(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new EntityNotFoundException("User", null));
        Map<String, Object> export = new java.util.LinkedHashMap<>();
        export.put("profile", Map.of(
            "email", user.getEmail(),
            "firstName", user.getFirstName(),
            "lastName", user.getLastName(),
            "role", user.getRole(),
            "createdAt", user.getCreatedAt()
        ));
        export.put("subscriptions", subscriptionRepository.findByTravelerId(user.getId(), org.springframework.data.domain.Pageable.unpaged())
            .getContent().stream().map(s -> Map.of(
                "travelTitle", s.getTravel().getTitle(),
                "status", s.getStatus(),
                "subscribedAt", s.getSubscribedAt()
            )).toList());
        return export;
    }

    @Transactional
    public void deleteMyAccount(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new EntityNotFoundException("User", null));
        // Erase from Neo4j (removes user node and all SUBSCRIBED_TO relationships)
        neo4jSyncService.deleteUserNode(user.getId());
        // Anonymize manager name in Elasticsearch travel documents
        elasticsearchTravelRepository.findByManagerId(user.getId()).forEach(doc -> {
            doc.setManagerName("Deleted User");
            elasticsearchTravelRepository.save(doc);
        });
        // Soft-delete the PostgreSQL record with email anonymisation
        user.setActive(false);
        user.setEmail("deleted_" + user.getId() + "@deleted.local");
        userRepository.save(user);
    }
}
