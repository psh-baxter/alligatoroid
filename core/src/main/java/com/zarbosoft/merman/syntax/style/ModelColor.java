package com.zarbosoft.merman.syntax.style;

public abstract class ModelColor {
  public static class RGB extends ModelColor {
    public static final ModelColor white = new RGB(1, 1, 1);
    public static final ModelColor black = new RGB(0, 0, 0);

    public final double r;

    public final double g;

    public final double b;

    public RGB(double r, double g, double b) {
      this.r = r;
      this.g = g;
      this.b = b;
    }

    public static RGB polarOKLab(
        /** Lightness, 0-1 */
        double lightness,
        /** Saturation roughly, 0-1 */
        double chroma,
        /** Hue, 0-360 */
        double hue) {
      // https://bottosson.github.io/posts/oklab/
      double hInRad = hue / 360.0 * Math.PI * 2;
      double a = chroma * Math.cos(hInRad);
      double b = chroma * Math.sin(hInRad);
      double l_ = lightness + 0.3963377774f * a + 0.2158037573f * b;
      double m_ = lightness - 0.1055613458f * a - 0.0638541728f * b;
      double s_ = lightness - 0.0894841775f * a - 1.2914855480f * b;
      double l = l_ * l_ * l_;
      double m = m_ * m_ * m_;
      double s = s_ * s_ * s_;
      return new RGB(
          +4.0767416621f * l - 3.3077115913f * m + 0.2309699292f * s,
          -1.2684380046f * l + 2.6097574011f * m - 0.3413193965f * s,
          -0.0041960863f * l - 0.7034186147f * m + 1.7076147010f * s);
    }

    public static RGB hex(String hex) {
      return new RGB(
          Integer.parseInt(hex.substring(0, 2), 16) / 255.0,
          Integer.parseInt(hex.substring(2, 4), 16) / 255.0,
          Integer.parseInt(hex.substring(4, 6), 16) / 255.0);
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

    public static RGBA polarOKLab(
        /** Lightness, 0-1 */
        double lightness,
        /** Saturation roughly, 0-1 */
        double chroma,
        /** Hue, 0-360 */
        double hue,
        double alpha) {
      RGB temp = RGB.polarOKLab(lightness, chroma, hue);
      return new RGBA(temp.r, temp.g, temp.b, alpha);
    }
  }
}
