package com.zarbosoft.merman.editorcore;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.document.fields.FieldArray;
import com.zarbosoft.merman.core.syntax.AtomType;
import com.zarbosoft.merman.core.visual.visuals.ArrayCursor;
import com.zarbosoft.merman.core.visual.visuals.VisualFrontArray;
import com.zarbosoft.merman.core.visual.visuals.VisualFrontAtomBase;
import com.zarbosoft.merman.core.visual.visuals.VisualFrontPrimitive;
import com.zarbosoft.merman.editorcore.cursors.EditArrayCursor;
import com.zarbosoft.merman.editorcore.cursors.EditAtomCursor;
import com.zarbosoft.merman.editorcore.cursors.EditPrimitiveCursor;
import com.zarbosoft.merman.editorcore.gap.EditGapCursor;
import com.zarbosoft.merman.editorcore.history.changes.ChangeArray;
import com.zarbosoft.rendaw.common.ROSet;
import com.zarbosoft.rendaw.common.TSList;

public class EditorCursorFactory implements com.zarbosoft.merman.core.CursorFactory {
  public final Editor editor;

  public EditorCursorFactory(Editor editor) {
    this.editor = editor;
  }

  @Override
  public VisualFrontPrimitive.Cursor createPrimitiveCursor(
      Context context,
      VisualFrontPrimitive visualPrimitive,
      boolean leadFirst,
      int beginOffset,
      int endOffset) {
    Atom atom = visualPrimitive.value.atomParentRef.atom();
    if (atom.type == context.syntax.gap || atom.type == context.syntax.suffixGap)
      return new EditGapCursor(editor, visualPrimitive, leadFirst, beginOffset, endOffset);
    else
      return new EditPrimitiveCursor(context, visualPrimitive, leadFirst, beginOffset, endOffset);
  }

  @Override
  public ArrayCursor createArrayCursor(
      Context context, VisualFrontArray visual, boolean leadFirst, int start, int end) {
    return new EditArrayCursor(context, visual, leadFirst, start, end);
  }

  @Override
  public VisualFrontAtomBase.Cursor createAtomCursor(Context context, VisualFrontAtomBase base) {
    return new EditAtomCursor(context, base);
  }

  @Override
  public boolean prepSelectEmptyArray(Context context, FieldArray value) {
    Editor editor = Editor.get(context);
    ROSet<AtomType> candidates = context.syntax.splayedTypes.get(value.back().elementAtomType());
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
