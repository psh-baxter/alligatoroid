package com.zarbosoft.merman.editor.visual.visuals;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.Value;
import com.zarbosoft.merman.editor.Action;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.Cursor;
import com.zarbosoft.merman.editor.Hoverable;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.editor.SelectionState;
import com.zarbosoft.merman.editor.visual.alignment.Alignment;
import com.zarbosoft.merman.editor.visual.Vector;
import com.zarbosoft.merman.editor.visual.Visual;
import com.zarbosoft.merman.editor.visual.VisualLeaf;
import com.zarbosoft.merman.editor.visual.VisualParent;
import com.zarbosoft.merman.editor.visual.attachments.BorderAttachment;
import com.zarbosoft.merman.editor.visual.tags.Tags;
import com.zarbosoft.merman.editor.visual.tags.TagsChange;
import com.zarbosoft.merman.editor.wall.Brick;
import com.zarbosoft.merman.editor.wall.BrickInterface;
import com.zarbosoft.merman.syntax.style.Style;
import com.zarbosoft.merman.syntax.symbol.Symbol;
import com.zarbosoft.rendaw.common.DeadCode;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.ROSet;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSSet;

public abstract class VisualFrontAtomBase extends Visual implements VisualLeaf {
  protected VisualAtom body;
  VisualParent parent;
  private NestedHoverable hoverable;
  private NestedCursor selection;
  private Brick ellipsis = null;

  @Override
  public void compact(final Context context) {}

  @Override
  public void expand(final Context context) {}

  public VisualFrontAtomBase(final int visualDepth) {
    super(visualDepth);
  }

  public interface VisualNestedDispatcher {
    void handle(VisualFrontAtomFromArray visual);

    void handle(VisualFrontAtom visual);
  }

  public abstract void dispatch(VisualNestedDispatcher dispatcher);

  public abstract Atom atomGet();

  public abstract String nodeType();

  protected abstract Value value();

  protected abstract Path getBackPath();

  protected abstract Symbol ellipsis();

  @Override
  public VisualParent parent() {
    return parent;
  }

  @Override
  public Brick getFirstBrick(final Context context) {
    if (ellipsize(context)) return ellipsis;
    if (body == null) return parent.getNextBrick(context);
    return body.getFirstBrick(context);
  }

  @Override
  public Brick getLastBrick(final Context context) {
    if (ellipsize(context)) return ellipsis;
    if (body == null) return parent.getPreviousBrick(context);
    return body.getLastBrick(context);
  }

  @Override
  public void root(
      final Context context,
      final VisualParent parent,
      final int visualDepth,
      final int depthScore) {
    this.parent = parent;
    if (ellipsize(context)) {
      if (body != null) {
        body.uproot(context, null);
        body = null;
        context.triggerIdleLayBricks(parent, 0, 1, 1, null, null);
      }
    } else {
      if (ellipsis != null) ellipsis.destroy(context);
      if (atomGet() != null) {
        if (body == null) {
          coreSet(context, atomGet());
          context.triggerIdleLayBricks(parent, 0, 1, 1, null, null);
        } else body.root(context, new NestedParent(), visualDepth + 1, depthScore);
      }
    }
  }

  @Override
  public void uproot(final Context context, final Visual root) {
    if (selection != null) context.clearSelection();
    if (hoverable != null) context.clearHover();
    if (ellipsis != null) ellipsis.destroy(context);
    if (body != null) {
      body.uproot(context, root);
      body = null;
    }
  }

  private class NestedHoverable extends Hoverable {
    public final BorderAttachment border;

    private NestedHoverable(final Context context, final Brick first, final Brick last) {
      border = new BorderAttachment(context, getBorderStyle(context, baseTags(context)).obbox);
      border.setFirst(context, first);
      border.setLast(context, last);
    }

    @Override
    protected void clear(final Context context) {
      border.destroy(context);
      hoverable = null;
    }

    @Override
    public void click(final Context context) {
      selectAnyChild(context);
    }

    @Override
    public VisualAtom atom() {
      return VisualFrontAtomBase.this.parent.atomVisual();
    }

    @Override
    public Visual visual() {
      return VisualFrontAtomBase.this;
    }

    @Override
    public void tagsChanged(final Context context) {
      border.setStyle(context, getBorderStyle(context, baseTags(context)).obbox);
    }
  }

  @Override
  public Hoverable hover(final Context context, final Vector point) {
    if (selection != null) return null;
    if (hoverable != null) {
    } else if (ellipsis != null) {
      hoverable = new NestedHoverable(context, ellipsis, ellipsis);
    } else {
      hoverable =
          new NestedHoverable(context, body.getFirstBrick(context), body.getLastBrick(context));
    }
    return hoverable;
  }

