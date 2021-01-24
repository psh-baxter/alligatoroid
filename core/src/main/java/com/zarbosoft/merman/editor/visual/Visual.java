package com.zarbosoft.merman.editor.visual;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.Hoverable;
import com.zarbosoft.merman.editor.visual.tags.TagsChange;
import com.zarbosoft.merman.editor.visual.visuals.VisualAtom;
import com.zarbosoft.merman.editor.wall.Brick;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROMap;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSList;

public abstract class Visual {
  public int visualDepth;

  public Visual(final int visualDepth) {
    this.visualDepth = visualDepth;
  }

  public abstract VisualParent parent();

  public abstract void tagsChanged(Context context);

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

  public abstract void getLeafPropertiesForTagsChange(
          Context context, TSList<ROPair<Brick, Brick.Properties>> brickProperties, TagsChange change);

  public int depthScore() {
    final VisualParent parent = parent();
    if (parent == null) return 0;
    final VisualAtom atomVisual = parent.atomVisual();
    if (atomVisual == null) return 0;
    return atomVisual.depthScore;
  }

  public abstract void uproot(Context context, Visual root);

  public void root(
      final Context context,
      final VisualParent parent,
      final ROMap<String, Alignment> alignments,
      final int depth,
      final int depthScore) {
    this.visualDepth = depth;
  }

  public abstract boolean selectAnyChild(final Context context);

  public Hoverable hover(final Context context, final Vector point) {
    return parent().hover(context, point);
  }
}
