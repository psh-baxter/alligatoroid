package com.zarbosoft.merman.syntax.primitivepattern;

import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.nodes.Repeat;

public class Repeat1 extends Pattern {
  public final Pattern pattern;

  public Repeat1(Pattern pattern) {
    this.pattern = pattern;
  }

  @Override
  public Node build() {
    return new Repeat(pattern.build()).min(1);
  }
}
