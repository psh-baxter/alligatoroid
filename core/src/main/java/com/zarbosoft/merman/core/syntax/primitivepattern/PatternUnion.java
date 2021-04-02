package com.zarbosoft.merman.core.syntax.primitivepattern;

import com.zarbosoft.merman.core.editor.I18nEngine;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.rendaw.common.TSList;

public class PatternUnion extends Pattern {
  public final TSList<Pattern> children;

  public PatternUnion(TSList<Pattern> children) {
    this.children = children;
  }

  @Override
  public Node build() {
    final com.zarbosoft.pidgoon.nodes.Union out = new com.zarbosoft.pidgoon.nodes.Union();
    for (final Pattern child : children) out.add(child.build());
    return out;
  }
}
