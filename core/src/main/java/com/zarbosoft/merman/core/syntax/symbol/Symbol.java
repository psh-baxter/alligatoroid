package com.zarbosoft.merman.core.syntax.symbol;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.MultiError;
import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.display.CourseDisplayNode;
import com.zarbosoft.merman.core.syntax.AtomType;
import com.zarbosoft.merman.core.wall.Brick;
import com.zarbosoft.merman.core.wall.BrickInterface;

public abstract class Symbol {
  public abstract CourseDisplayNode createDisplay(Context context);

  public abstract Brick createBrick(Context context, BrickInterface inter);

    public abstract void finish(MultiError errors, SyntaxPath typePath, AtomType atomType);
}
