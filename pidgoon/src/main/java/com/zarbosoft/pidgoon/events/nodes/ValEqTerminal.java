package com.zarbosoft.pidgoon.events.nodes;

import com.zarbosoft.pidgoon.events.Event;
import com.zarbosoft.pidgoon.Store;

public class ValEqTerminal extends Terminal {
  public final Event match;

  public ValEqTerminal(Event match) {
    this.match = match;
  }

  @Override
  protected boolean matches(Event event, Store store) {
    return event == match;
  }
}
