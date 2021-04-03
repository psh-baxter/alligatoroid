package com.zarbosoft.merman.core.editor.display.derived;

import com.zarbosoft.merman.core.editor.Context;
import com.zarbosoft.merman.core.editor.display.Drawing;
import com.zarbosoft.merman.core.editor.display.DrawingContext;
import com.zarbosoft.merman.core.editor.visual.Vector;
import com.zarbosoft.merman.core.syntax.style.BoxStyle;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSList;

public class Box {
  public final Drawing drawing;
  private final double toPixels;
  private Vector offset;
  private BoxStyle style;
  private double stylePadding;
  private double styleLineThickness;
  private double styleRoundRadius;

  public Box(final Context context) {
    drawing = context.display.drawing();
    toPixels = context.toPixels;
  }

  public void setStyle(final BoxStyle style) {
    this.style = style;
    stylePadding = style.padding * toPixels;
    styleLineThickness = style.lineThickness * toPixels;
    styleRoundRadius = style.roundRadius * toPixels;
  }

  public void setPosition(final Vector vector, final boolean animate) {
    drawing.setPosition(offset.add(vector), animate);
  }

  public void setSize(final Context context, double converseSpan, double transverseSpan) {
    drawing.clear();
    converseSpan += stylePadding * 2;
    transverseSpan += stylePadding * 2;
    final int buffer = (int) (styleLineThickness + 1);
    drawing.resize(context, new Vector(converseSpan + buffer * 2, transverseSpan + buffer * 2));
    offset = new Vector(-(buffer + stylePadding), -(buffer + stylePadding));
    final DrawingContext gc = drawing.begin(context);
    gc.translate(buffer, buffer);
    if (style.fill) {
      gc.beginFillPath();
      gc.setFillColor(style.fillColor);
      path(gc, converseSpan, transverseSpan);
      gc.closePath();
    }
    if (style.line) {
      gc.beginStrokePath();
      gc.setLineColor(style.lineColor);
      gc.setLineThickness(styleLineThickness);
      path(gc, converseSpan, transverseSpan);
      gc.closePath();
    }
  }

  private void path(
      final DrawingContext gc, final double converseSpan, final double transverseSpan) {
    Obbox.drawRounded(
        gc,
        TSList.of(
            new ROPair<>(new Vector(0, 0), style.roundStart),
            new ROPair<>(new Vector(converseSpan, 0), style.roundOuterEdges),
            new ROPair<>(new Vector(converseSpan, transverseSpan), style.roundEnd),
            new ROPair<>(new Vector(0, transverseSpan), style.roundOuterEdges)),
        styleRoundRadius);
  }
}
