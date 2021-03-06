package com.zarbosoft.merman.core.visual;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.Hoverable;
import com.zarbosoft.merman.core.visual.visuals.VisualAtom;
import com.zarbosoft.rendaw.common.ROPair;

public interface VisualLeaf {
  /**
   * @param context
   * @param point
   * @return hoverable, if it's a mutable hoverable whether the relevant state changed
   */
  ROPair<Hoverable, Boolean> hover(
      final Context context, final Vector point); // Should map to method in Visual

  VisualParent parent(); // Should map to method in Visual

  default VisualAtom atomVisual() {
    return parent().atomVisual();
  }
}
