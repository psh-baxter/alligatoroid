package com.zarbosoft.pidgoon.nodes;

import com.zarbosoft.pidgoon.model.MismatchCause;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.model.Parent;
import com.zarbosoft.pidgoon.model.Parse;
import com.zarbosoft.pidgoon.model.Position;
import com.zarbosoft.pidgoon.model.RefParent;
import com.zarbosoft.pidgoon.model.Store;
import com.zarbosoft.rendaw.common.ROMap;

/** Matches any event/byte. */
public class Wildcard extends Node {
  @Override
  public void context(
      final Parse context,
      final Store store,
      final Parent parent,
      final ROMap<Object, RefParent> seen,
      final MismatchCause cause) {
    context.leaves.add(
        new Parse.State() {
          @Override
          public <T> T color() {
            return (T) store.color;
          }

          @Override
          public void parse(final Parse step, final Position position) {
            parent.advance(
                step, store.record(position), new MismatchCause(Wildcard.this, store.color));
          }
        });
  }

  @Override
  public String toString() {
    return "* (wildcard)";
  }
}
