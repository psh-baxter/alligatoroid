package com.zarbosoft.merman.editor.display;

import com.zarbosoft.merman.core.display.Font;
import com.zarbosoft.merman.core.display.Text;
import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.syntax.style.ModelColor;
import com.zarbosoft.rendaw.common.DeadCode;

public class MockeryText extends MockeryCourseDisplayNode implements Text {
  String text;
  MockeryFont font = new MockeryFont(20);

  @Override
  public String text() {
    return text;
  }

  @Override
  public void setText(final Context context, final String text) {
    this.text = text;
  }

  @Override
  public void setColor(final Context context, final ModelColor color) {}

  @Override
  public Font font() {
    return font;
  }

  @Override
  public void setFont(final Context context, final Font font) {
    this.font = (MockeryFont) font;
  }

  @Override
  public int getIndexAtConverse(final Context context, final double converse) {
    throw new DeadCode();
  }

  @Override
  public double getConverseAtIndex(final int index) {
    if (index > text.length()) throw new AssertionError();
    return font.measurer().getWidth(text.substring(0, index));
  }

  @Override
  public double converseSpan() {
    return font.measurer().getWidth(text);
  }

  @Override
  public Object inner_() {
    return null;
  }

  @Override
  public double ascent() {
    return (font.size * 8) / 10;
  }

  @Override
  public double descent() {
    return (font.size * 2) / 10;
  }
}
