package com.zarbosoft.merman.core.syntax.primitivepattern;

import com.zarbosoft.pidgoon.events.EscapableResult;
import com.zarbosoft.pidgoon.events.nodes.Terminal;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSList;

import static com.zarbosoft.rendaw.common.Common.isOrdered;

class CharacterRangeTerminal extends BaseTerminal {
  final String low;
  final String high;

  protected CharacterRangeTerminal(boolean capture, String low, String high) {
    super(capture);
    this.low = low;
    this.high = high;
  }

  @Override
  protected ROPair<Boolean, ROList<String>> matches1(CharacterEvent event) {
    return new ROPair<>(
        isOrdered(low, event.value) && isOrdered(event.value, high), TSList.of(event.value));
  }
}
