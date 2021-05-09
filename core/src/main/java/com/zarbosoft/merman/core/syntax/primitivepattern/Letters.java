package com.zarbosoft.merman.core.syntax.primitivepattern;

import com.zarbosoft.pidgoon.events.EscapableResult;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.nodes.Union;
import com.zarbosoft.rendaw.common.ROList;

public class Letters extends Pattern {
  @Override
  public Node<EscapableResult<ROList<String>>> build(boolean capture) {
      return new Union<EscapableResult<ROList<String>>>()
        .add(new CharacterRangeTerminal(capture, "a", "z"))
        .add(new CharacterRangeTerminal(capture, "A", "Z"));
  }
}
