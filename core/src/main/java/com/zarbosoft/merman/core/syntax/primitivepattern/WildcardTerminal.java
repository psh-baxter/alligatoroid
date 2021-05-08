package com.zarbosoft.merman.core.syntax.primitivepattern;

import com.zarbosoft.pidgoon.events.nodes.Terminal;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSList;

public class WildcardTerminal extends Terminal<CharacterEvent, ROList<String>> {
    @Override
    protected ROPair<Boolean, ROList<String>> matches(CharacterEvent event) {
        return new ROPair<>(true, TSList.of(event.value));
    }
}
