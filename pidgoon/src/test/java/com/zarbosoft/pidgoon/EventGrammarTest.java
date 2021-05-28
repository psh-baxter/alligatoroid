package com.zarbosoft.pidgoon;

import com.zarbosoft.pidgoon.errors.InvalidStream;
import com.zarbosoft.pidgoon.events.MatchingEvent;
import com.zarbosoft.pidgoon.events.ParseBuilder;
import com.zarbosoft.pidgoon.events.ParseEventSink;
import com.zarbosoft.pidgoon.events.nodes.MatchingEventTerminal;
import com.zarbosoft.pidgoon.model.Grammar;
import com.zarbosoft.pidgoon.nodes.HomogenousSequence;
import com.zarbosoft.pidgoon.nodes.Reference;
import org.junit.Test;

public class EventGrammarTest {
  @Test(expected = InvalidStream.class)
  public void testEventGrammarFailure() {
    final Grammar inner = new Grammar();
    Reference.Key<Object> key = new Reference.Key<>();
    inner.add(
        key,
        new HomogenousSequence()
            .add(new MatchingEventTerminal<>(new EventA()))
            .add(new MatchingEventTerminal<>(new EventB())));
    ParseEventSink<Object> parse = new ParseBuilder<>(key).grammar(inner).parallelParse();
    parse = parse.push(new EventB(), "");
    parse = parse.push(new EventB(), "");
    parse.result();
  }

  @Test(expected = InvalidStream.class)
  public void testEventGrammarEOFFailure() {
    final Grammar inner = new Grammar();
    Reference.Key<Object> key = new Reference.Key<>();
    inner.add(
        key,
        new HomogenousSequence()
            .add(new MatchingEventTerminal<>(new EventA()))
            .add(new MatchingEventTerminal<>(new EventB())));
    ParseEventSink<Object> parse = new ParseBuilder<>(key).grammar(inner).parallelParse();
    parse = parse.push(new EventA(), "");
    parse = parse.push(new EventA(), "");
    parse.result();
  }

  @Test
  public void testEventGrammarPass() {
    final Grammar inner = new Grammar();
    Reference.Key<Object> key = new Reference.Key<>();
    inner.add(
        key,
        new HomogenousSequence()
            .add(new MatchingEventTerminal<>(new EventA()))
            .add(new MatchingEventTerminal<>(new EventB())));
    ParseEventSink<Object> parse = new ParseBuilder<>(key).grammar(inner).parallelParse();
    parse = parse.push(new EventA(), "");
    //noinspection UnusedAssignment
    parse = parse.push(new EventB(), "");
  }

  public static class EventA implements MatchingEvent {}

  public static class EventB implements MatchingEvent {}
}
