package com.zarbosoft.merman.core.syntax.primitivepattern;

import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.nodes.HomogenousSequence;
import com.zarbosoft.pidgoon.nodes.MergeSequence;
import com.zarbosoft.rendaw.common.ROList;

public class PatternSequence extends Pattern {
  public final ROList<Pattern> children;

  public PatternSequence(ROList<Pattern> children) {
    this.children = children;
  }

  @Override
  public Node<ROList<String>> build(boolean capture) {
    final MergeSequence<String> out = new MergeSequence<String>();
    for (final Pattern child : children) out.add(child.build(capture));
    return out;
  }
}
