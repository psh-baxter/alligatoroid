package com.zarbosoft.merman.editorcore.displayderived;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.display.FreeDisplayNode;

/**
 * A container occupies a space and allocates that space to children. setConverseSpan will always be
 * called once immediately, so initial layout can happen there.
 */
public interface Container extends FreeDisplayNode {
  public void setConverseSpan(Context context, double span);
}
