package com.matteria.mapping.configuration.model;

import java.math.BigDecimal;
import java.util.UUID;

public class Product {
    private final UUID uuid;
    private final String name;
    private final String description;
    private final BigDecimal price;

    public Product(UUID uuid, String name, String description, BigDecimal price) {
        this.uuid = uuid;
        this.name = name;
        this.description = description;
        this.price = price;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }
}
