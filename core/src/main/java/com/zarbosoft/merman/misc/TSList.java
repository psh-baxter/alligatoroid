package com.zarbosoft.merman.misc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class TSList<T> implements ROList<T> {
  private final List<T> values;

  public TSList(ROList<T> other) {
    this.values = new ArrayList<>(((TSList<T>) other).values);
  }

  public TSList() {
    this.values = new ArrayList<>();
  }

  private TSList(List<T> values) {
    this.values = values;
  }

  public static <T> TSList<T> of(T... values) {
    return new TSList<>(Arrays.asList(values));
  }

  @Override
  public Iterator<T> iterator() {
    return values.iterator();
  }

  public boolean isEmpty() {
    return values.isEmpty();
  }

  public int size() {
    return values.size();
  }

  public T get(int i) {
    return values.get(i);
  }

  @Override
  public TSSet<T> toSet() {
    return new TSSet<T>(new HashSet<>(values));
  }

  @Override
  public ROList<T> sublist(int start, int end) {
    return new TSList<>(values.subList(start,end));
  }

  public TSList<T> add(T val) {
    values.add(val);
    return this;
  }

  public TSList<T> remove(T val) {
    values.remove(val);
    return this;
  }

  public TSList<T> addAll(ROList<? extends T> val) {
    values.addAll(((TSList<T>) val).values);
    return this;
  }

  public TSList<T> removeAll(ROList<? extends T> val) {
    values.removeAll(((TSList<T>) val).values);
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TSList<?> tsList = (TSList<?>) o;
    return Objects.equals(values, tsList.values);
  }

  @Override
  public int hashCode() {
    return Objects.hash(values);
  }
}
