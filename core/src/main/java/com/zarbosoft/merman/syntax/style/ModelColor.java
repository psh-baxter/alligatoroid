package com.zarbosoft.merman.syntax.style;

public abstract class ModelColor {

  public static class RGB extends ModelColor {
    public static final ModelColor white;

    static {
      final RGB tempWhite = new RGB();
      tempWhite.r = 1;
      tempWhite.g = 1;
      tempWhite.b = 1;
      white = tempWhite;
    }

    public double r;

    public double g;

    public double b;
  }

  public static class RGBA extends ModelColor {

    public double r;

    public double g;

    public double b;

    public double a;
  }
}
