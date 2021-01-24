package com.zarbosoft.pidgoon.nodes;

import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.internal.BaseParent;
import com.zarbosoft.pidgoon.internal.Parent;
import com.zarbosoft.pidgoon.Store;
import com.zarbosoft.pidgoon.nodes.Reference.RefParent;
import com.zarbosoft.pidgoon.parse.Parse;
import com.zarbosoft.rendaw.common.ROMap;

public class Not extends Node {
  private final Node root;

  public Not(final Node root) {
    this.root = root;
  }

  @Override
  public void context(
      final Parse context,
      final Store store,
      final Parent parent,
      final ROMap<Object, RefParent> seen,
      final Object cause) {
    root.context(
        context,
        store.push(),
        new BaseParent(parent) {
          @Override
          public void error(final Parse step, final Store store, final Object cause) {
            parent.advance(step, store.pop(), cause);
          }

          @Override
          public void advance(final Parse step, Store store, final Object cause) {
            store = store.pop();
            super.error(step, store, cause);
          }
        },
        seen,
        cause);
  }

  @Override
  public String toString() {
    final String out;
    if ((root instanceof Sequence) || (root instanceof Union) || (root instanceof Repeat)) {
      out = String.format("~(%s)", root);
    } else {
      out = String.format("~%s", root);
    }
    return out;
  }
}
