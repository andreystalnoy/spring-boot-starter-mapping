package com.matteria.mapping.core;

import com.matteria.mapping.MappingException;
import java.util.*;
import java.util.function.Function;

public class MappingService {
    private static final String  DEFAULT_NAME = "default";
    private final MappingRegistry registry;

    public MappingService(MappingRegistry registry) {
        this.registry = registry;
    }

    public <I, O> O map(I input, Class<O> outputClass) {
        return map(DEFAULT_NAME, input, outputClass);
    }

    public <I, O> O map(String value, I input, Class<O> outputClass) {
        if (input == null) {
            throw new MappingException("Input object cannot be null");
        } else if (outputClass == null) {
            throw new MappingException("Output class cannot be null");
        }

        @SuppressWarnings("unchecked")
        Class<I> inputClass = (Class<I>) input.getClass();
        Function<I, O> mapper = registry.get(value, inputClass, outputClass);
        if (mapper == null) {
            throw new IllegalArgumentException("No mapper registered for " + inputClass + " to " + outputClass +
                    " with value " + value);
        }
        return mapper.apply(input);
    }

    public <I, O> CollectionMapper<O> map(Collection<I> input, Class<O> elementOutputClass) {
        return map(DEFAULT_NAME, input, elementOutputClass);
    }

    public <I, O> CollectionMapper<O> map(String value, Collection<I> input, Class<O> outputClass) {
        if (input == null) {
            throw new MappingException("Input object cannot be null");
        } else if (outputClass == null) {
            throw new MappingException("Output class cannot be null");
        }

        Collection<O> result = new ArrayList<>(input.size());

        for (I element : input) {
            if (element == null) {
                result.add(null);
                continue;
            }
            result.add(map(value, element, outputClass));
        }

        return new CollectionMapper<>(result);
    }

    public static class CollectionMapper<O> {
        private final Collection<O> collection;

        public CollectionMapper(Collection<O> collection) {
            this.collection = collection;
        }

        public Set<O> toSet() {
            return new HashSet<>(collection);
        }

        public List<O> toList() {
            return new ArrayList<>(collection);
        }
    }
}
