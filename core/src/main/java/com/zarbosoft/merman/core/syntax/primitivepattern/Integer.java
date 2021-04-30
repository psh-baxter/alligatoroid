package com.zarbosoft.merman.core.syntax.primitivepattern;

import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.nodes.Repeat;
import com.zarbosoft.pidgoon.nodes.Sequence;

public class Integer extends Pattern {
  @Override
  public Node build(boolean capture) {
    return new Sequence()
        .add(new Repeat(Pattern.characterRange(capture, "-", "-")).max(1))
        .add(new Repeat(Pattern.characterRange(capture, "0", "9")).min(1));
  }
}
