package com.zarbosoft.merman.editorcore.syntaxgap.actions;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.ValueArray;
import com.zarbosoft.merman.editor.Action;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.visual.visuals.VisualNestedBase;
import com.zarbosoft.merman.editorcore.editing.EditingExtension;
import com.zarbosoft.merman.editorcore.history.EditAction;
import com.zarbosoft.merman.editorcore.history.History;
import com.zarbosoft.merman.editorcore.history.changes.ChangeArray;

@Action.StaticID(id = "suffix")
public class AtomActionSuffix extends EditAction {
  private final VisualNestedBase base;
  private final EditingExtension.AtomSet set;

  public AtomActionSuffix(History history, VisualNestedBase base, EditingExtension.AtomSet set) {
    super(history);
    this.base = base;
    this.set = set;
  }

  @Override
  public boolean run1(final Context context) {
    final Atom old = base.atomGet();
    final Atom gap = context.syntax.suffix.create();
    set.set(context, gap);
    history.apply(
        context,
        new ChangeArray((ValueArray) gap.fields.getOpt("value"), 0, 0, ImmutableList.of(old)));
    gap.fields.getOpt("gap").selectDown(context);
    return true;
  }
}
