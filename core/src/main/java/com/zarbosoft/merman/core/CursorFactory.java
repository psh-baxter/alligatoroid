package com.zarbosoft.merman.core;

import com.zarbosoft.merman.core.document.fields.FieldArray;
import com.zarbosoft.merman.core.visual.visuals.ArrayCursor;
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

  ArrayCursor createArrayCursor(
      final Context context,
      final VisualFrontArray visual,
      final boolean leadFirst,
      final int start,
      final int end);

  VisualFrontAtomBase.Cursor createAtomCursor(final Context context, VisualFrontAtomBase base);

  boolean prepSelectEmptyArray(Context context, FieldArray value);
}
