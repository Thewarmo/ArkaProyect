package com.arka.customer_service.domain.entities;

/**
 * Enum que representa los países donde opera Arka
 */
public enum Country {
    COLOMBIA("Colombia", "CO", "+57"),
    ECUADOR("Ecuador", "EC", "+593"),
    PERU("Perú", "PE", "+51"),
    CHILE("Chile", "CL", "+56");

    private final String name;
    private final String code;
    private final String phonePrefix;

    Country(String name, String code, String phonePrefix) {
        this.name = name;
        this.code = code;
        this.phonePrefix = phonePrefix;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getPhonePrefix() {
        return phonePrefix;
    }
}
