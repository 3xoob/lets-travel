package com.letstravel.service;

import com.letstravel.domain.*;
import com.letstravel.domain.enums.*;
import com.letstravel.dto.subscription.SubscribeRequest;
import com.letstravel.exception.BusinessException;
import com.letstravel.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock TravelRepository travelRepository;
    @Mock SubscriptionRepository subscriptionRepository;
    @Mock PaymentRepository paymentRepository;
    @Mock UserRepository userRepository;
    @Mock ApplicationEventPublisher eventPublisher;
    @Mock StripeService stripeService;
    @Mock PayPalService payPalService;
    @InjectMocks SubscriptionService subscriptionService;

    User traveler;
    Travel travel;

    @BeforeEach
    void setUp() {
        traveler = User.builder()
            .id(1L)
            .email("traveler@test.com")
            .role(UserRole.TRAVELER)
            .isActive(true)
            .build();

        travel = Travel.builder()
            .id(1L)
            .title("Test Trip")
            .price(BigDecimal.valueOf(500))
            .status(TravelStatus.PUBLISHED)
            .capacity(10)
            .currentEnrollment(0)
            .startDate(LocalDate.now().plusDays(10))
            .endDate(LocalDate.now().plusDays(17))
            .build();
    }

    @Test
    void subscribe_alreadySubscribed_throws() {
        var req = new SubscribeRequest(travel.getId(), PaymentMethod.STRIPE);
        when(userRepository.findByEmail(traveler.getEmail())).thenReturn(Optional.of(traveler));
        when(travelRepository.findById(travel.getId())).thenReturn(Optional.of(travel));
        when(subscriptionRepository.findByTravelIdAndTravelerId(travel.getId(), traveler.getId()))
            .thenReturn(Optional.of(new Subscription()));

        assertThatThrownBy(() -> subscriptionService.subscribe(req, traveler.getEmail()))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void subscribe_travelFull_throws() {
        travel.setCurrentEnrollment(travel.getCapacity());
        var req = new SubscribeRequest(travel.getId(), PaymentMethod.STRIPE);
        when(userRepository.findByEmail(traveler.getEmail())).thenReturn(Optional.of(traveler));
        when(travelRepository.findById(travel.getId())).thenReturn(Optional.of(travel));

        assertThatThrownBy(() -> subscriptionService.subscribe(req, traveler.getEmail()))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void subscribe_pastCutoff_throws() {
        travel.setStartDate(LocalDate.now().plusDays(1));
        var req = new SubscribeRequest(travel.getId(), PaymentMethod.STRIPE);
        when(userRepository.findByEmail(traveler.getEmail())).thenReturn(Optional.of(traveler));
        when(travelRepository.findById(travel.getId())).thenReturn(Optional.of(travel));

        assertThatThrownBy(() -> subscriptionService.subscribe(req, traveler.getEmail()))
            .isInstanceOf(BusinessException.class);
    }
}
