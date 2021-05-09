package com.zarbosoft.merman.core.syntax.primitivepattern;

import com.zarbosoft.merman.core.Environment;
import com.zarbosoft.pidgoon.events.EscapableResult;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.nodes.MergeEscapableSequence;
import com.zarbosoft.rendaw.common.ROList;

public class PatternString extends Pattern {
  public final ROList<String> string;

  public PatternString(Environment env, String string) {
    this.string = env.splitGlyphs(string);
  }

  @Override
  public Node<EscapableResult<ROList<String>>> build(boolean capture) {
    MergeEscapableSequence<String> out = new MergeEscapableSequence<>();
    for (String glyph : string) {
      out.add(new CharacterTerminal(capture, glyph));
    }
    return out;
  }
}
