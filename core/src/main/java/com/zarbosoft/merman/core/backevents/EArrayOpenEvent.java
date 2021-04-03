package com.zarbosoft.merman.core.backevents;

import com.zarbosoft.pidgoon.events.MatchingEvent;
import com.zarbosoft.rendaw.common.Format;

public class EArrayOpenEvent implements BackEvent {

  @Override
  public boolean matches(final MatchingEvent event) {
    return event instanceof EArrayOpenEvent;
  }

  @Override
  public String toString() {
    return Format.format("ARRAY OPEN");
  }
}
