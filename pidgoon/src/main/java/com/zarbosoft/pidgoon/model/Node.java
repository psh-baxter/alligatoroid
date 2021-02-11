package com.zarbosoft.pidgoon.model;

import com.zarbosoft.rendaw.common.ROMap;

public abstract class Node {
  public void context(
          final Parse context, final Store store, final Parent parent, final Object cause) {
    context(context, store, parent, ROMap.empty, cause);
  }

  public abstract void context(
      Parse context, Store store, Parent parent, ROMap<Object, RefParent> seen, Object cause);
}
