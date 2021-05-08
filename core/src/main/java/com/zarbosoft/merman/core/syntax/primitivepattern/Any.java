package com.zarbosoft.merman.core.syntax.primitivepattern;

import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.nodes.Discard;
import com.zarbosoft.rendaw.common.ROList;

public class Any extends Pattern {
  public static Pattern repeatedAny = new Repeat0(new Any());

  @Override
  public Node<ROList<String>> build(boolean capture) {
    if (capture) return new WildcardTerminal();
    else return new Discard<>(new WildcardTerminal());
  }
}
