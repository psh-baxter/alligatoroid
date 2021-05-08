package com.zarbosoft.merman.core.syntax.primitivepattern;

import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.nodes.Repeat;
import com.zarbosoft.rendaw.common.ROList;

public class Maybe extends Pattern {
  public final Pattern pattern;

  public Maybe(Pattern pattern) {
    this.pattern = pattern;
  }

  @Override
  public Node<ROList<String>> build(boolean capture) {
    return new Repeat(pattern.build(capture)).min(0).max(1);
  }
}
