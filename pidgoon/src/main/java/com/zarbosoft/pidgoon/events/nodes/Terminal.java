package com.zarbosoft.pidgoon.events.nodes;

import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.State;
import com.zarbosoft.pidgoon.Store;
import com.zarbosoft.pidgoon.events.Event;
import com.zarbosoft.pidgoon.events.Position;
import com.zarbosoft.pidgoon.internal.Parent;
import com.zarbosoft.pidgoon.nodes.Reference.RefParent;
import com.zarbosoft.pidgoon.parse.Parse;
import com.zarbosoft.rendaw.common.ROMap;

/** Base node to match a single event. Define `matches` to use. */
public abstract class Terminal extends Node {
  public Terminal() {}

  @Override
  public void context(
      final Parse context,
      final Store prestore,
      final Parent parent,
      final ROMap<Object, RefParent> seen,
      final Object cause) {
    context.leaves.add(
        new State() {
          @Override
          public <T> T color() {
            return (T) prestore.color;
          }

          @Override
          public void parse(final Parse step, final com.zarbosoft.pidgoon.Position sourcePosition) {
            Store store = prestore;
            final Position position = (Position) sourcePosition;
            store = store.record(position);
            if (matches(position.get(), store)) {
              parent.advance(step, store, this);
            } else {
              parent.error(step, store, this);
            }
          }
        });
  }

  /**
   * Defines conditions and checks if the current terminal matches those conditions.
   *
   * @param event current terminal
   * @param store Current store - creation in progress so may be modified
   * @return true if conditions match
   */
  protected abstract boolean matches(final Event event, Store store);
}
