package com.zarbosoft.merman.jfxcore.serialization;

import com.google.gson.stream.JsonReader;
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
import com.zarbosoft.pidgoon.nodes.Reference;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSList;

import java.io.InputStream;
import java.io.InputStreamReader;

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

  public static ROList<? extends ROPair> streamEvents(InputStream stream) {
    return uncheck(
        () -> {
          TSList<ROPair<? extends Event, Object>> out = new TSList<>();
          JsonReader stream1 = new JsonReader(new InputStreamReader(stream));
          JSONPath path = new JSONObjectPath(null);

          while (true) {
            BackEvent e = null;
            switch (stream1.peek()) {
              case BEGIN_ARRAY:
                {
                  e = new EArrayOpenEvent();
                  stream1.beginArray();
                  break;
                }
              case END_ARRAY:
                {
                  e = new EArrayCloseEvent();
                  stream1.endArray();
                  break;
                }
              case BEGIN_OBJECT:
                {
                  e = new EObjectOpenEvent();
                  stream1.beginObject();
                  break;
                }
              case END_OBJECT:
                {
                  e = new EObjectCloseEvent();
                  stream1.endObject();
                  break;
                }
              case NAME:
                {
                  e = new EKeyEvent(stream1.nextName());
                  break;
                }
              case STRING:
                {
                  e = new EPrimitiveEvent(stream1.nextString());
                  break;
                }
              case NUMBER:
                {
                  e = new JSpecialPrimitiveEvent(stream1.nextString());
                  break;
                }
              case BOOLEAN:
                {
                  e = new JSpecialPrimitiveEvent(stream1.nextBoolean() ? "true" : "false");
                  break;
                }
              case NULL:
                {
                  e = new JSpecialPrimitiveEvent("null");
                  stream1.nextNull();
                  break;
                }
              case END_DOCUMENT:
                {
                  break;
                }
              default:
                throw new Assertion();
            }
            if (e == null) break;
            out.add(new ROPair<>(e, path));
            path = path.push(e);
          }
          return out;
        });
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

  public O parallelParse(InputStream stream) {
    return parallelParse(streamEvents(stream));
  }

  public O serialParse(InputStream stream) {
    return serialParsePosition(streamEvents(stream));
  }
}
