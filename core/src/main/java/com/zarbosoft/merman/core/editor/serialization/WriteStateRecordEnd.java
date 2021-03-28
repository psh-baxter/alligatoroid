package com.zarbosoft.merman.core.editor.serialization;

import com.zarbosoft.rendaw.common.TSList;

public class WriteStateRecordEnd extends WriteState {
    @Override
    public void run(final TSList<WriteState> stack, final EventConsumer writer) {
        writer.recordEnd();
    }
}
