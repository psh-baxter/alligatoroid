package com.zarbosoft.merman.syntax.primitivepattern;

import com.zarbosoft.pidgoon.Node;

import java.util.List;

public class PatternSequence extends Pattern {
  public List<Pattern> children;

  @Override
  public Node build() {
    final com.zarbosoft.pidgoon.nodes.Sequence out = new com.zarbosoft.pidgoon.nodes.Sequence();
    for (final Pattern child : children) out.add(child.build());
    return out;
  }
}
