package com.zarbosoft.merman.core.editor.wall;

import com.zarbosoft.merman.core.editor.Context;
import com.zarbosoft.merman.core.editor.Hoverable;
import com.zarbosoft.merman.core.editor.display.DisplayNode;
import com.zarbosoft.merman.core.editor.visual.Vector;
import com.zarbosoft.merman.core.editor.visual.VisualLeaf;
import com.zarbosoft.merman.core.editor.visual.alignment.Alignment;
import com.zarbosoft.merman.core.syntax.style.Style;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSSet;

public abstract class Brick {
  public final Style.SplitMode splitMode;
  public final BrickInterface inter;
  public final Style style;
  public Course parent;
  public int index;
  public Alignment alignment;
  public double converse;
  public double converseSpan;
  /** Used to recalc alignment min when a brick is removed from alignment */
  protected double preAlignConverse;

  TSSet<Attachment> attachments = Context.createSet.get();

  protected Brick(final BrickInterface inter, Style style, Style.SplitMode splitMode) {
    this.style = style;
    this.splitMode = splitMode;
    this.inter = inter;
  }

  public boolean isSplit(boolean compact) {
    switch (splitMode) {
      case NEVER:
        return false;
      case COMPACT:
        return compact;
      case ALWAYS:
        return true;
      default:
        throw new Assertion();
    }
  }

  public boolean isSplit() {
    return isSplit(inter.getVisual().atomVisual().compact);
  }

  public abstract double getConverse();

  public abstract double converseEdge();

  public abstract double converseSpan();

  public abstract DisplayNode getDisplayNode();

  public abstract void setConverse(Context context, double minConverse, double converse);

  public final double getPreAlignConverse() {
    return preAlignConverse;
  }

  /**
   * @param context
   * @return A new brick or null (no elements before or brick already exists)
   */
  public Brick createPrevious(final Context context) {
    return inter.createPrevious(context);
  }

  /**
   * @param context
   * @return A new brick or null (no elements afterward or brick already exists)
   */
  public Brick createNext(final Context context) {
    return inter.createNext(context);
  }

  public ROPair<Hoverable, Boolean> hover(final Context context, final Vector point) {
    return inter.getVisual().hover(context, point);
  }

  public VisualLeaf getVisual() {
    return inter.getVisual();
  }

  public void setParent(final Course parent, final int index) {
    this.parent = parent;
    this.index = index;
  }

  public abstract void allocateTransverse(Context context, double ascent, double descent);

  public void addAfter(final Context context, final Brick brick) {
    if (brick.isSplit()) {
      parent.breakCourse(context, index + 1).add(context, 0, TSList.of(brick));
    } else parent.add(context, index + 1, TSList.of(brick));
  }

  public void addBefore(final Context context, final Brick brick) {
    if (isSplit()) {
      if (parent.index == 0) {
        parent.add(context, 0, TSList.of(brick));
        parent.breakCourse(context, 1);
      } else {
        if (brick.isSplit()) {
          final Course previousCourse = parent.parent.children.get(parent.index - 1);
          final int insertIndex = previousCourse.children.size();
          previousCourse.add(context, insertIndex, TSList.of(brick));
          previousCourse.breakCourse(context, insertIndex);
        } else {
          final Course previousCourse = parent.parent.children.get(parent.index - 1);
          previousCourse.add(context, previousCourse.children.size(), TSList.of(brick));
        }
      }
    } else {
      if (index > 0 && brick.isSplit()) {
        parent.breakCourse(context, index).add(context, 0, TSList.of(brick));
      } else parent.add(context, index, TSList.of(brick));
    }
  }

  public Brick previous() {
    if (index == 0) {
      if (parent.index == 0) {
        return null;
      }
      return parent.parent.children.get(parent.index - 1).children.last();
    }
    return parent.children.get(index - 1);
  }

  public Brick next() {
    if (index + 1 == parent.children.size()) {
      if (parent.index + 1 == parent.parent.children.size()) {
        return null;
      }
      return parent.parent.children.get(parent.index + 1).children.get(0);
    }
    return parent.children.get(index + 1);
  }

  /**
   * Call when a layout property of the brick has changed (size, alignment)
   *
   * @param context
   */
  public void changed(final Context context) {
    String alignmentName = isSplit() ? style.splitAlignment : style.alignment;
    if (alignmentName == null) this.alignment = null;
    else this.alignment = inter.findAlignment(alignmentName);
    if (parent != null) parent.changed(context, index);
  }

  public void addAttachment(final Context context, final Attachment attachment) {
    attachments.add(attachment);
    attachment.setConverse(context, getConverse());
    if (parent != null) {
      attachment.setTransverse(context, parent.transverseStart);
      attachment.setTransverseSpan(context, parent.ascent, parent.descent);
    }
  }

  public void removeAttachment(final Attachment attachment) {
    attachments.remove(attachment);
  }

  public TSSet<Attachment> getAttachments() {
    return attachments;
  }

  protected void destroyed(final Context context) {
    inter.brickDestroyed(context);
    if (alignment != null) alignment.removeBrick(context, this);
  }

  public void destroy(final Context context) {
    for (Attachment attachment : attachments.mut()) {
      attachment.destroy(context);
    }
    parent.removeFromSystem(context, index);
    destroyed(context);
  }

  public abstract double descent();

  public abstract double ascent();
}
