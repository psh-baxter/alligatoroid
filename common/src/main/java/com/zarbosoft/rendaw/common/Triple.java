package com.zarbosoft.rendaw.common;

import java.util.Comparator;
import java.util.Objects;

public class Triple<T1, T2, T3> implements Comparable<Triple> {
  public static final Comparator<
          Triple<? extends Comparable, ? extends Comparable, ? extends Comparable>>
      comparator =
          new ChainComparator<
                  Triple<? extends Comparable, ? extends Comparable, ? extends Comparable>>()
              .lesserFirst(a -> a.first)
              .lesserFirst(a -> a.second)
              .lesserFirst(a -> a.third)
              .build();

  @FunctionalInterface
  public interface Consumer<T1, T2, T3> {
    void accept(T1 a, T2 b, T3 c);
  }

  @FunctionalInterface
  public interface Function<R, T1, T2, T3> {
    R accept(T1 a, T2 b, T3 c);
  }

  public T1 first;
  public T2 second;
  public T3 third;

  public Triple(final T1 first, final T2 second, final T3 third) {
    super();
    this.first = first;
    this.second = second;
    this.third = third;
  }

  @FunctionalInterface
  public interface MapOperator<A, B, C, D> {
    D apply(A a, B b, C c);
  }

  public <T> T map(final MapOperator<T1, T2, T3, T> operator) {
    return operator.apply(first, second, third);
  }

  @Override
  public String toString() {
    return String.format("Triple[%s, %s, %s]", first, second, third);
  }

  @Override
  public boolean equals(final Object obj) {
    if (!(obj instanceof Triple)) return false;
    final Triple obj1 = (Triple) obj;
    if (first == null && obj1.first != null) return false;
    if (second == null && obj1.second != null) return false;
    if (third == null && obj1.third != null) return false;
    return Objects.equals(first, obj1.first)
        && Objects.equals(second, obj1.second)
        && Objects.equals(third, obj1.third);
  }

  @Override
  public int compareTo(final Triple o) {
    return comparator.compare(
        (Triple<? extends Comparable, ? extends Comparable, ? extends Comparable>) this, o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(first, second, third);
  }
}
