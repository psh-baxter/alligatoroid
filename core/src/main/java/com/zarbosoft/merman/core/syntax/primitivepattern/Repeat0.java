package com.zarbosoft.merman.core.syntax.primitivepattern;

import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.nodes.MergeRepeat;
import com.zarbosoft.rendaw.common.ROList;

public class Repeat0 extends Pattern {
  public final Pattern pattern;

  public Repeat0(Pattern pattern) {
    this.pattern = pattern;
  }

  @Override
  public Node<ROList<String>> build(boolean capture) {
    return new MergeRepeat<String>(pattern.build(capture));
  }
}
