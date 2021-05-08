package com.zarbosoft.merman.core.visual.visuals;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.syntax.front.FrontSymbol;
import com.zarbosoft.merman.core.visual.Visual;
import com.zarbosoft.merman.core.visual.VisualLeaf;
import com.zarbosoft.merman.core.visual.VisualParent;
import com.zarbosoft.merman.core.visual.alignment.Alignment;
import com.zarbosoft.merman.core.visual.condition.ConditionAttachment;
import com.zarbosoft.merman.core.wall.Brick;
import com.zarbosoft.merman.core.wall.BrickInterface;
import com.zarbosoft.rendaw.common.TSList;

import javax.annotation.Nonnull;

public class VisualSymbol extends Visual
    implements VisualLeaf, ConditionAttachment.Listener, BrickInterface {
  private final FrontSymbol frontSymbol;
  public VisualParent parent;
  public Brick brick = null;
  public ConditionAttachment condition = null;

  public VisualSymbol(
      final VisualParent parent,
      final FrontSymbol frontSymbol,
      final ConditionAttachment condition,
      final int visualDepth) {
    super(visualDepth);
    this.parent = parent;
    this.frontSymbol = frontSymbol;
    if (condition != null) {
      this.condition = condition;
      condition.register(this);
    }
  }

  @Override
  public void conditionChanged(final Context context, final boolean show) {
    if (show) {
      context.triggerIdleLayBricks(parent, 0, 1, 1, null, null);
    } else if (brick != null) {
      brick.destroy(context);
    }
  }

  @Override
  public VisualParent parent() {
    return parent;
  }

  @Override
  public boolean selectAnyChild(final Context context) {
    return false;
  }

  @Override
  public @Nonnull CreateBrickResult createOrGetCornerstoneCandidate(final Context context) {
    if (condition != null)
      return CreateBrickResult
          .empty(); // Cornerstones can't suddenly disappear without cursor changing
    if (brick != null) return CreateBrickResult.brick(brick);
    brick = frontSymbol.type.createBrick(context, this);
    return CreateBrickResult.brick(brick);
  }

  @Override
  public @Nonnull ExtendBrickResult createFirstBrick(final Context context) {
    if (brick != null) return ExtendBrickResult.exists();
    if (condition != null && !condition.show()) return ExtendBrickResult.empty();
    brick = frontSymbol.type.createBrick(context, this);
    return ExtendBrickResult.brick(brick);
  }

  @Override
  public @Nonnull ExtendBrickResult createLastBrick(final Context context) {
    return createFirstBrick(context);
  }

  @Override
  public Brick getFirstBrick(final Context context) {
    return brick;
  }

  @Override
  public Brick getLastBrick(final Context context) {
    return brick;
  }

  @Override
  public void compact(Context context) {
    if (brick != null) brick.changed(context);
  }

  @Override
  public void expand(Context context) {
    if (brick != null) brick.changed(context);
  }

  @Override
  public void getLeafBricks(final Context context, TSList<Brick> bricks) {
    if (brick == null) return;
    bricks.add(brick);
  }

  @Override
  public void root(
      final Context context,
      final VisualParent parent,
      final int visualDepth,
      final int depthScore) {
    super.root(context, parent, visualDepth, depthScore);
    expand(context);
  }

  @Override
  public void uproot(final Context context, final Visual root) {
    if (brick != null) brick.destroy(context);
    if (condition != null) condition.destroy(context);
  }

  @Override
  public VisualLeaf getVisual() {
    return this;
  }

  @Override
  public ExtendBrickResult createPrevious(final Context context) {
    return parent.createPreviousBrick(context);
  }

  @Override
  public ExtendBrickResult createNext(final Context context) {
    return parent.createNextBrick(context);
  }

  @Override
  public void brickDestroyed(final Context context) {
    brick = null;
  }

  @Override
  public Alignment findAlignment(String alignment) {
    return parent.atomVisual().findAlignment(alignment);
  }
}
