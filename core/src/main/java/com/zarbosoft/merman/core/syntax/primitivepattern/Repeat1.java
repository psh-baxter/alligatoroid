package com.zarbosoft.merman.core.syntax.primitivepattern;

import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.nodes.Repeat;

public class Repeat1 extends Pattern {
  public final Pattern pattern;

  public Repeat1(Pattern pattern) {
    this.pattern = pattern;
  }

  @Override
  public Node build(boolean capture) {
    return new Repeat(pattern.build(capture)).min(1);
  }
}
