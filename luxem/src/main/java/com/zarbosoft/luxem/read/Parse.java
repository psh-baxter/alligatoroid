package com.zarbosoft.luxem.read;

import com.zarbosoft.luxem.Luxem;
import com.zarbosoft.pidgoon.BaseParseBuilder;
import com.zarbosoft.pidgoon.nodes.Reference;

import java.io.InputStream;

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

  public O parallelParse(final InputStream stream) {
    return parallelParse(
        Luxem.streamEvents(stream, factory == null ? new Reader.DefaultEventFactory() : factory));
  }

  public O serialParse(final InputStream stream) {
    return serialParsePosition(
        Luxem.streamEvents(stream, factory == null ? new Reader.DefaultEventFactory() : factory));
  }
}
