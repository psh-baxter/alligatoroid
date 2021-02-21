package com.zarbosoft.merman.editorcore.editing.actions;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.visual.visuals.VisualFrontArray;
import com.zarbosoft.merman.editorcore.editing.EditingExtension;
import com.zarbosoft.merman.editorcore.history.EditAction;
import com.zarbosoft.merman.editorcore.history.changes.ChangeArray;

public class ArrayActionPaste extends EditAction {
  private final VisualFrontArray.ArrayCursor cursor;

  public ArrayActionPaste(EditingExtension edit, VisualFrontArray.ArrayCursor cursor) {
    super(edit);
    this.cursor = cursor;
  }

  @Override
  public void run1(final Context context) {
    context.uncopy(
        cursor.visual.value.back().elementAtomType(),
        atoms -> {
          if (atoms.isEmpty()) return;
          edit.history.apply(
              context,
              new ChangeArray(
                  cursor.visual.value,
                  cursor.beginIndex,
                  cursor.endIndex - cursor.beginIndex + 1,
                  atoms));
        });
  }

  @Override
  public String id() {
    return "paste";
  }
}
