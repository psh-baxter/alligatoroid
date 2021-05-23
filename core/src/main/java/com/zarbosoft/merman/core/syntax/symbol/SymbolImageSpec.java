package com.zarbosoft.merman.core.syntax.symbol;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.MultiError;
import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.display.CourseDisplayNode;
import com.zarbosoft.merman.core.display.Image;
import com.zarbosoft.merman.core.syntax.AtomType;
import com.zarbosoft.merman.core.syntax.style.Style;
import com.zarbosoft.merman.core.wall.Brick;
import com.zarbosoft.merman.core.wall.BrickInterface;
import com.zarbosoft.merman.core.wall.bricks.BrickImage;

public class SymbolImageSpec extends Symbol {
  public final Style style;
  public final Style.SplitMode splitMode;

  public static class Config {
    public Style.SplitMode splitMode = Style.SplitMode.NEVER;
    public Style.Config style = new Style.Config();

    public Config splitMode(Style.SplitMode mode) {
      this.splitMode = mode;
      return this;
    }
  }

  public SymbolImageSpec(Config config) {
      this.style = new Style(config.style);
    splitMode = config.splitMode;
  }

  @Override
  public CourseDisplayNode createDisplay(final Context context) {
    Image image = context.display.image();
    image.setImage(context, style.image);
    image.rotate(context, style.rotate);
    return image;
  }

  @Override
  public Brick createBrick(final Context context, final BrickInterface inter) {
    return new BrickImage(context, inter, splitMode, style);
  }

  @Override
  public void finish(MultiError errors, SyntaxPath typePath, AtomType atomType) {
  }
}
