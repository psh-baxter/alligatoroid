package com.zarbosoft.merman.core.syntax.primitivepattern;

import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.nodes.MergeRepeat;
import com.zarbosoft.pidgoon.nodes.MergeSequence;
import com.zarbosoft.pidgoon.nodes.Union;
import com.zarbosoft.rendaw.common.ROList;

public class JsonDecimal extends Pattern {
  @Override
  public Node<ROList<String>> build(boolean capture) {
    Node<ROList<String>> digits = new CharacterRangeTerminal(capture, "0", "9");
    return new MergeSequence<String>()
        .add(new MergeRepeat<String>(new CharacterRangeTerminal(capture, "-", "-")).max(1))
        .add(
            new Union<ROList<String>>()
                .add(new CharacterRangeTerminal(capture, "0", "0"))
                .add(new CharacterRangeTerminal(capture, "1", "9"))
                .add(new MergeRepeat<String>(digits)))
        .add(
            new MergeRepeat<String>(
                    new MergeSequence<String>()
                        .add(new CharacterRangeTerminal(capture, ".", "-"))
                        .add(new MergeRepeat<String>(digits)))
                .max(1))
        .add(
            new MergeRepeat<String>(
                    new MergeSequence<String>()
                        .add(
                            new MergeRepeat<String>(new CharacterRangeTerminal(capture, "e", "E"))
                                .max(1))
                        .add(
                            new MergeRepeat<String>(new CharacterRangeTerminal(capture, "+", "-"))
                                .max(1))
                        .add(new MergeRepeat<String>(digits).min(1)))
                .max(1));
  }
}
