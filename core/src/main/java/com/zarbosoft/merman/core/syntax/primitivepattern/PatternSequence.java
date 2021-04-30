package com.zarbosoft.merman.core.syntax.primitivepattern;

import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.rendaw.common.ROList;

public class PatternSequence extends Pattern {
  public final ROList<Pattern> children;

  public PatternSequence(ROList<Pattern> children) {
    this.children = children;
  }

  @Override
  public Node build(boolean capture) {
    final com.zarbosoft.pidgoon.nodes.Sequence out = new com.zarbosoft.pidgoon.nodes.Sequence();
    for (final Pattern child : children) out.add(child.build(capture));
    return out;
  }
}
