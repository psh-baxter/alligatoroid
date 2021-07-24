package com.zarbosoft.merman.core.visual.visuals;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.visual.Visual;

class HoverableFieldArray extends HoverableFieldArrayBase {
  public int index;

  HoverableFieldArray(final Context context, VisualFieldArray visual) {
    super(visual, context);
  }

  @Override
  public SyntaxPath getSyntaxPath() {
    return visual.value.getSyntaxPath().add(String.valueOf(index));
  }

  @Override
  public void select(final Context context) {
    visual.select(context, true, index, index);
  }

  @Override
  public void notifyRangeAdjusted(
      final Context context, final int index, final int removed, final int added) {
    if (this.index >= index + removed) {
      setIndex(context, this.index - removed + added);
    } else if (this.index >= index) {
      context.clearHover();
    }
  }

  public void setIndex(final Context context, final int index) {
    this.index = index;
    border.setFirst(context, getElementVisual(index).getFirstBrick(context));
    border.setLast(context, getElementVisual(index).getLastBrick(context));
  }

  private Visual getElementVisual(int index) {
    // return visual.children.get(visual.visualIndex(index));
    return visual.value.data.get(index).visual;
  }

  @Override
  public void notifySelected(final Context context, final int start, final int end) {
    if (this.index >= start && this.index <= end) {
      context.clearHover();
    }
  }
}
