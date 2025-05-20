package com.matteria.mapping.core;

import java.util.function.Function;

public class MappingService {
    private static final String  DEFAULT_NAME = "default";
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
        return map(DEFAULT_NAME, input, outputClass);
    }
}
