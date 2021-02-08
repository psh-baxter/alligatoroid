package com.zarbosoft.pidgoon.nodes;

import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.Store;
import com.zarbosoft.pidgoon.internal.BaseParent;
import com.zarbosoft.pidgoon.internal.Parent;
import com.zarbosoft.pidgoon.parse.Parse;
import com.zarbosoft.rendaw.common.ROMap;

/**
 * Attaches a color to a parse subtree. If this branch fails to parse, the color will be used in
 * place of a tree-structure-based explanation.
 */
public class Color extends Node {
  public final Node child;
  private final Object color;

  public Color(final Object color, final Node child) {
    this.color = color;
    this.child = child;
  }

  @Override
  public void context(
      final Parse context,
      final Store store,
      final Parent parent,
      final ROMap<Object, Reference.RefParent> seen,
      final Object cause) {
    Object wasColor = store.color;
    store.color = color;
    child.context(context, store, new ColorParent(parent, wasColor), seen, cause);
  }

  private static class ColorParent extends BaseParent {
    private final Object wasColor;

    public ColorParent(Parent parent, Object wasColor) {
      super(parent);
      this.wasColor = wasColor;
    }

    @Override
    public void advance(final Parse step, final Store store, final Object cause) {
      store.color = wasColor;
      parent.advance(step, store, cause);
    }
  }
}
