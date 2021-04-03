package com.zarbosoft.merman.core.wall.bricks;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.display.Blank;
import com.zarbosoft.merman.core.display.DisplayNode;
import com.zarbosoft.merman.core.visual.Vector;
import com.zarbosoft.merman.core.wall.Brick;
import com.zarbosoft.merman.core.wall.BrickInterface;
import com.zarbosoft.merman.core.syntax.style.Style;

public class BrickEmpty extends Brick {
  private final Blank visual;
  private final double ascent;
  private final double descent;
  private double converse = 0;

  public BrickEmpty(
      final Context context, final BrickInterface inter, Style.SplitMode splitMode, Style style) {
    super(inter, style, splitMode);
    visual = context.display.blank();
    double toPixels = context.toPixels;
    ascent = style.spaceTransverseBefore * toPixels;
    descent = style.spaceTransverseAfter * toPixels;
    converseSpan = style.space * toPixels + style.spaceBefore * toPixels + style.spaceAfter * toPixels;
    changed(context);
  }

  @Override
  public double converseEdge() {
    return converse + converseSpan;
  }

  @Override
  public double converseSpan() {
    return converseSpan;
  }

  @Override
  public DisplayNode getDisplayNode() {
    return visual;
  }

  @Override
  public void setConverse(final Context context, final double minConverse, final double converse) {
    this.preAlignConverse = minConverse;
    this.converse = converse;
    visual.setBaselinePosition(new Vector(converse, 0), false);
  }

  @Override
  public void allocateTransverse(final Context context, final double ascent, final double descent) {}

  @Override
  public double descent() {
    return descent;
  }

  @Override
  public double ascent() {
    return ascent;
  }

  @Override
  public double getConverse() {
    return converse;
  }
}
