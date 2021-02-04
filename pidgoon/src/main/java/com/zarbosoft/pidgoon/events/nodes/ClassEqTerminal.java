package com.zarbosoft.pidgoon.events.nodes;

import com.zarbosoft.pidgoon.Store;
import com.zarbosoft.pidgoon.events.Event;

public class ClassEqTerminal extends Terminal {
  public final Class match;

  public ClassEqTerminal(Class match) {
    this.match = match;
  }

  @Override
  protected boolean matches(Event event, Store store) {
    return event.getClass() == match;
  }
}
