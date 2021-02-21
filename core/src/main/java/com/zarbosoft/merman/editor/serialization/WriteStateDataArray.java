package com.zarbosoft.merman.editor.serialization;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.rendaw.common.TSList;

import java.util.Iterator;
import java.util.List;

public class WriteStateDataArray extends WriteState {
    private final Iterator<Atom> iterator;

    public WriteStateDataArray(final TSList<Atom> array) {
        this.iterator = array.iterator();
    }

    @Override
    public void run(final TSList<WriteState> stack, final EventConsumer writer) {
        Atom next = iterator.next();
        if (iterator.hasNext()) {
            stack.add(this);
        }
        next.write(stack);
    }
}
