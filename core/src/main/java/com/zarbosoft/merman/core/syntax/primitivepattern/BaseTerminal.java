package com.zarbosoft.merman.core.syntax.primitivepattern;

import com.zarbosoft.pidgoon.events.EscapableResult;
import com.zarbosoft.pidgoon.events.nodes.Terminal;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROPair;

public abstract class BaseTerminal extends Terminal<Object, EscapableResult<ROList<String>>> {
  private final boolean capture;

  protected BaseTerminal(boolean capture) {
    this.capture = capture;
  }

  @Override
  protected final ROPair<Boolean, EscapableResult<ROList<String>>> matches(Object event) {
    if (event instanceof ForceEndCharacterEvent)
      return new ROPair<>(true, new EscapableResult<>(false, false, ROList.empty));
    if (!(event instanceof CharacterEvent))
      return new ROPair<>(false, new EscapableResult<>(false, true, ROList.empty));
    ROPair<Boolean, ROList<String>> subMatch = matches1((CharacterEvent) event);
    return new ROPair<>(
        subMatch.first, new EscapableResult<>(true, true, capture ? subMatch.second : ROList.empty));
  }

  protected abstract ROPair<Boolean, ROList<String>> matches1(CharacterEvent event);
}
