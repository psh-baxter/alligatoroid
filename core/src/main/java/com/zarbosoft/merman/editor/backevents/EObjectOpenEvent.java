package com.zarbosoft.merman.editor.backevents;

import com.zarbosoft.pidgoon.events.MatchingEvent;

public class EObjectOpenEvent implements BackEvent {

  @Override
  public boolean matches(final MatchingEvent event) {
    return event.getClass() == getClass();
  }

  @Override
  public String toString() {
    return String.format("OBJECT OPEN");
  }
}
