package com.zarbosoft.luxem.events;

import com.zarbosoft.pidgoon.events.MatchingEvent;

public class LRecordOpenEvent implements LuxemEvent {
  public static final LRecordOpenEvent instance = new LRecordOpenEvent();

  private LRecordOpenEvent() {}

  @Override
  public boolean matches(final MatchingEvent event) {
    return event.getClass() == getClass();
  }

  @Override
  public String toString() {
    return "RECORD OPEN";
  }
}
