package com.zarbosoft.rendaw.common;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ROSet<T> implements ROSetRef<T> {
  @SuppressWarnings("rawtypes")
  public static ROSet empty = new ROSet(new HashSet<>());

  final Set<T> data;

  ROSet(Set<T> data) {
    this.data = new HashSet<>(data);
  }

  @Override
  public Set<T> inner_() {
    return data;
  }

  @Override
  public boolean isEmpty() {
    return data.isEmpty();
  }

  @Override
  public boolean some() {
    return !data.isEmpty();
  }

  @Override
  public boolean none() {
    return data.isEmpty();
  }

  @Override
  public boolean containsAll(ROSetRef<T> other) {
    return data.containsAll(other.inner_());
  }

  @Override
  public int size() {
    return data.size();
  }

  @Override
  public ROSet<T> own() {
    return this;
  }

  @Override
  public Iterator<T> iterator() {
    return data.iterator();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ROSetRef)) return false;
    return data.equals(((ROSetRef<?>) o).inner_());
  }

  @Override
  public int hashCode() {
    return data.hashCode();
  }

  @Override
  public TSSet<T> mut() {
    return new TSSet<>(new HashSet<>(data));
  }

  @Override
  public boolean contains(T t) {
    return data.contains(t);
  }
}
