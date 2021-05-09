package com.zarbosoft.merman.core.syntax.primitivepattern;

import com.zarbosoft.pidgoon.events.EscapableResult;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.nodes.MergeEscapableRepeat;
import com.zarbosoft.rendaw.common.ROList;

public class Maybe extends Pattern {
  public final Pattern pattern;

  public Maybe(Pattern pattern) {
    this.pattern = pattern;
  }

  @Override
  public Node<EscapableResult<ROList<String>>> build(boolean capture) {
    return new MergeEscapableRepeat<>(pattern.build(capture)).min(0).max(1);
  }
}
