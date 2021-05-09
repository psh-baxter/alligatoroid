package com.zarbosoft.merman.core.syntax.primitivepattern;

import com.zarbosoft.pidgoon.events.EscapableResult;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.nodes.MergeEscapableRepeat;
import com.zarbosoft.pidgoon.nodes.MergeEscapableSequence;
import com.zarbosoft.pidgoon.nodes.Union;
import com.zarbosoft.rendaw.common.ROList;

public class JsonDecimal extends Pattern {
  @Override
  public Node<EscapableResult<ROList<String>>> build(boolean capture) {
    CharacterRangeTerminal digits = new CharacterRangeTerminal(capture, "0", "9");
    return new MergeEscapableSequence<String>()
        .add(new MergeEscapableRepeat<String>(new CharacterRangeTerminal(capture, "-", "-")).max(1))
        .add(
            new Union<EscapableResult<ROList<String>>>()
                .add(new CharacterRangeTerminal(capture, "0", "0"))
                .add(new CharacterRangeTerminal(capture, "1", "9"))
                .add(new MergeEscapableRepeat<String>(digits)))
        .add(
            new MergeEscapableRepeat<String>(
                    new MergeEscapableSequence<String>()
                        .add(new CharacterRangeTerminal(capture, ".", "-"))
                        .add(new MergeEscapableRepeat<String>(digits)))
                .max(1))
        .add(
            new MergeEscapableRepeat<String>(
                    new MergeEscapableSequence<String>()
                        .add(
                            new MergeEscapableRepeat<String>(new CharacterRangeTerminal(capture, "e", "E"))
                                .max(1))
                        .add(
                            new MergeEscapableRepeat<String>(new CharacterRangeTerminal(capture, "+", "-"))
                                .max(1))
                        .add(new MergeEscapableRepeat<String>(digits).min(1)))
                .max(1));
  }
}
