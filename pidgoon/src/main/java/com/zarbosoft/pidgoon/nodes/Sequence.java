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

public class Sequence extends Node {
  List<Node> children = new ArrayList<>();

  public Sequence add(final Node child) {
    children.add(child);
    return this;
  }

  public Sequence addAll(final List<Node> children) {
    children.addAll(children);
    return this;
  }

  public Sequence visit(final Consumer<Sequence> visitor) {
    visitor.accept(this);
    return this;
  }

  @Override
  public void context(
      final Parse context,
      final Store store,
      final Parent parent,
      final ROMap<Object, RefParent> seen,
      final Object cause) {
    if (children.isEmpty()) {
      parent.advance(context, store, cause);
    } else {
      children.get(0).context(context, store.push(), new SeqParent(this, parent, 0), seen, cause);
    }
  }

  public boolean isEmpty() {
    return children.isEmpty();
  }

  private static class SeqParent extends BaseParent {
    final int step;
    private final Sequence self;

    public SeqParent(Sequence self, final Parent parent, final int step) {
      super(parent);
      this.step = step;
      this.self = self;
    }

    @Override
    public void advance(final Parse step, final Store store, final Object cause) {
      final Store tempStore = store.pop();
      final int nextStep = this.step + 1;
      if (nextStep >= self.children.size()) {
        parent.advance(step, tempStore, cause);
      } else {
        self.children
            .get(nextStep)
            .context(step, tempStore.push(), new SeqParent(self, parent, nextStep), cause);
      }
    }
  }
}
