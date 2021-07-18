package com.zarbosoft.merman.core.wall.bricks;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.display.DisplayNode;
import com.zarbosoft.merman.core.display.Font;
import com.zarbosoft.merman.core.display.Text;
import com.zarbosoft.merman.core.syntax.style.ModelColor;
import com.zarbosoft.merman.core.syntax.style.Style;
import com.zarbosoft.merman.core.visual.Vector;
import com.zarbosoft.merman.core.wall.Brick;
import com.zarbosoft.merman.core.wall.BrickInterface;

public class BrickText extends Brick {
  private final Font font;
  private final double toPixels;
  public Text text;

  public BrickText(
      final Context context,
      final BrickInterface inter,
      Style.SplitMode splitMode,
      Style style,
      ModelColor initialColor,
      int hackAvoidChanged) {
    super(inter, style, splitMode);
    toPixels = context.toPixels;
    text = context.display.text();
    text.setColor(context, initialColor);
    this.font = Context.getFont(context, style);
    text.setFont(context, font);
  }

  public BrickText(
      final Context context, final BrickInterface inter, Style.SplitMode splitMode, Style style) {
    this(context, inter, splitMode, style, style.color, 0);
    layoutPropertiesChanged(context);
  }

  public void setColor(Context context, ModelColor color) {
    text.setColor(context, color);
  }

  @Override
  public double descent() {
    return text.descent() + style.padding.transverseEnd * toPixels;
  }

  @Override
  public double ascent() {
    if (style.ascent != null) return style.ascent * toPixels;
    return text.ascent() + style.padding.converseStart * toPixels;
  }

  @Override
  public double converseEdge() {
    return getConverse() + converseSpan;
  }

  @Override
  public double converseSpan() {
    return converseSpan;
  }

  @Override
  public DisplayNode getDisplayNode() {
    return text;
  }

  @Override
  public void setConverse(final Context context, final double minConverse, final double converse) {
    this.preAlignConverse = minConverse;
    text.setConverse(style.padding.converseStart * toPixels + converse);
  }

  @Override
  public void allocateTransverse(final Context context, final double ascent, final double descent) {
    text.setBaselineTransverse(ascent);
  }

  public void setText(final Context context, final String text) {
    this.text.setText(context, text.replaceAll("\\p{Cntrl}", context.syntax.unprintable));
    this.converseSpan =
        font.measurer().getWidth(this.text.text())
            + style.padding.converseStart * toPixels
            + style.padding.converseEnd * toPixels;
    layoutPropertiesChanged(context);
  }

  @Override
  public double getConverse() {
    return text.converse() - style.padding.converseStart * toPixels;
  }

  public Font getFont() {
    return text.font();
  }

  public double getConverseOffset(final int index) {
    return text.getConverseAtIndex(index);
  }

  public int getUnder(final Context context, final Vector point) {
    return text.getIndexAtConverse(context, point.converse);
  }
}
