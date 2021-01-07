package com.zarbosoft.merman.editor.gap;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.display.DisplayNode;
import com.zarbosoft.rendaw.common.Pair;

public abstract class TwoColumnChoice {
  public abstract void choose(final Context context);

  /**
   *
   * @return 1st column (preview), 2nd column (name)
   */
  public abstract Pair<DisplayNode, DisplayNode> display(Context context);
}
