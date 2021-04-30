package com.zarbosoft.merman.editorcore.gap;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.display.CourseDisplayNode;
import com.zarbosoft.merman.editorcore.Editor;
import com.zarbosoft.rendaw.common.ROPair;

public abstract class TwoColumnChoice {
  public abstract void choose(final Editor editor);

  /** @return 1st column (preview), 2nd column (name)
   * @param editor*/
  public abstract ROPair<CourseDisplayNode, CourseDisplayNode> display(Editor editor);
}
