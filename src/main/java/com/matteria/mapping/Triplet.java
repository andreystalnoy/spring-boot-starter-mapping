package com.matteria.mapping;

import java.util.Objects;

public class Triplet<A, B> {
    public final String value;
    public final A first;
    public final B second;

    public Triplet(A a, B b) {
        this.value = "default";
        this.first = a;
        this.second = b;
    }

    public Triplet(String value, A a, B b) {
        this.value = value;
        this.first = a;
        this.second = b;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Triplet<?, ?> other)) return false;
        return Objects.equals(value, other.value)
                && Objects.equals(first, other.first)
                && Objects.equals(second, other.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, first, second);
    }
}