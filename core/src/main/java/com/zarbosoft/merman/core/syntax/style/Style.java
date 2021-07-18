package com.zarbosoft.merman.core.syntax.style;

public class Style {
  public final String alignment;
  public final String splitAlignment;
  public final Padding padding;
  public final ModelColor color;
  public final ModelColor invalidColor;
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
    if (config.padding == null) padding = Padding.empty;
    else padding = config.padding;
    if (config.color != null) color = config.color;
    else color = ModelColor.RGB.black;
    if (config.invalidColor != null) invalidColor = config.color;
    else invalidColor = color;
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
    public Padding padding;
    public ModelColor color;
    public String font;
    public Double fontSize;
    public String image;
    public Double rotate;
    public Double space;
    private String splitAlignment;
    private ModelColor invalidColor;

    public Config() {}

    public Config padding(Padding padding) {
      this.padding = padding;
      return this;
    }

    public Config space(double px) {
      space = px;
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

    public Config color(ModelColor color) {
      this.color = color;
      return this;
    }
    public Config invalidColor(ModelColor color) {
      this.invalidColor = color;
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

    public Config dupe() {
      Config config = new Config();
      config.ascent = ascent;
      config.obbox = obbox;
      config.alignment = alignment;
      config.padding = padding;
      config.color = color;
      config.font = font;
      config.fontSize = fontSize;
      config.image = image;
      config.rotate = rotate;
      config.space = space;
      config.splitAlignment = splitAlignment;
      return config;
    }
  }
}
