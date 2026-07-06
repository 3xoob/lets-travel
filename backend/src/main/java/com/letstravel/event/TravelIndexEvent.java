package com.letstravel.event;

import com.letstravel.domain.Travel;

public record TravelIndexEvent(Travel travel, String operation) {
    public static final String UPSERT = "UPSERT";
    public static final String DELETE = "DELETE";
}
