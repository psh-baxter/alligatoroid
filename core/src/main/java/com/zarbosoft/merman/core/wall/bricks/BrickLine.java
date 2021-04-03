package com.zarbosoft.merman.core.wall.bricks;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.Hoverable;
import com.zarbosoft.merman.core.visual.Vector;
import com.zarbosoft.merman.core.visual.visuals.VisualFrontPrimitive;
import com.zarbosoft.merman.core.syntax.style.Style;
import com.zarbosoft.rendaw.common.ROPair;

public class BrickLine extends BrickText {
  private final VisualFrontPrimitive.Line line;

  public BrickLine(
      final Context context,
      final VisualFrontPrimitive.Line line,
      Style.SplitMode splitMode,
      Style style) {
    super(context, line, splitMode, style, 0);
    this.line = line;
    changed(context);
  }

  @Override
  public boolean isSplit(boolean compact) {
    if (line.index > 0) return true;
    return super.isSplit(compact);
  }

  @Override
  public ROPair<Hoverable, Boolean> hover(final Context context, final Vector point) {
    return line.hover(context, point);
  }
}
