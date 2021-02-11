package com.zarbosoft.pidgoon.model;

public interface Parent {
  void advance(Parse step, Store store, Object cause);

  void error(Parse step, Store store, Object cause);

  long size(Parent stopAt, long start);
}
