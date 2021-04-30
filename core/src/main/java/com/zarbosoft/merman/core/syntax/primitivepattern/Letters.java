package com.zarbosoft.merman.core.syntax.primitivepattern;

import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.nodes.Union;

public class Letters extends Pattern {
  @Override
  public Node build(boolean capture) {
    return new Union()
        .add(Pattern.characterRange(capture, "a", "z"))
        .add(Pattern.characterRange(capture, "A", "Z"));
  }
}
