package com.zarbosoft.pidgoon.events.nodes;

import com.zarbosoft.pidgoon.events.MatchingEvent;
import com.zarbosoft.rendaw.common.ROPair;

/** Use this terminal if the events themselves define matching conditions. */
public class MatchingEventTerminal<T extends MatchingEvent> extends Terminal<T, T> {
  private final MatchingEvent value;

  public MatchingEventTerminal(final MatchingEvent value) {
    this.value = value;
  }

  @Override
  protected ROPair<Boolean, T> matches(T event) {
    return new ROPair<>(value.matches((MatchingEvent) event), event);
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
