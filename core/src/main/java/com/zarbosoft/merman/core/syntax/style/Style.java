package com.zarbosoft.merman.core.syntax.style;

public class Style {
  public final String alignment;
  public final String splitAlignment;
  public final double spaceBefore;
  public final double spaceAfter;
  public final double spaceTransverseBefore;
  public final double spaceTransverseAfter;
  public final ModelColor color;
  public final String font;
  public final double fontSize;
  public final String image;
  /** Degrees */
  public final double rotate;

  public final double space;
  public final BoxStyle box;
  public final ObboxStyle obbox;

  private Style(
      String alignment,
      String splitAlignment,
      Double spaceBefore,
      Double spaceAfter,
      Double spaceTransverseBefore,
      Double spaceTransverseAfter,
      ModelColor color,
      String font,
      Double fontSize,
      String image,
      Double rotate,
      Double space,
      BoxStyle box,
      ObboxStyle obbox) {
    this.alignment = alignment;
    this.splitAlignment = splitAlignment;
    this.spaceBefore = spaceBefore;
    this.spaceAfter = spaceAfter;
    this.spaceTransverseBefore = spaceTransverseBefore;
    this.spaceTransverseAfter = spaceTransverseAfter;
    this.color = color;
    this.font = font;
    this.fontSize = fontSize;
    this.image = image;
    this.rotate = rotate;
    this.space = space;
    this.box = box;
    this.obbox = obbox;
  }

  public static enum SplitMode {
    NEVER,
    COMPACT,
    ALWAYS
  }

  public static final class Config {
    public String alignment;
    public Double spaceBefore;
    public Double spaceAfter;
    public Double spaceTransverseBefore;
    public Double spaceTransverseAfter;
    // Text/image/shape only
    public ModelColor color;
    // Text only
    public String font;
    public Double fontSize;
    // Image only
    public String image;
    public Double rotate;
    // Space only
    public Double space;
    // Other
    public final BoxStyle.Config box = new BoxStyle.Config();
    public final ObboxStyle.Config obbox = new ObboxStyle.Config();
    private String splitAlignment;

    public Config() {}

    public Config space(double px) {
      space = px;
      return this;
    }

    public Config spaceBefore(final double space) {
      spaceBefore = space;
      return this;
    }

    public Config spaceAfter(final double space) {
      spaceAfter = space;
      return this;
    }

    public Config spaceTransverseBefore(final double space) {
      spaceTransverseBefore = space;
      return this;
    }

    public Config spaceTransverseAfter(final double space) {
      spaceTransverseAfter = space;
      return this;
    }

    public Config alignment(final String name) {
      alignment = name;
      return this;
    }

    public Config splitAlignment(final String name) {
      splitAlignment = name;
      return this;
    }

    public Style create() {
      String alignment = null;
      String splitAlignment = null;
      double spaceBefore = 0;
      double spaceAfter = 0;
      double spaceTransverseBefore = 0;
      double spaceTransverseAfter = 0;
      ModelColor color = new ModelColor.RGB(0, 0, 0);
      String font = null;
      double fontSize = 14;
      String image = null;
      double rotate = 0;
      double space = 0;
      if (this.alignment != null) alignment = this.alignment;
      if (this.splitAlignment != null) splitAlignment = this.splitAlignment;
      if (this.spaceBefore != null) spaceBefore = this.spaceBefore;
      if (this.spaceAfter != null) spaceAfter = this.spaceAfter;
      if (this.spaceTransverseBefore != null) spaceTransverseBefore = this.spaceTransverseBefore;
      if (this.spaceTransverseAfter != null) spaceTransverseAfter = this.spaceTransverseAfter;
      if (this.color != null) color = this.color;
      if (this.font != null) font = this.font;
      if (this.fontSize != null) fontSize = this.fontSize;
      if (this.image != null) image = this.image;
      if (this.rotate != null) rotate = this.rotate;
      if (this.space != null) space = this.space;
      return new Style(
          alignment,
          splitAlignment,
          spaceBefore,
          spaceAfter,
          spaceTransverseBefore,
          spaceTransverseAfter,
          color,
          font,
          fontSize,
          image,
          rotate,
          space,
          new BoxStyle(this.box),
          new ObboxStyle(this.obbox));
    }

    public Config color(ModelColor.RGB color) {
      this.color = color;
      return this;
    }

    public Config fontSize(double size) {
      this.fontSize = size;
      return this;
    }

    public Config font(String name) {
      this.font = name;
      return this;
    }
  }
}
