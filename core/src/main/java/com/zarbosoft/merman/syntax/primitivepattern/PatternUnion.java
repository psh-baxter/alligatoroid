package com.zarbosoft.merman.syntax.primitivepattern;

import com.zarbosoft.merman.editor.I18nEngine;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.rendaw.common.TSList;

public class PatternUnion extends Pattern {
  public final TSList<Pattern> children;

  public PatternUnion(TSList<Pattern> children) {
    this.children = children;
  }

  @Override
  public Node build(I18nEngine i18n) {
    final com.zarbosoft.pidgoon.nodes.Union out = new com.zarbosoft.pidgoon.nodes.Union();
    for (final Pattern child : children) out.add(child.build(i18n));
    return out;
  }
}
