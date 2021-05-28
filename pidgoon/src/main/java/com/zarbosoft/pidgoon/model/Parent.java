package com.zarbosoft.pidgoon.model;

public interface Parent<E> {
  void advance(Grammar grammar, Step step, Leaf leaf, E result, MismatchCause mismatchCause);

  void error(Grammar grammar, Step step, Leaf leaf, MismatchCause mismatchCause);
}
