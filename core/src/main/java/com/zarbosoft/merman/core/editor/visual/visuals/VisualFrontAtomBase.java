package com.zarbosoft.merman.core.editor.visual.visuals;

import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.document.values.Field;
import com.zarbosoft.merman.core.document.values.FieldAtom;
import com.zarbosoft.merman.core.editor.Action;
import com.zarbosoft.merman.core.editor.Context;
import com.zarbosoft.merman.core.editor.Cursor;
import com.zarbosoft.merman.core.editor.Hoverable;
import com.zarbosoft.merman.core.editor.Path;
import com.zarbosoft.merman.core.editor.SelectionState;
import com.zarbosoft.merman.core.editor.visual.Vector;
import com.zarbosoft.merman.core.editor.visual.Visual;
import com.zarbosoft.merman.core.editor.visual.VisualLeaf;
import com.zarbosoft.merman.core.editor.visual.VisualParent;
import com.zarbosoft.merman.core.editor.visual.alignment.Alignment;
import com.zarbosoft.merman.core.editor.visual.attachments.BorderAttachment;
import com.zarbosoft.merman.core.editor.wall.Brick;
import com.zarbosoft.merman.core.editor.wall.BrickInterface;
import com.zarbosoft.merman.core.syntax.symbol.Symbol;
import com.zarbosoft.rendaw.common.DeadCode;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSList;

public abstract class VisualFrontAtomBase extends Visual implements VisualLeaf {
  private final Symbol ellipsisSpec;
  protected VisualAtom body;
  VisualParent parent;
  private NestedHoverable hoverable;
  private NestedCursor selection;
  private Brick ellipsis = null;

  public VisualFrontAtomBase(final int visualDepth, Symbol ellipsis) {
    super(visualDepth);
    ellipsisSpec = ellipsis;
  }

  @Override
  public void compact(final Context context) {}

  @Override
  public void expand(final Context context) {}

  public abstract void dispatch(VisualNestedDispatcher dispatcher);

  public abstract Atom atomGet();

  public abstract String nodeType();

  protected abstract Field value();

  protected abstract Path getBackPath();

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

  @Override
  public ROPair<Hoverable, Boolean> hover(final Context context, final Vector point) {
    if (selection != null) return null;
    if (hoverable != null) {
      return new ROPair<>(hoverable, false);
    } else if (ellipsis != null) {
      return new ROPair<>(hoverable = new NestedHoverable(this, context, ellipsis, ellipsis), true);
    } else {
      return new ROPair<>(
          hoverable =
              new NestedHoverable(
                  this, context, body.getFirstBrick(context), body.getLastBrick(context)),
          true);
    }
  }

  @Override
  public void getLeafBricks(final Context context, TSList<Brick> bricks) {
    body.getLeafBricks(context, bricks);
  }

  private Brick createEllipsis(final Context context) {
    if (ellipsis != null) return null;
    ellipsis =
        ellipsisSpec.createBrick(
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
              public Alignment findAlignment(String alignment) {
                return parent.atomVisual().findAlignment(alignment);
              }
            });
    return ellipsis;
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

  @Override
  public boolean selectAnyChild(final Context context) {
    return value().selectInto(context);
  }

  public interface VisualNestedDispatcher {
    void handle(VisualFrontAtomFromArray visual);

    void handle(VisualFrontAtom visual);
  }

  public static class NestedCursor extends Cursor {
    private final ROList<Action> actions;
    public VisualFrontAtomBase base;
    private BorderAttachment border;

    public NestedCursor(VisualFrontAtomBase base, final Context context) {
      this.base = base;
      border = new BorderAttachment(context, context.syntax.cursorStyle.obbox);
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
      return base.getBackPath().add(FieldAtom.SYNTAX_PATH_KEY);
    }

    @Override
    public void dispatch(Dispatcher dispatcher) {
      dispatcher.handle(this);
    }

    public Brick nudge(final Context context) {
      final Brick first = base.body.createOrGetFirstBrick(context);
      context.wall.setCornerstone(
          context,
          first,
          () -> base.parent.getPreviousBrick(context),
          () -> base.parent.getNextBrick(context));
      return first;
    }
  }

  private static class VisualNodeSelectionState implements SelectionState {
    private final Field field;

    private VisualNodeSelectionState(final Field field) {
      this.field = field;
    }

    @Override
    public void select(final Context context) {
      field.selectInto(context);
    }
  }

  private static class AtomActionEnter implements Action {
    private final VisualFrontAtomBase base;

    public AtomActionEnter(VisualFrontAtomBase base) {
      this.base = base;
    }

    public String id() {
      return "enter";
    }

    @Override
    public void run(final Context context) {
      base.body.selectAnyChild(context);
    }
  }

  private static class AtomActionExit implements Action {
    private final VisualFrontAtomBase base;

    public AtomActionExit(VisualFrontAtomBase base) {
      this.base = base;
    }

    public String id() {
      return "exit";
    }

    @Override
    public void run(final Context context) {

      if (base.value().atomParentRef == null) return;
      base.value().atomParentRef.selectAtomParent(context);
    }
  }

  private static class AtomActionNext implements Action {
    private final VisualFrontAtomBase base;

    public AtomActionNext(VisualFrontAtomBase base) {
      this.base = base;
    }

    public String id() {
      return "next";
    }

    @Override
    public void run(final Context context) {
      base.parent.selectNext(context);
    }
  }

  private static class AtomActionPrevious implements Action {
    private final VisualFrontAtomBase base;

    public AtomActionPrevious(VisualFrontAtomBase base) {
      this.base = base;
    }

    public String id() {
      return "previous";
    }

    @Override
    public void run(final Context context) {
      base.parent.selectPrevious(context);
    }
  }

  private static class AtomActionWindow implements Action {
    private final VisualFrontAtomBase base;

    public AtomActionWindow(VisualFrontAtomBase base) {
      this.base = base;
    }

    public String id() {
      return "window";
    }

    @Override
    public void run(final Context context) {
      final Atom root = base.atomGet();
      if (!root.visual.selectAnyChild(context)) return;
      context.windowExact(root);
      context.triggerIdleLayBricksOutward();
    }
  }

  private static class NestedHoverable extends Hoverable {
    public final BorderAttachment border;
    private final VisualFrontAtomBase visual;

    private NestedHoverable(
        VisualFrontAtomBase visual, final Context context, final Brick first, final Brick last) {
      border = new BorderAttachment(context, context.syntax.hoverStyle.obbox);
      border.setFirst(context, first);
      border.setLast(context, last);
      this.visual = visual;
    }

    @Override
    public Path getSyntaxPath() {
      return visual.getBackPath().add(FieldAtom.SYNTAX_PATH_KEY);
    }

    @Override
    protected void clear(final Context context) {
      border.destroy(context);
      visual.hoverable = null;
    }

    @Override
    public void select(final Context context) {
      visual.selectAnyChild(context);
    }

    @Override
    public VisualAtom atom() {
      return visual.parent.atomVisual();
    }

    @Override
    public Visual visual() {
      return visual;
    }
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
    public ROPair<Hoverable, Boolean> hover(final Context context, final Vector point) {
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
}
