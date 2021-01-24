package com.zarbosoft.rendaw.common;

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
  public TSList<T> sublist(int start, int end) {
    return new TSList<>(values.subList(start, end));
  }

  @Override
  public TSList<T> subFrom(int start) {
    return new TSList<>(values.subList(start, values.size()));
  }

  @Override
  public List<T> inner_() {
    return values;
  }

  @Override
  public TSList<T> mut() {
    return new TSList<>(new ArrayList<>(values));
  }

  @Override
  public int lastIndexOf(T value) {
    return values.lastIndexOf(value);
  }

  @Override
  public T last() {
    return values.get(values.size() - 1);
  }

  public TSList<T> add(T... val) {
    for (T t : val) {
      //noinspection UseBulkOperation
      values.add(t);
    }
    return this;
  }

  public void insert(int offset, T ...val) {
    values.addAll(offset, Arrays.asList(val));
  }

  public TSList<T> removeVal(T val) {
    values.remove(val);
    return this;
  }

  public TSList<T> remove(int index) {
    values.remove(index);
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

  public void clear() {
    values.clear();
  }

  public void insertAll(int offset, ROList<T> values) {
    this.values.addAll(offset, values.inner_());
  }

  public void reverse() {
    for (int i = 0; i < values.size() / 2; ++i) {
      T t = values.get(0);
      int ri = values.size() - i - 1;
      values.set(0, values.get(ri));
      values.set(ri, t);
    }
  }
}
