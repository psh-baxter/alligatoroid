package com.zarbosoft.merman.core.visual.visuals;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.Hoverable;
import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.visual.Visual;
import com.zarbosoft.merman.core.visual.attachments.BorderAttachment;

public class AtomHoverable extends Hoverable {
  public final VisualAtom visual;
  final BorderAttachment border;
  public int index;

  AtomHoverable(VisualAtom visual, final Context context) {
    this.visual = visual;
    border = new BorderAttachment(context, context.syntax.hoverStyle.obbox);
  }

  @Override
  protected void clear(final Context context) {
    border.destroy(context);
    if (visual.hoverable == this) visual.hoverable = null;
  }

  @Override
  public Visual visual() {
    return visual;
  }

  @Override
  public SyntaxPath getSyntaxPath() {
    return visual.atom.getSyntaxPath().add(String.valueOf(index));
  }

  @Override
  public void select(final Context context) {
    visual.select(context, index);
  }

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
    border.setFirst(context, visual.selectable.get(index).second.getFirstBrick(context));
    border.setLast(context, visual.selectable.get(index).second.getLastBrick(context));
  }

  public void notifySelected(final Context context, final int index) {
    if (this.index == index) {
      context.clearHover();
    }
  }
}
