package com.zarbosoft.luxem.read;

import com.zarbosoft.luxem.Luxem;
import com.zarbosoft.pidgoon.BaseParseBuilder;
import com.zarbosoft.pidgoon.events.Event;
import com.zarbosoft.pidgoon.events.ParseEventSink;
import com.zarbosoft.rendaw.common.Pair;
import com.zarbosoft.rendaw.common.ROPair;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Parse<O> extends BaseParseBuilder<Parse<O>> {
  private int eventUncertainty = 20;
  private Reader.EventFactory factory = null;

  private Parse(final Parse<O> other) {
    super(other);
    this.eventUncertainty = other.eventUncertainty;
    this.factory = other.factory;
  }

  public Parse() {}

  public Parse<O> eventUncertainty(final int limit) {
    if (eventUncertainty != 20)
      throw new IllegalArgumentException("Max event uncertainty already set");
    final Parse<O> out = split();
    out.eventUncertainty = limit;
    return out;
  }

  @Override
  protected Parse<O> split() {
    return new Parse<>(this);
  }

  public Parse<O> eventFactory(final Reader.EventFactory factory) {
    if (this.factory != null) throw new IllegalArgumentException("Factory already set");
    final Parse<O> out = split();
    out.factory = factory;
    return out;
  }

  public O parse(final String string) {
    return parse(new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8)));
  }

  public O parse(final InputStream stream) {
    return parse(
        Luxem.streamEvents(stream, factory == null ? new Reader.DefaultEventFactory() : factory));
  }

  public O parse(final List<ROPair<Event, Object>> stream) {
    ParseEventSink<O> stream1 =
        new com.zarbosoft.pidgoon.events.ParseBuilder<O>()
            .grammar(grammar)
            .root(root)
            .store(initialStore)
            .errorHistory(errorHistoryLimit)
            .dumpAmbiguity(dumpAmbiguity)
            .uncertainty(eventUncertainty)
            .parse();
    for (final ROPair<Event, Object> pair : stream)
      stream1 = stream1.push(pair.first, pair.second);
    return stream1.result();
  }
}
