package com.zarbosoft.merman.syntax.style;

public class BoxStyle {
  public final Integer padding;
  public final Boolean roundStart;
  public final Boolean roundEnd;
  public final Boolean roundOuterEdges;
  public final Integer roundRadius;
  public final Boolean line;
  public final ModelColor lineColor;
  public final Double lineThickness;
  public final Boolean fill;
  public final ModelColor fillColor;

  public BoxStyle(Config config) {
    this.padding = config.padding == null ? 4 : config.padding;
    this.roundStart = config.roundStart == null ? false : config.roundStart;
    this.roundEnd = config.roundEnd == null ? false : config.roundEnd;
    this.roundOuterEdges = config.roundOuterEdges == null ? false : config.roundOuterEdges;
    this.roundRadius = config.roundRadius == null ? 0 : config.roundRadius;
    this.line = config.line == null ? true : config.line;
    this.lineColor = config.lineColor == null ? ModelColor.RGB.black : config.lineColor;
    this.lineThickness = config.lineThickness == null ? 1 : config.lineThickness;
    this.fill = config.fill == null ? false : config.fill;
    this.fillColor = config.fillColor == null ? ModelColor.RGB.white : config.fillColor;
  }

  public static class Config {
    public Integer padding;
    public Boolean roundStart;
    public Boolean roundEnd;
    public Boolean roundOuterEdges;
    public Integer roundRadius;
    public Boolean line;
    public ModelColor lineColor;
    public Double lineThickness;
    public Boolean fill;
    public ModelColor fillColor;
  }
}
