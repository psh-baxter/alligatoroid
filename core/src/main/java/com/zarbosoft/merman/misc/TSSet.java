package com.zarbosoft.merman.misc;

import com.google.common.collect.Sets;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class TSSet<T> implements ROSetRef<T> {
  private Set<T> data;

  public TSSet() {
    data = new HashSet<>();
  }

  TSSet(Set<T> data) {
    this.data = new HashSet<>(data);
  }

  public TSSet(ROSet<T> data) {
    this.data = new HashSet<>(data.data);
  }

  public TSSet(T[] initial) {
    this.data = new HashSet<>(Arrays.asList(initial));
  }

  public ROSet<T> ro() {
    Set<T> data = this.data;
    this.data = null;
    return new ROSet<>(data);
  }

  public ROSet<T> roCopy() {
    return new ROSet<>(new HashSet<>(data));
  }

  public boolean addNew(T v) {
    return data.add(v);
  }

  public TSSet<T> add(T v) {
    data.add(v);
    return this;
  }

  public TSSet<T> remove(T v) {
    data.remove(v);
    return this;
  }

  public TSSet<T> addAll(ROSetRef<T> data) {
    this.data.addAll(data.inner_());
    return this;
  }

  public TSSet<T> removeAll(ROSetRef<T> data) {
    this.data.removeAll(data.inner_());
    return this;
  }

  public boolean removeAnyOld(ROSetRef<T> remove) {
    return data.removeAll(remove.inner_());
  }

  public boolean addAnyNew(ROSetRef<T> add) {
    return data.addAll(add.inner_());
  }

  @Override
  public Set<T> inner_() {
    return data;
  }

  @Override
  public TSSet<T> union(ROSetRef<T> other) {
    return new TSSet<T>(Sets.union(data, other.inner_()));
  }

  @Override
  public TSSet<T> intersect(ROSetRef<T> other) {
    return new TSSet<T>(Sets.intersection(data, other.inner_()));
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
    return roCopy();
  }

  @Override
  public TSSet<T> mut() {
    return copy();
  }

  @Override
  public Iterator<T> iterator() {
    return data.iterator();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TSSet<?> tsSet = (TSSet<?>) o;
    return data.equals(tsSet.data);
  }

  @Override
  public int hashCode() {
    return data.hashCode();
  }

  public TSSet<T> copy() {
    return new TSSet<>(new HashSet<>(data));
  }

  public void addAll(T[] remove) {
    this.data.addAll(Arrays.asList(remove));
  }

  public boolean removeOld(T t) {
    return this.data.remove(t);
  }
}
