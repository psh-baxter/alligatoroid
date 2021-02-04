package com.zarbosoft.pidgoon.nodes;

import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.Store;
import com.zarbosoft.pidgoon.internal.BaseParent;
import com.zarbosoft.pidgoon.internal.Parent;
import com.zarbosoft.pidgoon.nodes.Reference.RefParent;
import com.zarbosoft.pidgoon.parse.Parse;
import com.zarbosoft.rendaw.common.ROMap;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Match exactly one child. */
public class Union extends Node {
  List<Node> children = new ArrayList<>();

  public Union add(final Node child) {
    children.add(child);
    return this;
  }

  public <T> Union apply(Consumer<Union> c) {
    c.accept(this);
    return this;
  }

  @Override
  public void context(
      final Parse context,
      final Store store,
      final Parent parent,
      final ROMap<Object, RefParent> seen,
      final Object cause) {
    for (Node child : children) {
          child.context(
                  context,
                  store.push(),
                  new BaseParent(parent) {
                      @Override
                      public void advance(final Parse step, final Store store, final Object cause) {
                          parent.advance(step, store.pop(), cause);
                      }
                  },
                  seen,
                  cause);
      }
  }
}
