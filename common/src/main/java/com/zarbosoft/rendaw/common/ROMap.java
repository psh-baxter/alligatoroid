package com.zarbosoft.rendaw.common;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public interface ROMap<K, V> extends Iterable<Map.Entry<K, V>> {
  public static final ROMap empty = new TSMap<>();

  V getOr(K k, Supplier<V> v);

  ROSet<K> keys();

  Iterator<V> iterValues();

  boolean has(K k);

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

  /**
   * Get, asserts value exists
   *
   * @param k
   * @return
   */
  public V getNull(K k);

  TSMap<K, V> mut();

  boolean some();
  boolean none();
}
