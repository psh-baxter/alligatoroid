package com.zarbosoft.pidgoon.events;

public class Position implements com.zarbosoft.pidgoon.model.Position {
  public final Event event;
  public final Object at;

  public Position(final Event event, final Object at) {
    this.event = event;
    this.at = at;
  }

  @Override
  public com.zarbosoft.pidgoon.model.Position advance() {
    return this;
  }

  @Override
  public boolean isEOF() {
    return false;
  }

  public Event get() {
    return event;
  }
}
