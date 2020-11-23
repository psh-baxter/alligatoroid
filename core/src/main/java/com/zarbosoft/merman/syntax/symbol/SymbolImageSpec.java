package com.zarbosoft.merman.syntax.symbol;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.display.DisplayNode;
import com.zarbosoft.merman.editor.display.Image;
import com.zarbosoft.merman.editor.visual.tags.PartTag;
import com.zarbosoft.merman.editor.wall.Brick;
import com.zarbosoft.merman.editor.wall.BrickInterface;
import com.zarbosoft.merman.editor.wall.bricks.BrickImage;
import com.zarbosoft.merman.syntax.style.Style;

import java.nio.file.Paths;

public class SymbolImageSpec extends Symbol {
  @Override
  public DisplayNode createDisplay(final Context context) {
    return context.display.image();
  }

  @Override
  public void style(final Context context, final DisplayNode node, final Style.Baked style) {
    final Image image = (Image) node;
    image.setImage(context, Paths.get(style.image));
    image.rotate(context, style.rotate);
  }

  @Override
  public Brick createBrick(final Context context, final BrickInterface inter) {
    return new BrickImage(context, inter);
  }

  public static PartTag partTag = new PartTag("symbol-image");
  @Override
  public PartTag partTag() {
    return partTag;
  }
}
