package com.zarbosoft.merman.core.display.derived;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.display.Drawing;
import com.zarbosoft.merman.core.display.DrawingContext;
import com.zarbosoft.merman.core.syntax.style.ObboxStyle;
import com.zarbosoft.merman.core.visual.Vector;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSList;

public class Obbox {
  public final Drawing drawing;
  private final double toPixels;
  ObboxStyle style;
  private double stylePaddingConverse;
  private double stylePaddingConverseEnd;
  private double stylePaddingTransverse;
  private double stylePaddingTransverseEnd;
  private double styleLineThickness;
  private double styleRoundRadius;

  public Obbox(final Context context) {
    drawing = context.display.drawing();
    toPixels = context.toPixels;
  }

  private static double simpleNorm(double v) {
    if (aeq(v, 0, 0.1)) return 0;
    if (v < 0) return -1;
    return 1;
  }

  private static boolean aeq(double a, double b, double t) {
    return (a - b) * (a - b) < t * t;
  }

  public static void drawRounded(
      DrawingContext gc, TSList<ROPair<Vector, Boolean>> points, double baseRadius) {
    for (int i = 0; i < points.size(); ++i) {
      final ROPair<Vector, Boolean> mid = points.get(i);
      if (mid.second) {
        final ROPair<Vector, Boolean> pre = points.get((i + points.size() - 1) % points.size());
        final ROPair<Vector, Boolean> post = points.get((i + 1) % points.size());
        final Vector toPre =
            new Vector(
                pre.first.converse - mid.first.converse,
                pre.first.transverse - mid.first.transverse);
        final Vector toPost =
            new Vector(
                post.first.converse - mid.first.converse,
                post.first.transverse - mid.first.transverse);
        double radius = baseRadius;
        // Math.max on converse/transverse assumes non-angled segments to select one (provides distance)
        radius =
            Math.min(radius, Math.max(Math.abs(toPre.converse), Math.abs(toPre.transverse)) / 2);
        radius =
            Math.min(radius, Math.max(Math.abs(toPost.converse), Math.abs(toPost.transverse)) / 2);
        if (i == 0) {
          gc.moveTo(
              mid.first.converse + simpleNorm(toPre.converse) * radius,
              mid.first.transverse + simpleNorm(toPre.transverse) * radius);
        }
        gc.arcTo(
            mid.first.converse,
            mid.first.transverse,
            mid.first.converse + simpleNorm(toPost.converse) * radius,
            mid.first.transverse + simpleNorm(toPost.transverse) * radius,
            radius);
      } else {
        if (i == 0) {
          gc.moveTo(mid.first.converse, mid.first.transverse);
        } else {
          gc.lineTo(mid.first.converse, mid.first.transverse);
        }
      }
    }
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

  public void setSize(
      final Context context,
      boolean oneLine,
      double firstLineConverse,
      double firstLineTransverse,
      double firstLineTransverseEnd,
      double lastLineConverseEnd,
      double lastLineTransverse,
      double lastLineTransverseEnd) {
    drawing.clear();
    firstLineConverse -= stylePaddingConverse;
    firstLineTransverse -= stylePaddingTransverse;
    firstLineTransverseEnd =
        oneLine
            ? firstLineTransverseEnd + stylePaddingTransverseEnd
            : firstLineTransverseEnd - stylePaddingTransverse;
    lastLineConverseEnd += stylePaddingConverseEnd;
    lastLineTransverse += stylePaddingTransverseEnd;
    lastLineTransverseEnd += stylePaddingTransverseEnd;
    final int buffer = (int) (styleLineThickness + 1);
    final Vector wh =
        new Vector(
            context.edge + stylePaddingConverse + stylePaddingConverseEnd + buffer * 2,
            lastLineTransverseEnd - firstLineTransverse + buffer * 2);
    drawing.resize(context, wh);
    drawing.setPosition(
        new Vector(-(buffer + stylePaddingConverse), firstLineTransverse - buffer), false);
    final DrawingContext gc = drawing.begin(context);
    gc.translate(buffer + stylePaddingConverse, buffer);
    firstLineTransverseEnd -= firstLineTransverse;
    lastLineTransverse -= firstLineTransverse;
    lastLineTransverseEnd -= firstLineTransverse;
    firstLineTransverse = 0;

    if (style.fill) {
      gc.beginFillPath();
      gc.setFillColor(style.fillColor);
      path(
          gc,
          oneLine,
          -stylePaddingConverse,
          context.edge + stylePaddingConverseEnd,
          firstLineConverse,
          firstLineTransverse,
          firstLineTransverseEnd,
          lastLineConverseEnd,
          lastLineTransverse,
          lastLineTransverseEnd);
      gc.closePath();
    }
    if (style.line) {
      gc.beginStrokePath();
      gc.setLineColor(style.lineColor);
      gc.setLineThickness(styleLineThickness);
      path(
          gc,
          oneLine,
          -stylePaddingConverse,
          context.edge + stylePaddingConverseEnd,
          firstLineConverse,
          firstLineTransverse,
          firstLineTransverseEnd,
          lastLineConverseEnd,
          lastLineTransverse,
          lastLineTransverseEnd);
      gc.closePath();
    }
  }

  private void path(
      final DrawingContext gc,
      final boolean oneLine,
      final double converseZero,
      final double converseEdge,
      final double firstLineConverse,
      final double firstLineTransverse,
      final double firstLineTransverseEnd,
      final double lastLineConverseEnd,
      final double lastLineTransverse,
      final double lastLineTransverseEnd) {
    TSList<ROPair<Vector, Boolean>> points;
    if (oneLine) {
      points =
          TSList.of(
              new ROPair<>(new Vector(firstLineConverse, firstLineTransverse), style.roundStart),
              new ROPair<>(
                  new Vector(lastLineConverseEnd, firstLineTransverse), style.roundOuterCorners),
              new ROPair<>(new Vector(lastLineConverseEnd, firstLineTransverseEnd), style.roundEnd),
              new ROPair<>(
                  new Vector(firstLineConverse, firstLineTransverseEnd), style.roundOuterCorners));
    } else {
      points = TSList.of();
      points.add(
          new ROPair<>(new Vector(firstLineConverse, firstLineTransverse), style.roundStart));
      points.add(
          new ROPair<>(new Vector(converseEdge, firstLineTransverse), style.roundOuterCorners));
      if (!aeq(lastLineConverseEnd, converseEdge, 5)) {
        points.add(
            new ROPair<>(new Vector(converseEdge, lastLineTransverse), style.roundInnerCorners));
        points.add(
            new ROPair<>(new Vector(lastLineConverseEnd, lastLineTransverse), style.roundConcave));
      }
      points.add(
          new ROPair<>(new Vector(lastLineConverseEnd, lastLineTransverseEnd), style.roundEnd));
      points.add(
          new ROPair<>(new Vector(converseZero, lastLineTransverseEnd), style.roundOuterCorners));
      if (!aeq(firstLineConverse, converseZero, 5)) {
        points.add(
            new ROPair<>(
                new Vector(converseZero, firstLineTransverseEnd), style.roundInnerCorners));
        points.add(
            new ROPair<>(
                new Vector(firstLineConverse, firstLineTransverseEnd), style.roundConcave));
      }
    }
    drawRounded(gc, points, styleRoundRadius);
  }
}
