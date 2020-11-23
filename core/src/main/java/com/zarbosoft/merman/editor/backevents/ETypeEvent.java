package com.zarbosoft.merman.editor.backevents;

import com.zarbosoft.pidgoon.events.MatchingEvent;

public class ETypeEvent implements BackEvent {
  public String value;

  public ETypeEvent(final String string) {
    value = string;
  }

  public ETypeEvent() {}

  @Override
  public boolean matches(final MatchingEvent event) {
    return event instanceof ETypeEvent
        && (value == null || value.equals(((ETypeEvent) event).value));
  }

  @Override
  public String toString() {
    return String.format("TYPE %s", value == null ? "*" : value);
  }
}
