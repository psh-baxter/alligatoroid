package com.zarbosoft.merman.core;

import com.zarbosoft.merman.core.document.fields.FieldArray;
import com.zarbosoft.merman.core.visual.visuals.FieldArrayCursor;
import com.zarbosoft.merman.core.visual.visuals.FieldAtomCursor;
import com.zarbosoft.merman.core.visual.visuals.VisualFieldArray;
import com.zarbosoft.merman.core.visual.visuals.VisualFrontAtomBase;
import com.zarbosoft.merman.core.visual.visuals.VisualFrontPrimitive;

public class ViewerCursorFactory implements CursorFactory {
  @Override
  public VisualFrontPrimitive.Cursor createPrimitiveCursor(
      Context context,
      VisualFrontPrimitive visualPrimitive,
      boolean leadFirst,
      int beginOffset,
      int endOffset) {
    return new VisualFrontPrimitive.Cursor(
            context, visualPrimitive, leadFirst, beginOffset, endOffset);
  }

  @Override
  public FieldArrayCursor createArrayCursor(
          Context context, VisualFieldArray visual, boolean leadFirst, int start, int end) {
    return new FieldArrayCursor(context, visual, leadFirst, start, end);
  }

  @Override
  public FieldAtomCursor createAtomCursor(Context context, VisualFrontAtomBase base) {
    return new FieldAtomCursor(context, base);
  }

  @Override
  public boolean prepSelectEmptyArray(Context context, FieldArray value) {
    return false;
  }
}
