package com.zarbosoft.merman.editor.wall;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.Hoverable;
import com.zarbosoft.merman.editor.display.DisplayNode;
import com.zarbosoft.merman.editor.visual.Alignment;
import com.zarbosoft.merman.editor.visual.AlignmentListener;
import com.zarbosoft.merman.editor.visual.Vector;
import com.zarbosoft.merman.editor.visual.VisualLeaf;
import com.zarbosoft.merman.editor.visual.tags.TagsChange;
import com.zarbosoft.merman.syntax.style.Style;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSSet;

public abstract class Brick implements AlignmentListener {
  public Course parent;
  public int index;
  TSSet<Attachment> attachments = Context.createSet.get();
  public Style style;
  public Alignment alignment;
  public int minConverse;
  public final BrickInterface inter;

  protected Brick(final BrickInterface inter) {
    this.inter = inter;
  }

  public abstract int getConverse(Context context);

  public abstract int converseEdge(final Context context);

  public abstract DisplayNode getDisplayNode();

  public abstract void setConverse(Context context, int minConverse, int converse);

  public abstract void tagsChanged(Context context);

  public abstract Properties properties(final Context context, final Style style);

  @Override
  public final void align(final Context context) {
    changed(context);
  }

  @Override
  public final int getConverseLowerBound(final Context context) {
    return minConverse;
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

  protected Style getStyle() {
    return style;
  }

  public TSSet<String> getTags(final Context context) {
    return inter.getTags(context);
  }

  public Properties properties(final Context context) {
    return properties(context, getStyle());
  }

  public Properties getPropertiesForTagsChange(final Context context, final TagsChange change) {
    TSSet<String> tags = inter.getTags(context).mut();
    change.apply(tags);
    return properties(context, context.getStyle(tags.ro()));
  }

  public Hoverable hover(final Context context, final Vector point) {
    return inter.getVisual().hover(context, point);
  }

  public VisualLeaf getVisual() {
    return inter.getVisual();
  }

  public void setParent(final Course parent, final int index) {
    this.parent = parent;
    this.index = index;
  }

  public static class Properties {
    public final boolean split;
    public final int ascent;
    public final int descent;
    public final Alignment alignment;
    public final int converseSpan;

    public Properties(
        final boolean split,
        final int ascent,
        final int descent,
        final Alignment alignment,
        final int converseSpan) {
      this.split = split;
      this.ascent = ascent;
      this.descent = descent;
      this.alignment = alignment;
      this.converseSpan = converseSpan;
    }
  }

  public abstract void allocateTransverse(Context context, int ascent, int descent);

  public void addAfter(final Context context, final Brick brick) {
    final Properties properties = brick.properties(context);
    if (properties.split) {
      parent.breakCourse(context, index + 1).add(context, 0, TSList.of(brick));
    } else parent.add(context, index + 1, TSList.of(brick));
  }

  public void addBefore(final Context context, final Brick brick) {
    if (properties(context).split) {
      if (parent.index == 0) {
        parent.add(context, 0, TSList.of(brick));
        parent.breakCourse(context, 1);
      } else {
        if (brick.properties(context).split) {
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
      if (index > 0 && brick.properties(context).split) {
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
    if (parent != null) parent.changed(context, index);
  }

  public void addAttachment(final Context context, final Attachment attachment) {
    attachments.add(attachment);
    attachment.setConverse(context, getConverse(context));
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
    if (alignment != null) alignment.removeListener(context, this);
  }

  public void destroy(final Context context) {
    for (Attachment attachment : attachments.mut()) {
      attachment.destroy(context);
    }
    parent.removeFromSystem(context, index);
    destroyed(context);
  }
}
