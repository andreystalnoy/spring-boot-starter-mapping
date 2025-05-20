package com.matteria.mapping.configuration;

import com.matteria.mapping.Mapping;
import org.springframework.context.annotation.Configuration;

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

}
