package com.zarbosoft.merman.core.editor.backevents;

import com.zarbosoft.pidgoon.events.MatchingEvent;
import com.zarbosoft.rendaw.common.Format;

public class EObjectCloseEvent implements BackEvent {

  @Override
  public boolean matches(final MatchingEvent event) {
    return event instanceof EObjectCloseEvent;
  }

  @Override
  public String toString() {
    return Format.format("OBJECT CLOSE");
  }
}
