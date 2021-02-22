package com.zarbosoft.merman.editor.visual.attachments;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.display.Drawing;
import com.zarbosoft.merman.editor.display.DrawingContext;
import com.zarbosoft.merman.editor.display.Font;
import com.zarbosoft.merman.editor.visual.Vector;
import com.zarbosoft.merman.editor.wall.Attachment;
import com.zarbosoft.merman.editor.wall.bricks.BrickText;
import com.zarbosoft.merman.syntax.style.ObboxStyle;

public class CursorAttachment {
  public Drawing drawing;
  Vector offset;
  private ObboxStyle style;
  private int index;
  private double startConverse;
  private double startTransverse;
  private double transverseAscent;
  private BrickText brick;
  private final Attachment attachment =
      new Attachment() {
        @Override
        public void setTransverse(final Context context, final double transverse) {
          startTransverse = transverse;
          place();
        }

        @Override
        public void setConverse(final Context context, final double converse) {
          startConverse = converse;
          place();
        }

        @Override
        public void setTransverseSpan(
            final Context context, final double ascent, final double descent) {
          transverseAscent = ascent;
          place();
        }

        @Override
        public void destroy(final Context context) {
          brick = null;
          offset = null;
        }
      };
  public CursorAttachment(final Context context) {
    drawing = context.display.drawing();
    context.overlay.add(drawing);
  }

  private void place() {
    if (offset == null) return;
    drawing.setPosition(
        new Vector(
                startConverse + brick.getConverseOffset(index), startTransverse + transverseAscent)
            .add(offset),
        false);
  }

  public void setPosition(final Context context, final BrickText brick, final int index) {
    if (this.brick != brick) {
      offset = null;
      if (this.brick != null) this.brick.removeAttachment(this.attachment);
      this.brick = brick;
      if (this.brick == null) return;
      this.brick.addAttachment(context, this.attachment);
      redraw(context);
    }
    this.index = index;
    place();
  }

  private void redraw(final Context context) {
    final Font font = brick.getFont();
    final int ascent = (int) (font.getAscent() * 1.8);
    final int descent = (int) (font.getDescent() * 1.8);
    final int halfBuffer = (int) (style.lineThickness / 2 + 0.5);
    final int buffer = halfBuffer * 2;
    final Vector size = new Vector(buffer + 1, ascent + (style.roundStart ? buffer : 0));
    drawing.clear();
    drawing.resize(context, size);
    final DrawingContext gc = drawing.begin(context);
    gc.setLineThickness(style.lineThickness);
    if (style.roundStart) gc.setLineCapRound();
    else gc.setLineCapFlat();
    gc.setLineColor(style.lineColor);
    gc.beginStrokePath();
    gc.moveTo(halfBuffer, halfBuffer);
    gc.lineTo(size.converse - halfBuffer - 1, size.transverse - halfBuffer - 1);
    gc.closePath();
    offset = new Vector(halfBuffer, -ascent + descent - (style.roundStart ? halfBuffer : 0));
  }

  public void destroy(final Context context) {
    if (brick != null) brick.removeAttachment(this.attachment);
    context.overlay.remove(drawing);
  }

  public void setStyle(final Context context, final ObboxStyle style) {
    this.style = style;
    if (brick != null) redraw(context);
  }
}
