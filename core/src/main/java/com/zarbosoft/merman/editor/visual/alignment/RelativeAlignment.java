package com.zarbosoft.merman.editor.visual.alignment;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.visual.visuals.VisualAtom;
import com.zarbosoft.merman.editor.wall.Brick;

public class RelativeAlignment extends Alignment {
  private final String baseKey;
  private final int offset;
  public final boolean collapse;
  private Alignment base;

  public RelativeAlignment(
      /** Name of alignment in ancestors (closest) */
      final String baseKey,
      /** Pixel offset from base alignment */
      final int offset,
      /**
       * If true, doesn't contribute offset to derived alignments if this alignment doesn't have any
       * bricks
       */
      boolean collapse) {
    this.baseKey = baseKey;
    this.offset = offset;
    this.collapse = collapse;
    converse = offset;
  }

  @Override
  public void feedback(final Context context, final double position) {}

  @Override
  public void root(final Context context, final VisualAtom atom) {
    if (base != null) {
      base.removeDerived(this);
    }
    base = atom.findParentAlignment(baseKey);
    if (base == this) throw new AssertionError("Alignment parented to self");
    if (base != null) base.addDerived(this);
    changed(context);
  }

  @Override
  public void addBrick(Context context, Brick brick) {
    super.addBrick(context, brick);
    if (collapse && bricks.size() == 1) changed(context);
  }

  @Override
  public void removeBrick(Context context, Brick brick) {
    super.removeBrick(context, brick);
    if (collapse && bricks.isEmpty()) changed(context);
  }

  @Override
  public void destroy(final Context context) {}

  @Override
  public void changed(Context context) {
    converse = (base == null ? 0 : base.converse) + (collapse && bricks.isEmpty() ? 0 : offset);
    super.changed(context);
  }
}
