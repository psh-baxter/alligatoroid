package com.zarbosoft.merman.syntax.symbol;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.display.DisplayNode;
import com.zarbosoft.merman.editor.visual.tags.PartTag;
import com.zarbosoft.merman.editor.wall.Brick;
import com.zarbosoft.merman.editor.wall.BrickInterface;
import com.zarbosoft.merman.syntax.style.Style;

public abstract class Symbol {
  public abstract DisplayNode createDisplay(Context context);

  public abstract void style(Context context, DisplayNode node, Style.Baked style);

  public abstract Brick createBrick(Context context, BrickInterface inter);

  public abstract PartTag partTag();
}
