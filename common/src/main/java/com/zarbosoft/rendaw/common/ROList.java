package com.zarbosoft.rendaw.common;

import java.util.List;
import java.util.function.Consumer;

public interface ROList<T> extends Iterable<T> {
  @SuppressWarnings("rawtypes")
  public static final ROList empty = new TSList();

  public boolean isEmpty();

  public int size();

  public T get(int i);

  TSSet<T> toSet();

  ROList<T> sublist(int start, int end);

  ROList<T> subFrom(int start);

  List<T> inner_();

  TSList<T> mut();

  int lastIndexOf(T value);

  T last();

  T getRev(int i);

  default void forEach(Consumer<? super T> action) {
    throw new Assertion();
  }

  /**
   * @param other
   * @return number of elements that match; 0 = no match, size() == all match
   */
  int longestMatch(ROList<T> other);

  ROList<T> subUntil(int end);
}
