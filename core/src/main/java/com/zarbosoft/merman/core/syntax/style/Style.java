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
  public final ObboxStyle obbox;
  /** Override automatic ascent */
  public final Double ascent;

  public Style(Config config) {
    if (config.alignment != null) alignment = config.alignment;
    else alignment = null;
    if (config.splitAlignment != null) splitAlignment = config.splitAlignment;
    else splitAlignment = null;
    if (config.spaceBefore != null) spaceBefore = config.spaceBefore;
    else spaceBefore = 0;
    if (config.spaceAfter != null) spaceAfter = config.spaceAfter;
    else spaceAfter = 0;
    if (config.spaceTransverseBefore != null) spaceTransverseBefore = config.spaceTransverseBefore;
    else spaceTransverseBefore = 0;
    if (config.spaceTransverseAfter != null) spaceTransverseAfter = config.spaceTransverseAfter;
    else spaceTransverseAfter = 0;
    if (config.color != null) color = config.color;
    else color = new ModelColor.RGB(0, 0, 0);
    if (config.font != null) font = config.font;
    else font = null;
    if (config.fontSize != null) fontSize = config.fontSize;
    else fontSize = 14;
    if (config.image != null) image = config.image;
    else image = null;
    if (config.rotate != null) rotate = config.rotate;
    else rotate = 0;
    if (config.space != null) space = config.space;
    else space = 0;
    if (config.ascent == null) {
      ascent = null;
    } else {
      ascent = config.ascent;
    }
    obbox = config.obbox == null ? new ObboxStyle(new ObboxStyle.Config()) : config.obbox;
  }

  public static enum SplitMode {
    NEVER,
    COMPACT,
    ALWAYS
  }

  public static final class Config {
    public Double ascent;
    public ObboxStyle obbox;
    public String alignment;
    public Double spaceBefore;
    public Double spaceAfter;
    public Double spaceTransverseBefore;
    public Double spaceTransverseAfter;
    public ModelColor color;
    public String font;
    public Double fontSize;
    public String image;
    public Double rotate;
    public Double space;
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

    public Config obbox(ObboxStyle obbox) {
      this.obbox = obbox;
      return this;
    }
  }
}
