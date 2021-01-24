package com.zarbosoft.merman.syntax.primitivepattern;

import com.zarbosoft.merman.editor.I18nEngine;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.events.Event;
import com.zarbosoft.pidgoon.events.nodes.MatchingEventTerminal;
import com.zarbosoft.pidgoon.nodes.Sequence;

public class PatternString extends Pattern {
  public final String string;

  public PatternString(String string) {
    this.string = string;
  }

  @Override
  public Node build(I18nEngine i18n) {
    Sequence out = new Sequence();
    for (Event g0 : Pattern.splitGlyphs(i18n, string)) {
      CharacterEvent g = (CharacterEvent) g0;
      out.add(new MatchingEventTerminal(g));
    }
    return out;
  }
}
