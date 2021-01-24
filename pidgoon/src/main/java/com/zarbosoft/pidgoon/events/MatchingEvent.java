package com.zarbosoft.pidgoon.events;

public interface MatchingEvent extends Event {

  default boolean matches(final MatchingEvent event) {
    return getClass() == event.getClass();
  }
}
