package com.zarbosoft.merman.core.syntax.primitivepattern;

import com.zarbosoft.pidgoon.events.EscapableResult;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.nodes.Union;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.TSList;

public class PatternUnion extends Pattern {
  public final ROList<Pattern> children;

  public PatternUnion(TSList<Pattern> children) {
    this.children = children;
  }

  @Override
  public Node<EscapableResult<ROList<String>>> build(boolean capture) {
    final Union<EscapableResult<ROList<String>>> out = new Union<>();
    for (final Pattern child : children) out.add(child.build(capture));
    return out;
  }
}
