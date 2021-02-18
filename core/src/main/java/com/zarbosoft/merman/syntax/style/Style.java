package com.zarbosoft.merman.syntax.style;

import com.zarbosoft.rendaw.common.TSList;

public class Style {
  public final String alignment;
  public final String splitAlignment;
  public final Integer spaceBefore;
  public final Integer spaceAfter;
  public final Integer spaceTransverseBefore;
  public final Integer spaceTransverseAfter;
  public final ModelColor color;

  // Text/image/shape only
  public final String font;

  // Text only
  public final Integer fontSize;
  public final String image;

  // Image only
  /** Degrees */
  public final Integer rotate;
  public final Integer space;

  // Space only
  public final BoxStyle box;

  // Other
  public final ObboxStyle obbox;

  private Style(
      String alignment,
      String splitAlignment,
      Integer spaceBefore,
      Integer spaceAfter,
      Integer spaceTransverseBefore,
      Integer spaceTransverseAfter,
      ModelColor color,
      String font,
      Integer fontSize,
      String image,
      Integer rotate,
      Integer space,
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
    public Integer spaceBefore;
    public Integer spaceAfter;
    public Integer spaceTransverseBefore;
    public Integer spaceTransverseAfter;
    // Text/image/shape only
    public ModelColor color;
    // Text only
    public String font;
    public Integer fontSize;
    // Image only
    public String image;
    public Integer rotate;
    // Space only
    public Integer space;
    // Other
    public BoxStyle.Spec box;
    public ObboxStyle.Spec obbox;
    private String splitAlignment;

    public Config() {}

    public Config space(int px) {
      space = px;
      return this;
    }

    public Config spaceBefore(final int space) {
      spaceBefore = space;
      return this;
    }

    public Config spaceAfter(final int space) {
      spaceAfter = space;
      return this;
    }

    public Config spaceTransverseBefore(final int space) {
      spaceTransverseBefore = space;
      return this;
    }

    public Config spaceTransverseAfter(final int space) {
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
      int spaceBefore = 0;
      int spaceAfter = 0;
      int spaceTransverseBefore = 0;
      int spaceTransverseAfter = 0;
      ModelColor color = new ModelColor.RGB(0, 0, 0);
      String font = null;
      int fontSize = 14;
      String image = null;
      int rotate = 0;
      int space = 0;
      TSList<BoxStyle.Spec> boxToMerge = new TSList<BoxStyle.Spec>();
      TSList<ObboxStyle.Spec> obboxToMerge = new TSList<ObboxStyle.Spec>();
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
      if (this.box != null) boxToMerge.add(this.box);
      if (this.obbox != null) obboxToMerge.add(this.obbox);
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
          BoxStyle.create(boxToMerge),
          ObboxStyle.create(obboxToMerge));
    }
  }
}
