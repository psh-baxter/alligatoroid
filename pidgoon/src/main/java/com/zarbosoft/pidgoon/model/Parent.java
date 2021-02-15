package com.zarbosoft.pidgoon.model;

public interface Parent {
  void advance(Parse step, Store store, MismatchCause mismatchCause);

  void error(Parse step, Store store, MismatchCause mismatchCause);

  long size(Parent stopAt, long start);
}
