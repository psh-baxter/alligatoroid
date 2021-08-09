package com.zarbosoft.luxem.events;

import com.zarbosoft.pidgoon.events.MatchingEvent;
import com.zarbosoft.rendaw.common.Format;

public class LTypeEvent implements LuxemEvent {
  public String value;

  public LTypeEvent(final String string) {
    this.value = string;
  }

  public LTypeEvent() {}

  @Override
  public boolean matches(final MatchingEvent event) {
    return event.getClass() == getClass()
        && (value == null || value.equals(((LTypeEvent) event).value));
  }

  @Override
  public String toString() {
    return Format.format("TYPE (%s)", value);
  }
}
