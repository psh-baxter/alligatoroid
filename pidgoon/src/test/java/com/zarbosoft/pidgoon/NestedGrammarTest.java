package com.zarbosoft.pidgoon;

import com.zarbosoft.pidgoon.errors.InvalidStream;
import com.zarbosoft.pidgoon.events.ParseBuilder;
import com.zarbosoft.pidgoon.events.ParseEventSink;
import com.zarbosoft.pidgoon.events.nodes.MatchingEventTerminal;
import com.zarbosoft.pidgoon.events.StackStore;
import com.zarbosoft.pidgoon.model.Grammar;
import com.zarbosoft.pidgoon.nodes.Operator;
import com.zarbosoft.pidgoon.nodes.Repeat;
import com.zarbosoft.pidgoon.nodes.Sequence;
import org.junit.Test;

import static com.zarbosoft.pidgoon.EventGrammarTest.EventA;
import static com.zarbosoft.pidgoon.EventGrammarTest.EventB;

public class NestedGrammarTest {
  @Test(expected = InvalidStream.class)
  public void testInnerFailure() {
    final Grammar inner = new Grammar();
    inner.add(
        "root",
        new Sequence()
            .add(new MatchingEventTerminal(new EventA()))
            .add(new MatchingEventTerminal(new EventB())));
    final Grammar outer = new Grammar();
    outer.add(
        "root",
        new Repeat(
                new Operator<StackStore>(new MatchingEventTerminal(new EventA())) {
                  @Override
                  protected StackStore process(StackStore s) {
                    ParseEventSink<Object> e = s.stackTop();
                    e = e.push(s.top(), "");
                    return s.popStack().pushStack(e);
                  }
                })
            .min(2)
            .max(2));
    ParseEventSink<Object> outerParse =
        new ParseBuilder<>()
            .grammar(outer)
            .store(
                new StackStore()
                    .pushStack(new ParseBuilder<>().grammar(inner).root("root").parse()))
            .root("root")
            .parse();
    outerParse = outerParse.push(new EventA(), "");
    outerParse = outerParse.push(new EventA(), "");
    outerParse.result();
  }
}
