package com.zarbosoft.merman.editorcore.gap;

import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.pidgoon.events.Event;

public class AtomEvent implements Event {
    public final Atom atom;

    AtomEvent(Atom atom) {
        this.atom = atom;
    }
}
