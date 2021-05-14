package com.zarbosoft.merman.core.visual.visuals;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.Hoverable;
import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.document.fields.Field;
import com.zarbosoft.merman.core.document.fields.FieldAtom;
import com.zarbosoft.merman.core.syntax.symbol.Symbol;
import com.zarbosoft.merman.core.visual.Vector;
import com.zarbosoft.merman.core.visual.Visual;
import com.zarbosoft.merman.core.visual.VisualLeaf;
import com.zarbosoft.merman.core.visual.VisualParent;
import com.zarbosoft.merman.core.visual.alignment.Alignment;
import com.zarbosoft.merman.core.visual.attachments.BorderAttachment;
import com.zarbosoft.merman.core.wall.Brick;
import com.zarbosoft.merman.core.wall.BrickInterface;
import com.zarbosoft.rendaw.common.DeadCode;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSList;

public abstract class VisualFrontAtomBase extends Visual implements VisualLeaf {
  private final Symbol ellipsisSpec;
  public FieldAtomCursor cursor;
  protected VisualAtom body;
  VisualParent parent;
  private NestedHoverable hoverable;
  private Brick ellipsis = null;

  public VisualFrontAtomBase(final int visualDepth, Symbol ellipsis) {
    super(visualDepth);
    ellipsisSpec = ellipsis;
  }
  @Override
  public void notifyLastBrickCreated(Context context, Brick brick) {
    if (cursor != null) cursor.border.setLast(context, brick);
    if (hoverable != null) hoverable.border.setLast(context, brick);
    parent.notifyLastBrickCreated(context, brick);
  }

  @Override
  public void notifyFirstBrickCreated(Context context, Brick brick) {
    if (cursor != null) cursor.border.setFirst(context, brick);
    if (hoverable != null) hoverable.border.setFirst(context, brick);
    parent.notifyFirstBrickCreated(context, brick);
  }

  @Override
  public void compact(final Context context) {}

  @Override
  public void expand(final Context context) {}

  public abstract void dispatch(VisualNestedDispatcher dispatcher);

  public abstract Atom atomGet();

  public abstract String nodeType();

  protected abstract Field value();

  public abstract String backId();

  protected abstract SyntaxPath getBackPath();

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
        } else body.root(context, new FrontAtomParent(), visualDepth + 1, depthScore);
      }
    }
  }

  @Override
  public void uproot(final Context context, final Visual root) {
    if (cursor != null) context.clearCursor();
    if (hoverable != null) context.clearHover();
    if (ellipsis != null) ellipsis.destroy(context);
    if (body != null) {
      body.uproot(context, root);
      body = null;
    }
  }

  @Override
  public ROPair<Hoverable, Boolean> hover(final Context context, final Vector point) {
    if (cursor != null) return null;
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
              public ExtendBrickResult createPrevious(final Context context) {
                return parent.createPreviousBrick(context);
              }

              @Override
              public ExtendBrickResult createNext(final Context context) {
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
    notifyFirstBrickCreated(context,ellipsis);
    notifyLastBrickCreated(context,ellipsis);
    return ellipsis;
  }

  private boolean ellipsize(final Context context) {
    if (!context.window) return false;
    if (parent.atomVisual() == null) return false;
    return parent.atomVisual().depthScore >= context.ellipsizeThreshold;
  }

  @Override
  public CreateBrickResult createOrGetCornerstoneCandidate(final Context context) {
    if (ellipsize(context)) {
      if (ellipsis != null) return CreateBrickResult.brick(ellipsis);
      return CreateBrickResult.brick(createEllipsis(context));
    } else return body.createOrGetCornerstoneCandidate(context);
  }

  @Override
  public ExtendBrickResult createFirstBrick(final Context context) {
    if (ellipsize(context)) {
      if (ellipsis != null) return ExtendBrickResult.exists();
      return ExtendBrickResult.brick(createEllipsis(context));
    } else {
      return body.createFirstBrick(context);
    }
  }

  @Override
  public ExtendBrickResult createLastBrick(final Context context) {
    if (ellipsize(context)) {
      if (ellipsis != null) return ExtendBrickResult.exists();
      return ExtendBrickResult.brick(createEllipsis(context));
    } else {
      return body.createLastBrick(context);
    }
  }

  public void select(final Context context) {
    if (cursor != null) return;
    else if (hoverable != null) {
      context.clearHover();
    }
    cursor = context.cursorFactory.createAtomCursor(context, this);
    context.setCursor(cursor);
  }

  protected void set(final Context context, final Atom data) {
    if (ellipsize(context)) return;
    boolean fixDeepSelection = false;
    boolean fixDeepHover = false;
    if (context.cursor != null) {
      if (context.cursor.getVisual() == this) {
        fixDeepSelection = true;
        context.clearCursor();
      } else {
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
        (VisualAtom) data.ensureVisual(context, new FrontAtomParent(), visualDepth + 1, depthScore());
    if (cursor != null) cursor.nudgeCreation(context);
  }

  @Override
  public boolean selectAnyChild(final Context context) {
    return value().selectInto(context);
  }

  public interface VisualNestedDispatcher {
    void handle(VisualFrontAtomFromArray visual);

    void handle(VisualFieldAtom visual);
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
    public SyntaxPath getSyntaxPath() {
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

  private class FrontAtomParent extends VisualParent {
    @Override
    public Visual visual() {
      return VisualFrontAtomBase.this;
    }

    @Override
    public VisualAtom atomVisual() {
      return parent.atomVisual();
    }

    @Override
    public ExtendBrickResult createPreviousBrick(final Context context) {
      return parent.createPreviousBrick(context);
    }

    @Override
    public ExtendBrickResult createNextBrick(final Context context) {
      return parent.createNextBrick(context);
    }

    @Override
    public void notifyLastBrickCreated(Context context, Brick brick) {
      VisualFrontAtomBase.this.notifyLastBrickCreated(context, brick);
    }

    @Override
    public void notifyFirstBrickCreated(Context context, Brick brick) {
      VisualFrontAtomBase.this.notifyFirstBrickCreated(context, brick);
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
