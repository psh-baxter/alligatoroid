package com.zarbosoft.pidgoon.nodes;

import com.zarbosoft.pidgoon.model.Grammar;
import com.zarbosoft.pidgoon.model.MismatchCause;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.model.Parent;
import com.zarbosoft.pidgoon.model.Step;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROMap;
import com.zarbosoft.rendaw.common.ROSet;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;

import java.util.Map;

/** Match each child exactly once. */
public abstract class BaseSet<K, T> extends Node<ROList<T>> {
  TSMap<Node<K>, Boolean> children = new TSMap<>();

  public BaseSet<K, T> add(final Node<K> child) {
    children.put(child, false);
    return this;
  }

  public BaseSet<K, T> add(final Node<K> child, final boolean required) {
    children.put(child, required);
    return this;
  }
  public abstract TSList<T> combine(TSList<T> out, K value);

  @Override
  public void context(
      Grammar grammar,
      final Step step,
      final Parent<ROList<T>> parent,
      Step.Branch branch,
      final ROMap<Object, Reference.RefParent> seen,
      final MismatchCause cause,
      Object color) {
    advance(grammar, step, branch, parent, seen, cause, children, color, ROList.empty);
  }

  private void advance(
      Grammar grammar,
      final Step step,
      Step.Branch branch,
      final Parent<ROList<T>> parent,
      final ROMap<Object, Reference.RefParent> seen,
      final MismatchCause cause,
      final ROMap<Node<K>, Boolean> remaining,
      Object color,
      ROList<T> collected) {
    boolean requiredRemain = false;
    for (Map.Entry<Node<K>, Boolean> e : remaining) {
      if (e.getValue()) {
        requiredRemain = true;
        break;
      }
    }
    if (!requiredRemain) {
      parent.advance(grammar, step, branch, collected, cause);
    }
    for (Map.Entry<Node<K>, Boolean> c : remaining) {
      c.getKey()
          .context(
              grammar,
              step,
              new SetParent<K, T>(this, parent, remaining.mut().remove(c.getKey()), collected, color),
              branch,
              seen,
              cause,
              color);
    }
  }

  private static class SetParent<K, T> implements Parent<K> {
    public final Parent<ROList<T>> parent;
    final ROList<T> collected;
    final ROMap<Node<K>, Boolean> remaining;
    private final BaseSet<K, T> base;
    private final Object color;

    public SetParent(
        BaseSet<K, T> base,
        final Parent<ROList<T>> parent,
        final ROMap<Node<K>, Boolean> remaining,
        ROList<T> collected,
        Object color) {
      super();
      this.base = base;
      this.parent = parent;
      this.remaining = remaining;
      this.collected = collected;
      this.color = color;
    }

    @Override
    public void advance(
        Grammar grammar, final Step step, Step.Branch branch, K result, final MismatchCause cause) {
      base.advance(
          grammar,
          step,
          branch,
          parent,
          ROMap.empty,
          cause,
          remaining,
          color,
          base.combine(collected.mut(), result));
    }

    @Override
    public void error(
        Grammar grammar, final Step step, Step.Branch branch, final MismatchCause cause) {
      parent.error(grammar, step, branch, cause);
    }
  }
}
