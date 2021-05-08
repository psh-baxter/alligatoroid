package com.zarbosoft.merman.core.syntax.primitivepattern;

import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.nodes.Union;
import com.zarbosoft.rendaw.common.ROList;

public class Letters extends Pattern {
  @Override
  public Node<ROList<String>> build(boolean capture) {
      return new Union()
        .add(new CharacterRangeTerminal(capture, "a", "z"))
        .add(new CharacterRangeTerminal(capture, "A", "Z"));
  }
}
