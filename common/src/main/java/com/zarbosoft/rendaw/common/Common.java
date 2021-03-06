package com.zarbosoft.rendaw.common;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Consumer;

public class Common {
  public static <T> Iterable<T> iterable(final Iterator<T> arg) {
    return new IteratorIterable<>(arg);
  }

  public static <T> boolean isOrdered(final Comparator<T> comparator, final T a, final T b) {
    return comparator.compare(a, b) <= 0;
  }

  public static <T extends Comparable<T>> boolean isOrdered(final T a, final T b) {
    return a.compareTo(b) <= 0;
  }

  public static <T extends Comparable<T>> boolean isOrderedExclusive(final T a, final T b) {
    return a.compareTo(b) < 0;
  }

  public static RuntimeException uncheck(final Exception e) {
    if (e instanceof RuntimeException) return (RuntimeException) e;
    if (e instanceof IOException) return new UncheckedIOException((IOException) e);
    return new UncheckedException(e);
  }

  public static <T> T uncheck(final Thrower1<T> code) {
    try {
      return code.get();
    } catch (final Exception e) {
      throw Common.uncheck(e);
    }
  }

  public static void uncheck(final Thrower2 code) {
    try {
      code.get();
    } catch (final Exception e) {
      throw Common.uncheck(e);
    }
  }

  @FunctionalInterface
  public interface Thrower1<T> {
    T get() throws Exception, Error;
  }

  @FunctionalInterface
  public interface Thrower2 {
    void get() throws Exception, Error;
  }

  public static class UncheckedException extends RuntimeException {
    public UncheckedException(final Throwable e) {
      super(e);
    }
  }

  private static class IteratorIterable<T> implements Iterable<T> {
    private final Iterator<T> arg;

    public IteratorIterable(Iterator<T> arg) {
      this.arg = arg;
    }

    @Override
    public void forEach(Consumer<? super T> action) {
      throw new Assertion();
    }

    @Override
    public Iterator<T> iterator() {
      return arg;
    }
  }
}
