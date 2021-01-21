package com.zarbosoft.merman.misc;

import com.zarbosoft.merman.syntax.alignments.AlignmentSpec;
import com.zarbosoft.rendaw.common.Assertion;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public interface ROMap<K, V> extends Iterable<Map.Entry<K, V>> {
  public final static ROMap empty = new TSMap<>();

  V getOr(K k, Supplier<V> v);

  Set<K> keys();

  Iterator<V> iterValues();

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
}
