package com.zarbosoft.merman.editor.visual.attachments;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.display.derived.Obbox;
import com.zarbosoft.merman.editor.wall.Attachment;
import com.zarbosoft.merman.editor.wall.Brick;
import com.zarbosoft.merman.syntax.style.ObboxStyle;

public class BorderAttachment {
  private final Obbox border;
  Brick first;
  Brick last;
  private double startConverse;
  private double startTransverse;
  private double startTransverseSpan;
  private double endTransverse;
  private double endTransverseSpan;
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
          // Either brick is destroyed with border (nested placeholder)
          // or used with another attachment that resets the brick when destroyed
        }
      };

  public BorderAttachment(final Context context, final ObboxStyle style) {
    border = new Obbox(context);
    border.setStyle(style);
    context.background.add(border.drawing);
  }

  public void setFirst(final Context context, final Brick first) {
    if (this.first != null) this.first.removeAttachment(this.firstAttachment);
    this.first = first;
    if (first == null) return;
    this.first.addAttachment(context, this.firstAttachment);
  }

  public void setLast(final Context context, final Brick last) {
    if (this.last != null) this.last.removeAttachment(this.lastAttachment);
    this.last = last;
    if (last == null) return;
    this.last.addAttachment(context, this.lastAttachment);
  }

  public void destroy(final Context context) {
    if (first != null) this.first.removeAttachment(this.firstAttachment);
    if (last != null) this.last.removeAttachment(this.lastAttachment);
    context.background.remove(border.drawing);
  }

  public void redraw(final Context context) {
    if (first == null) return;
    if (last == null) return;
    border.setSize(
        context,
        startConverse,
        startTransverse,
        startTransverse + startTransverseSpan,
        last.converseEdge(),
        endTransverse,
        endTransverse + endTransverseSpan);
  }

  public void setStyle(final Context context, final ObboxStyle style) {
    border.setStyle(style);
    redraw(context);
  }
}
