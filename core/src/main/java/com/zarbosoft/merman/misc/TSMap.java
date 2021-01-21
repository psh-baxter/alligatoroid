package com.zarbosoft.merman.misc;

import com.zarbosoft.merman.document.values.Value;
import com.zarbosoft.merman.syntax.alignments.AlignmentSpec;
import com.zarbosoft.rendaw.common.Assertion;

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
  public V getOr(K k, Supplier<V> v) {
    Object out = ((Map) inner).getOrDefault(k, missing);
    if (out == missing) return v.get();
    return (V) out;
  }

  public V put(K k, V v) {
    if (k == null) throw new Assertion();
    return putNull(k, v);
  }

  public V putNull(K k, V v) {
    return inner.put(k, v);
  }

  public void putNew(K k, V v) {
    if (k == null) throw new Assertion();
    putNewNull(k, v);
  }

  public void putNewNull(K k, V v) {
    V old = inner.put(k, v);
    if (old != null) throw new Assertion();
  }

  public V remove(K k) {
    return inner.remove(k);
  }

  public Set<Map.Entry<K, V>> entries() {
    return inner.entrySet();
  }

  public Set<K> keySet() {
    return inner.keySet();
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

  public TSMap<K, V> putChain(K k, V v) {
    putNewNull(k, v);
    return this;
  }

  @Override
  public Iterator<Map.Entry<K, V>> iterator() {
    return inner.entrySet().iterator();
  }

  @Override
  public Set<K> keys() {
    return inner.keySet();
  }

  @Override
  public Iterator<V> iterValues() {
    return inner.values().iterator();
  }

  public void clear() {
    inner.clear();
  }

  public TSMap<K, V> putAll(ROMap<K, V> other) {
    inner.putAll(((TSMap<K, V>) other).inner);
    return this;
  }
}
