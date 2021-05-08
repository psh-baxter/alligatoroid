package com.zarbosoft.merman.editorcore.gap;

import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.syntax.AtomType;
import com.zarbosoft.pidgoon.events.Event;
import com.zarbosoft.pidgoon.events.nodes.Terminal;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.ROSet;

public class AtomTypeMatch extends Terminal<AtomEvent, Atom> {
    public final ROSet<AtomType> type;

    AtomTypeMatch(ROSet<AtomType> type) {
        this.type = type;
    }

    @Override
    protected ROPair<Boolean, Atom> matches(AtomEvent event) {
        return new ROPair<>(type.contains(((AtomEvent) event).atom.type), event.atom);
    }
}
