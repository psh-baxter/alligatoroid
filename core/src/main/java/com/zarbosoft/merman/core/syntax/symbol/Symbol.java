package com.zarbosoft.merman.core.syntax.symbol;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.display.DisplayNode;
import com.zarbosoft.merman.core.wall.Brick;
import com.zarbosoft.merman.core.wall.BrickInterface;

public abstract class Symbol {
  public abstract DisplayNode createDisplay(Context context);

  public abstract Brick createBrick(Context context, BrickInterface inter);
}
