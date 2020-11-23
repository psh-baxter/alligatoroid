package com.zarbosoft.merman.editor.backevents;

import com.zarbosoft.pidgoon.events.MatchingEvent;

public class EArrayOpenEvent implements BackEvent {

  @Override
  public boolean matches(final MatchingEvent event) {
    return event instanceof EArrayOpenEvent;
  }

  @Override
  public String toString() {
    return String.format("ARRAY OPEN");
  }
}
