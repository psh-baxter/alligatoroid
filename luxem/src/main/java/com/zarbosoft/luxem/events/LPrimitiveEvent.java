package com.zarbosoft.luxem.events;

import com.zarbosoft.pidgoon.events.MatchingEvent;

public class LPrimitiveEvent implements LuxemEvent {
  public String value;

  public LPrimitiveEvent(final String value) {
    this.value = value;
  }

  public LPrimitiveEvent() {}

  @Override
  public boolean matches(final MatchingEvent event) {
    return event.getClass() == getClass()
        && (value == null || value.equals(((LPrimitiveEvent) event).value));
  }
}
