package com.zarbosoft.merman.editor.visual.visuals;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.merman.editor.Action;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.visual.visuals.VisualNestedBase;

@Action.StaticID(id = "copy")
class AtomActionCopy extends Action {
    private final VisualNestedBase base;

    public AtomActionCopy(VisualNestedBase base) {
        this.base = base;
    }

    @Override
    public boolean run(final Context context) {

        context.copy(ImmutableList.of(base.atomGet()));
        return true;
    }

}
