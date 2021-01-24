package com.zarbosoft.merman.editor.serialization;

import java.util.Deque;

public abstract class WriteState {
    public abstract void run(Deque<WriteState> stack, EventConsumer writer);
}
