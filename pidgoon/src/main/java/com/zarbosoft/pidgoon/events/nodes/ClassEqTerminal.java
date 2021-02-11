package com.zarbosoft.pidgoon.events.nodes;

import com.zarbosoft.pidgoon.model.Store;
import com.zarbosoft.pidgoon.events.Event;

public class ClassEqTerminal extends Terminal {
  /*
  TODO should probably just replace this with an enum-based event
   */
  public final String className;

  public ClassEqTerminal(String className) {
    this.className = className;
  }

  @Override
  protected boolean matches(Event event, Store store) {
    return className.equals(event.getClass().getName());
  }
}
