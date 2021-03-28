package com.zarbosoft.merman.core.editor.visual.visuals;

import com.zarbosoft.merman.core.editor.Action;
import com.zarbosoft.merman.core.editor.Context;
import com.zarbosoft.rendaw.common.TSList;

class AtomActionCopy implements Action {
    public String id() {
        return "copy";
    }
    private final VisualFrontAtomBase base;

    public AtomActionCopy(VisualFrontAtomBase base) {
        this.base = base;
    }

    @Override
    public void run(final Context context) {
        context.copy(TSList.of(base.atomGet()));
    }
}
