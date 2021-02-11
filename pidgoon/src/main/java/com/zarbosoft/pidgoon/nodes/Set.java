package com.zarbosoft.pidgoon.nodes;

import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.model.RefParent;
import com.zarbosoft.pidgoon.model.Store;
import com.zarbosoft.pidgoon.BaseParent;
import com.zarbosoft.pidgoon.model.Parent;
import com.zarbosoft.pidgoon.model.Parse;
import com.zarbosoft.rendaw.common.ROMap;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.ROSet;
import com.zarbosoft.rendaw.common.ROSetRef;
import com.zarbosoft.rendaw.common.TSSet;

import java.util.Collection;

/** Match each child exactly once. */
public class Set extends Node {
  TSSet<ROPair<Node, Boolean>> children = new TSSet<>();

  public Set add(final Node child) {
    children.add(new ROPair<>(child, true));
    return this;
  }

  public Set add(final Node child, final boolean required) {
    children.add(new ROPair<>(child, required));
    return this;
  }

  public Set addAll(final Collection<Node> children) {
    children.addAll(children);
    return this;
  }

  @Override
  public void context(
      final Parse context,
      final Store store,
      final Parent parent,
      final ROMap<Object, RefParent> seen,
      final Object cause) {
    advance(context, store, parent, seen, cause, children);
  }

  private void advance(
      final Parse step,
      final Store store,
      final Parent parent,
      final ROMap<Object, RefParent> seen,
      final Object cause,
      final ROSetRef<ROPair<Node, Boolean>> remaining) {
    boolean matched = false;
    for (ROPair<Node, Boolean> p : remaining) {
      if (p.second) {
        matched = true;
        break;
      }
    }
    if (!matched) {
      parent.advance(step, store, cause);
    }
    remaining.forEach(
        c -> {
          c.first.context(
              step,
              store.push(),
              new SetParent(parent, remaining.mut().remove(c).ro()),
              seen,
              cause);
        });
  }

  private class SetParent extends BaseParent {
    ROSet<ROPair<Node, Boolean>> remaining;
    Parent parent;

    public SetParent(final Parent parent, final ROSet<ROPair<Node, Boolean>> remaining) {
      super(parent);
      this.parent = parent;
      this.remaining = remaining;
    }

    @Override
    public void advance(final Parse step, final Store store, final Object cause) {
      Set.this.advance(step, store.pop(), parent, ROMap.empty, cause, remaining);
    }
  }
}
