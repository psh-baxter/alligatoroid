package com.zarbosoft.merman.editor.visual.visuals;

import com.zarbosoft.merman.editor.Action;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.rendaw.common.TSList;

@Action.StaticID(id = "copy")
class AtomActionCopy extends Action {
    private final VisualFrontAtomBase base;

    public AtomActionCopy(VisualFrontAtomBase base) {
        this.base = base;
    }

    @Override
    public boolean run(final Context context) {
        context.copy(TSList.of(base.atomGet()));
        return true;
    }

}
