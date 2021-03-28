package com.zarbosoft.merman.core.syntax.symbol;

import com.zarbosoft.merman.core.editor.Context;
import com.zarbosoft.merman.core.editor.display.DisplayNode;
import com.zarbosoft.merman.core.editor.wall.Brick;
import com.zarbosoft.merman.core.editor.wall.BrickInterface;

public abstract class Symbol {
  public abstract DisplayNode createDisplay(Context context);

  public abstract Brick createBrick(Context context, BrickInterface inter);
}
