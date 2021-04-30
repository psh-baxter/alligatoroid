package com.zarbosoft.merman.core;

import com.zarbosoft.merman.core.visual.visuals.VisualFrontArray;
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
  public VisualFrontArray.Cursor createArrayCursor(
      Context context, VisualFrontArray visual, boolean leadFirst, int start, int end) {
    return new VisualFrontArray.Cursor(context, visual, leadFirst, start, end);
  }

  @Override
  public VisualFrontAtomBase.Cursor createAtomCursor(Context context, VisualFrontAtomBase base) {
    return new VisualFrontAtomBase.Cursor(context, base);
  }
}
