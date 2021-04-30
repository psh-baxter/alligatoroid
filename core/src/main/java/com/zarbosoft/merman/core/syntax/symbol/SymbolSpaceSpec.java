package com.zarbosoft.merman.core.syntax.symbol;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.display.Blank;
import com.zarbosoft.merman.core.display.CourseDisplayNode;
import com.zarbosoft.merman.core.syntax.style.Style;
import com.zarbosoft.merman.core.wall.Brick;
import com.zarbosoft.merman.core.wall.BrickInterface;
import com.zarbosoft.merman.core.wall.bricks.BrickEmpty;

public class SymbolSpaceSpec extends Symbol {
  public final Style.SplitMode splitMode;
  public final Style style;

  public static class Config {
    public Style.SplitMode splitMode = Style.SplitMode.NEVER;
    public Style style = new Style(new Style.Config());

      public Config style(Style style) {
      this.style = style;
      return this;
    }

    public Config splitMode(Style.SplitMode splitMode) {
      this.splitMode = splitMode;
      return this;
    }
  }

  public SymbolSpaceSpec(Config config) {
    this.style = config.style;
    this.splitMode = config.splitMode;
  }

  @Override
  public CourseDisplayNode createDisplay(final Context context) {
    final Blank blank = context.display.blank();
    blank.setConverseSpan(context, style.space);
    return blank;
  }

  @Override
  public Brick createBrick(final Context context, final BrickInterface inter) {
    return new BrickEmpty(context, inter, splitMode, style);
  }
}
