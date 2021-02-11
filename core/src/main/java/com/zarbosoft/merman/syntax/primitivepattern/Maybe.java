package com.zarbosoft.merman.syntax.primitivepattern;

import com.zarbosoft.merman.editor.I18nEngine;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.nodes.Repeat;

public class Maybe extends Pattern {
  public final Pattern pattern;

  public Maybe(Pattern pattern) {
    this.pattern = pattern;
  }

  @Override
  public Node build(I18nEngine i18n) {
    return new Repeat(pattern.build(i18n)).min(0).max(1);
  }
}
