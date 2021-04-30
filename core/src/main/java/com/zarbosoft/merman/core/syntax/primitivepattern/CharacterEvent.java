package com.zarbosoft.merman.core.syntax.primitivepattern;

import com.zarbosoft.pidgoon.events.Event;
import com.zarbosoft.pidgoon.events.MatchingEvent;

public class CharacterEvent implements Event {
  public final String value;

  public CharacterEvent(final String value) {
    this.value = value;
  }
}
