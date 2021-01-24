package com.zarbosoft.merman.syntax.primitivepattern;

import com.zarbosoft.merman.editor.I18nEngine;
import com.zarbosoft.pidgoon.Node;

import java.util.List;

public class PatternUnion extends Pattern {
  public final List<Pattern> children;

  public PatternUnion(List<Pattern> children) {
    this.children = children;
  }

  @Override
  public Node build(I18nEngine i18n) {
    final com.zarbosoft.pidgoon.nodes.Union out = new com.zarbosoft.pidgoon.nodes.Union();
    for (final Pattern child : children) out.add(child.build(i18n));
    return out;
  }
}
