package com.matteria.mapping.core;

import com.matteria.mapping.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class MappingRegistry {
    private final Map<Pair<Class<?>, Class<?>>, Function<?, ?>> registry = new HashMap<>();

    public <I, O> void register(String value, Class<?> inputClass, Class<?> outputClass, Function<?, ?> function) {
        Pair<Class<?>, Class<?>> key = new Pair<>(value, inputClass, outputClass);
        if (registry.containsKey(key)) {
            throw new IllegalStateException("Mapper already registered: " + key);
        }
        registry.put(key, function);
    }

    @SuppressWarnings("unchecked")
    public <I, O> Function<I, O> get(String value, Class<I> inputClass, Class<O> outputClass) {
        return (Function<I, O>) registry.get(new Pair<>(value, inputClass, outputClass));
    }
}
