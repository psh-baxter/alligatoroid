package com.zarbosoft.merman.core.editor.serialization;

import com.zarbosoft.rendaw.common.TSList;

public abstract class WriteState {
    public abstract void run(TSList<WriteState> stack, EventConsumer writer);
}
