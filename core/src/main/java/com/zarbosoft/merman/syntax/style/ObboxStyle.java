package com.zarbosoft.merman.syntax.style;

import com.zarbosoft.merman.misc.ROList;

import static com.zarbosoft.rendaw.common.Common.uncheck;

public class ObboxStyle {

  public final Integer padding;

  public final Boolean roundStart;

  public final Boolean roundEnd;

  public final Boolean roundOuterEdges;

  public final Boolean roundInnerEdges;

  public final Boolean roundConcave;

  public final Integer roundRadius;

  public final Boolean line;

  public final ModelColor lineColor;

  public final Double lineThickness;

  public final Boolean fill;

  public final ModelColor fillColor;

  public ObboxStyle(
      Integer padding,
      Boolean roundStart,
      Boolean roundEnd,
      Boolean roundOuterEdges,
      Boolean roundInnerEdges,
      Boolean roundConcave,
      Integer roundRadius,
      Boolean line,
      ModelColor lineColor,
      Double lineThickness,
      Boolean fill,
      ModelColor fillColor) {
    this.padding = padding;
    this.roundStart = roundStart;
    this.roundEnd = roundEnd;
    this.roundOuterEdges = roundOuterEdges;
    this.roundInnerEdges = roundInnerEdges;
    this.roundConcave = roundConcave;
    this.roundRadius = roundRadius;
    this.line = line;
    this.lineColor = lineColor;
    this.lineThickness = lineThickness;
    this.fill = fill;
    this.fillColor = fillColor;
  }

  public static class Spec {
    public final Integer padding;

    public final Boolean roundStart;

    public final Boolean roundEnd;

    public final Boolean roundOuterEdges;

    public final Boolean roundInnerEdges;

    public final Boolean roundConcave;

    public final Integer roundRadius;

    public final Boolean line;

    public final ModelColor lineColor;

    public final Double lineThickness;

    public final Boolean fill;

    public final ModelColor fillColor;

    public Spec(
        Integer padding,
        Boolean roundStart,
        Boolean roundEnd,
        Boolean roundOuterEdges,
        Boolean roundInnerEdges,
        Boolean roundConcave,
        Integer roundRadius,
        Boolean line,
        ModelColor lineColor,
        Double lineThickness,
        Boolean fill,
        ModelColor fillColor) {
      this.padding = padding;
      this.roundStart = roundStart;
      this.roundEnd = roundEnd;
      this.roundOuterEdges = roundOuterEdges;
      this.roundInnerEdges = roundInnerEdges;
      this.roundConcave = roundConcave;
      this.roundRadius = roundRadius;
      this.line = line;
      this.lineColor = lineColor;
      this.lineThickness = lineThickness;
      this.fill = fill;
      this.fillColor = fillColor;
    }
  }

  public static ObboxStyle create(ROList<Spec> toMerge) {
    int padding = 4;
    boolean roundStart = false;
    boolean roundEnd = false;
    boolean roundOuterEdges = false;
    boolean roundInnerEdges = false;
    boolean roundConcave = false;
    int roundRadius = 0;
    boolean line = true;
    ModelColor lineColor = new ModelColor.RGB(0,0,0);
    double lineThickness = 1;
    boolean fill = false;
    ModelColor fillColor = ModelColor.RGB.white;
    for (Spec spec : toMerge) {
      if (spec.padding != null) padding = spec.padding;
      if (spec.roundStart != null) roundStart = spec.roundStart;
      if (spec.roundEnd != null) roundEnd = spec.roundEnd;
      if (spec.roundOuterEdges != null) roundOuterEdges = spec.roundOuterEdges;
      if (spec.roundInnerEdges != null) roundInnerEdges = spec.roundInnerEdges;
      if (spec.roundConcave != null) roundConcave = spec.roundConcave;
      if (spec.roundRadius != null) roundRadius = spec.roundRadius;
      if (spec.line != null) line = spec.line;
      if (spec.lineColor != null) lineColor = spec.lineColor;
      if (spec.lineThickness != null) lineThickness = spec.lineThickness;
      if (spec.fill != null) fill = spec.fill;
      if (spec.fillColor != null) fillColor = spec.fillColor;
    }
    return new ObboxStyle(
        padding,
        roundStart,
        roundEnd,
        roundOuterEdges,
        roundInnerEdges,
        roundConcave,
        roundRadius,
        line,
        lineColor,
        lineThickness,
        fill,
        fillColor);
  }
}
