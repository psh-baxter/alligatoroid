package com.zarbosoft.merman.misc;

import java.util.Set;

public interface ROSetRef<T> extends Iterable<T> {
    Set<T> inner_();

    ROSetRef<T> union(ROSetRef<T> other);

    ROSetRef<T> intersect(ROSetRef<T> other);

    boolean isEmpty();

    boolean some();

    boolean none();

    boolean containsAll(ROSetRef<T> other);

    int size();

    ROSet<T> own();

    TSSet<T> mut();
}
