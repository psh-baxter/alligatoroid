package com.zarbosoft.merman.editor.wall.bricks;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.display.Blank;
import com.zarbosoft.merman.editor.display.DisplayNode;
import com.zarbosoft.merman.editor.visual.Vector;
import com.zarbosoft.merman.editor.wall.Brick;
import com.zarbosoft.merman.editor.wall.BrickInterface;
import com.zarbosoft.merman.syntax.style.Style;

public class BrickEmpty extends Brick {
  private final Blank visual;
  private final int ascent;
  private final int descent;
  private int converse = 0;

  public BrickEmpty(
      final Context context, final BrickInterface inter, Style.SplitMode splitMode, Style style) {
    super(inter, style, splitMode);
    visual = context.display.blank();
    ascent = style.spaceTransverseBefore;
    descent = style.spaceTransverseAfter;
    converseSpan = style.space + style.spaceBefore + style.spaceAfter;
    changed(context);
  }

  @Override
  public int converseEdge() {
    return converse + converseSpan;
  }

  @Override
  public int converseSpan() {
    return converseSpan;
  }

  @Override
  public DisplayNode getDisplayNode() {
    return visual;
  }

  @Override
  public void setConverse(final Context context, final int minConverse, final int converse) {
    this.preAlignConverse = minConverse;
    this.converse = converse;
    visual.setBaselinePosition(new Vector(converse, 0), false);
  }

  @Override
  public void allocateTransverse(final Context context, final int ascent, final int descent) {}

  @Override
  public int descent() {
    return descent;
  }

  @Override
  public int ascent() {
    return ascent;
  }

  @Override
  public int getConverse() {
    return converse;
  }
}
