package com.zarbosoft.merman.core.syntax.primitivepattern;

import com.zarbosoft.pidgoon.events.EscapableResult;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.nodes.MergeEscapableRepeat;
import com.zarbosoft.pidgoon.nodes.MergeEscapableSequence;
import com.zarbosoft.rendaw.common.ROList;

public class Integer extends Pattern {
  @Override
  public Node<EscapableResult<ROList<String>>> build(boolean capture) {
      return new MergeEscapableSequence<String>()
          .add(new MergeEscapableRepeat<String>(new CharacterRangeTerminal(capture, "-", "-")).max(1))
          .add(new MergeEscapableRepeat<String>(new CharacterRangeTerminal(capture, "0", "9")).min(1));
  }
}
