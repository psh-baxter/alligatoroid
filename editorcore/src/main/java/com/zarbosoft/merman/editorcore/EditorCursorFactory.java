package com.zarbosoft.merman.editorcore;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.document.fields.FieldArray;
import com.zarbosoft.merman.core.syntax.AtomType;
import com.zarbosoft.merman.core.visual.visuals.CursorFieldPrimitive;
import com.zarbosoft.merman.core.visual.visuals.CursorFieldArray;
import com.zarbosoft.merman.core.visual.visuals.VisualFieldArray;
import com.zarbosoft.merman.core.visual.visuals.VisualFrontPrimitive;
import com.zarbosoft.merman.editorcore.cursors.EditCursorFieldArray;
import com.zarbosoft.merman.editorcore.cursors.EditCursorFieldPrimitive;
import com.zarbosoft.merman.editorcore.gap.EditGapCursorFieldPrimitive;
import com.zarbosoft.merman.editorcore.history.changes.ChangeArray;
import com.zarbosoft.rendaw.common.ROOrderedSetRef;
import com.zarbosoft.rendaw.common.TSList;

public class EditorCursorFactory implements com.zarbosoft.merman.core.CursorFactory {
  public final Editor editor;

  public EditorCursorFactory(Editor editor) {
    this.editor = editor;
  }

  @Override
  public final CursorFieldPrimitive createFieldPrimitiveCursor(
      Context context,
      VisualFrontPrimitive visualPrimitive,
      boolean leadFirst,
      int beginOffset,
      int endOffset) {
    Atom atom = visualPrimitive.value.atomParentRef.atom();
    if (atom.type == context.syntax.gap || atom.type == context.syntax.suffixGap)
      return createGapCursor(visualPrimitive, leadFirst, beginOffset, endOffset);
    else return createPrimitiveCursor1(context, visualPrimitive, leadFirst, beginOffset, endOffset);
  }

  public EditGapCursorFieldPrimitive createGapCursor(
      VisualFrontPrimitive visualPrimitive, boolean leadFirst, int beginOffset, int endOffset) {
    return new EditGapCursorFieldPrimitive(editor, visualPrimitive, leadFirst, beginOffset, endOffset);
  }

  public CursorFieldPrimitive createPrimitiveCursor1(
      Context context,
      VisualFrontPrimitive visualPrimitive,
      boolean leadFirst,
      int beginOffset,
      int endOffset) {
    return new EditCursorFieldPrimitive(context, visualPrimitive, leadFirst, beginOffset, endOffset);
  }

  @Override
  public CursorFieldArray createFieldArrayCursor(
          Context context, VisualFieldArray visual, boolean leadFirst, int start, int end) {
    return new EditCursorFieldArray(context, visual, leadFirst, start, end);
  }

  @Override
  public boolean prepSelectEmptyArray(Context context, FieldArray value) {
    Editor editor = Editor.get(context);
    ROOrderedSetRef<AtomType> candidates =
        context.syntax.splayedTypes.get(value.back().elementAtomType());
    editor.history.record(
        context,
        null,
        recorder -> {
          recorder.apply(
              context,
              new ChangeArray(
                  value,
                  0,
                  0,
                  TSList.of(
                      candidates.size() == 1
                          ? Editor.createEmptyAtom(context.syntax, candidates.iterator().next())
                          : Editor.createEmptyAtom(context.syntax, context.syntax.gap))));
        });
    return true;
  }
}
