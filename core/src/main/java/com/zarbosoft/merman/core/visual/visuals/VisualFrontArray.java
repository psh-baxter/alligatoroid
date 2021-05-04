package com.zarbosoft.merman.core.visual.visuals;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.Hoverable;
import com.zarbosoft.merman.core.SelectionState;
import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.document.fields.FieldArray;
import com.zarbosoft.merman.core.syntax.front.FrontArraySpecBase;
import com.zarbosoft.merman.core.syntax.front.FrontSymbol;
import com.zarbosoft.merman.core.visual.Vector;
import com.zarbosoft.merman.core.visual.Visual;
import com.zarbosoft.merman.core.visual.VisualLeaf;
import com.zarbosoft.merman.core.visual.VisualParent;
import com.zarbosoft.merman.core.visual.alignment.Alignment;
import com.zarbosoft.merman.core.visual.attachments.BorderAttachment;
import com.zarbosoft.merman.core.wall.Brick;
import com.zarbosoft.merman.core.wall.BrickInterface;
import com.zarbosoft.rendaw.common.DeadCode;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSList;

import java.util.function.Consumer;

public class VisualFrontArray extends VisualGroup implements VisualLeaf {
  public final FieldArray value;
  private final FieldArray.Listener dataListener;
  private final FrontArraySpecBase front;
  public Cursor selection;
  private Brick ellipsis = null;
  private Brick empty = null;
  private ArrayHoverable hoverable;

  public VisualFrontArray(
      final FrontArraySpecBase front,
      final VisualParent parent,
      final Atom atom,
      final int visualDepth) {
    super(visualDepth);
    this.front = front;
    this.value = front.field.get(atom.fields);
    dataListener = new DataListener(value, parent);
    value.addListener(dataListener);
    value.visual = this;
  }

  private void coreChange(
      final Context context, final int index, final int remove, final ROList<Atom> add) {
    int visualIndex = index;
    int visualRemove = remove;
    if (!front.separator.isEmpty()) {
      visualIndex = index == 0 ? 0 : visualIndex * 2 - 1;
      visualRemove = Math.min(visualRemove * 2, children.size());
    }
    // Remove
    remove(context, visualIndex, visualRemove);

    // Add
    if (!add.isEmpty()) {
      int addIndex = visualIndex;
      final Consumer<Integer> addSeparator =
          addAt -> {
            final VisualGroup group =
                new VisualGroup(
                    context,
                    new ArrayVisualParent(this, addAt, false),
                    visualDepth + 1,
                    depthScore());
            for (int fixIndex = 0; fixIndex < front.separator.size(); ++fixIndex) {
              final FrontSymbol fix = front.separator.get(fixIndex);
              group.add(
                  context,
                  fix.createVisual(
                      context, group.createParent(fixIndex), group.visualDepth + 1, depthScore()));
            }
            if (atomVisual().compact) group.compact(context);
            super.add(context, group, addAt);
          };
      for (final Atom atom : add) {
        if (!front.separator.isEmpty() && addIndex > 0) addSeparator.accept(addIndex++);
        final VisualGroup group =
            new VisualGroup(
                context,
                new ArrayVisualParent(this, addIndex, true),
                visualDepth + 1,
                depthScore());
        int groupIndex = 0;
        for (final FrontSymbol fix : front.prefix)
          group.add(
              context,
              fix.createVisual(
                  context, group.createParent(groupIndex++), group.visualDepth + 1, depthScore()));
        final VisualAtom nodeVisual =
            (VisualAtom)
                atom.ensureVisual(
                    context, group.createParent(groupIndex++), group.visualDepth + 2, depthScore());
        group.add(
            context,
            new Visual(group.visualDepth + 1) {
              @Override
              public VisualParent parent() {
                return nodeVisual.parent();
              }

              @Override
              public Brick createOrGetCornerstoneCandidate(final Context context) {
                return nodeVisual.createOrGetCornerstoneCandidate(context);
              }

              @Override
              public Brick createFirstBrick(final Context context) {
                return nodeVisual.createFirstBrick(context);
              }

              @Override
              public Brick createLastBrick(final Context context) {
                return nodeVisual.createLastBrick(context);
              }

              @Override
              public Brick getFirstBrick(final Context context) {
                return nodeVisual.getFirstBrick(context);
              }

              @Override
              public Brick getLastBrick(final Context context) {
                return nodeVisual.getLastBrick(context);
              }

              @Override
              public void compact(final Context context) {}

              @Override
              public void expand(final Context context) {}

              @Override
              public void getLeafBricks(final Context context, TSList<Brick> bricks) {}

              @Override
              public void uproot(final Context context, final Visual root) {
                nodeVisual.uproot(context, root);
              }

              @Override
              public void root(
                  final Context context,
                  final VisualParent parent,
                  final int depth,
                  final int depthScore) {
                super.root(context, parent, depth, depthScore);
                nodeVisual.root(context, parent, depth + 1, depthScore);
              }

              @Override
              public boolean selectAnyChild(final Context context) {
                return nodeVisual.selectAnyChild(context);
              }
            });
        for (final FrontSymbol fix : front.suffix)
          group.add(
              context,
              fix.createVisual(
                  context, group.createParent(groupIndex++), group.visualDepth + 1, depthScore()));
        if (atomVisual().compact) group.compact(context);
        super.add(context, group, addIndex++);
      }
      if (!front.separator.isEmpty() && visualIndex == 0 && value.data.size() > add.size())
        addSeparator.accept(addIndex++);
    }
  }

