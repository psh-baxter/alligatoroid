package com.zarbosoft.merman.core;

import com.zarbosoft.merman.core.document.fields.FieldArray;
import com.zarbosoft.merman.core.visual.visuals.CursorAtom;
import com.zarbosoft.merman.core.visual.visuals.CursorFieldArray;
import com.zarbosoft.merman.core.visual.visuals.CursorFieldPrimitive;
import com.zarbosoft.merman.core.visual.visuals.VisualAtom;
import com.zarbosoft.merman.core.visual.visuals.VisualFieldArray;
import com.zarbosoft.merman.core.visual.visuals.VisualFrontPrimitive;

public class ViewerCursorFactory implements CursorFactory {
  @Override
  public CursorFieldPrimitive createFieldPrimitiveCursor(
      Context context,
      VisualFrontPrimitive visualPrimitive,
      boolean leadFirst,
      int beginOffset,
      int endOffset) {
    return new CursorFieldPrimitive(context, visualPrimitive, leadFirst, beginOffset, endOffset);
  }

  @Override
  public CursorFieldArray createFieldArrayCursor(
      Context context, VisualFieldArray visual, boolean leadFirst, int start, int end) {
    return new CursorFieldArray(context, visual, leadFirst, start, end);
  }

  @Override
  public CursorAtom createAtomCursor(Context context, VisualAtom base, int index) {
    return new CursorAtom(context, base, index);
  }

  @Override
  public boolean prepSelectEmptyArray(Context context, FieldArray value) {
    return false;
  }
}
