package com.zarbosoft.luxem.events;

import com.zarbosoft.pidgoon.events.MatchingEvent;

public class LRecordCloseEvent implements LuxemEvent {
  public static final LRecordCloseEvent instance = new LRecordCloseEvent();

  private LRecordCloseEvent() {}

  @Override
  public boolean matches(final MatchingEvent event) {
    return event.getClass() == getClass();
  }

  @Override
  public String toString() {
    return "RECORD CLOSE";
  }
}
