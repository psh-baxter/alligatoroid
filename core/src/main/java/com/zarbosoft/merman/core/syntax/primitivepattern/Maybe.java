package com.zarbosoft.merman.core.syntax.primitivepattern;

import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.nodes.Repeat;

public class Maybe extends Pattern {
  public final Pattern pattern;

  public Maybe(Pattern pattern) {
    this.pattern = pattern;
  }

  @Override
  public Node build() {
    return new Repeat(pattern.build()).min(0).max(1);
  }
}
