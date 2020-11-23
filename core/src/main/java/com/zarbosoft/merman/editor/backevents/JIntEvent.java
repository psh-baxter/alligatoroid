package com.zarbosoft.merman.editor.backevents;

import com.zarbosoft.pidgoon.events.MatchingEvent;

public class JIntEvent implements BackEvent {
  public String value = null;

  public JIntEvent(final String value) {
    this.value = value;
  }

  public JIntEvent() {}

  @Override
  public boolean matches(final MatchingEvent event) {
    if (value == null) return event instanceof JIntEvent || event instanceof JNullEvent;
    else return event instanceof JIntEvent && value.equals(((JIntEvent) event).value);
  }

  @Override
  public String toString() {
    if (value == null) return "(int) *";
    return String.format("(int) %s", value);
  }
}
