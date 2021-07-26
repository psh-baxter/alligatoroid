package com.zarbosoft.merman.core.serialization;

import com.zarbosoft.merman.core.Environment;
import com.zarbosoft.rendaw.common.TSList;

public abstract class WriteState {
    public abstract void run(Environment env, TSList<WriteState> stack, EventConsumer writer);
}
