package com.zarbosoft.pidgoon.events;

public class Position {
  public final Object event;
  public final Object at;

  public Position(final Object event, final Object at) {
    this.event = event;
    this.at = at;
  }

  public Object get() {
    return event;
  }
}
