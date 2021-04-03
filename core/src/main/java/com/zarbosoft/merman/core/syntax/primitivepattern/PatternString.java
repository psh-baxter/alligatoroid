package com.zarbosoft.merman.core.syntax.primitivepattern;

import com.zarbosoft.merman.core.I18nEngine;
import com.zarbosoft.pidgoon.events.Event;
import com.zarbosoft.pidgoon.events.nodes.MatchingEventTerminal;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.nodes.Sequence;

import java.util.List;

public class PatternString extends Pattern {
  public final List<CharacterEvent> string;

  public PatternString(I18nEngine i18n, String string) {
    this.string = (List<CharacterEvent>) Pattern.splitGlyphs(i18n, string);
  }

  @Override
  public Node build() {
    Sequence out = new Sequence();
    for (Event g0 : string) {
      CharacterEvent g = (CharacterEvent) g0;
      out.add(new MatchingEventTerminal(g));
    }
    return out;
  }
}
