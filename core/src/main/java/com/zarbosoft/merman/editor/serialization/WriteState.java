package com.zarbosoft.merman.editor.serialization;

import com.zarbosoft.rendaw.common.TSList;

import java.util.Deque;

public abstract class WriteState {
    public abstract void run(TSList<WriteState> stack, EventConsumer writer);
}
