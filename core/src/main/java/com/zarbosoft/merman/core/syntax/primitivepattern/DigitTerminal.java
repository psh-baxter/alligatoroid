package com.zarbosoft.merman.core.syntax.primitivepattern;

import com.zarbosoft.pidgoon.events.nodes.Terminal;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSList;

public class DigitTerminal extends Terminal<CharacterEvent, ROList<String>> {
    @Override
    protected ROPair<Boolean, ROList<String>> matches(CharacterEvent event) {
        if (event.value.length() != 1) return new ROPair<>(false, null);
        char c = event.value.charAt(0);
        return new ROPair<>(c >= '0' && c <= '9', TSList.of(event.value));
    }
}
