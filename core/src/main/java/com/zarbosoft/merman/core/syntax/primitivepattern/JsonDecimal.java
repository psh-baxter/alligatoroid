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
    CharacterRangeTerminal digit = new CharacterRangeTerminal(capture, "0", "9");
    Union<EscapableResult<ROList<String>>> noLeadingZeroDigits =
        new Union<EscapableResult<ROList<String>>>()
            .add(new CharacterRangeTerminal(capture, "0", "0"))
            .add(
                new MergeEscapableSequence<String>()
                    .add(new CharacterRangeTerminal(capture, "1", "9"))
                    .add(new MergeEscapableRepeat<String>(digit)));
    return new MergeEscapableSequence<String>()
        .add(new MergeEscapableRepeat<String>(new CharacterRangeTerminal(capture, "-", "-")).max(1))
        .add(noLeadingZeroDigits)
        .add(
            new MergeEscapableRepeat<String>(
                    new MergeEscapableSequence<String>()
                        .add(new CharacterRangeTerminal(capture, ".", "."))
                        .add(new MergeEscapableRepeat<String>(digit)))
                .max(1))
        .add(
            new MergeEscapableRepeat<String>(
                    new MergeEscapableSequence<String>()
                        .add(
                            new Union<EscapableResult<ROList<String>>>()
                                .add(new CharacterRangeTerminal(capture, "e", "e"))
                                .add(new CharacterRangeTerminal(capture, "E", "E")))
                        .add(
                            new MergeEscapableRepeat<String>(
                                    new Union<EscapableResult<ROList<String>>>()
                                        .add(new CharacterRangeTerminal(capture, "-", "-"))
                                        .add(new CharacterRangeTerminal(capture, "+", "+")))
                                .max(1))
                        .add(noLeadingZeroDigits))
                .max(1));
  }
}
