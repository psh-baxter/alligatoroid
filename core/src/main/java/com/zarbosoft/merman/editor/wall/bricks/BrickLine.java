package com.zarbosoft.merman.editor.wall.bricks;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.Hoverable;
import com.zarbosoft.merman.editor.visual.Vector;
import com.zarbosoft.merman.editor.visual.visuals.VisualFrontPrimitive;
import com.zarbosoft.merman.syntax.style.Style;
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
