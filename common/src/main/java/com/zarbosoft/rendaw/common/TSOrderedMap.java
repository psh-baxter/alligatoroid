package com.zarbosoft.rendaw.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TSOrderedMap<K, V> implements ROOrderedMap<K, V> {
  final List<ROPair<K, V>> ordered;
  final Map<K, V> unordered;

  public TSOrderedMap() {
    ordered = new ArrayList<>();
    unordered = new HashMap<>();
  }

  public TSOrderedMap(Consumer<TSOrderedMap<K, V>> c) {
    this();
    c.accept(this);
  }

  public TSOrderedMap<K, V> put(K k, V v) {
    if (!unordered.containsKey(k)) {
      unordered.put(k, v);
      ordered.add(new ROPair<>(k, v));
    }
    return this;
  }

  public V getCreate(K k, Supplier<V> s) {
    V got = unordered.get(k);
    if (got != null) {
      return got;
    }
    got = s.get();
    unordered.put(k, got);
    ordered.add(new ROPair<>(k, got));
    return got;
  }

  @Override
  public V getOpt(K key) {
    return unordered.get(key);
  }

  @Override
  public boolean has(K key) {
    return unordered.containsKey(key);
  }

  @Override
  public Iterator<V> iterValues() {
    Iterator<ROPair<K, V>> iter = ordered.iterator();
    return new Iterator<V>() {
      @Override
      public boolean hasNext() {
        return iter.hasNext();
      }

      @Override
      public V next() {
        return iter.next().second;
      }
    };
  }

  @Override
  public Iterator<ROPair<K, V>> iterator() {
    return ordered.iterator();
  }

  public TSOrderedMap<K, V> putNew(K k, V v) {
    if (unordered.containsKey(k)) throw new Assertion();
    put(k, v);
    return this;
  }
}
