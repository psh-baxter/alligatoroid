package com.zarbosoft.pidgoon.nodes;

import com.zarbosoft.pidgoon.model.MismatchCause;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.model.RefParent;
import com.zarbosoft.pidgoon.model.Store;
import com.zarbosoft.pidgoon.BaseParent;
import com.zarbosoft.pidgoon.model.Parent;
import com.zarbosoft.pidgoon.model.Parse;
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
      final MismatchCause cause) {
    root.context(context, store.push(), new RepParent(this, parent, 0), seen, cause);
    if (min == 0) parent.advance(context, store, cause);
  }

  private static class RepParent extends BaseParent {
    final long count;
    private final Repeat self;

    public RepParent(Repeat self, Parent parent, final long count) {
      super(parent);
      this.count = count;
      this.self = self;
    }

    @Override
    public void advance(final Parse step, final Store store, final MismatchCause cause) {
      final Store tempStore = store.pop();
      final long nextCount = count + 1;
      if ((self.max == 0) && (nextCount == self.max)) {
        parent.advance(step, tempStore, cause);
        return;
      } else {
        self.root.context(step, tempStore.push(), new RepParent(self, parent, nextCount), cause);
        if ((self.min == 0) || (nextCount >= self.min)) parent.advance(step, tempStore, cause);
      }
    }
  }
}
