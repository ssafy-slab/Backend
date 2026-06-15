package com.ssafy.ssafy_slap.place.service;

import java.util.Arrays;
import java.util.List;

public enum NearbyFacilityType {
    GAS_STATION("OL7", "주유소"),
    PHARMACY("PM9", "약국"),
    CONVENIENCE_STORE("CS2", "편의점");

    private final String categoryGroupCode;
    private final String label;

    NearbyFacilityType(String categoryGroupCode, String label) {
        this.categoryGroupCode = categoryGroupCode;
        this.label = label;
    }

    public String categoryGroupCode() {
        return categoryGroupCode;
    }

    public String getCategoryGroupCode() {
        return categoryGroupCode;
    }

    public String label() {
        return label;
    }

    public String getLabel() {
        return label;
    }

    public static List<NearbyFacilityType> defaultValues() {
        return List.of(GAS_STATION, PHARMACY, CONVENIENCE_STORE);
    }

    public static NearbyFacilityType fromName(String name) {
        return Arrays.stream(values())
                .filter(type -> type.name().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported nearby facility type: " + name));
    }
}
