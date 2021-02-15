package com.zarbosoft.pidgoon;

import com.zarbosoft.pidgoon.model.MismatchCause;
import com.zarbosoft.pidgoon.model.Parent;
import com.zarbosoft.pidgoon.model.Parse;
import com.zarbosoft.pidgoon.model.Store;

public abstract class BaseParent implements Parent {
  public final Parent parent;

  public BaseParent(final Parent parent) {
    super();
    this.parent = parent;
  }

  @Override
  public void error(final Parse step, final Store store, final MismatchCause cause) {
    parent.error(step, store, cause);
  }

  @Override
  public long size(final Parent stopAt, final long start) {
    return parent.size(stopAt, start + 1);
  }
}
