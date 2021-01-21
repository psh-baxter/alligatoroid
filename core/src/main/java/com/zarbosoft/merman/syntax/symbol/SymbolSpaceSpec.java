package com.zarbosoft.merman.syntax.symbol;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.display.Blank;
import com.zarbosoft.merman.editor.display.DisplayNode;
import com.zarbosoft.merman.editor.visual.tags.Tags;
import com.zarbosoft.merman.editor.wall.Brick;
import com.zarbosoft.merman.editor.wall.BrickInterface;
import com.zarbosoft.merman.editor.wall.bricks.BrickSpace;
import com.zarbosoft.merman.syntax.style.Style;

public class SymbolSpaceSpec extends Symbol {
  @Override
  public DisplayNode createDisplay(final Context context) {
    final Blank blank = context.display.blank();
    return blank;
  }

  @Override
  public void style(final Context context, final DisplayNode node, final Style style) {}

  @Override
  public Brick createBrick(final Context context, final BrickInterface inter) {
    return new BrickSpace(context, inter);
  }

  @Override
  public String partTag() {
    return Tags.TAG_SYMBOL_SPACE;
  }
}
