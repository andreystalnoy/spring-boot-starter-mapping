package com.matteria.mapping.configuration.model;

public record Address(String street, String city, String index, Country country) {
    public String formattedAddress() {
        return String.format("%s, %s, %s, %s", street, city, index, country.name());
    }
}
