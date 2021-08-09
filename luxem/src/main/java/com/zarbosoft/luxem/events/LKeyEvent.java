package com.zarbosoft.luxem.events;

import com.zarbosoft.pidgoon.events.MatchingEvent;
import com.zarbosoft.rendaw.common.Format;

public class LKeyEvent implements LuxemEvent {
  public String value;

  public LKeyEvent(final String string) {
    value = string;
  }

  public LKeyEvent() {}

  @Override
  public boolean matches(final MatchingEvent event) {
    return event.getClass() == getClass()
        && (value == null || value.equals(((LKeyEvent) event).value));
  }

  @Override
  public String toString() {
    return Format.format("KEY (%s)", value);
  }
}
