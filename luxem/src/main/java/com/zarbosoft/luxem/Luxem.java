package com.zarbosoft.luxem;

import com.zarbosoft.luxem.read.BufferedReader;
import com.zarbosoft.luxem.read.Reader;
import com.zarbosoft.luxem.read.path.LuxemArrayPath;
import com.zarbosoft.luxem.read.path.LuxemObjectPath;
import com.zarbosoft.luxem.read.path.LuxemPath;
import com.zarbosoft.pidgoon.events.Event;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSList;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/** Methods for common use cases. */
public class Luxem {
  public static ROList<? extends ROPair> streamEvents(
      final InputStream source, final Reader.EventFactory factory) {
    class State {
      LuxemPath path = new LuxemArrayPath(null);
      TSList<ROPair<Event, LuxemPath>> events = new TSList<>();
    }
    final State state = new State();
    final BufferedReader reader =
        new BufferedReader() {
          @Override
          protected void eatRecordBegin() {
            state.path = new LuxemObjectPath(state.path.value());
            state.events.add(new ROPair<>(factory.objectOpen(), state.path));
          }

          @Override
          protected void eatArrayBegin() {
            state.path = new LuxemArrayPath(state.path.value());
            state.events.add(new ROPair<>(factory.arrayOpen(), state.path));
          }

          @Override
          protected void eatArrayEnd() {
            state.path = state.path.pop();
            state.events.add(new ROPair<>(factory.arrayClose(), state.path));
          }

          @Override
          protected void eatRecordEnd() {
            state.path = state.path.pop();
            state.events.add(new ROPair<>(factory.objectClose(), state.path));
          }

          @Override
          protected void eatType(byte[] bytes) {
            state.path = state.path.type();
            state.events.add(
                new ROPair<>(factory.type(new String(bytes, StandardCharsets.UTF_8)), state.path));
          }

          @Override
          protected void eatKey(byte[] bytes) {
            final String string = new String(bytes, StandardCharsets.UTF_8);
            state.path = state.path.unkey();
            state.events.add(new ROPair<>(factory.key(string), state.path));
            state.path = state.path.key(string);
          }

          @Override
          protected void eatPrimitive(byte[] bytes) {
            state.path = state.path.value();
            state.events.add(
                new ROPair<>(
                    factory.primitive(new String(bytes, StandardCharsets.UTF_8)), state.path));
          }
        };
    reader.feed(source);
    reader.finish();
    return state.events;
  }
}
