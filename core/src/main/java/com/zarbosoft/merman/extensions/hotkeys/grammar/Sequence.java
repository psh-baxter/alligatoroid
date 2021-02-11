package com.zarbosoft.merman.extensions.hotkeys.grammar;

import java.util.List;

public class Sequence implements Node {

  public List<Node> nodes;

  @Override
  public com.zarbosoft.pidgoon.model.Node build() {
    final com.zarbosoft.pidgoon.nodes.Sequence out = new com.zarbosoft.pidgoon.nodes.Sequence();
    for (final Node node : nodes) out.add(node.build());
    return out;
  }
}
