package com.zarbosoft.rendaw.common;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface ROOrderedMap<K, V> extends Iterable<ROPair<K, V>> {
  public static final ROOrderedMap empty = new TSOrderedMap<>();

  V getOpt(K key);

  boolean has(K key);

  Iterator<V> iterValues();
}
