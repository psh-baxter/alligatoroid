package com.zarbosoft.pidgoon.nodes;

import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.internal.Parent;
import com.zarbosoft.pidgoon.Store;
import com.zarbosoft.pidgoon.parse.Parse;
import org.pcollections.PMap;

import java.util.ArrayList;
import java.util.List;

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
      final PMap<Object, RefParent> seen,
      final Object cause) {
    if (seen.containsKey(key)) {
      seen.get(key).loopParents.add(parent);
      return;
    }
    final RefParent subParent = new RefParent(parent);
    get(context).context(context, store.push(), subParent, seen.plus(key, subParent), cause);
  }

  private Node get(final Parse context) {
    if (base == null) {
      base = context.grammar.getNode(key);
    }
    return base;
  }

  @Override
  public String toString() {
    return key.toString();
  }

  public static class RefParent implements Parent {
    Parent originalParent;
    List<Parent> loopParents = new ArrayList<>();

    public RefParent(final Parent parent) {
      originalParent = parent;
    }

    @Override
    public void advance(final Parse step, final Store store, final Object cause) {
      final Store tempStore = store.pop();
      originalParent.advance(step, tempStore, cause);
      for (final Parent p : loopParents) {
        p.advance(step, tempStore.inject(p.size(this, 1)), cause);
      }
    }

    @Override
    public void error(final Parse step, final Store store, final Object cause) {
      originalParent.error(step, store, cause);
    }

    @Override
    public long size(final Parent stopAt, final long start) {
      if (stopAt == this) return start;
      return originalParent.size(stopAt, start + 1);
    }

    @Override
    public void cut(final Parse step, final String name) {
      originalParent.cut(step, name);
    }
  }
}
