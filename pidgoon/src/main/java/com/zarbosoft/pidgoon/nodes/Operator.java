package com.zarbosoft.pidgoon.nodes;

import com.zarbosoft.pidgoon.model.ExceptionMismatchCause;
import com.zarbosoft.pidgoon.model.MismatchCause;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.model.RefParent;
import com.zarbosoft.pidgoon.model.Store;
import com.zarbosoft.pidgoon.errors.AbortParse;
import com.zarbosoft.pidgoon.BaseParent;
import com.zarbosoft.pidgoon.model.Parent;
import com.zarbosoft.pidgoon.model.Parse;
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
      final MismatchCause cause) {
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
    public void advance(final Parse step, final Store store, final MismatchCause cause) {
      Store tempStore = store;
      try {
        tempStore = operator.process((S) store);
      } catch (final AbortParse a) {
        parent.error(step, tempStore, new ExceptionMismatchCause(operator, store.color, a));
        return;
      }
      parent.advance(step, tempStore.pop(), cause);
    }
  }
}
