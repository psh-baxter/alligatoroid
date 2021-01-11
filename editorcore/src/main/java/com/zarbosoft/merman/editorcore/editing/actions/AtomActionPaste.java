package com.zarbosoft.merman.editorcore.editing.actions;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.editor.Action;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.visual.visuals.VisualNestedBase;
import com.zarbosoft.merman.editorcore.editing.EditingExtension;
import com.zarbosoft.merman.editorcore.history.EditAction;
import com.zarbosoft.merman.editorcore.history.History;

import java.util.List;

@Action.StaticID(id = "paste")
public class AtomActionPaste extends EditAction {
  private final VisualNestedBase base;
  private final EditingExtension.AtomSet set;

  public AtomActionPaste(History history, VisualNestedBase base, EditingExtension.AtomSet set) {
    super(history);
    this.base = base;
    this.set = set;
  }

  @Override
  public boolean run1(final Context context) {
    final List<Atom> atoms = context.uncopy(base.nodeType());
    if (atoms.size() != 1) return false;
    set.set(context, atoms.get(0));
    return true;
  }
}
