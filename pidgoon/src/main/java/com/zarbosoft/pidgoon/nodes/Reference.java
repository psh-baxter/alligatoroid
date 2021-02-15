package com.zarbosoft.pidgoon.nodes;

import com.zarbosoft.pidgoon.model.ExceptionMismatchCause;
import com.zarbosoft.pidgoon.model.MismatchCause;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.model.Parent;
import com.zarbosoft.pidgoon.model.Parse;
import com.zarbosoft.pidgoon.model.RefParent;
import com.zarbosoft.pidgoon.model.Store;
import com.zarbosoft.rendaw.common.ROMap;

/** Recurse via the grammar rule of this name. */
public class Reference extends Node {
  private final Object key;
  private Node base = null;

  public Reference(final Object key) {
    super();
    this.key = key;
  }

  @Override
  public void context(
      final Parse context,
      final Store store,
      final Parent parent,
      final ROMap<Object, RefParent> seen,
      final MismatchCause cause) {
    if (seen.has(key)) {
      seen.get(key).loopParents.add(parent);
      return;
    }
    final RefParent subParent = new RefParent(parent);
    if (base == null) {
      try {
        base = context.grammar.getNode(key);
      } catch (Exception e) {
        parent.error(context, store, new ExceptionMismatchCause(this, store.color, e));
        return;
      }
    }
    base.context(context, store.push(), subParent, seen.mut().put(key, subParent), cause);
  }

  @Override
  public String toString() {
    return "REFERENCE " +key.toString();
  }
}
