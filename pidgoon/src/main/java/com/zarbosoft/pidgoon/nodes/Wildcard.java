package com.zarbosoft.pidgoon.nodes;

import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.internal.Parent;
import com.zarbosoft.pidgoon.Position;
import com.zarbosoft.pidgoon.State;
import com.zarbosoft.pidgoon.Store;
import com.zarbosoft.pidgoon.nodes.Reference.RefParent;
import com.zarbosoft.pidgoon.parse.Parse;
import org.pcollections.PMap;

/** Matches any event/byte. */
public class Wildcard extends Node {
  @Override
  public void context(
      final Parse context,
      final Store store,
      final Parent parent,
      final PMap<Object, RefParent> seen,
      final Object cause) {
    context.leaves.add(
        new State() {
          @Override
          public <T> T color() {
            return (T) store.color;
          }

          @Override
          public void parse(final Parse step, final Position position) {
            parent.advance(step, store.record(position), this);
          }
        });
  }
}
