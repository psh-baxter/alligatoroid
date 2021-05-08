package com.zarbosoft.merman.core.syntax.primitivepattern;

import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.nodes.MergeRepeat;
import com.zarbosoft.pidgoon.nodes.MergeSequence;
import com.zarbosoft.rendaw.common.ROList;

public class Integer extends Pattern {
  @Override
  public Node<ROList<String>> build(boolean capture) {
      return new MergeSequence<String>()
          .add(new MergeRepeat<String>(new CharacterRangeTerminal(capture, "-", "-")).max(1))
          .add(new MergeRepeat<String>(new CharacterRangeTerminal(capture, "0", "9")).min(1));
  }
}
