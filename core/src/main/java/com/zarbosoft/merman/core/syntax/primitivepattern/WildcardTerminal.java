package com.zarbosoft.merman.core.syntax.primitivepattern;

import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSList;

public class WildcardTerminal extends BaseTerminal {
  protected WildcardTerminal(boolean capture) {
    super(capture);
  }

  @Override
  protected ROPair<Boolean, ROList<String>> matches1(CharacterEvent event) {
    return new ROPair<>(true, TSList.of(event.value));
  }
}
