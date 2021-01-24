package com.zarbosoft.rendaw.common;

import java.util.Comparator;
import java.util.Objects;

public class ROPair<T1, T2> implements Comparable<ROPair> {
  public static final Comparator<ROPair<? extends Comparable, ? extends Comparable>> comparator =
      new ChainComparator<ROPair<? extends Comparable, ? extends Comparable>>()
          .lesserFirst(a -> a.first)
          .lesserFirst(a -> a.second)
          .build();

  public final T1 first;
  public final T2 second;

  public ROPair(final T1 first, final T2 second) {
    super();
    this.first = first;
    this.second = second;
  }

  @Override
  public String toString() {
    return String.format("ROPair[%s, %s]", first, second);
  }

  @Override
  public boolean equals(final Object obj) {
    if (!(obj instanceof ROPair)) return false;
    final ROPair obj1 = (ROPair) obj;
    if (first == null && obj1.first != null) return false;
    if (second == null && obj1.second != null) return false;
    return Objects.equals(first, obj1.first) && Objects.equals(second, obj1.second);
  }

  @Override
  public int compareTo(final ROPair o) {
    return comparator.compare((ROPair<? extends Comparable, ? extends Comparable>) this, o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(first, second);
  }
}
