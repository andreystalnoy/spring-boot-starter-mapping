package com.matteria.mapping.core;

import com.matteria.mapping.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class MappingRegistry {
    private final Map<Pair<Class<?>, Class<?>>, Function<?, ?>> registry = new HashMap<>();
    private final Map<Integer, Function<?, ?>> lookupCache = new ConcurrentHashMap<>();

    public <I, O> void register(String value, Class<?> inputClass, Class<?> outputClass, Function<?, ?> function) {
        Pair<Class<?>, Class<?>> key = new Pair<>(value, inputClass, outputClass);
        if (registry.containsKey(key)) {
            throw new IllegalStateException("Mapper already registered: " + key);
        }
        registry.put(key, function);
    }

    @SuppressWarnings("unchecked")
    public <I, O> Function<I, O> get(String value, Class<I> inputClass, Class<O> outputClass) {
        int hash = Pair.computeHashCode(value, inputClass, outputClass);

        Function<I, O> cachedFunction = (Function<I, O>) lookupCache.get(hash);
        if (cachedFunction != null) {
            return cachedFunction;
        }

        // Fall back to regular lookup if not in cache
        Pair<Class<?>, Class<?>> key = new Pair<>(value, inputClass, outputClass);
        Function<I, O> function = (Function<I, O>) registry.get(key);

        if (function != null) {
            lookupCache.put(hash, function);
        }

        return function;
    }
}
