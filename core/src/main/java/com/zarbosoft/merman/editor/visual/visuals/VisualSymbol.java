package com.zarbosoft.merman.editor.visual.visuals;

import com.google.common.collect.Iterables;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.visual.Alignment;
import com.zarbosoft.merman.editor.visual.Visual;
import com.zarbosoft.merman.editor.visual.VisualLeaf;
import com.zarbosoft.merman.editor.visual.VisualParent;
import com.zarbosoft.merman.editor.visual.condition.ConditionAttachment;
import com.zarbosoft.merman.editor.visual.tags.TagsChange;
import com.zarbosoft.merman.editor.wall.Brick;
import com.zarbosoft.merman.editor.wall.BrickInterface;
import com.zarbosoft.merman.misc.ROMap;
import com.zarbosoft.merman.misc.TSSet;
import com.zarbosoft.merman.syntax.front.FrontSymbol;
import com.zarbosoft.merman.syntax.style.Style;
import com.zarbosoft.rendaw.common.Pair;

import java.util.Arrays;
import java.util.stream.Stream;

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
      context.idleLayBricks(parent, 0, 1, 1, null, null);
    } else if (brick != null) {
      brick.destroy(context);
    }
  }

  @Override
  public VisualParent parent() {
    return parent;
  }

  @Override
  public boolean selectDown(final Context context) {
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
    context.bricksCreated(this, brick);
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
  public void compact(Context context) {}

  @Override
  public void expand(Context context) {}

  public void tagsChanged(final Context context) {
    if (brick != null) {
      brick.tagsChanged(context);
    }
  }

  @Override
  public Iterable<Pair<Brick, Brick.Properties>> getLeafPropertiesForTagsChange(
      final Context context, final TagsChange change) {
    if (brick == null) return Iterables.concat();
    return Arrays.asList(new Pair<>(brick, brick.getPropertiesForTagsChange(context, change)));
  }

  @Override
  public void root(
      final Context context,
      final VisualParent parent,
      final ROMap<String, Alignment> alignments,
      final int visualDepth,
      final int depthScore) {
    super.root(context, parent, alignments, visualDepth, depthScore);
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
  public Alignment getAlignment(final Style style) {
    return parent.atomVisual().getAlignment(style.alignment);
  }

  @Override
  public TSSet<String> getTags(final Context context) {
    return atomVisual().getTags(context).addAll(frontSymbol.tags).add(frontSymbol.type.partTag());
  }

  @Override
  public Stream<Brick> streamBricks() {
    if (brick == null) return Stream.empty();
    return Stream.of(brick);
  }
}
