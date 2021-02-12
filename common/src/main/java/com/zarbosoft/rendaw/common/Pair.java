package com.zarbosoft.rendaw.common;

import java.util.Comparator;
import java.util.Objects;

public class Pair<T1, T2> implements Comparable<Pair> {
  public static final Comparator<Pair<? extends Comparable, ? extends Comparable>> comparator =
      new ChainComparator<Pair<? extends Comparable, ? extends Comparable>>()
          .lesserFirst(a -> a.first)
          .lesserFirst(a -> a.second)
          .build();

  public T1 first;
  public T2 second;

  public Pair(final T1 first, final T2 second) {
    super();
    this.first = first;
    this.second = second;
  }

  @Override
  public String toString() {
    return Format.format("Pair[%s, %s]", first, second);
  }

  @Override
  public boolean equals(final Object obj) {
    if (!(obj instanceof Pair)) return false;
    final Pair obj1 = (Pair) obj;
    if (first == null && obj1.first != null) return false;
    if (second == null && obj1.second != null) return false;
    return Objects.equals(first, obj1.first) && Objects.equals(second, obj1.second);
  }

  @Override
  public int compareTo(final Pair o) {
    return comparator.compare((Pair) this, o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(first, second);
  }
}
