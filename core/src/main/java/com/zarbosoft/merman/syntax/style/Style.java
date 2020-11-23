package com.zarbosoft.merman.syntax.style;

import com.google.common.collect.ImmutableSet;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.display.Font;
import com.zarbosoft.merman.editor.visual.tags.Tag;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import static com.zarbosoft.rendaw.common.Common.uncheck;

public class Style {

  public Set<Tag> with = new HashSet<>();

  public Set<Tag> without = new HashSet<>();

  public Boolean split = null;

  public String alignment = null;

  public Integer spaceBefore = null;

  public Integer spaceAfter = null;

  public Integer spaceTransverseBefore = null;

  public Integer spaceTransverseAfter = null;

  // Text/image/shape only

  public ModelColor color = null;

  // Text only

  public String font = null;

  public Integer fontSize = null;

  // Image only

  public String image = null;

  public Integer rotate = null;

  // Space only

  public Integer space = null;

  // Other

  public BoxStyle box = null;

  public ObboxStyle obbox = null;

  public static class Baked {
    public static Set<Class<?>> mergeableTypes =
        ImmutableSet.of(
            Integer.class,
            Double.class,
            Boolean.class,
            String.class,
            ModelColor.class,
            BoxStyle.class,
            ObboxStyle.class);
    public Set<Tag> tags = new HashSet<>();
    public boolean split = false;
    public String alignment = null;
    public int spaceBefore = 0;
    public int spaceAfter = 0;
    public int spaceTransverseBefore = 0;
    public int spaceTransverseAfter = 0;
    public ModelColor color = new ModelColor.RGB();
    public String font = null;
    public int fontSize = 14;
    public String image = null;
    public int rotate = 0;
    public int space = 0;
    public BoxStyle.Baked box = new BoxStyle.Baked();
    public ObboxStyle.Baked obbox = new ObboxStyle.Baked();

    public Baked(final Set<Tag> tags) {
      this.tags.addAll(tags);
    }

    public void merge(final Style style) {
      for (final Field field : Style.class.getFields()) {
        if (!mergeableTypes.contains(field.getType())) continue;
        final Object value = uncheck(() -> field.get(style));
        if (value != null) {
          if (field.getName().equals("box")) box.merge((BoxStyle) value);
          else if (field.getName().equals("obbox")) obbox.merge((ObboxStyle) value);
          else uncheck(() -> getClass().getField(field.getName()).set(this, value));
        }
      }
    }

    public Font getFont(final Context context) {
      if (font == null) return context.display.font(null, fontSize);
      return context.display.font(font, fontSize);
    }
  }
}
