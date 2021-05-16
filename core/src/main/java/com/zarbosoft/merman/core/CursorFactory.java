package com.zarbosoft.merman.core;

import com.zarbosoft.merman.core.document.fields.FieldArray;
import com.zarbosoft.merman.core.visual.visuals.CursorAtom;
import com.zarbosoft.merman.core.visual.visuals.CursorFieldPrimitive;
import com.zarbosoft.merman.core.visual.visuals.CursorFieldArray;
import com.zarbosoft.merman.core.visual.visuals.VisualAtom;
import com.zarbosoft.merman.core.visual.visuals.VisualFieldArray;
import com.zarbosoft.merman.core.visual.visuals.VisualFrontPrimitive;

public interface CursorFactory {
  CursorFieldPrimitive createFieldPrimitiveCursor(
      final Context context,
      final VisualFrontPrimitive visualPrimitive,
      final boolean leadFirst,
      final int beginOffset,
      final int endOffset);

  CursorFieldArray createFieldArrayCursor(
      final Context context,
      final VisualFieldArray visual,
      final boolean leadFirst,
      final int start,
      final int end);

  CursorAtom createAtomCursor(final Context context, VisualAtom base, int index);

  boolean prepSelectEmptyArray(Context context, FieldArray value);
}
