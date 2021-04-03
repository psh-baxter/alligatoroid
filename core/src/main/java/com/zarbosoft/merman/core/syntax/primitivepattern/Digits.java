package com.zarbosoft.merman.core.syntax.primitivepattern;

import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.model.Store;
import com.zarbosoft.pidgoon.events.Event;
import com.zarbosoft.pidgoon.events.nodes.Terminal;

public class Digits extends Pattern {
  @Override
  public Node build() {
    return new Terminal() {
      @Override
      protected boolean matches(Event event0, Store store) {
        String v = ((CharacterEvent) event0).value;
        if (v.length() != 1) return false;
        char c = v.charAt(0);
        return c >= '0' && c <= '9';
      }
    };
  }
}
