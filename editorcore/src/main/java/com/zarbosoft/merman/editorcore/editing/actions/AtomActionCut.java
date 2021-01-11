package com.zarbosoft.merman.editorcore.editing.actions;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.merman.editor.Action;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.visual.visuals.VisualNestedBase;
import com.zarbosoft.merman.editorcore.editing.EditingExtension;
import com.zarbosoft.merman.editorcore.history.EditAction;
import com.zarbosoft.merman.editorcore.history.History;

@Action.StaticID(id = "cut")
public
class AtomActionCut extends EditAction {
    private final VisualNestedBase base;
    private final EditingExtension.AtomSet set;

    public AtomActionCut(History history, VisualNestedBase base, EditingExtension.AtomSet set) {
        super(history);
        this.base = base;
        this.set = set;
    }

    @Override
    public boolean run1(final Context context) {
        context.copy(ImmutableList.of(base.atomGet()));
        set.set(context, context.syntax.gap.create());
        return true;
    }
}
