package com.zarbosoft.merman.editorcore.editing.actions;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.ValueArray;
import com.zarbosoft.merman.editor.Action;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.visual.visuals.VisualFrontAtomBase;
import com.zarbosoft.merman.editorcore.editing.BaseGapAtomType;
import com.zarbosoft.merman.editorcore.editing.EditingExtension;
import com.zarbosoft.merman.editorcore.history.EditAction;
import com.zarbosoft.merman.editorcore.history.changes.ChangeArray;

import java.util.List;

@Action.StaticID(id = "paste")
public class AtomActionPaste extends EditAction {
  private final VisualFrontAtomBase base;

  public AtomActionPaste(EditingExtension edit, VisualFrontAtomBase base) {
    super(edit);
    this.base = base;
  }

  @Override
  public boolean run1(final Context context) {
    final List<Atom> atoms = context.uncopy(base.nodeType());
    if (atoms.isEmpty()) return false;
    if (atoms.size() == 1) {
      edit.atomSet(context, edit.history, base, atoms.get(0));
    } else {
      Atom gap = edit.suffixGap.create();
      edit.atomSet(context, edit.history, base, gap);
      edit.history.apply(
          context,
          new ChangeArray(
              (ValueArray) gap.fields.get(BaseGapAtomType.GAP_PRIMITIVE_KEY), 0, 0, atoms));
    }
    return true;
  }
}
