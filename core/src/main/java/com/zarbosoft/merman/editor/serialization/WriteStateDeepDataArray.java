package com.zarbosoft.merman.editor.serialization;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.syntax.back.BackSpec;
import com.zarbosoft.rendaw.common.TSMap;

import java.util.Deque;
import java.util.Iterator;
import java.util.List;

public class WriteStateDeepDataArray extends WriteState {
    private final Iterator<Atom> iterator;
    private final String elementKey;
    private final BackSpec element;

    public WriteStateDeepDataArray(BackSpec element, String elementKey, final List<Atom> array) {
        this.element = element;
        this.elementKey = elementKey;
        this.iterator = array.iterator();
    }

    @Override
    public void run(final Deque<WriteState> stack, final EventConsumer writer) {
        if (!iterator.hasNext()) {
            stack.removeLast();
            return;
        }
        final Atom next = iterator.next();
        element.write(stack, new TSMap<String, Object>().put(elementKey, next), writer);
    }
}
