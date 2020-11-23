package com.zarbosoft.merman.syntax.primitivepattern;

import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.nodes.Wildcard;

public class Any extends Pattern {
  @Override
  public Node build() {
    return new Wildcard();
  }
}
