package com.letstravel.domain;

import com.letstravel.domain.enums.TravelCategory;
import com.letstravel.domain.enums.TravelStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "travels")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Travel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "destination_city", nullable = false)
    private String destinationCity;

    @Column(name = "destination_country", nullable = false)
    private String destinationCountry;

    @Column(name = "destination_latitude", precision = 9, scale = 6)
    private BigDecimal destinationLatitude;

    @Column(name = "destination_longitude", precision = 9, scale = 6)
    private BigDecimal destinationLongitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "travel_category")
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.NAMED_ENUM)
    private TravelCategory category;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "travel_tags", joinColumns = @JoinColumn(name = "travel_id"))
    @Column(name = "tag")
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int capacity;

    @Column(name = "current_enrollment", nullable = false)
    private int currentEnrollment = 0;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "travel_images", joinColumns = @JoinColumn(name = "travel_id"))
    @Column(name = "image_url")
    @Builder.Default
    private List<String> imageUrls = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "travel_status")
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.NAMED_ENUM)
    private TravelStatus status = TravelStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", nullable = false)
    private User manager;

    @Version
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean isSubscriptionAllowed() {
        return status == TravelStatus.PUBLISHED
                && currentEnrollment < capacity
                && LocalDate.now().plusDays(3).isBefore(startDate);
    }

    public boolean isUnsubscriptionAllowed() {
        return LocalDate.now().plusDays(3).isBefore(startDate);
    }
}
