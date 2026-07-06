package com.letstravel.service;

import com.letstravel.config.AppProperties;
import com.letstravel.domain.ManagerProfile;
import com.letstravel.domain.Travel;
import com.letstravel.domain.User;
import com.letstravel.domain.enums.TravelStatus;
import com.letstravel.domain.enums.UserRole;
import com.letstravel.dto.travel.TravelRequest;
import com.letstravel.dto.travel.TravelResponse;
import com.letstravel.dto.travel.TravelSummary;
import com.letstravel.event.TravelIndexEvent;
import com.letstravel.exception.BusinessException;
import com.letstravel.exception.EntityNotFoundException;
import com.letstravel.repository.FeedbackRepository;
import com.letstravel.repository.ManagerProfileRepository;
import com.letstravel.repository.TravelRepository;
import com.letstravel.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TravelService {

    private final TravelRepository travelRepository;
    private final UserRepository userRepository;
    private final FeedbackRepository feedbackRepository;
    private final ManagerProfileRepository managerProfileRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final AppProperties appProperties;

    @Transactional
    public TravelResponse createTravel(TravelRequest req, String managerEmail) {
        User manager = userRepository.findByEmail(managerEmail)
            .orElseThrow(() -> new EntityNotFoundException("User", null));
        Travel travel = mapToEntity(req, new Travel());
        travel.setManager(manager);
        travel.setStatus(req.status() != null ? req.status() : TravelStatus.DRAFT);
        Travel saved = travelRepository.save(travel);
        eventPublisher.publishEvent(new TravelIndexEvent(saved, TravelIndexEvent.UPSERT));
        updateManagerStats(manager);
        return toResponse(saved);
    }

    @Transactional
    public TravelResponse updateTravel(Long id, TravelRequest req, String userEmail) {
        Travel travel = travelRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Travel", id));
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new EntityNotFoundException("User", null));
        if (!travel.getManager().getId().equals(user.getId()) && user.getRole() != UserRole.ADMIN) {
            throw new BusinessException("Not authorized to update this travel", HttpStatus.FORBIDDEN);
        }
        mapToEntity(req, travel);
        if (req.status() != null) travel.setStatus(req.status());
        Travel saved = travelRepository.save(travel);
        eventPublisher.publishEvent(new TravelIndexEvent(saved, TravelIndexEvent.UPSERT));
        return toResponse(saved);
    }

    @Transactional
    public void deleteTravel(Long id, String userEmail) {
        Travel travel = travelRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Travel", id));
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new EntityNotFoundException("User", null));
        if (!travel.getManager().getId().equals(user.getId()) && user.getRole() != UserRole.ADMIN) {
            throw new BusinessException("Not authorized to delete this travel", HttpStatus.FORBIDDEN);
        }
        travel.setStatus(TravelStatus.CANCELLED);
        travelRepository.save(travel);
        eventPublisher.publishEvent(new TravelIndexEvent(travel, TravelIndexEvent.DELETE));
    }

    @Transactional(readOnly = true)
    public TravelResponse getTravel(Long id) {
        Travel travel = travelRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Travel", id));
        return toResponse(travel);
    }

    @Transactional(readOnly = true)
    public Page<TravelSummary> getAllTravels(Pageable pageable, TravelStatus status) {
        TravelStatus effectiveStatus = (status != null) ? status : TravelStatus.PUBLISHED;
        return travelRepository.findByStatus(effectiveStatus, pageable).map(TravelService::toSummary);
    }

    @Transactional(readOnly = true)
    public Page<TravelSummary> getManagerTravels(String managerEmail, Pageable pageable) {
        User manager = userRepository.findByEmail(managerEmail)
            .orElseThrow(() -> new EntityNotFoundException("User", null));
        return travelRepository.findByManager(manager, pageable).map(TravelService::toSummary);
    }

    @Transactional
    public List<String> uploadImages(Long id, List<MultipartFile> files, String managerEmail) {
        Travel travel = travelRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Travel", id));
        User user = userRepository.findByEmail(managerEmail)
            .orElseThrow(() -> new EntityNotFoundException("User", null));
        if (!travel.getManager().getId().equals(user.getId()) && user.getRole() != UserRole.ADMIN) {
            throw new BusinessException("Not authorized", HttpStatus.FORBIDDEN);
        }
        List<String> urls = new ArrayList<>();
        String uploadsDir = appProperties.getUploads().getDir();
        Path dir = Paths.get(uploadsDir, id.toString());
        try {
            Files.createDirectories(dir);
            for (MultipartFile file : files) {
                String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                Path dest = dir.resolve(fileName);
                file.transferTo(dest.toFile());
                urls.add("/uploads/" + id + "/" + fileName);
            }
        } catch (IOException e) {
            throw new BusinessException("Failed to upload images: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        travel.getImageUrls().addAll(urls);
        travelRepository.save(travel);
        return urls;
    }

    private void updateManagerStats(User manager) {
        managerProfileRepository.findByUserId(manager.getId()).ifPresent(profile -> {
            long trips = travelRepository.countByManagerAndStatus(manager, TravelStatus.PUBLISHED)
                       + travelRepository.countByManagerAndStatus(manager, TravelStatus.COMPLETED);
            profile.setTotalTrips((int) trips);
            managerProfileRepository.save(profile);
        });
    }

    private Travel mapToEntity(TravelRequest req, Travel travel) {
        travel.setTitle(req.title());
        travel.setDescription(req.description());
        travel.setDestinationCity(req.destination());
        travel.setDestinationCountry(req.country());
        travel.setDestinationLatitude(req.latitude() != null ? new java.math.BigDecimal(req.latitude().toString()) : null);
        travel.setDestinationLongitude(req.longitude() != null ? new java.math.BigDecimal(req.longitude().toString()) : null);
        travel.setCategory(req.category());
        travel.setTags(req.tags() != null ? req.tags() : new ArrayList<>());
        travel.setStartDate(req.startDate());
        travel.setEndDate(req.endDate());
        travel.setPrice(req.price());
        travel.setCapacity(req.capacity());
        return travel;
    }

    public TravelResponse toResponse(Travel t) {
        Double avg = feedbackRepository.findAverageRatingByTravelId(t.getId()).orElse(0.0);
        long count = feedbackRepository.countByTravelId(t.getId());
        ManagerProfile mp = null;
        try {
            mp = managerProfileRepository.findByUserId(t.getManager().getId()).orElse(null);
        } catch (Exception ignored) {}
        BigDecimal managerRating = (mp != null) ? mp.getAverageRating() : BigDecimal.ZERO;
        return new TravelResponse(
            t.getId(), t.getTitle(), t.getDescription(),
            t.getDestinationCity(), t.getDestinationCountry(),
            t.getDestinationLatitude(), t.getDestinationLongitude(),
            t.getCategory(), t.getTags(),
            t.getStartDate(), t.getEndDate(),
            t.getPrice(), t.getCapacity(), t.getCurrentEnrollment(),
            t.getImageUrls(), t.getStatus(),
            new TravelResponse.ManagerSummary(t.getManager().getId(),
                t.getManager().getFirstName(), t.getManager().getLastName(), managerRating),
            BigDecimal.valueOf(avg), count,
            t.isSubscriptionAllowed(), t.isUnsubscriptionAllowed(),
            t.getCreatedAt());
    }

    public static TravelSummary toSummary(Travel t) {
        return new TravelSummary(
            t.getId(), t.getTitle(),
            t.getDestinationCity(), t.getDestinationCountry(),
            t.getCategory(), t.getTags(),
            t.getStartDate(), t.getEndDate(),
            t.getPrice(), t.getCapacity(), t.getCurrentEnrollment(),
            t.getImageUrls(), t.getStatus(), BigDecimal.ZERO);
    }

    // field needed by UserService
    private com.letstravel.domain.ManagerProfile getProfileFor(User manager) {
        return managerProfileRepository.findByUserId(manager.getId()).orElse(null);
    }
}
