package com.zarbosoft.pidgoon.events;

public class Position {
  public final Event event;
  public final Object at;

  public Position(final Event event, final Object at) {
    this.event = event;
    this.at = at;
  }

  public Event get() {
    return event;
  }
}
