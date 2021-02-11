package com.zarbosoft.pidgoon.events.nodes;

import com.zarbosoft.pidgoon.model.Store;
import com.zarbosoft.pidgoon.events.Event;
import com.zarbosoft.pidgoon.events.MatchingEvent;

/** Use this terminal if the events themselves define matching conditions. */
public class MatchingEventTerminal extends Terminal {
  private final MatchingEvent value;

  public MatchingEventTerminal(final MatchingEvent value) {
    this.value = value;
  }

  @Override
  protected boolean matches(final Event event, final Store store) {
    return value.matches((MatchingEvent) event);
  }
}
