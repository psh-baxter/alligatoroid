package com.zarbosoft.merman.core;

import com.zarbosoft.merman.core.visual.visuals.VisualFrontArray;
import com.zarbosoft.merman.core.visual.visuals.VisualFrontAtomBase;
import com.zarbosoft.merman.core.visual.visuals.VisualFrontPrimitive;

public interface CursorFactory {
  VisualFrontPrimitive.Cursor createPrimitiveCursor(
      final Context context,
      final VisualFrontPrimitive visualPrimitive,
      final boolean leadFirst,
      final int beginOffset,
      final int endOffset);

  VisualFrontArray.Cursor createArrayCursor(
      final Context context,
      final VisualFrontArray visual,
      final boolean leadFirst,
      final int start,
      final int end);

  VisualFrontAtomBase.Cursor createAtomCursor(final Context context, VisualFrontAtomBase base);
}
