package com.zarbosoft.merman.syntax.primitivepattern;

import com.zarbosoft.merman.editor.I18nEngine;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.nodes.Wildcard;

public class Any extends Pattern {
  public static Pattern repeatedAny = new Repeat0(new Any());

  @Override
  public Node build(I18nEngine i18n) {
    return new Wildcard();
  }
}
