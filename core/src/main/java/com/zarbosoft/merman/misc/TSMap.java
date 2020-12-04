package com.zarbosoft.merman.misc;

import com.zarbosoft.rendaw.common.Assertion;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TSMap<K, V> {
  private static final Object missing = new Object();
  public Map<K, V> inner;

  public TSMap() {
    this.inner = new HashMap<>();
  }

  public TSMap(Map<K, V> inner) {
    this.inner = inner;
  }

  public V getOpt(K k) {
    return inner.get(k);
  }

  public V get(K k) {
    if (k == null) throw new Assertion();
    return getNull(k);
  }

  public V getNull(K k) {
    Object out = ((Map) inner).getOrDefault(k, missing);
    if (out == missing) throw new Assertion();
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

  public static class Builder<K, V> {
    TSMap<K, V> val = new TSMap<>();

    public Builder<K, V> put(K k, V v) {
      val.putNew(k, v);
      return this;
    }

    public TSMap<K, V> build() {
      return val;
    }

    public Builder<K, V> putNull(K k, V v) {
      val.putNewNull(k, v);
      return this;
    }
  }
}
