package com.matteria.mapping;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class MappingRegistry {
    private final Map<Triplet<Class<?>, Class<?>>, Function<?, ?>> registry = new HashMap<>();

    public <I, O> void register(String value, Class<?> inputClass, Class<?> outputClass, Function<?, ?> function) {
        Triplet<Class<?>, Class<?>> key = new Triplet<>(value, inputClass, outputClass);
        if (registry.containsKey(key)) {
            throw new IllegalStateException("Mapper already registered: " + key);
        }
        registry.put(key, function);
    }

    @SuppressWarnings("unchecked")
    public <I, O> Function<I, O> get(String value, Class<I> inputClass, Class<O> outputClass) {
        return (Function<I, O>) registry.get(new Triplet<>(value, inputClass, outputClass));
    }
}
