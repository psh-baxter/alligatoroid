package com.zarbosoft.merman.editor.serialization;

import com.zarbosoft.merman.syntax.back.BackSpec;
import com.zarbosoft.rendaw.common.ROMap;
import com.zarbosoft.rendaw.common.TSMap;

import java.util.Deque;
import java.util.Iterator;
import java.util.Map;

public class WriteStateRecord extends WriteState {
    private final TSMap<String, Object> data;
    private final Iterator<Map.Entry<String, BackSpec>> iterator;

    public WriteStateRecord(
            final TSMap<String, Object> data, final ROMap<String, BackSpec> record) {
        this.data = data;
        this.iterator = record.iterator();
    }

    @Override
    public void run(final Deque<WriteState> stack, final EventConsumer writer) {
        if (!iterator.hasNext()) {
            stack.removeLast();
            return;
        }
        final Map.Entry<String, BackSpec> next = iterator.next();
        writer.key(next.getKey());
        BackSpec part = next.getValue();
        part.write(stack, data, writer);
    }
}
