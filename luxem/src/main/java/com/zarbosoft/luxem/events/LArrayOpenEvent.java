package com.zarbosoft.luxem.events;

import com.zarbosoft.pidgoon.events.MatchingEvent;

public class LArrayOpenEvent implements LuxemEvent {
  public static final LArrayOpenEvent instance = new LArrayOpenEvent();

  private LArrayOpenEvent() {}

  @Override
  public boolean matches(final MatchingEvent event) {
    return event.getClass() == getClass();
  }

  @Override
  public String toString() {
    return "ARRAY OPEN";
  }
}
