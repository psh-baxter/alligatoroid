package com.zarbosoft.merman.editor.wall.bricks;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.display.DisplayNode;
import com.zarbosoft.merman.editor.display.Font;
import com.zarbosoft.merman.editor.display.Text;
import com.zarbosoft.merman.editor.visual.Vector;
import com.zarbosoft.merman.editor.wall.Brick;
import com.zarbosoft.merman.editor.wall.BrickInterface;
import com.zarbosoft.merman.syntax.style.Style;

public class BrickText extends Brick {
  private final Font font;
  public Text text;

  public BrickText(
      final Context context,
      final BrickInterface inter,
      Style.SplitMode splitMode,
      Style style,
      int hackAvoidChanged) {
    super(inter, style, splitMode);
    text = context.display.text();
    text.setColor(context, style.color);
    text.setFont(context, Context.getFont(style, context));
    font = Context.getFont(style, context);
  }

  @Override
  public int descent() {
    return text.descent();
  }

  @Override
  public int ascent() {
    return text.ascent();
  }

  public BrickText(
      final Context context, final BrickInterface inter, Style.SplitMode splitMode, Style style) {
    this(context, inter, splitMode, style, 0);
    changed(context);
  }

  @Override
  public int converseEdge() {
    return text.converse() + converseSpan;
  }

  @Override
  public int converseSpan() {
    return converseSpan;
  }

  @Override
  public DisplayNode getDisplayNode() {
    return text;
  }

  @Override
  public void setConverse(final Context context, final int minConverse, final int converse) {
    this.preAlignConverse = minConverse;
    text.setConverse(converse);
  }

  @Override
  public void allocateTransverse(final Context context, final int ascent, final int descent) {
    text.setBaselineTransverse(ascent);
  }

  public void setText(final Context context, final String text) {
    this.text.setText(context, text.replaceAll("\\p{Cntrl}", context.syntax.unprintable));
    this.converseSpan = font.getWidth(this.text.text());
    changed(context);
  }

  @Override
  public int getConverse() {
    return text.converse();
  }

  public Font getFont() {
    return text.font();
  }

  public int getConverseOffset(final int index) {
    return text.getConverseAtIndex(index);
  }

  public int getUnder(final Context context, final Vector point) {
    return text.getIndexAtConverse(context, point.converse);
  }
}
