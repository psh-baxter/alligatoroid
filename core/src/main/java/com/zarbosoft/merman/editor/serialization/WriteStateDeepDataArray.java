package com.zarbosoft.merman.editor.serialization;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.syntax.back.BackSpec;
import com.zarbosoft.rendaw.common.ROMap;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;

import java.util.Iterator;
import java.util.List;

public class WriteStateDeepDataArray extends WriteState {
    private final Iterator<Atom> iterator;
    private final ROMap<String, BackSpec> boilerplate;

    public WriteStateDeepDataArray(final TSList<Atom> values, final ROMap<String, BackSpec> boilerplate) {
        this.iterator = values.iterator();
        this.boilerplate = boilerplate;
    }

    @Override
    public void run(final TSList<WriteState> stack, final EventConsumer writer) {
        final Atom next = iterator.next();
        BackSpec nextPlate = boilerplate.get(next.type.id());
        if (nextPlate != null) {
            nextPlate.write(stack, new TSMap<String, Object>().putNull(null, next), writer);
        } else {
            next.write(stack);
        }
        if (iterator.hasNext()) {
            stack.add(this);
        }
    }
}
