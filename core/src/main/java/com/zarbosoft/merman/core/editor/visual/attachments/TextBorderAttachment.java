package com.zarbosoft.merman.core.editor.visual.attachments;

import com.zarbosoft.merman.core.editor.Context;
import com.zarbosoft.merman.core.editor.display.derived.Obbox;
import com.zarbosoft.merman.core.editor.wall.Attachment;
import com.zarbosoft.merman.core.editor.wall.bricks.BrickText;
import com.zarbosoft.merman.core.syntax.style.ObboxStyle;

public class TextBorderAttachment {
  private final Obbox border;
  BrickText first;
  int firstIndex;
  BrickText last;
  int lastIndex;
  private double startConverse;
  private double startTransverse;
  private double startTransverseSpan;
  private double endConverse;
  private double endTransverse;
  private double endTransverseSpan;
  private boolean blockRedraw = false;
  private final Attachment firstAttachment =
      new Attachment() {
        @Override
        public void setTransverse(final Context context, final double transverse) {
          startTransverse = transverse;
          redraw(context);
        }

        @Override
        public void setConverse(final Context context, final double converse) {
          startConverse = converse;
          redraw(context);
        }

        @Override
        public void setTransverseSpan(
            final Context context, final double ascent, final double descent) {
          startTransverseSpan = ascent + descent;
          redraw(context);
        }

        @Override
        public void destroy(final Context context) {
          first = null;
        }
      };
  private final Attachment lastAttachment =
      new Attachment() {
        @Override
        public void setTransverse(final Context context, final double transverse) {
          endTransverse = transverse;
          redraw(context);
        }

        @Override
        public void setConverse(final Context context, final double converse) {
          endConverse = converse;
          redraw(context);
        }

        @Override
        public void setTransverseSpan(
            final Context context, final double ascent, final double descent) {
          endTransverseSpan = ascent + descent;
          redraw(context);
        }

        @Override
        public void destroy(final Context context) {
          last = null;
        }
      };

  public TextBorderAttachment(final Context context) {
    border = new Obbox(context);
    context.background.add(border.drawing);
  }

  public void setBoth(
      final Context context,
      final BrickText first,
      final int firstIndex,
      final BrickText last,
      final int lastIndex) {
    if (this.first != null && this.first != first)
      this.first.removeAttachment(this.firstAttachment);
    if (this.last != null && this.last != last) this.last.removeAttachment(this.lastAttachment);
    this.first = first;
    this.last = last;
    if (firstIndex < 0) throw new AssertionError();
    this.firstIndex = firstIndex;
    if (lastIndex < 0) throw new AssertionError();
    this.lastIndex = lastIndex;
    blockRedraw = true;
    if (first != null) this.first.addAttachment(context, this.firstAttachment);
    if (last != null) this.last.addAttachment(context, this.lastAttachment);
    blockRedraw = false;
    redraw(context);
  }

  public void destroy(final Context context) {
    if (first != null) first.removeAttachment(this.firstAttachment);
    if (last != null) last.removeAttachment(this.lastAttachment);
    context.background.remove(border.drawing);
  }

  public void redraw(final Context context) {
    if (first == null) return;
    if (last == null) return;
    if (blockRedraw) return;
    border.setSize(
        context,
        startConverse + first.getConverseOffset(firstIndex),
        startTransverse,
        startTransverse + startTransverseSpan,
        endConverse + last.getConverseOffset(lastIndex),
        endTransverse,
        endTransverse + endTransverseSpan);
  }

  public void setStyle(final Context context, final ObboxStyle style) {
    border.setStyle(style);
    redraw(context);
  }
}
