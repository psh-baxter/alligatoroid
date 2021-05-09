package com.zarbosoft.merman.core.visual.visuals;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.CursorState;
import com.zarbosoft.merman.core.document.fields.FieldArray;

public class ArrayCursorState implements CursorState {
    private final FieldArray value;
    private final int start;
    private final int end;
    private final boolean leadFirst;

    public ArrayCursorState(
            final FieldArray value, final boolean leadFirst, final int start, final int end) {
        this.value = value;
        this.leadFirst = leadFirst;
        this.start = start;
        this.end = end;
    }

    @Override
    public void select(final Context context) {
        value.selectInto(context, leadFirst, start, end);
    }
}
