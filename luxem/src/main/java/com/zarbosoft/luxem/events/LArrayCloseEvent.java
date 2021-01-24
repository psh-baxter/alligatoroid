package com.zarbosoft.luxem.events;

import com.zarbosoft.pidgoon.events.MatchingEvent;

public class LArrayCloseEvent implements LuxemEvent {
  public static LArrayCloseEvent instance = new LArrayCloseEvent();

  private LArrayCloseEvent() {}

  @Override
  public boolean matches(final MatchingEvent event) {
    return event.getClass() == getClass();
  }
}
