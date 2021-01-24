package com.zarbosoft.pidgoon.internal;

import com.zarbosoft.pidgoon.Store;
import com.zarbosoft.pidgoon.parse.Parse;

public interface Parent {
  void advance(Parse step, Store store, Object cause);

  void error(Parse step, Store store, Object cause);

  long size(Parent stopAt, long start);
}
