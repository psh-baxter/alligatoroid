package com.zarbosoft.merman.editor.backevents;

import com.zarbosoft.pidgoon.events.MatchingEvent;

public class JFloatEvent implements BackEvent {
  public String value = null;

  public JFloatEvent(final String value) {
    this.value = value;
  }

  public JFloatEvent() {}

  @Override
  public boolean matches(final MatchingEvent event) {
    if (value == null) return event instanceof JFloatEvent || event instanceof JNullEvent;
    else return event instanceof JFloatEvent && value.equals(((JFloatEvent) event).value);
  }

  @Override
  public String toString() {
    if (value == null) return "(float) *";
    return value;
  }
}
