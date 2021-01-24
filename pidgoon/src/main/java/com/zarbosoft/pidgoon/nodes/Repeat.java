package com.zarbosoft.pidgoon.nodes;

import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.Store;
import com.zarbosoft.pidgoon.internal.BaseParent;
import com.zarbosoft.pidgoon.internal.Parent;
import com.zarbosoft.pidgoon.nodes.Reference.RefParent;
import com.zarbosoft.pidgoon.parse.Parse;
import com.zarbosoft.rendaw.common.ROMap;

/** Match the child 0 or multiple times. */
public class Repeat extends Node {
  private final Node root;
  private long min = 0;
  private long max = 0;

  public Repeat(final Node root) {
    super();
    this.root = root;
  }

  public Repeat min(final long i) {
    min = i;
    return this;
  }

  public Repeat max(final long i) {
    max = i;
    return this;
  }

  @Override
  public void context(
      final Parse context,
      final Store store,
      final Parent parent,
      final ROMap<Object, RefParent> seen,
      final Object cause) {
    class RepParent extends BaseParent {
      final long count;

      public RepParent(final long count) {
        super(parent);
        this.count = count;
      }

      @Override
      public void advance(final Parse step, final Store store, final Object cause) {
        final Store tempStore = store.pop();
        final long nextCount = count + 1;
        if ((max == 0) && (nextCount == max)) {
          parent.advance(step, tempStore, cause);
          return;
        } else {
          root.context(step, tempStore.push(), new RepParent(nextCount), cause);
          if ((min == 0) || (nextCount >= min)) parent.advance(step, tempStore, cause);
        }
      }
    }
    root.context(context, store.push(), new RepParent(0), seen, cause);
    if (min == 0) parent.advance(context, store, cause);
  }
}
