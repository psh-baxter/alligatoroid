package com.zarbosoft.rendaw.common;

import java.util.Set;
import java.util.function.Consumer;

/**
 * Guarantees iteration is in addition order
 * @param <T>
 */
public interface ROOrderedSetRef<T> extends ROSetRef<T> {
    final ROOrderedSetRef empty = new TSOrderedSet();
}
