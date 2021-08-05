package com.zarbosoft.merman.editorcore;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.document.fields.FieldArray;
import com.zarbosoft.merman.core.visual.visuals.CursorAtom;
import com.zarbosoft.merman.core.visual.visuals.CursorFieldArray;
import com.zarbosoft.merman.core.visual.visuals.CursorFieldPrimitive;
import com.zarbosoft.merman.core.visual.visuals.VisualAtom;
import com.zarbosoft.merman.core.visual.visuals.VisualFieldArray;
import com.zarbosoft.merman.core.visual.visuals.VisualFieldPrimitive;
import com.zarbosoft.merman.editorcore.cursors.EditCursorAtom;
import com.zarbosoft.merman.editorcore.cursors.EditCursorFieldArray;
import com.zarbosoft.merman.editorcore.cursors.EditCursorFieldPrimitive;
import com.zarbosoft.merman.editorcore.gap.EditGapCursorFieldPrimitive;

public class EditorCursorFactory implements com.zarbosoft.merman.core.CursorFactory {
  public final Editor editor;

  public EditorCursorFactory(Editor editor) {
    this.editor = editor;
  }

  @Override
  public final CursorFieldPrimitive createFieldPrimitiveCursor(
      Context context,
      VisualFieldPrimitive visualPrimitive,
      boolean leadFirst,
      int beginOffset,
      int endOffset) {
    Atom atom = visualPrimitive.value.atomParentRef.atom();
    if (atom.type == context.syntax.gap || atom.type == context.syntax.suffixGap)
      return createGapCursor(visualPrimitive, leadFirst, beginOffset, endOffset);
    else return createPrimitiveCursor1(context, visualPrimitive, leadFirst, beginOffset, endOffset);
  }

  public EditGapCursorFieldPrimitive createGapCursor(
          VisualFieldPrimitive visualPrimitive, boolean leadFirst, int beginOffset, int endOffset) {
    return new EditGapCursorFieldPrimitive(
        editor, visualPrimitive, leadFirst, beginOffset, endOffset);
  }

  public CursorFieldPrimitive createPrimitiveCursor1(
      Context context,
      VisualFieldPrimitive visualPrimitive,
      boolean leadFirst,
      int beginOffset,
      int endOffset) {
    return new EditCursorFieldPrimitive(
        context, visualPrimitive, leadFirst, beginOffset, endOffset);
  }

  @Override
  public CursorFieldArray createFieldArrayCursor(
      Context context, VisualFieldArray visual, boolean leadFirst, int start, int end) {
    return new EditCursorFieldArray(context, visual, leadFirst, start, end);
  }

  @Override
  public CursorAtom createAtomCursor(Context context, VisualAtom base, int index) {
    return new EditCursorAtom(context, base, index);
  }

  @Override
  public boolean prepSelectEmptyArray(Context context, FieldArray value) {
    Editor editor = Editor.get(context);
    editor.history.record(
        editor,
        null,
        recorder -> {
          editor.arrayInsertNewDefault(recorder, value, 0);
        });
    return true;
  }
}
