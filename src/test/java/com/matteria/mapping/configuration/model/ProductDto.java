package com.matteria.mapping.configuration.model;

public record ProductDto(
    String uuid, String title, String description, String price, String address
) { }
