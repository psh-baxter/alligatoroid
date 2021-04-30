package com.zarbosoft.merman.core.syntax.primitivepattern;

import com.zarbosoft.pidgoon.events.StackStore;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.nodes.Sequence;
import com.zarbosoft.pidgoon.nodes.Wildcard;

public class Any extends Pattern {
  public static Pattern repeatedAny = new Repeat0(new Any());

  @Override
  public Node build(boolean capture) {
    return new Sequence().add(new Wildcard()).add(StackStore.pushVarStackSingle);
  }
}
