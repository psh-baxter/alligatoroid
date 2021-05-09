package com.zarbosoft.merman.core.syntax.primitivepattern;

import com.zarbosoft.pidgoon.events.EscapableResult;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.nodes.HomogenousSequence;
import com.zarbosoft.pidgoon.nodes.MergeEscapableSequence;
import com.zarbosoft.rendaw.common.ROList;

public class PatternSequence extends Pattern {
  public final ROList<Pattern> children;

  public PatternSequence(ROList<Pattern> children) {
    this.children = children;
  }

  @Override
  public Node<EscapableResult<ROList<String>>> build(boolean capture) {
    final MergeEscapableSequence<String> out = new MergeEscapableSequence<String>();
    for (final Pattern child : children) out.add(child.build(capture));
    return out;
  }
}