  @Override
  public void getLeafPropertiesForTagsChange(
      final Context context,
      TSList<ROPair<Brick, Brick.Properties>> brickProperties,
      final TagsChange change) {
    body.getLeafPropertiesForTagsChange(context, brickProperties, change);
  }

  private Brick createEllipsis(final Context context) {
    if (ellipsis != null) return null;
    ellipsis =
        ellipsis()
            .createBrick(
                context,
                new BrickInterface() {
                  @Override
                  public VisualLeaf getVisual() {
                    return VisualFrontAtomBase.this;
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
                    ellipsis = null;
                  }

                  @Override
                  public Alignment findAlignment(final Style style) {
                    return parent.atomVisual().findAlignment(style.alignment);
                  }

                  @Override
                  public TSSet<String> getTags(final Context context) {
                    return ellipsisTags(context);
                  }
                });
    ellipsis.tagsChanged(context);
    context.bricksCreated(this, ellipsis);
    return ellipsis;
  }

  public void tagsChanged(final Context context) {
    if (ellipsis != null) {
      ellipsis.tagsChanged(context);
    }
    if (selection != null) selection.tagsChanged(context);
    if (hoverable != null) hoverable.tagsChanged(context);
  }

  private boolean ellipsize(final Context context) {
    if (!context.window) return false;
    if (parent.atomVisual() == null) return false;
    return parent.atomVisual().depthScore >= context.ellipsizeThreshold;
  }

  @Override
  public Brick createOrGetFirstBrick(final Context context) {
    if (ellipsize(context)) {
      if (ellipsis != null) return ellipsis;
      return createEllipsis(context);
    } else return body.createOrGetFirstBrick(context);
  }

  @Override
  public Brick createFirstBrick(final Context context) {
    if (ellipsize(context)) {
      return createEllipsis(context);
    } else {
      return body.createFirstBrick(context);
    }
  }

  @Override
  public Brick createLastBrick(final Context context) {
    if (ellipsize(context)) {
      return createEllipsis(context);
    } else {
      return body.createLastBrick(context);
    }
  }

  public void select(final Context context) {
    if (selection != null) return;
    else if (hoverable != null) {
      context.clearHover();
    }
    selection = new NestedCursor(this, context);
    context.setCursor(selection);
  }

  private TSSet<String> baseTags(Context context) {
    return parent.atomVisual().getTags(context).add(Tags.TAG_PART_ATOM);
  }

  private TSSet<String> ellipsisTags(Context context) {
    return baseTags(context).add(Tags.TAG_PART_ELLIPSIS);
  }

  public static class NestedCursor extends Cursor {
    private final ROList<Action> actions;
    private BorderAttachment border;
    public VisualFrontAtomBase base;

    public NestedCursor(VisualFrontAtomBase base, final Context context) {
      this.base = base;
      border = new BorderAttachment(context, getBorderStyle(context).obbox);
      final Brick first = nudge(context);
      border.setFirst(context, first);
      border.setLast(context, base.body.getLastBrick(context));
      context.addActions(
          this.actions =
              TSList.of(
                  new AtomActionEnter(base),
                  new AtomActionExit(base),
                  new AtomActionNext(base),
                  new AtomActionPrevious(base),
                  new AtomActionWindow(base),
                  new AtomActionCopy(base)));
    }

    @Override
    public void clear(final Context context) {
      border.destroy(context);
      border = null;
      base.selection = null;
      context.removeActions(actions);
    }

    @Override
    public Visual getVisual() {
      return base;
    }

    @Override
    public SelectionState saveState() {
      return new VisualNodeSelectionState(base.value());
    }

    @Override
    public Path getSyntaxPath() {
      return base.getBackPath();
    }

    @Override
    public void tagsChanged(final Context context) {
      border.setStyle(context, getBorderStyle(context).obbox);
      super.tagsChanged(context);
    }

    @Override
    public ROSet<String> getTags(final Context context) {
      return base.baseTags(context).ro();
    }

    @Override
    public void dispatch(Dispatcher dispatcher) {
      dispatcher.handle(this);
    }

    public Brick nudge(final Context context) {
      final Brick first = base.body.createOrGetFirstBrick(context);
      context.foreground.setCornerstone(
          context,
          first,
          () -> base.parent.getPreviousBrick(context),
          () -> base.parent.getNextBrick(context));
      return first;
    }
  }

  private static class VisualNodeSelectionState implements SelectionState {
    private final Value value;

    private VisualNodeSelectionState(final Value value) {
      this.value = value;
    }

    @Override
    public void select(final Context context) {
      value.selectInto(context);
    }
  }

