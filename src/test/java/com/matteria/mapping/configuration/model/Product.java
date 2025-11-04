package com.matteria.mapping.configuration.model;

import java.math.BigDecimal;
import java.util.UUID;

public record Product(
        UUID uuid,
        String name,
        String description,
        BigDecimal price,
        Address address) {
}
