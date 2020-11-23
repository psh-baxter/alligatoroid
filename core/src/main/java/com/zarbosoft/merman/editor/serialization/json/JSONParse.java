package com.zarbosoft.merman.editor.serialization.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.zarbosoft.luxem.read.InvalidStream;
import com.zarbosoft.merman.editor.backevents.BackEvent;
import com.zarbosoft.merman.editor.backevents.EArrayCloseEvent;
import com.zarbosoft.merman.editor.backevents.EArrayOpenEvent;
import com.zarbosoft.merman.editor.backevents.EKeyEvent;
import com.zarbosoft.merman.editor.backevents.EObjectCloseEvent;
import com.zarbosoft.merman.editor.backevents.EObjectOpenEvent;
import com.zarbosoft.merman.editor.backevents.EPrimitiveEvent;
import com.zarbosoft.merman.editor.backevents.JFalseEvent;
import com.zarbosoft.merman.editor.backevents.JFloatEvent;
import com.zarbosoft.merman.editor.backevents.JIntEvent;
import com.zarbosoft.merman.editor.backevents.JNullEvent;
import com.zarbosoft.merman.editor.backevents.JTrueEvent;
import com.zarbosoft.merman.editor.serialization.json.path.JSONObjectPath;
import com.zarbosoft.merman.editor.serialization.json.path.JSONPath;
import com.zarbosoft.pidgoon.Store;
import com.zarbosoft.pidgoon.events.Event;
import com.zarbosoft.pidgoon.events.ParseEventSink;
import com.zarbosoft.pidgoon.events.stores.StackStore;
import com.zarbosoft.pidgoon.internal.BaseParseBuilder;
import com.zarbosoft.rendaw.common.Common;
import com.zarbosoft.rendaw.common.DeadCode;
import com.zarbosoft.rendaw.common.Pair;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.stream.Stream;

import static com.zarbosoft.rendaw.common.Common.concatNull;
import static com.zarbosoft.rendaw.common.Common.uncheck;

public class JSONParse<O> extends BaseParseBuilder<JSONParse<O>> {

  private int eventUncertainty = 20;

  private JSONParse(final JSONParse<O> other) {
    super(other);
    this.eventUncertainty = other.eventUncertainty;
  }

  public JSONParse() {}

  public JSONParse<O> eventUncertainty(final int limit) {
    if (eventUncertainty != 20)
      throw new IllegalArgumentException("Max event uncertainty already set");
    final JSONParse<O> out = split();
    out.eventUncertainty = limit;
    return out;
  }

  @Override
  protected JSONParse<O> split() {
    return new JSONParse<>(this);
  }

  public Stream<O> parseByElement(final String string) {
    return parseByElement(new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8)));
  }

  public Stream<O> parseByElement(final InputStream stream) {
    return parseByElement(streamEvents(stream));
  }

  public static Stream<Pair<? extends Event, Object>> streamEvents(InputStream stream) {
    return Common.stream(
        new Iterator<Pair<? extends Event, Object>>() {
          final JsonParser stream1 = uncheck(() -> new JsonFactory().createParser(stream));
          JSONPath path = new JSONObjectPath(null);
          JsonToken token = uncheck(() -> stream1.nextToken());

          @Override
          public boolean hasNext() {
            return token != null;
          }

          @Override
          public Pair<Event, Object> next() {
            return uncheck(
                () -> {
                  JsonToken token = this.token;
                  this.token = stream1.nextToken();
                  BackEvent e;
                  switch (token) {
                    case NOT_AVAILABLE:
                      // Only async mode
                      throw new DeadCode();
                    case START_OBJECT:
                      {
                        e = new EObjectOpenEvent();
                        break;
                      }
                    case END_OBJECT:
                      {
                        e = new EObjectCloseEvent();
                        break;
                      }
                    case START_ARRAY:
                      {
                        e = new EArrayOpenEvent();
                        break;
                      }
                    case END_ARRAY:
                      {
                        e = new EArrayCloseEvent();
                        break;
                      }
                    case FIELD_NAME:
                      {
                        e = new EKeyEvent(stream1.getCurrentName());
                        break;
                      }
                    case VALUE_EMBEDDED_OBJECT:
                      // Supposedly shouldn't apply with normal options
                      throw new DeadCode();
                    case VALUE_STRING:
                      {
                        e = new EPrimitiveEvent(stream1.getValueAsString());
                        break;
                      }
                    case VALUE_NUMBER_INT:
                      {
                        e = new JIntEvent(stream1.getValueAsString());
                        break;
                      }
                    case VALUE_NUMBER_FLOAT:
                      {
                        e = new JFloatEvent(stream1.getValueAsString());
                        break;
                      }
                    case VALUE_TRUE:
                      {
                        e = new JTrueEvent();
                        break;
                      }
                    case VALUE_FALSE:
                      {
                        e = new JFalseEvent();
                        break;
                      }
                    case VALUE_NULL:
                      {
                        e = new JNullEvent();
                        break;
                      }
                    default:
                      throw new DeadCode();
                  }
                  Pair<Event, Object> out = new Pair<>(e, path);
                  path = path.push(e);
                  return out;
                });
          }
        });
  }

  public Stream<O> parseByElement(final Stream<Pair<? extends Event, Object>> stream) {
    class State {
      ParseEventSink<O> stream = null;

      private void createStream() {
        stream =
            new com.zarbosoft.pidgoon.events.ParseBuilder<O>()
                .grammar(grammar)
                .root(root)
                .store(initialStore)
                .errorHistory(errorHistoryLimit)
                .dumpAmbiguity(dumpAmbiguity)
                .uncertainty(eventUncertainty)
                .parse();
      }

      public void handleEvent(final Pair<? extends Event, Object> pair) {
        stream = stream.push(pair.first, pair.second);
      }
    }
    final State state = new State();
    return concatNull(stream)
        .map(
            pair -> {
              if (pair == null) {
                if (state.stream == null) return null;
                else throw new InvalidStream(-1, "Input stream ended mid-element.");
              }
              if (state.stream == null) state.createStream();
              state.handleEvent(pair);
              if (state.stream.ended()) {
                O result = state.stream.result();
                state.stream = null;
                return result;
              } else return null;
            })
        .filter(o -> o != null);
  }

  public O parse(InputStream stream) {
    return parse(streamEvents(stream));
  }

  /**
   * Parse by pulling events from the stream.
   *
   * @param data
   * @return
   */
  public O parse(final Stream<Pair<? extends Event, Object>> data) {
    final Common.Mutable<ParseEventSink<O>> eventStream = new Common.Mutable<>(parse());
    data.forEach(
        pair -> {
          eventStream.value = eventStream.value.push(pair.first, pair.second.toString());
        });
    return eventStream.value.result();
  }

  /**
   * Instead of pulling from an input stream, use the returned EventStream to push events to the
   * parse.
   *
   * @return
   */
  public ParseEventSink<O> parse() {
    final Store store =
        initialStore == null
            ? new StackStore(env == null ? null : new HashMap<>(env))
            : initialStore;
    return new ParseEventSink<>(
        grammar, root, store, errorHistoryLimit, uncertaintyLimit, dumpAmbiguity);
  }
}
