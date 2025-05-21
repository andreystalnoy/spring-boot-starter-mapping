package com.matteria.mapping.configuration;

import com.matteria.mapping.Mapping;
import com.matteria.mapping.configuration.model.Address;
import com.matteria.mapping.configuration.model.Country;
import com.matteria.mapping.configuration.model.Product;
import com.matteria.mapping.configuration.model.ProductDto;
import com.matteria.mapping.core.MappingService;
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
    public Function<Product, ProductDto> productToDto(@Mapping Function<Address, String> addressToString) {
        return product ->
            new ProductDto(
                    product.uuid().toString(),
                    product.name(),
                    product.description(),
                    product.price().toString(),
                    addressToString.apply(product.address())
            );
    }

    @Mapping
    public Function<ProductDto, Product> dtoToProduct(@Mapping Function<String, Address> stringToAddress) {
        return dto -> new Product(
                UUID.fromString(dto.uuid()),
                dto.title(),
                dto.description(),
                new BigDecimal(dto.price()),
                stringToAddress.apply(dto.address())
        );
    }

    @Mapping("hiddenUuid")
    public Function<Product, ProductDto> dtoToProductHiddenUuid(@Mapping Function<Address, String> addressToString) {
        return product -> new ProductDto(
                "hidden",
                product.name(),
                product.description(),
                product.price().toString(),
                addressToString.apply(product.address())
        );
    }

    @Mapping
    public Function<Address, String> addressToString() {
        return address -> {
            if (address != null)
                return address.formattedAddress();
            else
                return null;
        };
    }

    @Mapping
    public Function<String, Address> stringToAddress() {
        return address -> {
            String[] addressParts = address.split(", ");
            return new Address(addressParts[0], addressParts[1], addressParts[2],
                    Country.valueOf(addressParts[3]));
        };
    }
}
