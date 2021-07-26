package com.zarbosoft.merman.core.serialization;

import com.zarbosoft.merman.core.Environment;
import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.rendaw.common.TSList;

import java.util.Iterator;

public class WriteStateDataArray extends WriteState {
    private final Iterator<Atom> iterator;

    public WriteStateDataArray(final TSList<Atom> array) {
        this.iterator = array.iterator();
    }

    @Override
    public void run(Environment env, final TSList<WriteState> stack, final EventConsumer writer) {
        Atom next = iterator.next();
        if (iterator.hasNext()) {
            stack.add(this);
        }
        next.write(stack);
    }
}
