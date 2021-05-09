package com.zarbosoft.merman.core.syntax.primitivepattern;

import com.zarbosoft.pidgoon.events.EscapableResult;
import com.zarbosoft.pidgoon.events.nodes.Terminal;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSList;

public class CharacterTerminal extends BaseTerminal {
    final String exact;

    protected CharacterTerminal(boolean capture, String exact) {
        super(capture);
        this.exact = exact;
    }

    @Override
    protected ROPair<Boolean, ROList<String>> matches1(CharacterEvent event) {
        return new ROPair<>(exact.equals(event.value), TSList.of(event.value));
    }
}
