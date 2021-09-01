package com.zarbosoft.rendaw.common;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface ROMap<K, V> extends Iterable<Map.Entry<K, V>> {
  public static final ROMap empty = new TSMap<>();

  V getOr(K k, Supplier<V> v);

  ROSet<K> keys();

  int size();

  Iterator<V> iterValues();

  boolean has(K k);

  @Override
  default void forEach(Consumer<? super Map.Entry<K, V>> action) {
    throw new Assertion();
  }

  /**
   * Get, no assertions
   *
   * @param k
   * @return
   */
  public V getOpt(K k);

  /**
   * Get, asserts key is not null, asserts value exists
   *
   * @param k
   * @return
   */
  public V get(K k);

  TSMap<K, V> mut();

  boolean some();

  boolean none();
}
