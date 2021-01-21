package com.zarbosoft.merman.syntax.style;

public abstract class ModelColor {

  public static class RGB extends ModelColor {
    public static final ModelColor white = new RGB(1, 1, 1);

    public final double r;

    public final double g;

    public final double b;

    public RGB(double r, double g, double b) {
      this.r = r;
      this.g = g;
      this.b = b;
    }
  }

  public static class RGBA extends ModelColor {

    public final double r;

    public final double g;

    public final double b;

    public final double a;

    public RGBA(double r, double g, double b, double a) {
      this.r = r;
      this.g = g;
      this.b = b;
      this.a = a;
    }
  }
}
