package com.zarbosoft.luxem;

import com.zarbosoft.luxem.read.BufferedReader;
import com.zarbosoft.luxem.read.Reader;
import com.zarbosoft.luxem.read.TreeReader;
import com.zarbosoft.luxem.read.path.LuxemArrayPath;
import com.zarbosoft.luxem.read.path.LuxemObjectPath;
import com.zarbosoft.luxem.read.path.LuxemPath;
import com.zarbosoft.pidgoon.events.Event;
import com.zarbosoft.rendaw.common.Pair;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.stream.Stream;

import static com.zarbosoft.rendaw.common.Common.drain;

/** Methods for common use cases. */
public class Luxem {
  /**
   * Read a luxem document as a tree of Lists, String Maps, Strings, and Typeds.
   *
   * @param data luxem
   * @return list of top level objects
   */
  public static List parse(final String data) {
    return new TreeReader().read(new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)));
  }

  /**
   * Read a luxem document as a tree of Lists, String Maps, Strings, and Typeds.
   *
   * @param data luxem
   * @return list of top level objects
   */
  public static List parse(final InputStream data) {
    return new TreeReader().read(data);
  }

  public static Stream<Pair<Event, Object>> streamEvents(
      final InputStream source, final Reader.EventFactory factory) {
    class State {
      LuxemPath path = new LuxemArrayPath(null);
      Deque<Pair<Event, Object>> events = new ArrayDeque<>();
    }
    final State state = new State();
    final BufferedReader reader =
        new BufferedReader() {
          @Override
          protected void eatRecordBegin() {
            state.path = new LuxemObjectPath(state.path.value());
            state.events.addLast(new Pair<>(factory.objectOpen(), state.path));
          }

          @Override
          protected void eatArrayBegin() {
            state.path = new LuxemArrayPath(state.path.value());
            state.events.addLast(new Pair<>(factory.arrayOpen(), state.path));
          }

          @Override
          protected void eatArrayEnd() {
            state.path = state.path.pop();
            state.events.addLast(new Pair<>(factory.arrayClose(), state.path));
          }

          @Override
          protected void eatRecordEnd() {
            state.path = state.path.pop();
            state.events.addLast(new Pair<>(factory.objectClose(), state.path));
          }

          @Override
          protected void eatType(byte[] bytes) {
            state.path = state.path.type();
            state.events.addLast(
                new Pair<>(factory.type(new String(bytes, StandardCharsets.UTF_8)), state.path));
          }

          @Override
          protected void eatKey(byte[] bytes) {
            final String string = new String(bytes, StandardCharsets.UTF_8);
            state.path = state.path.unkey();
            state.events.addLast(new Pair<>(factory.key(string), state.path));
            state.path = state.path.key(string);
          }

          @Override
          protected void eatPrimitive(byte[] bytes) {
            state.path = state.path.value();
            state.events.addLast(
                new Pair<>(
                    factory.primitive(new String(bytes, StandardCharsets.UTF_8)), state.path));
          }
        };
    return Reader.stream(reader, source)
        .flatMap(
            last -> {
              return drain(state.events);
            });
  }
}
