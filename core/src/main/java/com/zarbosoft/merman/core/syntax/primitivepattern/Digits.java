package com.zarbosoft.merman.core.syntax.primitivepattern;

import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.nodes.Discard;
import com.zarbosoft.rendaw.common.ROList;

public class Digits extends Pattern {
  @Override
  public Node<ROList<String>> build(boolean capture) {
    if (capture) return new DigitTerminal();
    else return new Discard<>(new DigitTerminal());
  }
}
