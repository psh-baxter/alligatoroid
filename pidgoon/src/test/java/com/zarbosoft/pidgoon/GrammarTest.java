package com.zarbosoft.pidgoon;

import com.zarbosoft.pidgoon.bytes.ParseBuilder;
import com.zarbosoft.pidgoon.bytes.nodes.Terminal;
import com.zarbosoft.pidgoon.bytes.stores.StackClipStore;
import com.zarbosoft.pidgoon.errors.InvalidStream;
import com.zarbosoft.pidgoon.nodes.Not;
import com.zarbosoft.pidgoon.nodes.Operator;
import com.zarbosoft.pidgoon.nodes.Reference;
import com.zarbosoft.pidgoon.nodes.Sequence;
import com.zarbosoft.pidgoon.nodes.Set;
import com.zarbosoft.pidgoon.nodes.Union;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GrammarTest {
  @Test(expected = InvalidStream.class)
  public void testEOFFail() {
    final Grammar grammar = new Grammar();
    grammar.add("root", Terminal.fromChar('a'));
    new ParseBuilder<>().grammar(grammar).root("root").parse("b");
  }

  @Test
  public void testUnion() {
    final Grammar grammar = new Grammar();
    grammar.add(
        "root",
        new Union()
            .add(ParseBuilder.stringSeq("zarolous"))
            .add(
                new Union()
                    .add(
                        ParseBuilder.stringSeq("zarolously")
                            .add(ParseBuilder.stringSeq("zindictive")))));
    new ParseBuilder<>().grammar(grammar).root("root").parse("zarolous");
  }

  @Test
  public void testLeftRecurseStack() {
    final Grammar grammar = new Grammar();
    grammar.add(
        "one",
        new Union()
            .add(Terminal.fromChar('a'))
            .add(new Sequence().add(new Reference("one")).add(Terminal.fromChar('z'))));
    grammar.add(
        "two",
        new Operator<StackClipStore>(new Reference("one")) {
          @Override
          protected StackClipStore process(StackClipStore s) {
            return s.pushStack(s.topData().toString());
          }
        });
    final Object result = new ParseBuilder<>().grammar(grammar).root("two").parse("azz");
    assertEquals("azz", result);
  }

  @Test
  public void testNot() {
    final Grammar grammar = new Grammar();
    grammar.add(
        "one",
        new Operator<StackClipStore>(
            new Sequence().add(new Not(Terminal.fromChar('a'))).add(Terminal.fromChar('z'))) {
          @Override
          protected StackClipStore process(StackClipStore s) {
            return s.pushStack(s.topData().toString());
          }
        });
    final Object result = new ParseBuilder<>().grammar(grammar).root("one").parse("qz");
    assertEquals("qz", result);
  }

  @Test
  public void testNot2() {
    final Grammar grammar = new Grammar();
    grammar.add(
        "one", new Not(new Union().add(Terminal.fromChar('a')).add(ParseBuilder.stringSeq("zoq"))));
    new ParseBuilder<>().grammar(grammar).root("one").parse("zot");
  }

  @Test(expected = InvalidStream.class)
  public void testFailNot() {
    final Grammar grammar = new Grammar();
    grammar.add(
        "one",
        new Operator<StackClipStore>(
            new Sequence().add(new Not(Terminal.fromChar('a'))).add(Terminal.fromChar('z'))) {
          @Override
          protected StackClipStore process(StackClipStore s) {
            return s.pushStack(s.topData().toString());
          }
        });
    new ParseBuilder<>().grammar(grammar).root("one").parse("az");
  }

  @Test
  public void testSeqStorage() {
    final Grammar grammar = new Grammar();
    grammar.add(
        "one",
        new Operator<StackClipStore>(
            new Sequence()
                .add(new Union().add(Terminal.fromChar('z')).add(Terminal.fromChar('z')))
                .add(Terminal.fromChar('a'))) {
          @Override
          protected StackClipStore process(StackClipStore s) {
            return s.pushStack(s.topData().toString());
          }
        });
    final Object result = new ParseBuilder<>().grammar(grammar).root("one").parse("za");
    assertEquals("za", result);
  }

  @Test
  public void testSet() {
    final Grammar grammar = new Grammar();
    grammar.add(
        "one",
        new Operator<StackClipStore>(
            new Sequence()
                .add(Terminal.fromChar('z'))
                .add(new Set().add(Terminal.fromChar('a')))
                .add(Terminal.fromChar('z'))) {
          @Override
          protected StackClipStore process(StackClipStore s) {
            return s.pushStack(s.topData().toString());
          }
        });
    final Object result = new ParseBuilder<>().grammar(grammar).root("one").parse("zaz");
    assertEquals("zaz", result);
  }

  @Test
  public void testEmptySet() {
    final Grammar grammar = new Grammar();
    grammar.add(
        "one",
        new Operator<StackClipStore>(
            new Sequence().add(Terminal.fromChar('z')).add(new Set()).add(Terminal.fromChar('z'))) {
          @Override
          protected StackClipStore process(StackClipStore s) {
            return s.pushStack(s.topData().toString());
          }
        });
    final Object result = new ParseBuilder<>().grammar(grammar).root("one").parse("zz");
    assertEquals("zz", result);
  }
}
