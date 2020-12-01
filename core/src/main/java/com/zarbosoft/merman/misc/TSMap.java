package com.zarbosoft.merman.misc;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TSMap<K, V> {
  public Map<K, V> inner;

  public TSMap() {
    this.inner = new HashMap<>();
  }

  public TSMap(Map<K, V> inner) {
    this.inner = inner;
  }

  public V get(K k) {
    return inner.get(k);
  }

  public V put(K k, V v) {
    return inner.put(k, v);
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
}