  protected void set(final Context context, final Atom data) {
    if (ellipsize(context)) return;
    boolean fixDeepSelection = false;
    boolean fixDeepHover = false;
    if (context.cursor != null) {
      VisualParent parent = context.cursor.getVisual().parent();
      while (parent != null) {
        final Visual visual = parent.visual();
        if (visual == this) {
          fixDeepSelection = true;
          break;
        }
        parent = visual.parent();
      }
    }
    if (hoverable == null && context.hover != null) {
      VisualParent parent = context.hover.visual().parent();
      while (parent != null) {
        final Visual visual = parent.visual();
        if (visual == this) {
          fixDeepHover = true;
          break;
        }
        parent = visual.parent();
      }
    }

    coreSet(context, data);
    context.triggerIdleLayBricks(parent, 0, 1, 1, null, null);

    if (fixDeepSelection) select(context);
    if (fixDeepHover) context.clearHover();
  }

  private void coreSet(final Context context, final Atom data) {
    if (body != null) body.uproot(context, null);
    this.body =
        (VisualAtom) data.ensureVisual(context, new NestedParent(), visualDepth + 1, depthScore());
    if (selection != null) selection.nudge(context);
  }

  private class NestedParent extends VisualParent {
    @Override
    public Visual visual() {
      return VisualFrontAtomBase.this;
    }

    @Override
    public VisualAtom atomVisual() {
      return parent.atomVisual();
    }

    @Override
    public Brick createPreviousBrick(final Context context) {
      return parent.createPreviousBrick(context);
    }

    @Override
    public Brick createNextBrick(final Context context) {
      return parent.createNextBrick(context);
    }

    @Override
    public void firstBrickChanged(final Context context, final Brick firstBrick) {
      if (selection != null) selection.border.setFirst(context, firstBrick);
      if (hoverable != null) hoverable.border.setFirst(context, firstBrick);
    }

    @Override
    public void lastBrickChanged(final Context context, final Brick lastBrick) {
      if (selection != null) selection.border.setFirst(context, lastBrick);
      if (hoverable != null) hoverable.border.setFirst(context, lastBrick);
    }

    @Override
    public Brick findPreviousBrick(final Context context) {
      return parent.findPreviousBrick(context);
    }

    @Override
    public Brick findNextBrick(final Context context) {
      return parent.findNextBrick(context);
    }

    @Override
    public Brick getPreviousBrick(final Context context) {
      return parent.getPreviousBrick(context);
    }

    @Override
    public Brick getNextBrick(final Context context) {
      return parent.getNextBrick(context);
    }

    @Override
    public Hoverable hover(final Context context, final Vector point) {
      return VisualFrontAtomBase.this.hover(context, point);
    }

    @Override
    public boolean selectNext(final Context context) {
      throw new DeadCode();
    }

    @Override
    public boolean selectPrevious(final Context context) {
      throw new DeadCode();
    }
  }

  @Override
  public boolean selectAnyChild(final Context context) {
    return value().selectInto(context);
  }

  private static class AtomActionEnter implements Action {
    public String id() {
        return "enter";
    }
    private final VisualFrontAtomBase base;

    public AtomActionEnter(VisualFrontAtomBase base) {
      this.base = base;
    }

    @Override
    public boolean run(final Context context) {
      return base.body.selectAnyChild(context);
    }
  }

  private static class AtomActionExit implements Action {
    public String id() {
        return "exit";
    }
    private final VisualFrontAtomBase base;

    public AtomActionExit(VisualFrontAtomBase base) {
      this.base = base;
    }

    @Override
    public boolean run(final Context context) {

      if (base.value().atomParentRef == null) return false;
      return base.value().atomParentRef.selectAtomParent(context);
    }
  }

  private static class AtomActionNext implements Action {
    public String id() {
        return "next";
    }
    private final VisualFrontAtomBase base;

    public AtomActionNext(VisualFrontAtomBase base) {
      this.base = base;
    }

    @Override
    public boolean run(final Context context) {
      return base.parent.selectNext(context);
    }
  }

  private static class AtomActionPrevious implements Action {
    public String id() {
        return "previous";
    }
    private final VisualFrontAtomBase base;

    public AtomActionPrevious(VisualFrontAtomBase base) {
      this.base = base;
    }

    @Override
    public boolean run(final Context context) {
      return base.parent.selectPrevious(context);
    }
  }

  private static class AtomActionWindow implements Action {
    public String id() {
        return "window";
    }
    private final VisualFrontAtomBase base;

    public AtomActionWindow(VisualFrontAtomBase base) {
      this.base = base;
    }

    @Override
    public boolean run(final Context context) {
      final Atom root = base.atomGet();
      if (!root.visual.selectAnyChild(context)) return false;
      context.windowExact(root);
      context.triggerIdleLayBricksOutward();
      return true;
    }
  }
}
