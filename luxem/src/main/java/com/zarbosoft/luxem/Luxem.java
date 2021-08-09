package com.zarbosoft.luxem;

import com.zarbosoft.luxem.read.BufferedReader;
import com.zarbosoft.luxem.read.Reader;
import com.zarbosoft.luxem.read.path.LuxemArrayPath;
import com.zarbosoft.luxem.read.path.LuxemPath;
import com.zarbosoft.luxem.read.path.LuxemRecordPath;
import com.zarbosoft.pidgoon.events.Event;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSList;

import java.io.InputStream;

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
            state.path = new LuxemRecordPath(state.path.value());
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
          protected void eatType(String text) {
            state.path = state.path.type();
            state.events.add(new ROPair<>(factory.type(text), state.path));
          }

          @Override
          protected void eatKey(String string) {
            state.path = state.path.unkey();
            state.events.add(new ROPair<>(factory.key(string), state.path));
            state.path = state.path.key(string);
          }

          @Override
          protected void eatPrimitive(String string) {
            state.path = state.path.value();
            state.events.add(new ROPair<>(factory.primitive(string), state.path));
          }
        };
    reader.feed(source);
    reader.finish();
    return state.events;
  }
}