  private Brick createEmpty(final Context context) {
    if (empty != null) return null;
    empty =
        front.empty.createBrick(
            context,
            new BrickInterface() {
              @Override
              public VisualLeaf getVisual() {
                return VisualFrontArray.this;
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
                empty = null;
              }

              @Override
              public Alignment findAlignment(String alignment) {
                return parent.atomVisual().findAlignment(alignment);
              }
            });
    return empty;
  }

  private boolean ellipsize(final Context context) {
    if (!context.window) return false;
    return parent.atomVisual().depthScore >= context.ellipsizeThreshold;
  }

  private int visualIndex(final int valueIndex) {
    if (front.separator.isEmpty()) return valueIndex;
    else return valueIndex * 2;
  }

  public void select(
      final Context context, final boolean leadFirst, final int start, final int end) {
    if (hoverable != null) hoverable.notifySelected(context, start, end);
    if (selection == null) {
      selection = context.cursorFactory.createArrayCursor(context, this, leadFirst, start, end);
      context.setCursor(selection);
    } else {
      selection.setRange(context, start, end);
    }
  }

  @Override
  public Brick getFirstBrick(final Context context) {
    if (empty != null) return empty;
    if (ellipsize(context)) return ellipsis;
    return super.getFirstBrick(context);
  }

  @Override
  public Brick getLastBrick(final Context context) {
    if (empty != null) return empty;
    if (ellipsize(context)) return ellipsis;
    return super.getLastBrick(context);
  }

  @Override
  public boolean selectAnyChild(final Context context) {
    value.selectInto(context, true, 0, 0);
    return true;
  }

  @Override
  public Brick createOrGetCornerstoneCandidate(final Context context) {
    if (value.data.isEmpty()) {
      if (empty != null) return empty;
      else return createEmpty(context);
    } else if (ellipsize(context)) {
      if (ellipsis != null) return ellipsis;
      else return createEllipsis(context);
    } else return super.createOrGetCornerstoneCandidate(context);
  }

  @Override
  public Brick createFirstBrick(final Context context) {
    if (value.data.isEmpty()) return createEmpty(context);
    if (ellipsize(context)) return createEllipsis(context);
    return super.createFirstBrick(context);
  }

  @Override
  public Brick createLastBrick(final Context context) {
    if (value.data.isEmpty()) return createEmpty(context);
    if (ellipsize(context)) return createEllipsis(context);
    return super.createLastBrick(context);
  }

