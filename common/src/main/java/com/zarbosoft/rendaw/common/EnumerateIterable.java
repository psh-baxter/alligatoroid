package com.zarbosoft.rendaw.common;

import java.util.Iterator;

public class EnumerateIterable<T> implements Iterable<EnumerateIterable.El<T>> {
  public final Iterable<T> inner;

  public EnumerateIterable(Iterable<T> inner) {
    this.inner = inner;
  }

  @Override
  public Iterator<El<T>> iterator() {
    Iterator<T> inner = this.inner.iterator();
    return new Iterator<El<T>>() {
      int index = 0;

      @Override
      public boolean hasNext() {
        return inner.hasNext();
      }

      @Override
      public El<T> next() {
        return new El<>(index++, inner.next());
      }
    };
  }

  public static class El<T> {
    public final int index;
    public final T value;

    public El(int index, T value) {
      this.index = index;
      this.value = value;
    }
  }
}
