package com.zarbosoft.merman.editor.backevents;

import com.zarbosoft.pidgoon.events.MatchingEvent;
import com.zarbosoft.rendaw.common.Format;

public class JSpecialPrimitiveEvent implements BackEvent {
  public String value = null;

  public JSpecialPrimitiveEvent(final String value) {
    this.value = value;
  }

  public JSpecialPrimitiveEvent() {}

  @Override
  public boolean matches(final MatchingEvent event) {
    if (value == null) return event instanceof JSpecialPrimitiveEvent;
    else return event instanceof JSpecialPrimitiveEvent && value.equals(((JSpecialPrimitiveEvent) event).value);
  }

  @Override
  public String toString() {
    if (value == null) return "*";
    return Format.format("\"%s\"", value);
  }
}
