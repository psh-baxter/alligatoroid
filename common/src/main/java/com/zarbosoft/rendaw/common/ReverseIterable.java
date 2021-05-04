package com.zarbosoft.rendaw.common;

import com.zarbosoft.rendaw.common.ROList;

import java.util.Iterator;

public class ReverseIterable<T> implements Iterable<T> {
  private final ROList<T> data;

  public ReverseIterable(ROList<T> data) {
    this.data = data;
  }

  @Override
  public Iterator<T> iterator() {
    return new Iterator<T>() {
      int i = 0;

      @Override
      public boolean hasNext() {
        return i < data.size();
      }

      @Override
      public T next() {
        return data.get(data.size() - 1 - i++);
      }
    };
  }
}
