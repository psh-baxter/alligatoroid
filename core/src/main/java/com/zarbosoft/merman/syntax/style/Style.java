package com.zarbosoft.merman.syntax.style;

import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROSet;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSSet;

public class Style {
  public final ROSet<String> tags;

  public final Boolean split;

  public final String alignment;

  public final Integer spaceBefore;

  public final Integer spaceAfter;

  public final Integer spaceTransverseBefore;

  public final Integer spaceTransverseAfter;

  // Text/image/shape only

  public final ModelColor color;

  // Text only

  public final String font;

  public final Integer fontSize;

  // Image only

  public final String image;
  /** Degrees */
  public final Integer rotate;

  // Space only

  public final Integer space;

  // Other

  public final BoxStyle box;

  public final ObboxStyle obbox;

  public Style(
      ROSet<String> tags,
      Boolean split,
      String alignment,
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
    this.tags = tags;
    this.split = split;
    this.alignment = alignment;
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

  public static Style create(ROList<Spec> toMerge) {
    TSSet<String> usedTags = new TSSet<>();
    boolean split = false;
    String alignment = null;
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
    for (Spec spec : toMerge) {
      usedTags.addAll(spec.with);
      if (spec.split != null) split = spec.split;
      if (spec.alignment != null) alignment = spec.alignment;
      if (spec.spaceBefore != null) spaceBefore = spec.spaceBefore;
      if (spec.spaceAfter != null) spaceAfter = spec.spaceAfter;
      if (spec.spaceTransverseBefore != null) spaceTransverseBefore = spec.spaceTransverseBefore;
      if (spec.spaceTransverseAfter != null) spaceTransverseAfter = spec.spaceTransverseAfter;
      if (spec.color != null) color = spec.color;
      if (spec.font != null) font = spec.font;
      if (spec.fontSize != null) fontSize = spec.fontSize;
      if (spec.image != null) image = spec.image;
      if (spec.rotate != null) rotate = spec.rotate;
      if (spec.space != null) space = spec.space;
      if (spec.box != null) boxToMerge.add(spec.box);
      if (spec.obbox != null) obboxToMerge.add(spec.obbox);
    }
    return new Style(
        usedTags.ro(),
        split,
        alignment,
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

  public static final class Spec {
    public ROSet<String> with;
    public ROSet<String> without;
    public Boolean split;
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

    public Spec() {}

    public Spec(
        ROSet<String> with,
        ROSet<String> without,
        Boolean split,
        String alignment,
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
        BoxStyle.Spec box,
        ObboxStyle.Spec obbox) {
      this.with = with;
      this.without = without;
      this.split = split;
      this.alignment = alignment;
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
  }
}
