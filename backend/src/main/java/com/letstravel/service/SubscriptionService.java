package com.letstravel.service;

import com.letstravel.domain.*;
import com.letstravel.domain.enums.*;
import com.letstravel.dto.payment.PaymentInitResponse;
import com.letstravel.dto.subscription.ManagerUnsubscribeRequest;
import com.letstravel.dto.subscription.SubscribeRequest;
import com.letstravel.dto.subscription.SubscriptionResponse;
import com.letstravel.dto.travel.TravelSummary;
import com.letstravel.exception.BusinessException;
import com.letstravel.exception.EntityNotFoundException;
import com.letstravel.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final TravelRepository travelRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final ManagerProfileRepository managerProfileRepository;
    private final StripeService stripeService;
    private final PayPalService payPalService;

    public SubscriptionService(
            SubscriptionRepository subscriptionRepository,
            TravelRepository travelRepository,
            UserRepository userRepository,
            PaymentRepository paymentRepository,
            ManagerProfileRepository managerProfileRepository,
            @Lazy StripeService stripeService,
            @Lazy PayPalService payPalService) {
        this.subscriptionRepository = subscriptionRepository;
        this.travelRepository = travelRepository;
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
        this.managerProfileRepository = managerProfileRepository;
        this.stripeService = stripeService;
        this.payPalService = payPalService;
    }

    @Transactional
    public PaymentInitResponse subscribe(SubscribeRequest req, String travelerEmail) {
        User traveler = userRepository.findByEmail(travelerEmail)
            .orElseThrow(() -> new EntityNotFoundException("User", null));
        Travel travel = travelRepository.findById(req.travelId())
            .orElseThrow(() -> new EntityNotFoundException("Travel", req.travelId()));

        if (!travel.isSubscriptionAllowed()) {
            throw new BusinessException("Travel is not open for subscription. Check status, capacity, or the 3-day cutoff rule.", HttpStatus.BAD_REQUEST);
        }
        subscriptionRepository.findByTravelIdAndTravelerId(travel.getId(), traveler.getId())
            .ifPresent(s -> {
                if (s.getStatus() == SubscriptionStatus.ACTIVE || s.getStatus() == SubscriptionStatus.PENDING) {
                    throw new BusinessException("Already subscribed to this travel", HttpStatus.CONFLICT);
                }
            });

        Payment payment = Payment.builder()
            .amount(travel.getPrice())
            .currency("USD")
            .method(req.paymentMethod())
            .status(PaymentStatus.PENDING)
            .build();
        payment = paymentRepository.save(payment);

        Subscription subscription = Subscription.builder()
            .travel(travel)
            .traveler(traveler)
            .payment(payment)
            .status(SubscriptionStatus.PENDING)
            .build();
        subscription = subscriptionRepository.save(subscription);

        String clientSecret = null;
        String approvalUrl = null;

        if (req.paymentMethod() == PaymentMethod.STRIPE) {
            Map<String, String> result = stripeService.createPaymentIntent(
                travel.getPrice(), "USD", subscription.getId(), payment.getId());
            clientSecret = result.get("clientSecret");
        } else {
            Map<String, String> result = payPalService.createOrder(
                travel.getPrice(), "USD", subscription.getId(), payment.getId());
            approvalUrl = result.get("approvalUrl");
        }

        return new PaymentInitResponse(subscription.getId(), payment.getId(),
            req.paymentMethod().name(), clientSecret, approvalUrl);
    }

    @Transactional
    public void confirmSubscription(Long subscriptionId) {
        Subscription sub = subscriptionRepository.findById(subscriptionId)
            .orElseThrow(() -> new EntityNotFoundException("Subscription", subscriptionId));
        sub.setStatus(SubscriptionStatus.ACTIVE);
        sub.getPayment().setStatus(PaymentStatus.COMPLETED);
        sub.getPayment().setPaidAt(LocalDateTime.now());
        // Increment enrollment with optimistic lock
        Travel travel = sub.getTravel();
        travel.setCurrentEnrollment(travel.getCurrentEnrollment() + 1);
        travelRepository.save(travel);
        subscriptionRepository.save(sub);
        // Update manager income
        managerProfileRepository.findByUserId(travel.getManager().getId()).ifPresent(mp -> {
            mp.setTotalIncome(mp.getTotalIncome().add(travel.getPrice()));
            managerProfileRepository.save(mp);
        });
    }

    @Transactional
    public void failSubscription(Long subscriptionId) {
        subscriptionRepository.findById(subscriptionId).ifPresent(sub -> {
            sub.setStatus(SubscriptionStatus.EXPIRED);
            sub.getPayment().setStatus(PaymentStatus.FAILED);
            subscriptionRepository.save(sub);
        });
    }

    @Transactional
    public void unsubscribe(Long subscriptionId, String email) {
        Subscription sub = subscriptionRepository.findById(subscriptionId)
            .orElseThrow(() -> new EntityNotFoundException("Subscription", subscriptionId));
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new EntityNotFoundException("User", null));
        if (!sub.getTraveler().getId().equals(user.getId()) && user.getRole() != UserRole.ADMIN) {
            throw new BusinessException("Not authorized", HttpStatus.FORBIDDEN);
        }
        if (!sub.getTravel().isUnsubscriptionAllowed()) {
            throw new BusinessException("Cannot unsubscribe within 3 days of departure", HttpStatus.BAD_REQUEST);
        }
        if (sub.getStatus() == SubscriptionStatus.ACTIVE) {
            Travel travel = sub.getTravel();
            travel.setCurrentEnrollment(Math.max(0, travel.getCurrentEnrollment() - 1));
            travelRepository.save(travel);
            if (sub.getPayment() != null && sub.getPayment().getStatus() == PaymentStatus.COMPLETED) {
                sub.getPayment().setStatus(PaymentStatus.REFUNDED);
                if (sub.getPayment().getMethod() == PaymentMethod.STRIPE && sub.getPayment().getProviderIntentId() != null) {
                    try { stripeService.createRefund(sub.getPayment().getProviderIntentId(), sub.getTravel().getPrice()); }
                    catch (Exception e) { log.error("Refund failed", e); }
                }
                managerProfileRepository.findByUserId(travel.getManager().getId()).ifPresent(mp -> {
                    mp.setTotalIncome(mp.getTotalIncome().subtract(travel.getPrice()));
                    managerProfileRepository.save(mp);
                });
            }
        }
        sub.setStatus(SubscriptionStatus.CANCELLED);
        sub.setCancelledAt(LocalDateTime.now());
        subscriptionRepository.save(sub);
    }

    @Transactional
    public void managerUnsubscribe(Long subscriptionId, ManagerUnsubscribeRequest req, String managerEmail) {
        Subscription sub = subscriptionRepository.findById(subscriptionId)
            .orElseThrow(() -> new EntityNotFoundException("Subscription", subscriptionId));
        User manager = userRepository.findByEmail(managerEmail)
            .orElseThrow(() -> new EntityNotFoundException("User", null));
        if (!sub.getTravel().getManager().getId().equals(manager.getId()) && manager.getRole() != UserRole.ADMIN) {
            throw new BusinessException("Not authorized", HttpStatus.FORBIDDEN);
        }
        if (sub.getStatus() == SubscriptionStatus.ACTIVE) {
            Travel travel = sub.getTravel();
            travel.setCurrentEnrollment(Math.max(0, travel.getCurrentEnrollment() - 1));
            travelRepository.save(travel);
        }
        sub.setStatus(SubscriptionStatus.CANCELLED);
        sub.setCancelledAt(LocalDateTime.now());
        sub.setCancellationReason(req.reason());
        subscriptionRepository.save(sub);
    }

    @Transactional(readOnly = true)
    public Page<SubscriptionResponse> getMySubscriptions(String email, Pageable pageable) {
        User traveler = userRepository.findByEmail(email)
            .orElseThrow(() -> new EntityNotFoundException("User", null));
        return subscriptionRepository.findByTravelerId(traveler.getId(), pageable)
            .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getTravelSubscribers(Long travelId, String managerEmail) {
        User manager = userRepository.findByEmail(managerEmail)
            .orElseThrow(() -> new EntityNotFoundException("User", null));
        Travel travel = travelRepository.findById(travelId)
            .orElseThrow(() -> new EntityNotFoundException("Travel", travelId));
        if (!travel.getManager().getId().equals(manager.getId()) && manager.getRole() != UserRole.ADMIN) {
            throw new BusinessException("Not authorized", HttpStatus.FORBIDDEN);
        }
        return subscriptionRepository.findByTravelId(travelId, Pageable.unpaged())
            .stream()
            .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE)
            .map(this::toResponse).toList();
    }

    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void cancelExpiredPendingSubscriptions() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(30);
        List<Subscription> expired = subscriptionRepository.findExpiredPending(cutoff);
        expired.forEach(s -> {
            s.setStatus(SubscriptionStatus.EXPIRED);
            if (s.getPayment() != null) s.getPayment().setStatus(PaymentStatus.FAILED);
        });
        subscriptionRepository.saveAll(expired);
        if (!expired.isEmpty()) log.info("Expired {} pending subscriptions", expired.size());
    }

    private SubscriptionResponse toResponse(Subscription s) {
        TravelSummary ts = TravelService.toSummary(s.getTravel());
        SubscriptionResponse.PaymentSummary ps = null;
        if (s.getPayment() != null) {
            Payment p = s.getPayment();
            ps = new SubscriptionResponse.PaymentSummary(p.getId(), p.getAmount(), p.getCurrency(),
                p.getMethod().name(), p.getStatus().name(), p.getPaidAt());
        }
        return new SubscriptionResponse(s.getId(), ts, s.getStatus(),
            s.getSubscribedAt(), s.getCancelledAt(), ps);
    }
}
