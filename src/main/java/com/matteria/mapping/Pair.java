package com.matteria.mapping;

import java.util.Objects;

public class Pair<A, B> {
    private final String value;
    private final A first;
    private final B second;
    private final int hashcode;

    public Pair(String value, A a, B b) {
        this.value = value;
        this.first = a;
        this.second = b;
        this.hashcode = computeHashCode(value, first, second);
    }

    public static<A, B> int computeHashCode(String value, A first, B second) {
        int result = 31 + (value == null ? 0 : value.hashCode());
        result = 31 * result + (first == null ? 0 : first.hashCode());
        result = 31 * result + (second == null ? 0 : second.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pair<?, ?> pair)) return false;

        if (first instanceof Class && pair.first instanceof Class) {
            if (first != pair.first) return false;
        } else if (!Objects.equals(first, pair.first)) return false;

        if (second instanceof Class && pair.second instanceof Class) {
            if (second != pair.second) return false;
        } else if (!Objects.equals(second, pair.second)) return false;

        return Objects.equals(value, pair.value);
    }

    @Override
    public int hashCode() {
        return hashcode;
    }
}