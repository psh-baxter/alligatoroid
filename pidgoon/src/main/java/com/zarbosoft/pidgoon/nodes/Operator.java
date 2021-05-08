package com.zarbosoft.pidgoon.nodes;

import com.zarbosoft.pidgoon.errors.AbortParse;
import com.zarbosoft.pidgoon.model.ExceptionMismatchCause;
import com.zarbosoft.pidgoon.model.Grammar;
import com.zarbosoft.pidgoon.model.MismatchCause;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.model.Parent;
import com.zarbosoft.pidgoon.model.Step;
import com.zarbosoft.rendaw.common.ROMap;

public abstract class Operator<L, T> extends Node<T> {
  private final Node<L> root;

  public Operator(final Node<L> root) {
    super();
    if (root == null) throw new AssertionError();
    this.root = root;
  }

  @Override
  public void context(
          Grammar grammar, final Step step,
          final Parent<T> parent,
          Step.Branch branch, ROMap<Object, Reference.RefParent> seen,
          final MismatchCause cause,
          Object color) {
    root.context(grammar, step, new OperatorParent<L, T>(this, parent),branch , seen, cause, color);
  }

  /**
   * Override to do custom stack modifications in a parse
   *
   * @param store
   * @return
   */
  protected abstract T process(L value);

  private static class OperatorParent<L, T> implements Parent<L> {
    public final Parent<T> parent;
    private final Operator<L, T> operator;

    public OperatorParent(Operator<L, T> operator, Parent<T> parent) {
      super();
      this.parent = parent;
      this.operator = operator;
    }

    @Override
    public void advance(
            Grammar grammar, final Step step, Step.Branch branch, L result, final MismatchCause cause) {
      T out;
      try {
        out = operator.process(result);
      } catch (final AbortParse a) {
        parent.error(grammar, step, branch, new ExceptionMismatchCause(operator, a));
        return;
      }
      parent.advance(grammar, step, branch, out, cause);
    }

    @Override
    public void error(Grammar grammar, final Step step, Step.Branch branch, final MismatchCause cause) {
      parent.error(grammar, step, branch, cause);
    }
  }
}
