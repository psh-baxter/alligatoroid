package com.zarbosoft.merman.editor.serialization;

import java.util.Deque;

public class WriteStateArrayEnd extends WriteState {
    @Override
    public void run(final Deque<WriteState> stack, final EventConsumer writer) {
        writer.arrayEnd();
        stack.removeLast();
    }
}
