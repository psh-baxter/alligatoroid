package com.zarbosoft.merman.editorcore.displayderived;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.display.Drawing;
import com.zarbosoft.merman.core.display.DrawingContext;
import com.zarbosoft.merman.core.display.derived.Obbox;
import com.zarbosoft.merman.core.visual.Vector;
import com.zarbosoft.merman.core.syntax.style.ObboxStyle;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSList;

public class Box {
  public final Drawing drawing;
  private final double toPixels;
  private Vector offset;
  private ObboxStyle style;
  private double stylePaddingConverse;
  private double stylePaddingConverseEnd;
  private double stylePaddingTransverse;
  private double stylePaddingTransverseEnd;
  private double styleLineThickness;
  private double styleRoundRadius;

  public Box(final Context context) {
    drawing = context.display.drawing();
    toPixels = context.toPixels;
  }

  public void setStyle(final ObboxStyle style) {
    this.style = style;
    stylePaddingConverse = style.padding.converseStart * toPixels;
    stylePaddingConverseEnd = style.padding.converseEnd * toPixels;
    stylePaddingTransverse = style.padding.transverseStart * toPixels;
    stylePaddingTransverseEnd = style.padding.transverseEnd * toPixels;
    styleLineThickness = style.lineThickness * toPixels;
    styleRoundRadius = style.roundRadius * toPixels;
  }

  public void setPosition(final Vector vector, final boolean animate) {
    drawing.setPosition(offset.add(vector), animate);
  }

  public void setSize(final Context context, double converseSpan, double transverseSpan) {
    drawing.clear();
    converseSpan += stylePaddingConverse + stylePaddingConverseEnd;
    transverseSpan += stylePaddingTransverse + stylePaddingTransverseEnd;
    final int buffer = (int) (styleLineThickness + 1);
    drawing.resize(context, new Vector(converseSpan + buffer * 2, transverseSpan + buffer * 2));
    offset = new Vector(-(buffer + stylePaddingConverse), -(buffer + stylePaddingTransverse));
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
            new ROPair<>(new Vector(converseSpan, 0), style.roundOuterCorners),
            new ROPair<>(new Vector(converseSpan, transverseSpan), style.roundEnd),
            new ROPair<>(new Vector(0, transverseSpan), style.roundOuterCorners)),
        styleRoundRadius);
  }
}
