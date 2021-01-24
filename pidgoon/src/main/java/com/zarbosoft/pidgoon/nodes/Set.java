package com.zarbosoft.pidgoon.nodes;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.internal.BaseParent;
import com.zarbosoft.pidgoon.internal.Parent;
import com.zarbosoft.pidgoon.Store;
import com.zarbosoft.pidgoon.nodes.Reference.RefParent;
import com.zarbosoft.pidgoon.parse.Parse;
import com.zarbosoft.rendaw.common.Pair;
import org.pcollections.HashTreePMap;
import org.pcollections.PMap;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Consumer;

/** Match each child exactly once. */
public class Set extends Node {
  java.util.Set<Pair<Node, Boolean>> children = new HashSet<>();

  public Set add(final Node child) {
    children.add(new Pair<>(child, true));
    return this;
  }

  public Set add(final Node child, final boolean required) {
    children.add(new Pair<>(child, required));
    return this;
  }

  public Set addAll(final Collection<Node> children) {
    children.addAll(children);
    return this;
  }

  public Set visit(final Consumer<Set> visitor) {
    visitor.accept(this);
    return this;
  }

  @Override
  public void context(
      final Parse context,
      final Store store,
      final Parent parent,
      final PMap<Object, RefParent> seen,
      final Object cause) {
    advance(context, store, parent, seen, cause, children);
  }

  private void advance(
      final Parse step,
      final Store store,
      final Parent parent,
      final PMap<Object, RefParent> seen,
      final Object cause,
      final java.util.Set<Pair<Node, Boolean>> remaining) {
    if (remaining.stream().noneMatch(c -> c.second)) {
      parent.advance(step, store, cause);
    }
    remaining.forEach(
        c -> {
          c.first.context(
              step,
              store.push(),
              new SetParent(parent, Sets.difference(remaining, ImmutableSet.of(c))),
              seen,
              cause);
        });
  }

  private class SetParent extends BaseParent {
    java.util.Set<Pair<Node, Boolean>> remaining;
    Parent parent;

    public SetParent(final Parent parent, final java.util.Set<Pair<Node, Boolean>> remaining) {
      super(parent);
      this.parent = parent;
      this.remaining = remaining;
    }

    @Override
    public void advance(final Parse step, final Store store, final Object cause) {
      Set.this.advance(step, store.pop(), parent, HashTreePMap.empty(), cause, remaining);
    }
  }
}
