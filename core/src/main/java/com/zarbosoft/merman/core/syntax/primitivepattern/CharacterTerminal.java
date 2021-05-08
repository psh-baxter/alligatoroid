package com.zarbosoft.merman.core.syntax.primitivepattern;

import com.zarbosoft.pidgoon.events.nodes.Terminal;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSList;

public class CharacterTerminal extends Terminal<Object, ROList<String>> {
  final String exact;
  private final boolean capture;

  CharacterTerminal(boolean capture, String exact) {
    this.capture = capture;
    this.exact = exact;
  }

  @Override
  protected ROPair<Boolean, ROList<String>> matches(Object event0) {
    if (!(event0 instanceof CharacterEvent)) return new ROPair<>(false, null);
    CharacterEvent event = (CharacterEvent) event0;
    return new ROPair<Boolean, ROList<String>>(
        exact.equals(event.value), capture ? TSList.of(event.value) : ROList.empty);
  }
}
