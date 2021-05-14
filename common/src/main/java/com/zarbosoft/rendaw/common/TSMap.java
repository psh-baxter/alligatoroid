package com.zarbosoft.rendaw.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class TSMap<K, V> implements ROMap<K, V> {
  private static final Object missing = new Object();
  public Map<K, V> inner;

  public TSMap(Consumer<TSMap<K, V>> build) {
    this.inner = new HashMap<>();
    build.accept(this);
  }

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
    Object val = ((Map) inner).getOrDefault(k, missing);
    if (val == missing) throw new Assertion();
    return (V) val;
  }

  @Override
  public TSMap<K, V> mut() {
    return new TSMap<>(new HashMap<>(inner));
  }

  @Override
  public V getOr(K k, Supplier<V> v) {
    Object val = ((Map) inner).getOrDefault(k, missing);
    if (val == missing) return v.get();
    return (V) val;
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

  public V removeGet(K k) {
    return inner.remove(k);
  }

  public TSMap<K, V> remove(K k) {
    inner.remove(k);
    return this;
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
    V got = inner.get(k);
    if (got != null) {
      return got;
    }
    got = s.get();
    inner.put(k, got);
    return got;
  }

  public TSMap<K, V> put(K k, V v) {
    putNull(k, v);
    return this;
  }

  public V update(K k, Function<V, V> map) {
    V got = map.apply(inner.get(k));
    inner.put(k, got);
    return got;
  }

  @Override
  public boolean some() {
    return !inner.isEmpty();
  }

  @Override
  public boolean none() {
    return inner.isEmpty();
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
