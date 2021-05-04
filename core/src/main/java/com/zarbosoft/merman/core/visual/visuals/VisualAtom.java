package com.zarbosoft.merman.core.visual.visuals;

import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.Hoverable;
import com.zarbosoft.merman.core.visual.Vector;
import com.zarbosoft.merman.core.visual.Visual;
import com.zarbosoft.merman.core.visual.VisualParent;
import com.zarbosoft.merman.core.visual.alignment.Alignment;
import com.zarbosoft.merman.core.wall.Brick;
import com.zarbosoft.merman.core.syntax.AtomType;
import com.zarbosoft.merman.core.syntax.alignments.AlignmentSpec;
import com.zarbosoft.merman.core.syntax.front.FrontSpec;
import com.zarbosoft.rendaw.common.DeadCode;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.ReverseIterable;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;

import java.util.Map;

public class VisualAtom extends Visual {
  public final Atom atom;
  public final TSList<Visual> children = new TSList<>();
  /** Merged map of parent alignments and this alignments */
  private final TSMap<String, Alignment> localAlignments = new TSMap<>();
  private final TSList<Visual> selectable = new TSList<>();
  public int depthScore = 0;
  public boolean compact = false;
  private VisualParent parent;

  public VisualAtom(
      final Context context,
      final VisualParent parent,
      final Atom atom,
      final int visualDepth,
      final int depthScore) {
    super(visualDepth);
    this.atom = atom;
    for (final Map.Entry<String, AlignmentSpec> entry : atom.type.alignments()) {
      localAlignments.put(entry.getKey(), entry.getValue().create());
    }
    rootInner(context, parent, visualDepth, depthScore);
    for (int index = 0; index < atom.type.front().size(); ++index) {
      FrontSpec front = atom.type.front().get(index);
      final Visual visual =
          front.createVisual(
              context,
              front.fieldId() == null
                  ? new ChildParent(index)
                  : new SelectableChildParent(index, selectable.size()),
              atom,
              this.visualDepth + 1,
              this.depthScore);
      children.add(visual);
      if (front.fieldId() != null) selectable.add(visual);
    }
    atom.visual = this;
  }

  @Override
  public VisualParent parent() {
    return parent;
  }

  public Alignment findAlignment(final String alignment) {
    Alignment found = localAlignments.getOpt(alignment);
    if (found != null) return found;
    return findParentAlignment(alignment);
  }

  @Override
  public boolean selectAnyChild(final Context context) {
    if (selectable.isEmpty()) return false;
    selectable.get(0).selectAnyChild(context);
    return true;
  }

  @Override
  public Brick createOrGetCornerstoneCandidate(final Context context) {
    for (Visual child : children) {
      Brick out = child.createOrGetCornerstoneCandidate(context);
      if (out != null) return out;
    }
    return null;
  }

  @Override
  public Brick createFirstBrick(final Context context) {
    for (Visual child : children) {
      Brick out = child.createFirstBrick(context);
      if (out != null) return out;
    }
    return null;
  }

  @Override
  public Brick createLastBrick(final Context context) {
    for (Visual child : new ReverseIterable<>(children)) {
      Brick out = child.createLastBrick(context);
      if (out != null) return out;
    }
    return null;
  }

  @Override
  public Brick getFirstBrick(final Context context) {
    return children.get(0).getFirstBrick(context);
  }

  @Override
  public Brick getLastBrick(final Context context) {
    return children.last().getLastBrick(context);
  }

  public int spacePriority() {
    return -atom.type.precedence();
  }

  @Override
  public void compact(final Context context) {
    compact = true;
    for (Visual child : children) {
      child.compact(context);
    }
  }

  @Override
  public void expand(final Context context) {
    compact = false;
    for (Visual c : children) {
      c.expand(context);
    }
  }

  @Override
  public void getLeafBricks(
          final Context context,
          TSList<Brick> bricks) {
    for (Visual child : children) {
      child.getLeafBricks(context, bricks);
    }
  }

  private void rootInner(
      final Context context,
      final VisualParent parent,
      final int visualDepth,
      final int depthScore) {
    compact = false;
    this.parent = parent;
    if (parent == null) {
      this.visualDepth = 0;
      this.depthScore = 0;
    } else {
      this.visualDepth = visualDepth;
      this.depthScore = depthScore + atom.type.depthScore();
    }
    for (final Map.Entry<String, Alignment> alignment : localAlignments) {
      alignment.getValue().root(context, this);
    }
  }

