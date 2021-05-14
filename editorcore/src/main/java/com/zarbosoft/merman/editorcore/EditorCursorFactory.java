package com.zarbosoft.merman.editorcore;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.document.fields.FieldArray;
import com.zarbosoft.merman.core.syntax.AtomType;
import com.zarbosoft.merman.core.visual.visuals.FieldArrayCursor;
import com.zarbosoft.merman.core.visual.visuals.FieldAtomCursor;
import com.zarbosoft.merman.core.visual.visuals.VisualFieldArray;
import com.zarbosoft.merman.core.visual.visuals.VisualFrontAtomBase;
import com.zarbosoft.merman.core.visual.visuals.VisualFrontPrimitive;
import com.zarbosoft.merman.editorcore.cursors.EditFieldArrayCursor;
import com.zarbosoft.merman.editorcore.cursors.EditFieldAtomCursor;
import com.zarbosoft.merman.editorcore.cursors.EditPrimitiveCursor;
import com.zarbosoft.merman.editorcore.gap.EditGapCursor;
import com.zarbosoft.merman.editorcore.history.changes.ChangeArray;
import com.zarbosoft.rendaw.common.ROOrderedSetRef;
import com.zarbosoft.rendaw.common.TSList;

public class EditorCursorFactory implements com.zarbosoft.merman.core.CursorFactory {
  public final Editor editor;

  public EditorCursorFactory(Editor editor) {
    this.editor = editor;
  }

  @Override
  public final VisualFrontPrimitive.Cursor createPrimitiveCursor(
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

  public EditGapCursor createGapCursor(
      VisualFrontPrimitive visualPrimitive, boolean leadFirst, int beginOffset, int endOffset) {
    return new EditGapCursor(editor, visualPrimitive, leadFirst, beginOffset, endOffset);
  }

  public VisualFrontPrimitive.Cursor createPrimitiveCursor1(
      Context context,
      VisualFrontPrimitive visualPrimitive,
      boolean leadFirst,
      int beginOffset,
      int endOffset) {
    return new EditPrimitiveCursor(context, visualPrimitive, leadFirst, beginOffset, endOffset);
  }

  @Override
  public FieldArrayCursor createArrayCursor(
          Context context, VisualFieldArray visual, boolean leadFirst, int start, int end) {
    return new EditFieldArrayCursor(context, visual, leadFirst, start, end);
  }

  @Override
  public FieldAtomCursor createAtomCursor(Context context, VisualFrontAtomBase base) {
    return new EditFieldAtomCursor(context, base);
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
