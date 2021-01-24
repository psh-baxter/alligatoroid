package com.zarbosoft.rendaw.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class TSMap<K, V> implements ROMap<K, V> {
  private static final Object missing = new Object();
  public Map<K, V> inner;

  public TSMap() {
    this.inner = new HashMap<>();
  }

  public TSMap(Map<K, V> inner) {
    this.inner = inner;
  }

  @Override
  public V getOpt(K k) {
    return inner.get(k);
  }

  @Override
  public V get(K k) {
    if (k == null) throw new Assertion();
    return getNull(k);
  }

  @Override
  public V getNull(K k) {
    Object out = ((Map) inner).getOrDefault(k, missing);
    if (out == missing) throw new Assertion();
    return (V) out;
  }

  @Override
  public TSMap<K, V> mut() {
    return new TSMap<>(new HashMap<>(inner));
  }

  @Override
  public V getOr(K k, Supplier<V> v) {
    Object out = ((Map) inner).getOrDefault(k, missing);
    if (out == missing) return v.get();
    return (V) out;
  }

  public V putReplace(K k, V v) {
    if (k == null) throw new Assertion();
    return putReplaceNull(k, v);
  }

  public V putReplaceNull(K k, V v) {
    return inner.put(k, v);
  }

  public TSMap<K, V> putNew(K k, V v) {
    if (k == null) throw new Assertion();
    putNull(k, v);
    return this;
  }

  public TSMap<K, V> putNull(K k, V v) {
    V old = inner.put(k, v);
    if (old != null) throw new Assertion();
    return this;
  }

  public V remove(K k) {
    return inner.remove(k);
  }

  public Set<Map.Entry<K, V>> entries() {
    return inner.entrySet();
  }

  public boolean contains(K child) {
    return inner.containsKey(child);
  }

  public Collection<V> values() {
    return inner.values();
  }

  public V getCreate(K k, Supplier<V> s) {
    return inner.computeIfAbsent(k, ignored -> s.get());
  }

  public TSMap<K, V> put(K k, V v) {
    putNull(k, v);
    return this;
  }

  @Override
  public Iterator<Map.Entry<K, V>> iterator() {
    return inner.entrySet().iterator();
  }

  @Override
  public ROSet<K> keys() {
    return new ROSet<>(inner.keySet());
  }

  @Override
  public Iterator<V> iterValues() {
    return inner.values().iterator();
  }

  @Override
  public boolean has(K k) {
    return inner.containsKey(k);
  }

  public void clear() {
    inner.clear();
  }

  public TSMap<K, V> putAll(ROMap<K, V> other) {
    inner.putAll(((TSMap<K, V>) other).inner);
    return this;
  }
}
