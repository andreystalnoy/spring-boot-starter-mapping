package com.matteria.mapping.configuration;

import com.matteria.mapping.Mapping;
import com.matteria.mapping.configuration.model.Product;
import com.matteria.mapping.configuration.model.ProductDto;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.function.Function;

@Configuration
public class MappingConfiguration {

    @Mapping
    public Function<String, Integer> stringToInteger() {
        return Integer::valueOf;
    }

    @Mapping
    public Function<Integer, String> integerToString() {
        return Object::toString;
    }

    @Mapping("plus")
    public Function<String, Integer> stringToIntegerPlusOne() {
        return s -> Integer.parseInt(s) + 1;
    }

    @Mapping("plus")
    public Function<Integer, String> integerToStringPlusOne() {
        return integer -> integer.toString() + 1;
    }

    @Mapping
    public Function<Product, ProductDto> productToDto() {
        return product -> new ProductDto(
                product.getUuid().toString(),
                product.getName(),
                product.getDescription(),
                product.getPrice().toString()
        );
    }

    @Mapping
    public Function<ProductDto, Product> dtoToProduct() {
        return dto -> new Product(
                UUID.fromString(dto.uuid()),
                dto.title(),
                dto.description(),
                new BigDecimal(dto.price())
        );
    }

    @Mapping("hiddenUuid")
    public Function<Product, ProductDto> dtoToProductHiddenUuid() {
        return product -> new ProductDto(
                "hidden",
                product.getName(),
                product.getDescription(),
                product.getPrice().toString()
        );
    }

}
