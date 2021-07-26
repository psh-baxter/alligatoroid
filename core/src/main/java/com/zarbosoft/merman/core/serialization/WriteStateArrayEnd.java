package com.zarbosoft.merman.core.serialization;

import com.zarbosoft.merman.core.Environment;
import com.zarbosoft.rendaw.common.TSList;

public class WriteStateArrayEnd extends WriteState {
    @Override
    public void run(Environment env, final TSList<WriteState> stack, final EventConsumer writer) {
        writer.arrayEnd();
    }
}
