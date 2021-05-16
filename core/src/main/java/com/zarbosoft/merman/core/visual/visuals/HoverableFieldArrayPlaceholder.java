package com.zarbosoft.merman.core.visual.visuals;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.wall.Brick;
import com.zarbosoft.rendaw.common.DeadCode;

class HoverableFieldArrayPlaceholder extends HoverableFieldArray {
  public final VisualFieldArray visual;

  HoverableFieldArrayPlaceholder(final Context context, final Brick brick, VisualFieldArray visual) {
    super(visual, context);
    this.visual = visual;
    border.setFirst(context, brick);
    border.setLast(context, brick);
  }

  @Override
  public SyntaxPath getSyntaxPath() {
    return visual.value.getSyntaxPath().add("0");
  }

  @Override
  public void select(final Context context) {
    visual.select(context, true, 0, 0);
  }

  @Override
  public void notifyRangeAdjusted(
      final Context context, final int index, final int removed, final int added) {
    throw new DeadCode();
  }

  @Override
  public void notifySelected(final Context context, final int start, final int end) {
    context.clearHover();
  }
}
