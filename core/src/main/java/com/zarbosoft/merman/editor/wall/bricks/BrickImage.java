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
    ascent = (int) image.transverseSpan();
    descent = (int) 0;
    converseSpan = (int) image.converseSpan();
    changed(context);
  }

  @Override
  public void allocateTransverse(final Context context, final int ascent, final int descent) {
    image.setTransverse(ascent, false);
  }

  @Override
  public int converseEdge() {
    return image.converseEdge();
  }

  @Override
  public int converseSpan() {
    return image.converseSpan();
  }

  @Override
  public int getConverse() {
    return image.converse();
  }

  @Override
  public DisplayNode getDisplayNode() {
    return image;
  }

  @Override
  public void setConverse(final Context context, final int minConverse, final int converse) {
    this.preAlignConverse = minConverse;
    image.setConverse(converse, false);
  }
}
