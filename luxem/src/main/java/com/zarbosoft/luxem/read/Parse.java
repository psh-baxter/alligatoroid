package com.zarbosoft.luxem.read;

import com.zarbosoft.luxem.Luxem;
import com.zarbosoft.pidgoon.BaseParseBuilder;
import com.zarbosoft.pidgoon.errors.InvalidStream;
import com.zarbosoft.pidgoon.errors.InvalidStreamAt;
import com.zarbosoft.pidgoon.events.ParseBuilder;
import com.zarbosoft.pidgoon.events.ParseEventSink;
import com.zarbosoft.pidgoon.events.Position;
import com.zarbosoft.pidgoon.nodes.Reference;
import com.zarbosoft.rendaw.common.ROList;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class Parse<O> extends BaseParseBuilder<O, Parse<O>> {
  private Reader.EventFactory factory = null;

  private Parse(final Parse<O> other) {
    super(other);
    this.factory = other.factory;
  }

  public Parse(Reference.Key root) {
    super(root);
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

  public O parse(final ROList<Position> stream) {
    ParseEventSink<O> stream1 =
        new ParseBuilder<O>(root)
            .grammar(grammar)
            .uncertainty(uncertaintyLimit)
            .parse();
    for (final Position pos : stream) {
      if (stream1.ended()) throw new InvalidStreamAt(pos, new InvalidStream(stream1.context()));
      stream1 = stream1.push(pos.event, pos.at);
    }
    return stream1.result();
  }
}
