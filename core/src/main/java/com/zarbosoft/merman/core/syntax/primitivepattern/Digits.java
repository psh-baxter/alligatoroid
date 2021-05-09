package com.zarbosoft.merman.core.syntax.primitivepattern;

import com.zarbosoft.pidgoon.events.EscapableResult;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.nodes.Discard;
import com.zarbosoft.rendaw.common.ROList;

public class Digits extends Pattern {
  @Override
  public Node<EscapableResult<ROList<String>>> build(boolean capture) {
    return new DigitTerminal(capture);
  }
}