  @Override
  public void root(
      final Context context,
      final VisualParent parent,
      final int visualDepth,
      final int depthScore) {
    rootInner(context, parent, visualDepth, depthScore);
    for (int index = 0; index < children.size(); ++index) {
      final Visual child = children.get(index);
      child.root(context, child.parent(), this.visualDepth + 1, this.depthScore);
    }
  }

  @Override
  public void uproot(final Context context, final Visual root) {
    if (root == this) return;
    atom.visual = null;
    for (int i = children.size(); i > 0; --i) {
      Visual child = children.get(i - 1);
      child.uproot(context, root);
    }
    for (final Map.Entry<String, Alignment> entry : localAlignments)
      entry.getValue().destroy(context);
  }

  public AtomType type() {
    return atom.type;
  }

  public Alignment findParentAlignment(String key) {
    if (this.parent == null) return null;
    VisualAtom at = this.parent.atomVisual();
    while (true) {
      Alignment found = at.localAlignments.getOpt(key);
      if (found != null) {
        return found;
      }
      if (at.parent == null) break;
      at = at.parent.atomVisual();
    }
    return null;
  }

  private class ChildParent extends VisualParent {
    private final int index;

    public ChildParent(final int index) {
      this.index = index;
    }

    @Override
    public Visual visual() {
      return VisualAtom.this;
    }

    @Override
    public VisualAtom atomVisual() {
      return VisualAtom.this;
    }

    @Override
    public Brick createNextBrick(final Context context) {
      if (index + 1 < children.size()) return children.get(index + 1).createFirstBrick(context);
      if (parent == null) return null;
      if (context.windowAtom() == VisualAtom.this.atom) return null;
      return parent.createNextBrick(context);
    }

    @Override
    public Brick createPreviousBrick(final Context context) {
      if (index - 1 >= 0) return children.get(index - 1).createLastBrick(context);
      if (parent == null) return null;
      if (context.windowAtom() == VisualAtom.this.atom) return null;
      return parent.createPreviousBrick(context);
    }

    @Override
    public Brick findPreviousBrick(final Context context) {
      for (int at = index - 1; at >= 0; --at) {
        final Brick test = children.get(at).getLastBrick(context);
        if (test != null) return test;
      }
      if (context.windowAtom() == VisualAtom.this.atom) return null;
      if (parent == null) return null;
      return parent.findPreviousBrick(context);
    }

    @Override
    public Brick findNextBrick(final Context context) {
      for (int at = index + 1; at < children.size(); ++at) {
        final Brick test = children.get(at).getLastBrick(context);
        if (test != null) return test;
      }
      if (context.windowAtom() == VisualAtom.this.atom) return null;
      if (parent == null) return null;
      return parent.findNextBrick(context);
    }

    @Override
    public Brick getPreviousBrick(final Context context) {
      if (index == 0) {
        if (context.windowAtom() == VisualAtom.this.atom) return null;
        if (parent == null) return null;
        return parent.getPreviousBrick(context);
      } else return children.get(index - 1).getLastBrick(context);
    }

    @Override
    public Brick getNextBrick(final Context context) {
      if (index + 1 >= children.size()) {
        if (context.windowAtom() == VisualAtom.this.atom) return null;
        if (parent == null) return null;
        return parent.getNextBrick(context);
      } else return children.get(index + 1).getFirstBrick(context);
    }

    @Override
    public ROPair<Hoverable, Boolean> hover(final Context context, final Vector point) {
      if (parent == null) return null;
      return parent.hover(context, point);
    }

    @Override
    public boolean selectPrevious(final Context context) {
      throw new DeadCode();
    }

    @Override
    public boolean selectNext(final Context context) {
      throw new DeadCode();
    }
  }

  private class SelectableChildParent extends ChildParent {
    private final int selectableIndex;

    public SelectableChildParent(final int index, final int selectableIndex) {
      super(index);
      this.selectableIndex = selectableIndex;
    }

    @Override
    public boolean selectNext(final Context context) {
      int at = selectableIndex;
      while (++at < selectable.size()) if (selectable.get(at).selectAnyChild(context)) return true;
      return false;
    }

    @Override
    public boolean selectPrevious(final Context context) {
      int at = selectableIndex;
      while (--at >= 0) if (selectable.get(at).selectAnyChild(context)) return true;
      return false;
    }
  }
}
