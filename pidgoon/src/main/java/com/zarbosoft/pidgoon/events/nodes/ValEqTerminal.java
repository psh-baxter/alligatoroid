package com.zarbosoft.pidgoon.events.nodes;

import com.zarbosoft.pidgoon.model.Store;
import com.zarbosoft.pidgoon.events.Event;

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
