package com.zarbosoft.merman.jfxcore.serialization;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.zarbosoft.merman.core.backevents.BackEvent;
import com.zarbosoft.merman.core.backevents.EArrayCloseEvent;
import com.zarbosoft.merman.core.backevents.EArrayOpenEvent;
import com.zarbosoft.merman.core.backevents.EKeyEvent;
import com.zarbosoft.merman.core.backevents.EObjectCloseEvent;
import com.zarbosoft.merman.core.backevents.EObjectOpenEvent;
import com.zarbosoft.merman.core.backevents.EPrimitiveEvent;
import com.zarbosoft.merman.core.backevents.JSpecialPrimitiveEvent;
import com.zarbosoft.merman.core.serialization.JSONObjectPath;
import com.zarbosoft.merman.core.serialization.JSONPath;
import com.zarbosoft.pidgoon.BaseParseBuilder;
import com.zarbosoft.pidgoon.events.Event;
import com.zarbosoft.pidgoon.events.ParseEventSink;
import com.zarbosoft.pidgoon.nodes.Reference;
import com.zarbosoft.rendaw.common.DeadCode;
import com.zarbosoft.rendaw.common.ROPair;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.zarbosoft.rendaw.common.Common.uncheck;

public class JSONParse<O> extends BaseParseBuilder<O, JSONParse<O>> {

  private int eventUncertainty = 20;

  private JSONParse(final JSONParse<O> other) {
    super(other);
    this.eventUncertainty = other.eventUncertainty;
  }

  public JSONParse(Reference.Key<O> root) {
    super(root);
  }

  public static List<ROPair<? extends Event, Object>> streamEvents(InputStream stream) {
    List<ROPair<? extends Event, Object>> out = new ArrayList<>();
    final JsonParser stream1 = uncheck(() -> new JsonFactory().createParser(stream));
    JSONPath path = new JSONObjectPath(null);
    JsonToken token = uncheck(() -> stream1.nextToken());

    while (token != null) {
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
            e = new EKeyEvent(uncheck(() -> stream1.getCurrentName()));
            break;
          }
        case VALUE_EMBEDDED_OBJECT:
          // Supposedly shouldn't apply with normal options
          throw new DeadCode();
        case VALUE_STRING:
          {
            e = new EPrimitiveEvent(uncheck(() -> stream1.getValueAsString()));
            break;
          }
        case VALUE_NUMBER_INT:
        case VALUE_NUMBER_FLOAT:
          {
            e = new JSpecialPrimitiveEvent(uncheck(() -> stream1.getValueAsString()));
            break;
          }
        case VALUE_TRUE:
          {
            e = new JSpecialPrimitiveEvent("true");
            break;
          }
        case VALUE_FALSE:
          {
            e = new JSpecialPrimitiveEvent("false");
            break;
          }
        case VALUE_NULL:
          {
            e = new JSpecialPrimitiveEvent("null");
            break;
          }
        default:
          throw new DeadCode();
      }
      out.add(new ROPair<>(e, path));
      path = path.push(e);
      token = uncheck(() -> stream1.nextToken());
    }
    return out;
  }

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

  public O parse(InputStream stream) {
    return parse(streamEvents(stream));
  }

  /**
   * Parse by pulling events from the stream.
   *
   * @param data
   * @return
   */
  public O parse(final List<ROPair<? extends Event, Object>> data) {
    ParseEventSink<O> eventStream = parse();
    for (ROPair<? extends Event, Object> pair : data) {
      eventStream = eventStream.push(pair.first, pair.second.toString());
    }
    return eventStream.result();
  }

  /**
   * Instead of pulling from an input stream, use the returned EventStream to push events to the
   * parse.
   *
   * @return
   */
  public ParseEventSink<O> parse() {
    return new ParseEventSink<>(grammar, root, uncertaintyLimit);
  }
}
