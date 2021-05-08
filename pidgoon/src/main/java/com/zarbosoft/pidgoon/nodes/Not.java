package com.zarbosoft.pidgoon.nodes;

import com.zarbosoft.pidgoon.model.Grammar;
import com.zarbosoft.pidgoon.model.MismatchCause;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.model.Parent;
import com.zarbosoft.pidgoon.model.Step;
import com.zarbosoft.rendaw.common.ROMap;

/** Returns null if the child node raises an error. Raises an error if the child node matches. */
public class Not extends Node<Void> {
  private final Node child;

  public Not(final Node child) {
    this.child = child;
  }

  @Override
  public void context(
          Grammar grammar, final Step step,
          final Parent<Void> parent,
          Step.Branch branch,
          ROMap<Object, Reference.RefParent> seen,
          final MismatchCause cause,
          Object color) {
    child.context(grammar, step, new OperatorParent(parent), branch, ROMap.empty, cause, color);
  }

  private static class OperatorParent implements Parent {
    public final Parent parent;

    public OperatorParent(Parent parent) {
      super();
      this.parent = parent;
    }

    @Override
    public void error(Grammar grammar, final Step step, Step.Branch branch, final MismatchCause cause) {
      parent.advance(grammar, step, branch, null, cause);
    }

    @Override
    public void advance(
            Grammar grammar, final Step step, Step.Branch branch, Object result, final MismatchCause cause) {
      error(grammar, step, branch, cause);
    }
  }
}
