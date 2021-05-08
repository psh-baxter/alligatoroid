package com.zarbosoft.pidgoon.nodes;

import com.zarbosoft.pidgoon.model.Grammar;
import com.zarbosoft.pidgoon.model.MismatchCause;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.model.Parent;
import com.zarbosoft.pidgoon.model.Step;
import com.zarbosoft.rendaw.common.ROMap;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Match exactly one child. */
public class Union<T> extends Node<T> {
  List<Node<T>> children = new ArrayList<>();

  public Union<T> add(final Node<T> child) {
    children.add(child);
    return this;
  }

  public Union<T> apply(Consumer<Union<T>> c) {
    c.accept(this);
    return this;
  }

  @Override
  public void context(
          Grammar grammar, final Step step,
          final Parent<T> parent,
          Step.Branch branch,
          final ROMap<Object, Reference.RefParent> seen,
          final MismatchCause cause,
          Object color) {
    for (Node<T> child : children) {
      child.context(grammar, step, new UnionParent<T>(parent), branch, seen, cause, color);
    }
  }

  private static class UnionParent<T> implements Parent<T> {
    public final Parent<T> parent;

    public UnionParent(Parent<T> parent) {
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
