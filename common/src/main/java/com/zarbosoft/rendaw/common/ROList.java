package com.zarbosoft.rendaw.common;

import java.math.BigDecimal;
import java.util.List;

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
}
