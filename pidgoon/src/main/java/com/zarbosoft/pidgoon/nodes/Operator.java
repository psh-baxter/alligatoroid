package com.zarbosoft.pidgoon.nodes;

import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.Store;
import com.zarbosoft.pidgoon.errors.AbortParse;
import com.zarbosoft.pidgoon.internal.BaseParent;
import com.zarbosoft.pidgoon.internal.Parent;
import com.zarbosoft.pidgoon.nodes.Reference.RefParent;
import com.zarbosoft.pidgoon.parse.Parse;
import com.zarbosoft.rendaw.common.ROMap;

public abstract class Operator<S extends Store> extends Node {
  private final Node root;

  public Operator() {
    super();
    root = null;
  }

  public Operator(final Node root) {
    super();
    if (root == null) throw new AssertionError();
    this.root = root;
  }

  @Override
  public void context(
      final Parse context,
      final Store store,
      final Parent parent,
      final ROMap<Object, RefParent> seen,
      final Object cause) {
    if (root == null) {
      parent.advance(context, process((S) store).pop(), cause);
    } else {
      root.context(context, store.push(), new OperatorParent(this, parent), seen, cause);
    }
  }

  /**
   * Override to do custom stack modifications in a parse
   *
   * @param store
   * @return
   */
  protected S process(S store) {
    return store;
  }

  private static class OperatorParent<S extends Store> extends BaseParent {
    private Operator<S> operator;

    public OperatorParent(Operator<S> operator, Parent parent) {
      super(parent);
      this.operator = operator;
    }

    @Override
    public void advance(final Parse step, final Store store, final Object cause) {
      Store tempStore = store;
      try {
        tempStore = operator.process((S) store);
      } catch (final AbortParse a) {
        parent.error(step, tempStore, a);
        return;
      }
      parent.advance(step, tempStore.pop(), cause);
    }
  }
}
