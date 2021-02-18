package com.zarbosoft.merman.editor.visual;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.Hoverable;
import com.zarbosoft.merman.editor.visual.visuals.VisualAtom;
import com.zarbosoft.merman.editor.wall.Brick;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.TSList;

public abstract class Visual {
  public int visualDepth;

  public Visual(final int visualDepth) {
    this.visualDepth = visualDepth;
  }

  public abstract VisualParent parent();

  public abstract Brick createOrGetFirstBrick(Context context);

  public abstract Brick createFirstBrick(Context context);

  public abstract Brick createLastBrick(Context context);

  public abstract Brick getFirstBrick(Context context);

  public abstract Brick getLastBrick(Context context);

  public ROList<Visual> children() {
    return ROList.empty;
  }

  public abstract void compact(Context context);

  public abstract void expand(Context context);

  /**
   * Returns bricks in order because the expansion algorithm tests expandability in courses in order
   *  @param context
   * @param bricks*/
  public abstract void getLeafBricks(
          Context context, TSList<Brick> bricks);

  public int depthScore() {
    final VisualParent parent = parent();
    if (parent == null) return 0;
    final VisualAtom atomVisual = parent.atomVisual();
    if (atomVisual == null) return 0;
    return atomVisual.depthScore;
  }

  public abstract void uproot(Context context, Visual root);

  public void root(
      final Context context, final VisualParent parent, final int depth, final int depthScore) {
    this.visualDepth = depth;
  }

  public abstract boolean selectAnyChild(final Context context);

  public Hoverable hover(final Context context, final Vector point) {
    return parent().hover(context, point);
  }
}
