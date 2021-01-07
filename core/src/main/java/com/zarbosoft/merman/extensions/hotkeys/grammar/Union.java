package com.zarbosoft.merman.extensions.hotkeys.grammar;

import java.util.List;

public class Union implements Node {

  public List<Node> nodes;

  @Override
  public com.zarbosoft.pidgoon.Node build() {
    final com.zarbosoft.pidgoon.nodes.Union out = new com.zarbosoft.pidgoon.nodes.Union();
    for (final Node node : nodes) out.add(node.build());
    return out;
  }
}
