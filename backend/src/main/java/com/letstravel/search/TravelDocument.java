package com.letstravel.search;

import com.letstravel.domain.Travel;
import com.letstravel.domain.enums.TravelCategory;
import com.letstravel.domain.enums.TravelStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Document(indexName = "travels")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TravelDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;

    @Field(type = FieldType.Keyword)
    private String destinationCity;

    @Field(type = FieldType.Keyword)
    private String destinationCountry;

    @GeoPointField
    private GeoPoint location;

    @Field(type = FieldType.Keyword)
    private String category;

    @Field(type = FieldType.Keyword)
    private List<String> tags;

    @Field(type = FieldType.Date, format = DateFormat.basic_date)
    private LocalDate startDate;

    @Field(type = FieldType.Date, format = DateFormat.basic_date)
    private LocalDate endDate;

    @Field(type = FieldType.Double)
    private BigDecimal price;

    @Field(type = FieldType.Integer)
    private int capacity;

    @Field(type = FieldType.Integer)
    private int currentEnrollment;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Text)
    private String managerName;

    @Field(type = FieldType.Long)
    private Long managerId;

    @Field(type = FieldType.Double)
    private BigDecimal averageRating;

    @Field(type = FieldType.Long)
    private long feedbackCount;

    @Field(type = FieldType.Keyword)
    private List<String> imageUrls;

    public static TravelDocument fromTravel(Travel travel, Double avgRating, long feedbackCount) {
        GeoPoint location = null;
        if (travel.getDestinationLatitude() != null && travel.getDestinationLongitude() != null) {
            location = new GeoPoint(
                travel.getDestinationLatitude().doubleValue(),
                travel.getDestinationLongitude().doubleValue());
        }
        return TravelDocument.builder()
            .id(travel.getId().toString())
            .title(travel.getTitle())
            .description(travel.getDescription())
            .destinationCity(travel.getDestinationCity())
            .destinationCountry(travel.getDestinationCountry())
            .location(location)
            .category(travel.getCategory().name())
            .tags(travel.getTags())
            .startDate(travel.getStartDate())
            .endDate(travel.getEndDate())
            .price(travel.getPrice())
            .capacity(travel.getCapacity())
            .currentEnrollment(travel.getCurrentEnrollment())
            .status(travel.getStatus().name())
            .managerName(travel.getManager().getFullName())
            .managerId(travel.getManager().getId())
            .averageRating(BigDecimal.valueOf(avgRating != null ? avgRating : 0.0))
            .feedbackCount(feedbackCount)
            .imageUrls(travel.getImageUrls())
            .build();
    }
}
