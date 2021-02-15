package com.zarbosoft.pidgoon.nodes;

import com.zarbosoft.pidgoon.BaseParent;
import com.zarbosoft.pidgoon.model.MismatchCause;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.model.Parent;
import com.zarbosoft.pidgoon.model.Parse;
import com.zarbosoft.pidgoon.model.RefParent;
import com.zarbosoft.pidgoon.model.Store;
import com.zarbosoft.rendaw.common.Format;
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
      final MismatchCause cause) {
    root.context(context, store.push(), new OperatorParent(parent), seen, cause);
  }

  @Override
  public String toString() {
    final String out;
    if ((root instanceof Sequence) || (root instanceof Union) || (root instanceof Repeat)) {
      out = Format.format("~(%s)", root);
    } else {
      out = Format.format("~%s", root);
    }
    return out;
  }

  private static class OperatorParent extends BaseParent {
    public OperatorParent(Parent parent) {
      super(parent);
    }

    @Override
    public void error(final Parse step, final Store store, final MismatchCause cause) {
      parent.advance(step, store.pop(), cause);
    }

    @Override
    public void advance(final Parse step, Store store, final MismatchCause cause) {
      store = store.pop();
      super.error(step, store, cause);
    }
  }
}
