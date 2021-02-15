package com.zarbosoft.merman.editor.backevents;

import com.zarbosoft.pidgoon.events.MatchingEvent;
import com.zarbosoft.rendaw.common.Format;

public class EKeyEvent implements BackEvent {
  public String value;

  public EKeyEvent(final String string) {
    value = string;
  }

  public EKeyEvent() {}

  @Override
  public boolean matches(final MatchingEvent event) {
    return event instanceof EKeyEvent && (value == null || value.equals(((EKeyEvent) event).value));
  }

  @Override
  public String toString() {
    return Format.format("KEY %s", value == null ? "*" : value + ":");
  }
}
