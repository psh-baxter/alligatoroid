package com.zarbosoft.rendaw.common;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ChainComparator<T> {
  private final List<BiFunction<T, T, Integer>> steps = new ArrayList<>();

  public ChainComparator() {}

  public ChainComparator<T> trueFirst(final Function<T, Boolean> accessor) {
    steps.add((a, b) -> accessor.apply(b).compareTo(accessor.apply(a)));
    return this;
  }

  public ChainComparator<T> falseFirst(final Function<T, Boolean> accessor) {
    steps.add((a, b) -> accessor.apply(a).compareTo(accessor.apply(b)));
    return this;
  }

  public <R extends Comparable> ChainComparator<T> lesserFirst(final Function<T, R> accessor) {
    steps.add((a, b) -> accessor.apply(a).compareTo(accessor.apply(b)));
    return this;
  }

  public <R extends Comparable> ChainComparator<T> greaterFirst(final Function<T, R> accessor) {
    steps.add((a, b) -> accessor.apply(b).compareTo(accessor.apply(a)));
    return this;
  }

  public Comparator<T> build() {
    return new InnerComparator<T>(steps);
  }

  private static class InnerComparator<T> implements Comparator<T> {
    private final List<BiFunction<T, T, Integer>> steps;

    public InnerComparator(List<BiFunction<T, T, Integer>> steps) {
      this.steps = steps;
    }

    @Override
    public int compare(final T o1, final T o2) {
      for (final BiFunction<T, T, Integer> step : steps) {
        int result = step.apply(o1, o2);
        if (result != 0) return result;
      }
      return 0;
    }
  }
}
