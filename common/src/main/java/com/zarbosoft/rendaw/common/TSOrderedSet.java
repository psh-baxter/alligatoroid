package com.zarbosoft.rendaw.common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class TSOrderedSet<T> implements ROOrderedSetRef<T> {
  private final Set<T> unordered;
  private final List<T> ordered;

  public TSOrderedSet() {
    unordered = new HashSet<>();
    ordered = new ArrayList<>();
  }

  public static <T> TSOrderedSet<T> of(T... values) {
    TSOrderedSet<T> out = new TSOrderedSet<>();
    for (T value : values) {
      out.add(value);
    }
    return out;
  }

  public TSOrderedSet<T> add(T value) {
    boolean added = unordered.add(value);
    if (added) {
      ordered.add(value);
    }
    return this;
  }

  public TSOrderedSet<T> addAll(ROOrderedSetRef<T> values) {
    for (T value : values) {
      add(value);
    }
    return this;
  }

  @Override
  public Set<T> inner_() {
    return unordered;
  }

  @Override
  public boolean containsAll(ROSetRef<T> other) {
    return unordered.containsAll(other.inner_());
  }

  @Override
  public int size() {
    return ordered.size();
  }

  @Override
  public ROSet<T> own() {
    return new ROSet<>(unordered);
  }

  @Override
  public TSSet<T> mut() {
    return new TSSet<>(unordered);
  }

  @Override
  public boolean contains(T t) {
    return unordered.contains(t);
  }

  @Override
  public Iterator<T> iterator() {
    return ordered.iterator();
  }
}
