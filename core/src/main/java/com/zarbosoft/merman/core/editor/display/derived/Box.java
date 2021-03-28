package com.zarbosoft.merman.core.editor.display.derived;

import com.zarbosoft.merman.core.editor.Context;
import com.zarbosoft.merman.core.editor.display.Drawing;
import com.zarbosoft.merman.core.editor.display.DrawingContext;
import com.zarbosoft.merman.core.editor.visual.Vector;
import com.zarbosoft.merman.core.syntax.style.BoxStyle;

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
    moveTo(gc, 0, transverseSpan / 2);
    cornerTo(gc, style.roundStart, 0, 0, converseSpan / 2, 0);
    cornerTo(gc, style.roundOuterEdges, converseSpan, 0, converseSpan, transverseSpan / 2);
    cornerTo(gc, style.roundEnd, converseSpan, transverseSpan, converseSpan / 2, transverseSpan);
    cornerTo(gc, style.roundOuterEdges, 0, transverseSpan, 0, transverseSpan / 2);
  }

  private void moveTo(final DrawingContext gc, final double c, final double t) {
    gc.moveTo(c, t);
  }

  private void cornerTo(
      final DrawingContext gc,
      final boolean round,
      final double c,
      final double t,
      final double c2,
      final double t2) {
    if (round) {
      gc.arcTo(c, t, c2, t2, styleRoundRadius);
    } else {
      gc.lineTo(c, t);
      gc.lineTo(c2, t2);
    }
  }
}
