package com.zarbosoft.merman.core.editor.visual.visuals;

import com.zarbosoft.merman.core.editor.Context;
import com.zarbosoft.merman.core.editor.visual.alignment.Alignment;
import com.zarbosoft.merman.core.editor.visual.Visual;
import com.zarbosoft.merman.core.editor.visual.VisualLeaf;
import com.zarbosoft.merman.core.editor.visual.VisualParent;
import com.zarbosoft.merman.core.editor.visual.condition.ConditionAttachment;
import com.zarbosoft.merman.core.editor.wall.Brick;
import com.zarbosoft.merman.core.editor.wall.BrickInterface;
import com.zarbosoft.merman.core.syntax.front.FrontSymbol;
import com.zarbosoft.rendaw.common.TSList;

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
  public Brick createOrGetFirstBrick(final Context context) {
    if (brick != null) return brick;
    return createFirstBrick(context);
  }

  @Override
  public Brick createFirstBrick(final Context context) {
    if (brick != null) return null;
    if (condition != null && !condition.show()) return null;
    brick = frontSymbol.type.createBrick(context, this);
    return brick;
  }

  @Override
  public Brick createLastBrick(final Context context) {
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
    if (brick != null)
    brick.changed(context);
  }

  @Override
  public void expand(Context context) {
    if (brick != null)
    brick.changed(context);
  }

  @Override
  public void getLeafBricks(
          final Context context,
          TSList<Brick> bricks) {
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
  public Brick createPrevious(final Context context) {
    return parent.createPreviousBrick(context);
  }

  @Override
  public Brick createNext(final Context context) {
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
