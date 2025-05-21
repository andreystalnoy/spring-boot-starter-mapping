package com.matteria.mapping.core;

import com.matteria.mapping.MappingException;
import java.util.*;
import java.util.function.Function;

public class MappingService {
    private static final String DEFAULT_NAME = "default";
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

        if (input.isEmpty()) {
            return new CollectionMapper<>(List.of());
        }

        Collection<O> result = new ArrayList<>(input.size());

        Class<?> commonInputClass = null;
        Function<Object, O> mapper = null;

        for (I element : input) {
            if (element == null) {
                result.add(null);
                continue;
            }

            if (commonInputClass == null || !commonInputClass.equals(element.getClass())) {
                commonInputClass = element.getClass();

                @SuppressWarnings("unchecked")
                Function<Object, O> newMapper = (Function<Object, O>) registry.get(
                        value, commonInputClass, outputClass);
                mapper = newMapper;

                if (mapper == null) {
                    throw new IllegalArgumentException("No mapper registered for " +
                            commonInputClass + " to " + outputClass + " with value " + value);
                }
            }

            result.add(mapper.apply(element));
        }

        return new CollectionMapper<>(result);
    }

    public static class CollectionMapper<O> {
        private final Collection<O> collection;
        private List<O> cachedList;
        private Set<O> cachedSet;

        public CollectionMapper(Collection<O> collection) {
            this.collection = collection;
        }

        public Set<O> toSet() {
            if (cachedSet == null) {
                // Use LinkedHashSet to preserve order if needed
                cachedSet = new LinkedHashSet<>(collection);
            }
            return cachedSet;
        }

        public List<O> toList() {
            if (cachedList == null) {
                if (collection instanceof List) {
                    // Avoid creating a new list if already a list
                    cachedList = new ArrayList<>(collection);
                } else {
                    cachedList = new ArrayList<>(collection);
                }
            }
            return cachedList;
        }
    }
}
