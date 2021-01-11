package com.zarbosoft.merman.editor.visual.visuals;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.Value;
import com.zarbosoft.merman.editor.*;
import com.zarbosoft.merman.editor.visual.*;
import com.zarbosoft.merman.editor.visual.attachments.BorderAttachment;
import com.zarbosoft.merman.editor.visual.tags.PartTag;
import com.zarbosoft.merman.editor.visual.tags.StateTag;
import com.zarbosoft.merman.editor.visual.tags.Tag;
import com.zarbosoft.merman.editor.visual.tags.TagsChange;
import com.zarbosoft.merman.editor.wall.Brick;
import com.zarbosoft.merman.editor.wall.BrickInterface;
import com.zarbosoft.merman.syntax.style.Style;
import com.zarbosoft.merman.syntax.symbol.Symbol;
import com.zarbosoft.rendaw.common.DeadCode;
import com.zarbosoft.rendaw.common.Pair;
import org.pcollections.PSet;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class VisualNestedBase extends Visual implements VisualLeaf {
  PSet<Tag> tags;
  PSet<Tag> ellipsisTags;
  protected VisualAtom body;
  VisualParent parent;
  private NestedHoverable hoverable;
  private NestedCursor selection;
  private Brick ellipsis = null;

  public VisualNestedBase(final PSet<Tag> tags, final int visualDepth) {
    super(visualDepth);
    this.tags = tags.plus(new PartTag("atom"));
    ellipsisTags = this.tags.plus(new PartTag("ellipsis"));
  }

  public interface VisualNestedDispatcher {
    void handle(VisualNestedFromArray visual);

    void handle(VisualNested visual);
  }

  public abstract void dispatch(VisualNestedDispatcher dispatcher);

  protected abstract void nodeSet(Context context, Atom value);

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
      final Map<String, Alignment> alignments,
      final int visualDepth,
      final int depthScore) {
    this.parent = parent;
    if (ellipsize(context)) {
      if (body != null) {
        body.uproot(context, null);
        body = null;
        context.idleLayBricks(parent, 0, 1, 1, null, null);
      }
    } else {
      if (ellipsis != null) ellipsis.destroy(context);
      if (atomGet() != null) {
        if (body == null) {
          coreSet(context, atomGet());
          context.idleLayBricks(parent, 0, 1, 1, null, null);
        } else body.root(context, new NestedParent(), alignments, visualDepth + 1, depthScore);
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
      border = new BorderAttachment(context, getBorderStyle(context, tags).obbox);
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
      selectDown(context);
    }

    @Override
    public VisualAtom atom() {
      return VisualNestedBase.this.parent.atomVisual();
    }

    @Override
    public Visual visual() {
      return VisualNestedBase.this;
    }

    @Override
    public void tagsChanged(final Context context) {
      border.setStyle(context, getBorderStyle(context, tags).obbox);
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
  public Iterable<Pair<Brick, Brick.Properties>> getLeafPropertiesForTagsChange(
      final Context context, final TagsChange change) {
    return body.getLeafPropertiesForTagsChange(context, change);
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
                    return VisualNestedBase.this;
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
                  public Alignment getAlignment(final Style.Baked style) {
                    return parent.atomVisual().getAlignment(style.alignment);
                  }

                  @Override
                  public PSet<Tag> getTags(final Context context) {
                    return ellipsisTags;
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

  @Override
  public void changeTags(final Context context, final TagsChange change) {
    tags = change.apply(tags);
    ellipsisTags = tags.plus(new PartTag("ellipsis"));
    tagsChanged(context);
  }

  @Override
  public Stream<Brick> streamBricks() {
    if (ellipsis != null) return Stream.of(ellipsis);
    return body.streamBricks();
  }

  private boolean ellipsize(final Context context) {
    if (!context.window) return false;
    if (parent.atomVisual() == null) return false;
    return parent.atomVisual().depthScore >= context.syntax.ellipsizeThreshold;
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
    context.setSelection(selection);
  }

  public static class NestedCursor extends Cursor {
    private BorderAttachment border;
    public VisualNestedBase base;

    public NestedCursor(VisualNestedBase base, final Context context) {
      border = new BorderAttachment(context, getBorderStyle(context, base.tags).obbox);
      final Brick first = nudge(context);
      border.setFirst(context, first);
      border.setLast(context, base.body.getLastBrick(context));
      context.addActions(
          this,
          Stream.of(
                  new AtomActionEnter(base),
                  new AtomActionExit(base),
                  new AtomActionNext(base),
                  new AtomActionPrevious(base),
                  new AtomActionWindow(base),
                  new AtomActionCopy(base))
              .collect(Collectors.toList()));
      this.base = base;
    }

    @Override
    public void clear(final Context context) {
      border.destroy(context);
      border = null;
      base.selection = null;
      context.removeActions(this);
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
      border.setStyle(context, getBorderStyle(context, base.tags).obbox);
      super.tagsChanged(context);
    }

    @Override
    public PSet<Tag> getTags(final Context context) {
      return base.tags;
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
      value.selectDown(context);
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
    context.idleLayBricks(parent, 0, 1, 1, null, null);

    if (fixDeepSelection) select(context);
    if (fixDeepHover) context.clearHover();
  }

  private void coreSet(final Context context, final Atom data) {
    if (body != null) body.uproot(context, null);
    this.body =
        (VisualAtom)
            data.createVisual(
                context,
                new NestedParent(),
                parent.atomVisual().alignments(),
                visualDepth + 1,
                depthScore());
    if (selection != null) selection.nudge(context);
  }

  private class NestedParent extends VisualParent {
    @Override
    public Visual visual() {
      return VisualNestedBase.this;
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
      return VisualNestedBase.this.hover(context, point);
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
  public boolean selectDown(final Context context) {
    return value().selectDown(context);
  }

  @Override
  public void compact(final Context context) {
    ellipsisTags = ellipsisTags.plus(new StateTag("compact"));
    if (ellipsis != null) ellipsis.tagsChanged(context);
  }

  @Override
  public void expand(final Context context) {
    ellipsisTags = ellipsisTags.minus(new StateTag("compact"));
    if (ellipsis != null) ellipsis.tagsChanged(context);
  }

  @Action.StaticID(id = "enter")
  private static class AtomActionEnter extends Action {
    private final VisualNestedBase base;

    public AtomActionEnter(VisualNestedBase base) {
      this.base = base;
    }

    @Override
    public boolean run(final Context context) {
      return base.body.selectDown(context);
    }
  }

  @Action.StaticID(id = "exit")
  private static class AtomActionExit extends Action {
    private final VisualNestedBase base;

    public AtomActionExit(VisualNestedBase base) {
      this.base = base;
    }

    @Override
    public boolean run(final Context context) {

      if (base.value().parent == null) return false;
      return base.value().parent.selectUp(context);
    }
  }

  @Action.StaticID(id = "next")
  private static class AtomActionNext extends Action {
    private final VisualNestedBase base;

    public AtomActionNext(VisualNestedBase base) {
      this.base = base;
    }

    @Override
    public boolean run(final Context context) {
      return base.parent.selectNext(context);
    }
  }

  @Action.StaticID(id = "previous")
  private static class AtomActionPrevious extends Action {
    private final VisualNestedBase base;

    public AtomActionPrevious(VisualNestedBase base) {
      this.base = base;
    }

    @Override
    public boolean run(final Context context) {
      return base.parent.selectPrevious(context);
    }
  }

  @Action.StaticID(id = "window")
  private static class AtomActionWindow extends Action {
    private final VisualNestedBase base;

    public AtomActionWindow(VisualNestedBase base) {
      this.base = base;
    }

    @Override
    public boolean run(final Context context) {
      final Atom root = base.atomGet();
      if (!root.visual.selectDown(context)) return false;
      context.setAtomWindow(root);
      return true;
    }
  }
}
