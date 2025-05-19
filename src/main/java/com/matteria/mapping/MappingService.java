package com.matteria.mapping;

import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class MappingService {
    private final MappingRegistry registry;

    public MappingService(MappingRegistry registry) {
        this.registry = registry;
    }

    public <I, O> O map(String value, I input, Class<O> outputClass) {
        @SuppressWarnings("unchecked")
        Class<I> inputClass = (Class<I>) input.getClass();
        Function<I, O> mapper = registry.get(value, inputClass, outputClass);
        if (mapper == null) {
            throw new IllegalArgumentException("No mapper registered for " + inputClass + " to " + outputClass +
                    " with value " + value);
        }
        return mapper.apply(input);
    }

    public <I, O> O map(I input, Class<O> outputClass) {
        return map("default", input, outputClass);
    }
}
