package com.zarbosoft.merman.core.syntax.primitivepattern;

import com.zarbosoft.pidgoon.events.nodes.Terminal;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSList;

import static com.zarbosoft.rendaw.common.Common.isOrdered;

class CharacterRangeTerminal extends Terminal<CharacterEvent, ROList<String>> {
  final String low;
  final String high;
  private final boolean capture;

  CharacterRangeTerminal(boolean capture, String low, String high) {
    this.capture = capture;
    this.low = low;
    this.high = high;
  }

  @Override
  protected ROPair<Boolean, ROList<String>> matches(CharacterEvent event) {
    return new ROPair<Boolean, ROList<String>>(
        isOrdered(low, event.value) && isOrdered(event.value, high),
        capture ? TSList.of(event.value) : ROList.empty);
  }
}
