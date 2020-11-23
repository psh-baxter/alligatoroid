package com.zarbosoft.merman.editor.backevents;

import com.zarbosoft.pidgoon.events.MatchingEvent;

public class EObjectCloseEvent implements BackEvent {

  @Override
  public boolean matches(final MatchingEvent event) {
    return event instanceof EObjectCloseEvent;
  }

  @Override
  public String toString() {
    return String.format("OBJECT CLOSE");
  }
}
