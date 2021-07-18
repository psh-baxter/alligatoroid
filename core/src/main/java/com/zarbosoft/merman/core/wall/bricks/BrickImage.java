package com.zarbosoft.merman.core.wall.bricks;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.display.DisplayNode;
import com.zarbosoft.merman.core.display.Image;
import com.zarbosoft.merman.core.syntax.style.Style;
import com.zarbosoft.merman.core.wall.Brick;
import com.zarbosoft.merman.core.wall.BrickInterface;

public class BrickImage extends Brick {
  private final Image image;
  private final double toPixels;

  public BrickImage(
      final Context context, final BrickInterface inter, Style.SplitMode splitMode, Style style) {
    super(inter, style, splitMode);
    image = context.display.image();
    image.setImage(context, style.image);
    image.rotate(context, style.rotate);
    converseSpan = (int) image.converseSpan();
    layoutPropertiesChanged(context);
    toPixels = context.toPixels;
  }

  @Override
  public void allocateTransverse(final Context context, final double ascent, final double descent) {
    image.setBaselineTransverse(ascent, false);
  }

  @Override
  public double descent() {
    return image.descent() + style.padding.transverseEnd * toPixels;
  }

  @Override
  public double ascent() {
    if (style.ascent != null) return style.ascent * toPixels;
    return image.ascent() + style.padding.transverseStart * toPixels;
  }

  @Override
  public double converseEdge() {
    return getConverse() + converseSpan;
  }

  @Override
  public double converseSpan() {
    return converseSpan
        + style.padding.converseStart * toPixels
        + style.padding.converseEnd * toPixels;
  }

  @Override
  public double getConverse() {
    return image.converse() - style.padding.converseStart * toPixels;
  }

  @Override
  public DisplayNode getDisplayNode() {
    return image;
  }

  @Override
  public void setConverse(final Context context, final double minConverse, final double converse) {
    this.preAlignConverse = minConverse;
    image.setConverse(converse + style.padding.converseStart * toPixels, false);
  }
}