  @Override
  public void root(
      final Context context,
      final VisualParent parent,
      final int visualDepth,
      final int depthScore) {
    this.parent = parent;
    if (value.data.isEmpty()) {
      super.root(context, parent, visualDepth, depthScore);
      if (empty == null) context.triggerIdleLayBricks(parent, 0, 1, 1, null, null);
    } else if (ellipsize(context)) {
      if (!children.isEmpty()) {
        remove(context, 0, children.size());
      }
      super.root(context, parent, visualDepth, depthScore);
      if (ellipsis == null) context.triggerIdleLayBricks(parent, 0, 1, 1, null, null);
    } else {
      if (ellipsis != null) ellipsis.destroy(context);
      super.root(context, parent, visualDepth, depthScore);
      if (children.isEmpty()) {
        coreChange(context, 0, 0, value.data);
        context.triggerIdleLayBricks(parent, 0, 1, 1, null, null);
      }
    }
  }

  private Brick createEllipsis(final Context context) {
    if (ellipsis != null) return null;
    ellipsis =
        front.ellipsis.createBrick(
            context,
            new BrickInterface() {
              @Override
              public VisualLeaf getVisual() {
                return VisualFrontArray.this;
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

  @Override
  public void uproot(final Context context, final Visual root) {
    if (root == this) {
      // Only root array, which should never be uprooted with itself as the stop point
      throw new AssertionError();
    }
    if (selection != null) context.clearCursor();
    if (hoverable != null) context.clearHover();
    if (ellipsis != null) ellipsis.destroy(context);
    if (empty != null) empty.destroy(context);
    value.removeListener(dataListener);
    value.visual = null;
    super.uproot(context, root);
    children.clear();
  }

  @Override
  public ROPair<Hoverable, Boolean> hover(final Context context, final Vector point) {
    if (empty != null) {
      hoverable = new PlaceholderHoverable(context, empty, this);
      return new ROPair<>(hoverable, true);
    } else if (ellipsis != null) {
      hoverable = new PlaceholderHoverable(context, ellipsis, this);
      return new ROPair<>(hoverable, true);
    } else return super.hover(context, point);
  }

  public static class Cursor extends com.zarbosoft.merman.core.Cursor {
    public final VisualFrontArray visual;
    public int beginIndex;
    public int endIndex;
    public boolean leadFirst;
    BorderAttachment border;

    public Cursor(
        final Context context,
        final VisualFrontArray visual,
        final boolean leadFirst,
        final int start,
        final int end) {
      this.visual = visual;
      border = new BorderAttachment(context, context.syntax.cursorStyle.obbox);
      this.leadFirst = leadFirst;
      setRange(context, start, end);
    }

    public void setRange(final Context context, final int begin, final int end) {
      setBeginInternal(context, begin);
      setEndInternal(context, end);
      border.setFirst(
          context, visual.children.get(visual.visualIndex(begin)).getFirstBrick(context));
      border.setLast(context, visual.children.get(visual.visualIndex(end)).getLastBrick(context));
    }

    private void setBeginInternal(final Context context, final int index) {
      beginIndex = index;
      if (leadFirst) setCornerstone(context, beginIndex);
    }

    private void setCornerstone(final Context context, final int index) {
      context.wall.setCornerstone(
          context,
          visual.children.get(visual.visualIndex(index)).createOrGetCornerstoneCandidate(context),
          () -> {
            for (int at = visual.visualIndex(index) - 1; at >= 0; --at) {
              final Brick found = visual.children.get(at).getLastBrick(context);
              if (found != null) return found;
            }
            return visual.parent.getPreviousBrick(context);
          },
          () -> {
            for (int at = visual.visualIndex(index) + 1; at < visual.children.size(); ++at) {
              final Brick found = visual.children.get(at).getFirstBrick(context);
              if (found != null) return found;
            }
            return visual.parent.getNextBrick(context);
          });
    }

    private void setEndInternal(final Context context, final int index) {
      endIndex = index;
      if (!leadFirst) setCornerstone(context, endIndex);
    }

    public void setBegin(final Context context, final int index) {
      leadFirst = true;
      setBeginInternal(context, index);
      border.setFirst(
          context, visual.children.get(visual.visualIndex(index)).getFirstBrick(context));
    }

    private void setEnd(final Context context, final int index) {
      leadFirst = false;
      setEndInternal(context, index);
      border.setLast(context, visual.children.get(visual.visualIndex(index)).getLastBrick(context));
    }

    public void setPosition(final Context context, final int index) {
      setEndInternal(context, index);
      setBeginInternal(context, index);
      border.setFirst(
          context, visual.children.get(visual.visualIndex(index)).getFirstBrick(context));
      border.setLast(context, visual.children.get(visual.visualIndex(index)).getLastBrick(context));
    }

    @Override
    public void destroy(final Context context) {
      border.destroy(context);
      visual.selection = null;
    }

    @Override
    public Visual getVisual() {
      return visual.children.get(beginIndex);
    }

    @Override
    public SelectionState saveState() {
      return new ArraySelectionState(visual.value, leadFirst, beginIndex, endIndex);
    }

    @Override
    public SyntaxPath getSyntaxPath() {
      return visual.value.getSyntaxPath().add(String.valueOf(beginIndex));
    }

    @Override
    public void dispatch(Dispatcher dispatcher) {
      dispatcher.handle(this);
    }

    public void actionEnter(final Context context) {
      visual.value.data.get(beginIndex).visual.selectAnyChild(context);
    }

    public void actionExit(final Context context) {
      visual.value.atomParentRef.selectAtomParent(context);
    }

    public void actionNext(final Context context) {
      visual.parent.selectNext(context);
    }

    public void actionPrevious(final Context context) {
      visual.parent.selectPrevious(context);
    }

    public void actionNextElement(final Context context) {
      Cursor.this.leadFirst = true;
      final int newIndex = Math.min(visual.value.data.size() - 1, endIndex + 1);
      if (newIndex == beginIndex && newIndex == endIndex) return;
      setPosition(context, newIndex);
    }

    public void actionPreviousElement(final Context context) {
      Cursor.this.leadFirst = true;
      final int newIndex = Math.max(0, beginIndex - 1);
      if (newIndex == beginIndex && newIndex == endIndex) return;
      setPosition(context, newIndex);
    }

    public void actionCopy(final Context context) {
      context.copy(visual.value.data.sublist(beginIndex, endIndex + 1));
    }

    public void actionGatherNext(final Context context) {
      final int newIndex = Math.min(visual.value.data.size() - 1, endIndex + 1);
      if (endIndex == newIndex) return;
      setEnd(context, newIndex);
    }

    public void actionReleaseNext(final Context context) {
      final int newIndex = Math.max(beginIndex, endIndex - 1);
      if (endIndex == newIndex) return;
      setEnd(context, newIndex);
    }

    public void actionGatherPrevious(final Context context) {
      final int newIndex = Math.max(0, beginIndex - 1);
      if (beginIndex == newIndex) return;
      setBegin(context, newIndex);
    }

    public void actionReleasePrevious(final Context context) {
      final int newIndex = Math.min(endIndex, beginIndex + 1);
      if (beginIndex == newIndex) return;
      setBegin(context, newIndex);
    }

    public void actionWindow(final Context context) {
      final Atom root = visual.value.data.get(beginIndex);
      if (root.visual.selectAnyChild(context)) {
        context.windowExact(root);
        context.triggerIdleLayBricksOutward();
        return;
      }
    }
  }

  private static class ArraySelectionState implements SelectionState {
    private final FieldArray value;
    private final int start;
    private final int end;
    private final boolean leadFirst;

    private ArraySelectionState(
        final FieldArray value, final boolean leadFirst, final int start, final int end) {
      this.value = value;
      this.leadFirst = leadFirst;
      this.start = start;
      this.end = end;
    }

    @Override
    public void select(final Context context) {
      value.selectInto(context, leadFirst, start, end);
    }
  }

  private abstract static class ArrayHoverable extends Hoverable {
    public final VisualFrontArray visual;
    final BorderAttachment border;

    ArrayHoverable(VisualFrontArray visual, final Context context) {
      this.visual = visual;
      border = new BorderAttachment(context, context.syntax.hoverStyle.obbox);
    }

    @Override
    protected void clear(final Context context) {
      border.destroy(context);
      if (visual.hoverable == this) visual.hoverable = null;
    }

    @Override
    public Visual visual() {
      return visual;
    }

    public abstract void notifyRangeAdjusted(Context context, int index, int removed, int added);

    public abstract void notifySelected(Context context, int start, int end);
  }

  private static class PlaceholderHoverable extends ArrayHoverable {
    public final VisualFrontArray visual;

    private PlaceholderHoverable(
        final Context context, final Brick brick, VisualFrontArray visual) {
      super(visual, context);
      this.visual = visual;
      border.setFirst(context, brick);
      border.setLast(context, brick);
    }

    @Override
    public SyntaxPath getSyntaxPath() {
      return visual.value.getSyntaxPath().add("0");
    }

    @Override
    public void select(final Context context) {
      visual.select(context, true, 0, 0);
    }

    @Override
    public VisualAtom atom() {
      return visual.parent.atomVisual();
    }

    @Override
    public void notifyRangeAdjusted(
        final Context context, final int index, final int removed, final int added) {
      throw new DeadCode();
    }

    @Override
    public void notifySelected(final Context context, final int start, final int end) {
      context.clearHover();
    }
  }

  private static class ArrayVisualParent extends Parent {
    private final boolean selectable;
    private final VisualFrontArray visual;

    public ArrayVisualParent(VisualFrontArray visual, final int index, final boolean selectable) {
      super(visual, index);
      this.selectable = selectable;
      this.visual = visual;
    }

    @Override
    public ROPair<Hoverable, Boolean> hover(final Context context, final Vector point) {
      if (!selectable) {
        if (visual.parent != null) return visual.parent.hover(context, point);
        return null;
      }
      boolean changed = false;
      int newIndex = valueIndex();
      if (visual.selection != null
          && visual.selection.beginIndex == visual.selection.endIndex
          && visual.selection.beginIndex == newIndex) return null;
      if (visual.hoverable == null) {
        visual.hoverable = visual.new ElementHoverable(context, visual);
        changed = true;
      }
      ElementHoverable elementHoverable = (ElementHoverable) visual.hoverable;
      if (elementHoverable.index != newIndex) changed = true;
      elementHoverable.setIndex(context, newIndex);
      return new ROPair<>(visual.hoverable, changed);
    }

    private int valueIndex() {
      if (visual.front.separator.isEmpty()) return index;
      return index / 2;
    }

    @Override
    public void firstBrickChanged(final Context context, final Brick firstBrick) {
      if (visual.selection != null && valueIndex() == visual.selection.beginIndex)
        visual.selection.border.setFirst(context, firstBrick);
      if (visual.hoverable != null && valueIndex() == ((ElementHoverable) visual.hoverable).index)
        ((ElementHoverable) visual.hoverable).border.setFirst(context, firstBrick);
    }

    @Override
    public void lastBrickChanged(final Context context, final Brick lastBrick) {
      if (visual.selection != null && valueIndex() == visual.selection.endIndex)
        visual.selection.border.setLast(context, lastBrick);
      if (visual.hoverable != null && valueIndex() == ((ElementHoverable) visual.hoverable).index)
        ((ElementHoverable) visual.hoverable).border.setLast(context, lastBrick);
    }
  }

  private class ElementHoverable extends ArrayHoverable {
    private int index;

    ElementHoverable(final Context context, VisualFrontArray visual) {
      super(visual, context);
    }

    @Override
    public SyntaxPath getSyntaxPath() {
      return visual.value.getSyntaxPath().add(String.valueOf(index));
    }

    @Override
    public void select(final Context context) {
      VisualFrontArray.this.select(context, true, index, index);
    }

    @Override
    public VisualAtom atom() {
      return VisualFrontArray.this.parent.atomVisual();
    }

    @Override
    public void notifyRangeAdjusted(
        final Context context, final int index, final int removed, final int added) {
      if (this.index >= index + removed) {
        setIndex(context, this.index - removed + added);
      } else if (this.index >= index) {
        context.clearHover();
      }
    }

    public void setIndex(final Context context, final int index) {
      this.index = index;
      border.setFirst(context, children.get(visualIndex(index)).getFirstBrick(context));
      border.setLast(context, children.get(visualIndex(index)).getLastBrick(context));
    }

    @Override
    public void notifySelected(final Context context, final int start, final int end) {
      if (this.index >= start && this.index <= end) {
        context.clearHover();
      }
    }
  }

  private class DataListener implements FieldArray.Listener {
    private final FieldArray value;
    private final VisualParent parent;

    public DataListener(FieldArray value, VisualParent parent) {
      this.value = value;
      this.parent = parent;
    }

    @Override
    public void changed(
        final Context context, final int index, final int remove, final ROList<Atom> add) {
      if (ellipsize(context)) {
        if (value.data.isEmpty()) {
          // Was blank, now ellipsized
          if (empty != null) empty.destroy(context);
          context.triggerIdleLayBricks(parent, 0, 1, 1, null, null);
          return;
        } else if (add.isEmpty() && remove == value.data.size()) {
          // Was ellipsized, now blank
        } else {
          // Was ellipsized, no change
          return;
        }
      }

      // Prep to fix selection if deep under an element
      Integer fixDeepSelectionIndex = null;
      Integer fixDeepHoverIndex = null;
      Integer oldSelectionBeginIndex = null;
      Integer oldSelectionEndIndex = null;
      if (selection != null) {
        oldSelectionBeginIndex = selection.beginIndex;
        oldSelectionEndIndex = selection.endIndex;
      } else if (context.cursor != null) {
        VisualParent parent = context.cursor.getVisual().parent();
        while (parent != null) {
          final Visual visual = parent.visual();
          if (visual == VisualFrontArray.this) {
            fixDeepSelectionIndex = ((ArrayVisualParent) parent).valueIndex();
            break;
          }
          parent = visual.parent();
        }
      }
      if (hoverable == null && context.hover != null) {
        VisualParent parent = context.hover.visual().parent();
        while (parent != null) {
          final Visual visual = parent.visual();
          if (visual == VisualFrontArray.this) {
            fixDeepHoverIndex = ((ArrayVisualParent) parent).valueIndex();
            break;
          }
          parent = visual.parent();
        }
      }

      // Create child visuals
      coreChange(context, index, remove, add);

      // Lay bricks if children added/totally cleared
      if (!add.isEmpty()) {
        if (empty != null) empty.destroy(context);
        final int layIndex = visualIndex(index);
        context.triggerIdleLayBricks(
            parent,
            layIndex,
            visualIndex(index + add.size()) - layIndex,
            children.size(),
            i -> children.get(i).getFirstBrick(context),
            i -> children.get(i).getLastBrick(context));
      } else if (value.data.isEmpty()) {
        if (ellipsis != null) ellipsis.destroy(context);
        context.triggerIdleLayBricks(parent, 0, 1, 1, null, null);
      }

      // Fix hover/selection
      if (hoverable != null) {
        hoverable.notifyRangeAdjusted(context, index, remove, add.size());
      } else if (fixDeepHoverIndex != null
          && fixDeepHoverIndex >= index
          && fixDeepHoverIndex < index + remove) {
        context.clearHover();
      }
      if (oldSelectionBeginIndex != null) {
        if (value.data.isEmpty()) value.atomParentRef.selectAtomParent(context);
        else {
          if (oldSelectionBeginIndex >= index + remove)
            selection.setBegin(context, oldSelectionBeginIndex - remove + add.size());
          else if (oldSelectionBeginIndex >= index)
            selection.setBegin(
                context, Math.min(value.data.size() - 1, index + Math.max(0, add.size() - 1)));
          if (oldSelectionEndIndex >= index + remove)
            selection.setEnd(context, oldSelectionEndIndex - remove + add.size());
          else if (oldSelectionEndIndex >= index)
            selection.setEnd(
                context, Math.min(value.data.size() - 1, index + Math.max(0, add.size() - 1)));
        }
      } else if (fixDeepSelectionIndex != null) {
        if (value.data.isEmpty()) value.atomParentRef.selectAtomParent(context);
        else if (fixDeepSelectionIndex >= index && fixDeepSelectionIndex < index + remove) {
          final int newIndex = Math.min(value.data.size() - 1, index + Math.max(0, add.size() - 1));
          select(context, true, newIndex, newIndex);
        }
      }
    }
  }
}
