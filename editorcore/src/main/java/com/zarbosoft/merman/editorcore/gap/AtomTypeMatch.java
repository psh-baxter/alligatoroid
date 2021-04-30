package com.zarbosoft.merman.editorcore.gap;

import com.zarbosoft.merman.core.syntax.AtomType;
import com.zarbosoft.pidgoon.events.Event;
import com.zarbosoft.pidgoon.events.nodes.Terminal;
import com.zarbosoft.pidgoon.model.Store;
import com.zarbosoft.rendaw.common.ROSet;

public class AtomTypeMatch extends Terminal {
    public final ROSet<AtomType> type;

    AtomTypeMatch(ROSet<AtomType> type) {
        this.type = type;
    }

    @Override
    protected boolean matches(Event event, Store store) {
        return type.contains(((AtomEvent) event).atom.type);
    }
}
