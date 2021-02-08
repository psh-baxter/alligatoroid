package com.zarbosoft.pidgoon.internal;

import com.zarbosoft.pidgoon.Store;
import com.zarbosoft.pidgoon.parse.Parse;

public abstract class BaseParent implements Parent {
  public final Parent parent;

  public BaseParent(final Parent parent) {
    super();
    this.parent = parent;
  }

  @Override
  public void error(final Parse step, final Store store, final Object cause) {
    parent.error(step, store, cause);
  }

  @Override
  public long size(final Parent stopAt, final long start) {
    return parent.size(stopAt, start + 1);
  }
}
