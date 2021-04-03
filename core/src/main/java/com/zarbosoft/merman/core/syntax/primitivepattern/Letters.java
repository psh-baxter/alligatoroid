package com.zarbosoft.merman.core.syntax.primitivepattern;

import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.nodes.Union;

public class Letters extends Pattern {
  @Override
  public Node build() {
    return new Union()
        .add(new CharacterRangeTerminal("a", "z"))
        .add(new CharacterRangeTerminal("A", "Z"));
  }
}
