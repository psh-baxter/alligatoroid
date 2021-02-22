package com.zarbosoft.merman.editor.visual.alignment;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.visual.visuals.VisualAtom;
import com.zarbosoft.merman.editor.wall.Brick;
import com.zarbosoft.rendaw.common.TSList;

public abstract class Alignment {
  public double converse = 0;
  protected TSList<Brick> bricks = new TSList<>();
  protected TSList<Alignment> derived = new TSList<>();

  public abstract void feedback(Context context, double converse);

  public void changed(final Context context) {
    for (Brick brick : bricks) {
      brick.changed(context);
    }
    for (Alignment alignment : derived) {
      alignment.changed(context);
    }
  }

  public void addDerived(Alignment alignment) {
    derived.add(alignment);
  }

  public void removeDerived(Alignment alignment) {
    derived.removeVal(alignment);
  }

  public void addBrick(Context context, Brick brick) {
    bricks.add(brick);
  }

  public void removeBrick(Context context, Brick brick) {
    bricks.removeVal(brick);
  }

  /**
   * Placed somewhere new in tree
   *
   * @param context
   * @param atom
   */
  public abstract void root(Context context, VisualAtom atom);

  public abstract void destroy(Context context);
}
