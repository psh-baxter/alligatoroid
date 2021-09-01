package com.zarbosoft.luxem.read;

import com.zarbosoft.luxem.Luxem;
import com.zarbosoft.pidgoon.BaseParseBuilder;
import com.zarbosoft.pidgoon.nodes.Reference;

import java.io.InputStream;

public class ParseBuilder<O> extends BaseParseBuilder<O, ParseBuilder<O>> {
  private Reader.EventFactory factory = null;

  private ParseBuilder(final ParseBuilder<O> other) {
    super(other);
    this.factory = other.factory;
  }

  public ParseBuilder(Reference.Key root) {
    super(root);
  }

  @Override
  protected ParseBuilder<O> split() {
    return new ParseBuilder<>(this);
  }

  public ParseBuilder<O> eventFactory(final Reader.EventFactory factory) {
    if (this.factory != null) throw new IllegalArgumentException("Factory already set");
    final ParseBuilder<O> out = split();
    out.factory = factory;
    return out;
  }

  public O parallelParse(final InputStream stream) {
    return parallelParse(
        Luxem.streamEvents(stream, factory == null ? new Reader.DefaultEventFactory() : factory));
  }

  public O serialParse(final InputStream stream) {
    return serialParsePosition(
        Luxem.streamEvents(stream, factory == null ? new Reader.DefaultEventFactory() : factory));
  }
}
