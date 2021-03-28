package com.zarbosoft.merman.jfxcore.jfxdisplay;

import com.zarbosoft.merman.core.editor.Context;
import com.zarbosoft.merman.core.editor.display.Font;
import javafx.geometry.Point2D;
import javafx.scene.text.Text;

public class JavaFXFont implements Font {
  javafx.scene.text.Font font;

  public JavaFXFont(final String font, final double size) {
    if (font == null) this.font = javafx.scene.text.Font.font(size);
    else this.font = javafx.scene.text.Font.font(font, size);
  }

  public JavaFXFont(final javafx.scene.text.Font font) {
    this.font = font;
  }

  /*
  @Override
  public double getAscent() {
  	helper.setFont(font);
  	helper.setBoundsType(DEFAULT_BOUNDS_TYPE);
  	final double ascent = helper.getBaselineOffset();
  	// RESTORE STATE
  	helper.setBoundsType(DEFAULT_BOUNDS_TYPE);
  	return (int) ascent;
  }

  @Override
  public double getDescent() {
  	helper.setFont(font);
  	helper.setBoundsType(DEFAULT_BOUNDS_TYPE);
  	final double ascent = helper.getBaselineOffset();
  	final Bounds bounds = helper.getLayoutBounds();
  	final double height = bounds.getMaxY() - bounds.getMinY();
  	// RESTORE STATE
  	helper.setBoundsType(DEFAULT_BOUNDS_TYPE);
  	return (int) (height - ascent);
  }
   */

  @Override
  public Measurer measurer() {
    final Text helper = new Text();
    helper.setWrappingWidth(0);
    helper.setLineSpacing(0);
    return new Measurer() {
      @Override
      public double getWidth(String text) {
        helper.setText(text);
        helper.setFont(font);
        // Note that the wrapping width needs to be set to zero before
        // getting the text's real preferred width.
        double w = helper.prefWidth(-1);
        helper.setWrappingWidth((int) Math.ceil(w));
        w = Math.ceil(helper.getLayoutBounds().getWidth());
        return (int) w;
      }

      @Override
      public int getIndexAtConverse(Context context, String text, double converse) {
        helper.setText(text);
        helper.setFont(font);
        // Note that the wrapping width needs to be set to zero before
        // getting the text's real preferred width.
        final double w = helper.prefWidth(-1);
        helper.setWrappingWidth((int) Math.ceil(w));
        final int offset = helper.hitTest(new Point2D(converse, 0)).getInsertionIndex();
        return offset;
      }
    };
  }
}
