package com.zarbosoft.merman.core.editor.display.derived;

import com.zarbosoft.merman.core.editor.Context;
import com.zarbosoft.merman.core.editor.display.Drawing;
import com.zarbosoft.merman.core.editor.display.DrawingContext;
import com.zarbosoft.merman.core.editor.visual.Vector;
import com.zarbosoft.merman.core.syntax.style.ObboxStyle;

public class Obbox {
  public final Drawing drawing;
  private final double toPixels;
  ObboxStyle style;
  private double stylePadding;
  private double styleLineThickness;
  private double styleRoundRadius;

  public Obbox(final Context context) {
    drawing = context.display.drawing();
    toPixels = context.toPixels;
  }

  public void setStyle(final ObboxStyle style) {
    this.style = style;
    stylePadding = style.padding * toPixels;
    styleLineThickness = style.lineThickness * toPixels;
    styleRoundRadius = style.roundRadius * toPixels;
  }

  public void setSize(
      final Context context, double sc, double st, double ste, double ec, double et, double ete) {
    final boolean oneLine = st == et;
    drawing.clear();
    sc -= stylePadding;
    st -= stylePadding;
    ste = oneLine ? ste + stylePadding : ste - stylePadding;
    ec += stylePadding;
    et += stylePadding;
    ete += stylePadding;
    final int buffer = (int) (styleLineThickness + 1);
    final Vector wh =
        new Vector(context.edge + stylePadding * 2 + buffer * 2, ete - st + buffer * 2);
    drawing.resize(context, wh);
    drawing.setPosition(new Vector(-(buffer + stylePadding), st - buffer), false);
    final DrawingContext gc = drawing.begin(context);
    gc.translate(buffer + stylePadding, buffer);
    ste -= st;
    et -= st;
    ete -= st;
    st = 0;
    if (style.fill) {
      gc.beginFillPath();
      gc.setFillColor(style.fillColor);
      path(gc, oneLine, -stylePadding, context.edge + stylePadding, sc, st, ste, ec, et, ete);
      gc.closePath();
    }
    if (style.line) {
      gc.beginStrokePath();
      gc.setLineColor(style.lineColor);
      gc.setLineThickness(styleLineThickness);
      path(gc, oneLine, -stylePadding, context.edge + stylePadding, sc, st, ste, ec, et, ete);
      gc.closePath();
    }
  }

  private void path(
      final DrawingContext gc,
      final boolean oneLine,
      final double converseZero,
      final double converseEdge,
      final double startConverse,
      final double startTransverse,
      final double startTransverseEdge,
      final double endConverse,
      final double endTransverse,
      final double endTransverseEdge) {
    if (oneLine) {
      moveTo(gc, startConverse, (startTransverse + startTransverseEdge) / 2);
      cornerTo(
          gc,
          style.roundStart,
          startConverse,
          startTransverse,
          (startConverse + endConverse) / 2,
          startTransverse);
      cornerTo(
          gc,
          style.roundOuterEdges,
          endConverse,
          startTransverse,
          endConverse,
          (startTransverse + startTransverseEdge) / 2);
      cornerTo(
          gc,
          style.roundEnd,
          endConverse,
          startTransverseEdge,
          (startConverse + endConverse) / 2,
          startTransverseEdge);
      cornerTo(
          gc,
          style.roundOuterEdges,
          startConverse,
          startTransverseEdge,
          startConverse,
          (startTransverse + startTransverseEdge) / 2);
    } else {
      moveTo(gc, startConverse, (startTransverse + startTransverseEdge) / 2);
      cornerTo(
          gc,
          style.roundStart,
          startConverse,
          startTransverse,
          (startConverse + converseEdge) / 2,
          startTransverse);
      cornerTo(
          gc,
          style.roundOuterEdges,
          converseEdge,
          startTransverse,
          converseEdge,
          (startTransverse + endTransverse) / 2);
      if (endConverse == converseEdge) {
        cornerTo(
            gc,
            style.roundInnerEdges,
            converseEdge,
            endTransverseEdge,
            (converseZero + converseEdge) / 2,
            endTransverseEdge);
      } else {
        cornerTo(
            gc,
            style.roundInnerEdges,
            converseEdge,
            endTransverse,
            (endConverse + converseEdge) / 2,
            endTransverse);
        cornerTo(
            gc,
            style.roundConcave,
            endConverse,
            endTransverse,
            endConverse,
            (endTransverse + endTransverseEdge) / 2);
        cornerTo(
            gc,
            style.roundEnd,
            endConverse,
            endTransverseEdge,
            (converseZero + endConverse) / 2,
            endTransverseEdge);
      }
      if (startConverse == converseZero) {
        cornerTo(
            gc,
            style.roundOuterEdges,
            converseZero,
            endTransverseEdge,
            converseZero,
            (startTransverse + startTransverseEdge) / 2);
      } else {
        cornerTo(
            gc,
            style.roundOuterEdges,
            converseZero,
            endTransverseEdge,
            converseZero,
            (startTransverseEdge + endTransverseEdge) / 2);
        cornerTo(
            gc,
            style.roundInnerEdges,
            converseZero,
            startTransverseEdge,
            startConverse / 2,
            startTransverseEdge);
        cornerTo(
            gc,
            style.roundConcave,
            startConverse,
            startTransverseEdge,
            startConverse,
            (startTransverse + startTransverseEdge) / 2);
      }
    }
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
