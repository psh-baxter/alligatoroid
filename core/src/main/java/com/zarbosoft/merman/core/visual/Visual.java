package com.zarbosoft.merman.core.visual;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.Hoverable;
import com.zarbosoft.merman.core.visual.visuals.VisualAtom;
import com.zarbosoft.merman.core.wall.Brick;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSList;

import javax.annotation.Nonnull;

public abstract class Visual {
  public int visualDepth;

  public Visual(final int visualDepth) {
    this.visualDepth = visualDepth;
  }

  public abstract VisualParent parent();

  /**
   * Used to get the cornerstone when changing selection, to seed bricklaying etc. All atoms must
   * return a value but if a field doesn't have a suitable cornerstone brick, it should return null.
   *
   * @param context
   * @return
   */
  public abstract @Nonnull CreateBrickResult createOrGetCornerstoneCandidate(Context context);

  /**
   * Used for laying bricks forward
   *
   * @param context
   * @return A new brick or null (no elements afterward or brick already exists)
   */
  public abstract @Nonnull ExtendBrickResult createFirstBrick(Context context);

  /**
   * Used for laying bricks backward
   *
   * @param context
   * @return A new brick or null (no elements afterward or brick already exists)
   */
  public abstract @Nonnull ExtendBrickResult createLastBrick(Context context);

  /**
   * Used for checking brick laying bounds
   *
   * @param context
   * @return brick or null if the first visual that would produce a brick hasn't yet
   */
  public abstract Brick getFirstBrick(Context context);

  /**
   * Used for checking brick laying bounds
   *
   * @param context
   * @return brick or null if the last visual that would produce a brick hasn't yet
   */
  public abstract Brick getLastBrick(Context context);

  public ROList<Visual> children() {
    return ROList.empty;
  }

  public abstract void compact(Context context);

  public abstract void expand(Context context);

  /**
   * Returns bricks in order because the expansion algorithm tests expandability in courses in order
   *
   * @param context
   * @param bricks
   */
  public abstract void getLeafBricks(Context context, TSList<Brick> bricks);

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

  public ROPair<Hoverable, Boolean> hover(final Context context, final Vector point) {
    return parent().hover(context, point);
  }

  public static class CreateBrickResult {
    /** No contents, no brick to create; skip and continue */
    public final boolean empty;
    /** Created or existing brick */
    public final Brick brick;

    private CreateBrickResult(boolean empty, Brick brick) {
      this.empty = empty;
      this.brick = brick;
    }

    public static CreateBrickResult empty() {
      return new CreateBrickResult(true, null);
    }

    public static CreateBrickResult brick(Brick brick) {
      return new CreateBrickResult(false, brick);
    }
  }

  public static class ExtendBrickResult {
    /** No contents, no brick to create; skip and continue */
    public final boolean empty;
    /** Brick was already created; stop laying bricks in this direction */
    public final boolean exists;
    /**
     * Created this brick, place and use as source for next. At the root level, an exists/empty
     * result means nothing left to lay so stop.
     */
    public final Brick brick;

    private ExtendBrickResult(boolean empty, boolean exists, Brick brick) {
      this.empty = empty;
      this.exists = exists;
      this.brick = brick;
    }

    public static ExtendBrickResult empty() {
      return new ExtendBrickResult(true, false, null);
    }

    public static ExtendBrickResult exists() {
      return new ExtendBrickResult(false, true, null);
    }

    public static ExtendBrickResult brick(Brick brick) {
      if (brick == null) throw new Assertion();
      return new ExtendBrickResult(false, false, brick);
    }
  }
}
