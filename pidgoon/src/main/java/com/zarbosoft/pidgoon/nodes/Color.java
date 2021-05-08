package com.zarbosoft.pidgoon.nodes;

import com.zarbosoft.pidgoon.model.Grammar;
import com.zarbosoft.pidgoon.model.MismatchCause;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.model.Parent;
import com.zarbosoft.pidgoon.model.Step;
import com.zarbosoft.rendaw.common.ROMap;

/**
 * Attaches a color to a parse subtree. If this branch fails to parse, the color will be used in
 * place of a tree-structure-based explanation.
 */
public class Color<T> extends Node<T> {
  public final Node<T> child;
  private final Object color;

  public Color(final Object color, final Node<T> child) {
    this.color = color;
    this.child = child;
  }

  @Override
  public void context(
          Grammar grammar, final Step step,
          final Parent<T> parent,
          Step.Branch branch, ROMap<Object, Reference.RefParent> seen,
          final MismatchCause cause,
          Object color) {
    child.context(grammar, step, new ColorParent<T>(parent), branch, seen, cause, this.color);
  }

  private static class ColorParent<T> implements Parent<T> {
    public final Parent<T> parent;

    public ColorParent(Parent parent) {
      super();
      this.parent = parent;
    }

    @Override
    public void advance(
            Grammar grammar, final Step step, Step.Branch branch, T result, final MismatchCause cause) {
      parent.advance(grammar, step, branch, result, cause);
    }

    @Override
    public void error(Grammar grammar, final Step step, Step.Branch branch, final MismatchCause cause) {
      parent.error(grammar, step, branch, cause);
    }
  }
}
