package com.zarbosoft.merman.core.editor.wall.bricks;

import com.zarbosoft.merman.core.editor.Context;
import com.zarbosoft.merman.core.editor.display.DisplayNode;
import com.zarbosoft.merman.core.editor.display.Image;
import com.zarbosoft.merman.core.editor.wall.Brick;
import com.zarbosoft.merman.core.editor.wall.BrickInterface;
import com.zarbosoft.merman.core.syntax.style.Style;

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
    changed(context);
    toPixels = context.toPixels;
  }

  @Override
  public void allocateTransverse(final Context context, final double ascent, final double descent) {
    image.setBaselineTransverse(ascent, false);
  }

  @Override
  public double descent() {
    return image.descent() + style.spaceTransverseAfter * toPixels;
  }

  @Override
  public double ascent() {
    return image.ascent() + style.spaceTransverseBefore * toPixels;
  }

  @Override
  public double converseEdge() {
    return getConverse() + converseSpan;
  }

  @Override
  public double converseSpan() {
    return converseSpan + style.spaceBefore * toPixels + style.spaceAfter * toPixels;
  }

  @Override
  public double getConverse() {
    return image.converse() - style.spaceBefore * toPixels;
  }

  @Override
  public DisplayNode getDisplayNode() {
    return image;
  }

  @Override
  public void setConverse(final Context context, final double minConverse, final double converse) {
    this.preAlignConverse = minConverse;
    image.setConverse(converse + style.spaceBefore * toPixels, false);
  }
}
