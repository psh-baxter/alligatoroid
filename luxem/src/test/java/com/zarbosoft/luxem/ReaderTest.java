package com.zarbosoft.luxem;

import com.zarbosoft.luxem.events.LPrimitiveEvent;
import com.zarbosoft.luxem.read.InvalidStream;
import com.zarbosoft.luxem.read.Reader;
import com.zarbosoft.pidgoon.events.Event;
import com.zarbosoft.pidgoon.events.MatchingEvent;
import com.zarbosoft.rendaw.common.ROPair;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReaderTest {
  public List<Event> read(final String source) {
    List<Event> out = new ArrayList<>();
    for (ROPair<Event, Object> p :
        Luxem.streamEvents(
            new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8)),
            new Reader.DefaultEventFactory())) {
      out.add(p.first);
    }
    return out;
  }

  public void check(final String source, final Event... events) {
    final List<Event> got = read(source);
    final List<Event> expected = Arrays.asList(events);
    if (got.size() != expected.size())
      throw new AssertionError(
          String.format(
              "Size mismatch:\nGot %s: %s\nExpected %s: %s",
              got.size(), got, expected.size(), expected));
    for (int i = 0; i < got.size(); ++i) {
      Event first = got.get(i);
      Event second = expected.get(i);
      if (!((MatchingEvent) second).matches((MatchingEvent) first)) {
        throw new AssertionError(
            String.format("Stream mismatch at %s:\nGot: %s\nExpected: %s", i, first, expected));
      }
    }
  }

  @Test
  public void testEmpty() {
    check("");
  }

  @Test
  public void testRootSingle() {
    check("a", new LPrimitiveEvent("a"));
  }

  @Test
  public void testRootSingleComma() {
    check("a,", new LPrimitiveEvent("a"));
  }

  @Test
  public void testRootArray() {
    check("a,b", new LPrimitiveEvent("a"), new LPrimitiveEvent("b"));
  }

  @Test(expected = InvalidStream.class)
  public void testMultipleKeys() {
    read("{a: b: c}");
  }

  @Test(expected = InvalidStream.class)
  public void testKeyInRootArray() {
    System.out.format("%s\n", read("a: b"));
  }

  @Test(expected = InvalidStream.class)
  public void testKeyInArray() {
    System.out.format("%s\n", read("[a: b]"));
  }
}
