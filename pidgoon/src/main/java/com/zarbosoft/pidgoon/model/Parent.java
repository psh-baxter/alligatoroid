package com.zarbosoft.pidgoon.model;

public interface Parent<E> {
  void advance(Grammar grammar, Step step, Step.Branch branch, E result, MismatchCause mismatchCause);

  void error(Grammar grammar, Step step, Step.Branch branch, MismatchCause mismatchCause);
}
