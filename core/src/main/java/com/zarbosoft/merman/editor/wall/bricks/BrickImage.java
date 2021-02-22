package com.zarbosoft.merman.editor.wall.bricks;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.display.DisplayNode;
import com.zarbosoft.merman.editor.display.Image;
import com.zarbosoft.merman.editor.wall.Brick;
import com.zarbosoft.merman.editor.wall.BrickInterface;
import com.zarbosoft.merman.syntax.style.Style;

public class BrickImage extends Brick {
  private final Image image;

  public BrickImage(final Context context, final BrickInterface inter, Style.SplitMode splitMode, Style style) {
    super(inter, style, splitMode);
    image = context.display.image();
    image.setImage(context, style.image);
    image.rotate(context, style.rotate);
    converseSpan = (int) image.converseSpan();
    changed(context);
  }

  @Override
  public void allocateTransverse(final Context context, final double ascent, final double descent) {
    image.setBaselineTransverse(ascent, false);
  }

  @Override
  public double descent() {
    return image.descent() + style.spaceTransverseAfter;
  }

  @Override
  public double ascent() {
    return image.ascent() + style.spaceTransverseBefore;
  }

  @Override
  public double converseEdge() {
    return getConverse() + converseSpan;
  }

  @Override
  public double converseSpan() {
    return converseSpan + style.spaceBefore + style.spaceAfter;
  }

  @Override
  public double getConverse() {
    return image.converse() - style.spaceBefore;
  }

  @Override
  public DisplayNode getDisplayNode() {
    return image;
  }

  @Override
  public void setConverse(final Context context, final double minConverse, final double converse) {
    this.preAlignConverse = minConverse;
    image.setConverse(converse + style.spaceBefore, false);
  }
}
