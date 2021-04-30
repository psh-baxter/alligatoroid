package com.zarbosoft.merman.core.syntax.primitivepattern;

import com.zarbosoft.merman.core.Environment;
import com.zarbosoft.pidgoon.events.Event;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.nodes.Sequence;
import com.zarbosoft.rendaw.common.TSList;

public class PatternString extends Pattern {
  public final TSList<String> string;

  public PatternString(Environment env, String string) {
    this.string = new TSList<>();
    for (Event e : env.splitGlyphs(string)) {
      this.string.add(((CharacterEvent) e).value);
    }
  }

  @Override
  public Node build(boolean capture) {
    Sequence out = new Sequence();
    for (String glyph : string) {
      out.add(Pattern.character(capture, glyph));
    }
    return out;
  }
}
