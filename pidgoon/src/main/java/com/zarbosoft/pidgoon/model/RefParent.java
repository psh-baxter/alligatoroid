package com.zarbosoft.pidgoon.model;

import java.util.ArrayList;
import java.util.List;

public class RefParent implements Parent {
  public final Parent originalParent;
  public final List<Parent> loopParents = new ArrayList<>();

  public RefParent(final Parent parent) {
    originalParent = parent;
  }

  @Override
  public void advance(final Parse step, final Store store, final Object cause) {
    final Store tempStore = store.pop();
    originalParent.advance(step, tempStore, cause);
    for (final Parent p : loopParents) {
      p.advance(step, tempStore.inject(p.size(this, 1)), cause);
    }
  }

  @Override
  public void error(final Parse step, final Store store, final Object cause) {
    originalParent.error(step, store, cause);
  }

  @Override
  public long size(final Parent stopAt, final long start) {
    if (stopAt == this) return start;
    return originalParent.size(stopAt, start + 1);
  }
}
