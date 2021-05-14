package com.zarbosoft.merman.core;

import com.zarbosoft.merman.core.document.fields.FieldArray;
import com.zarbosoft.merman.core.visual.visuals.FieldArrayCursor;
import com.zarbosoft.merman.core.visual.visuals.FieldAtomCursor;
import com.zarbosoft.merman.core.visual.visuals.VisualFieldArray;
import com.zarbosoft.merman.core.visual.visuals.VisualFrontAtomBase;
import com.zarbosoft.merman.core.visual.visuals.VisualFrontPrimitive;

public interface CursorFactory {
  VisualFrontPrimitive.Cursor createPrimitiveCursor(
      final Context context,
      final VisualFrontPrimitive visualPrimitive,
      final boolean leadFirst,
      final int beginOffset,
      final int endOffset);

  FieldArrayCursor createArrayCursor(
      final Context context,
      final VisualFieldArray visual,
      final boolean leadFirst,
      final int start,
      final int end);

  FieldAtomCursor createAtomCursor(final Context context, VisualFrontAtomBase base);

  boolean prepSelectEmptyArray(Context context, FieldArray value);
}
