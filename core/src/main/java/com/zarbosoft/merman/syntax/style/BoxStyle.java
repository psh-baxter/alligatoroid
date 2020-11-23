package com.zarbosoft.merman.syntax.style;

import java.lang.reflect.Field;

import static com.zarbosoft.rendaw.common.Common.uncheck;

public class BoxStyle {

  public Integer padding = null;

  public Boolean roundStart = null;

  public Boolean roundEnd = null;

  public Boolean roundOuterEdges = null;

  public Integer roundRadius = null;

  public Boolean line = null;

  public ModelColor lineColor = null;

  public Double lineThickness = null;

  public Boolean fill = null;

  public ModelColor fillColor = null;

  public void merge(final BoxStyle settings) {
    for (final Field field : getClass().getFields()) {
      if (field.getType() != Integer.class
          && field.getType() != Double.class
          && field.getType() != Boolean.class
          && field.getType() != String.class
          && field.getType() != ModelColor.class) continue;
      final Object value = uncheck(() -> field.get(settings));
      if (value != null) uncheck(() -> field.set(this, value));
    }
  }

  public static class Baked {
    public int padding = 4;
    public boolean roundStart = false;
    public boolean roundEnd = false;
    public boolean roundOuterEdges = false;
    public int roundRadius = 0;
    public boolean line = true;
    public ModelColor lineColor = new ModelColor.RGB();
    public double lineThickness = 1;
    public boolean fill = false;
    public ModelColor fillColor = ModelColor.RGB.white;

    public void merge(final BoxStyle settings) {
      for (final Field field : BoxStyle.class.getFields()) {
        if (field.getType() != Integer.class
            && field.getType() != Double.class
            && field.getType() != Boolean.class
            && field.getType() != String.class
            && field.getType() != ModelColor.class) continue;
        final Object value = uncheck(() -> field.get(settings));
        if (value != null) uncheck(() -> getClass().getField(field.getName()).set(this, value));
      }
    }
  }
}
