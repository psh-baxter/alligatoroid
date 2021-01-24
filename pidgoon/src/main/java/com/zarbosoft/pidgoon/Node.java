package com.zarbosoft.pidgoon;

import com.zarbosoft.pidgoon.internal.Parent;
import com.zarbosoft.pidgoon.nodes.Reference.RefParent;
import com.zarbosoft.pidgoon.parse.Parse;
import com.zarbosoft.rendaw.common.ROMap;

public abstract class Node {
  public void context(
      final Parse context, final Store store, final Parent parent, final Object cause) {
    context(context, store, parent, ROMap.empty, cause);
  }

  public abstract void context(
          Parse context, Store store, Parent parent, ROMap<Object, RefParent> seen, Object cause);
}
