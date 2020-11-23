package com.zarbosoft.merman.syntax.primitivepattern;

import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.nodes.Repeat;

public class Repeat0 extends Pattern {

  public Pattern pattern;

  @Override
  public Node build() {
    return new Repeat(pattern.build());
  }
}
