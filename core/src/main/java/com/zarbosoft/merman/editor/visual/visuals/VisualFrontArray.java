package com.zarbosoft.merman.editor.visual.visuals;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.ValueArray;
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
import com.zarbosoft.merman.editor.wall.bricks.BrickSpace;
import com.zarbosoft.merman.syntax.front.FrontSymbol;
import com.zarbosoft.merman.syntax.style.Style;
import com.zarbosoft.merman.syntax.symbol.Symbol;
import com.zarbosoft.rendaw.common.DeadCode;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.ROSet;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSSet;

import java.util.function.Consumer;

public abstract class VisualFrontArray extends VisualGroup implements VisualLeaf {
  public final ValueArray value;
  private final ValueArray.Listener dataListener;
  public ArrayCursor selection;
  private Brick ellipsis = null;
  private Brick empty = null;
  private ArrayHoverable hoverable;

  public VisualFrontArray(
      final Context context,
      final VisualParent parent,
      final ValueArray value,
      final int visualDepth,
      final int depthScore) {
    super(visualDepth);
    this.value = value;
    dataListener =
        new ValueArray.Listener() {

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
                      context,
                      Math.min(value.data.size() - 1, index + Math.max(0, add.size() - 1)));
                if (oldSelectionEndIndex >= index + remove)
                  selection.setEnd(context, oldSelectionEndIndex - remove + add.size());
                else if (oldSelectionEndIndex >= index)
                  selection.setEnd(
                      context,
                      Math.min(value.data.size() - 1, index + Math.max(0, add.size() - 1)));
              }
            } else if (fixDeepSelectionIndex != null) {
              if (value.data.isEmpty()) value.atomParentRef.selectAtomParent(context);
              else if (fixDeepSelectionIndex >= index && fixDeepSelectionIndex < index + remove) {
                final int newIndex =
                    Math.min(value.data.size() - 1, index + Math.max(0, add.size() - 1));
                select(context, true, newIndex, newIndex);
              }
            }
          }
        };
    value.addListener(dataListener);
    value.visual = this;
    root(context, parent, depthScore, depthScore);
  }

  private void coreChange(
      final Context context, final int index, final int remove, final ROList<Atom> add) {
    int visualIndex = index;
    int visualRemove = remove;
    if (!getSeparator().isEmpty()) {
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
                    context, new ArrayVisualParent(addAt, false), visualDepth + 1, depthScore());
            for (int fixIndex = 0; fixIndex < getSeparator().size(); ++fixIndex) {
              final FrontSymbol fix = getSeparator().get(fixIndex);
              group.add(
                  context,
                  fix.createVisual(
                      context, group.createParent(fixIndex), group.visualDepth + 1, depthScore()));
            }
            if (atomVisual().compact) group.compact(context);
            super.add(context, group, addAt);
          };
      for (final Atom atom : add) {
        if (!getSeparator().isEmpty() && addIndex > 0) addSeparator.accept(addIndex++);
        final VisualGroup group =
            new VisualGroup(
                context, new ArrayVisualParent(addIndex, true), visualDepth + 1, depthScore());
        int groupIndex = 0;
        for (final FrontSymbol fix : getElementPrefix())
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
              public void tagsChanged(Context context) {}

              @Override
              public Brick createOrGetFirstBrick(final Context context) {
                return nodeVisual.createOrGetFirstBrick(context);
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
              public void getLeafPropertiesForTagsChange(
                  final Context context,
                  TSList<ROPair<Brick, Brick.Properties>> brickProperties,
                  final TagsChange change) {}

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
        for (final FrontSymbol fix : getElementSuffix())
          group.add(
              context,
              fix.createVisual(
                  context, group.createParent(groupIndex++), group.visualDepth + 1, depthScore()));
        if (atomVisual().compact) group.compact(context);
        super.add(context, group, addIndex++);
      }
      if (!getSeparator().isEmpty() && visualIndex == 0 && value.data.size() > add.size())
        addSeparator.accept(addIndex++);
    }

    // Cleanup
    for (int i = 0; i < value.data.size(); ++i) {
      Atom child = value.data.get(i);
      TSSet<String> addTags = new TSSet<>();
      TSSet<String> removeTags = new TSSet<>();
      if (i == 0) addTags.add(Tags.TAG_ARRAY_FIRST);
      else removeTags.add(Tags.TAG_ARRAY_FIRST);
      if (i == value.data.size() - 1) addTags.add(Tags.TAG_ARRAY_LAST);
      else removeTags.add(Tags.TAG_ARRAY_LAST);
      if (i % 2 == 0) {
        addTags.add(Tags.TAG_ARRAY_EVEN);
        removeTags.add(Tags.TAG_ARRAY_ODD);
      } else {
        addTags.add(Tags.TAG_ARRAY_ODD);
        removeTags.add(Tags.TAG_ARRAY_EVEN);
      }
      child.changeTags(context, new TagsChange(addTags, removeTags));
    }
  }

  protected abstract boolean tagLast();

  protected abstract boolean tagFirst();

  protected abstract ROList<FrontSymbol> getElementPrefix();

  protected abstract ROList<FrontSymbol> getSeparator();

  protected abstract ROList<FrontSymbol> getElementSuffix();

  private Brick createEmpty(final Context context) {
    if (empty != null) return null;
    empty =
        new BrickSpace(
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
              public Alignment findAlignment(final Style style) {
                return parent.atomVisual().findAlignment(style.alignment);
              }

              @Override
              public TSSet<String> getTags(final Context context) {
                return VisualFrontArray.this.getTags(context).add(Tags.TAG_PART_EMPTY);
              }
            });
    context.bricksCreated(this, empty);
    return empty;
  }

  private TSSet<String> getTags(Context context) {
    return atomVisual().getTags(context);
  }

  private boolean ellipsize(final Context context) {
    if (!context.window) return false;
    return parent.atomVisual().depthScore >= context.ellipsizeThreshold;
  }

  private int visualIndex(final int valueIndex) {
    if (getSeparator().isEmpty()) return valueIndex;
    else return valueIndex * 2;
  }

  public void select(
      final Context context, final boolean leadFirst, final int start, final int end) {
    if (hoverable != null) hoverable.notifySelected(context, start, end);
    if (selection == null) {
      selection = new ArrayCursor(context, this, leadFirst, start, end);
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
  public Brick createOrGetFirstBrick(final Context context) {
    if (value.data.isEmpty()) {
      if (empty != null) return empty;
      else return createEmpty(context);
    } else if (ellipsize(context)) {
      if (ellipsis != null) return ellipsis;
      else return createEllipsis(context);
    } else return super.createOrGetFirstBrick(context);
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
        ellipsis()
            .createBrick(
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
                  public Alignment findAlignment(final Style style) {
                    return parent.atomVisual().findAlignment(style.alignment);
                  }

                  @Override
                  public TSSet<String> getTags(final Context context) {
                    return VisualFrontArray.this.getTags(context).add(Tags.TAG_PART_ELLIPSIS);
                  }
                });
    context.bricksCreated(this, ellipsis);
    return ellipsis;
  }

  protected abstract Symbol ellipsis();

  @Override
  public void uproot(final Context context, final Visual root) {
    if (root == this) {
      // Only root array, which should never be uprooted with itself as the stop point
      throw new AssertionError();
    }
    if (selection != null) context.clearSelection();
    if (hoverable != null) context.clearHover();
    if (ellipsis != null) ellipsis.destroy(context);
    if (empty != null) empty.destroy(context);
    value.removeListener(dataListener);
    value.visual = null;
    super.uproot(context, root);
    children.clear();
  }

  public void tagsChanged(final Context context) {
    super.tagsChanged(context);
    if (ellipsis != null) ellipsis.tagsChanged(context);
    if (empty != null) empty.tagsChanged(context);
    if (selection != null) selection.tagsChanged(context);
    if (hoverable != null) hoverable.tagsChanged(context);
  }

  @Override
  public Hoverable hover(final Context context, final Vector point) {
    if (empty != null) {
      hoverable = new PlaceholderHoverable(context, empty);
      return hoverable;
    } else if (ellipsis != null) {
      hoverable = new PlaceholderHoverable(context, ellipsis);
      return hoverable;
    } else return super.hover(context, point);
  }

  public static class ArrayCursor extends Cursor {
    public final VisualFrontArray self;
    private final ROList<Action> actions;
    public int beginIndex;
    public int endIndex;
    public boolean leadFirst;
    BorderAttachment border;

    public ArrayCursor(
        final Context context,
        final VisualFrontArray self,
        final boolean leadFirst,
        final int start,
        final int end) {
      this.self = self;
      border = new BorderAttachment(context, getBorderStyle(context).obbox);
      this.leadFirst = leadFirst;
      setRange(context, start, end);
      context.addActions(
          this.actions =
              TSList.of(
                  new ActionEnter(),
                  new ActionExit(),
                  new ActionNext(),
                  new ActionPrevious(),
                  new ActionNextElement(),
                  new ActionPreviousElement(),
                  new ActionCopy(),
                  new ActionGatherNext(),
                  new ActionReleaseNext(),
                  new ActionGatherPrevious(),
                  new ActionReleasePrevious(),
                  new ActionWindow()));
    }

    public void setRange(final Context context, final int begin, final int end) {
      setBeginInternal(context, begin);
      setEndInternal(context, end);
      border.setFirst(context, self.children.get(self.visualIndex(begin)).getFirstBrick(context));
      border.setLast(context, self.children.get(self.visualIndex(end)).getLastBrick(context));
    }

    private void setBeginInternal(final Context context, final int index) {
      beginIndex = index;
      if (leadFirst) setCornerstone(context, beginIndex);
    }

    private void setCornerstone(final Context context, final int index) {
      context.foreground.setCornerstone(
          context,
          self.children.get(self.visualIndex(index)).createOrGetFirstBrick(context),
          () -> {
            for (int at = self.visualIndex(index) - 1; at >= 0; --at) {
              final Brick found = self.children.get(at).getLastBrick(context);
              if (found != null) return found;
            }
            return self.parent.getPreviousBrick(context);
          },
          () -> {
            for (int at = self.visualIndex(index) + 1; at < self.children.size(); ++at) {
              final Brick found = self.children.get(at).getFirstBrick(context);
              if (found != null) return found;
            }
            return self.parent.getNextBrick(context);
          });
    }

    private void setEndInternal(final Context context, final int index) {
      endIndex = index;
      if (!leadFirst) setCornerstone(context, endIndex);
    }

    public void setBegin(final Context context, final int index) {
      leadFirst = true;
      setBeginInternal(context, index);
      border.setFirst(context, self.children.get(self.visualIndex(index)).getFirstBrick(context));
    }

    private void setEnd(final Context context, final int index) {
      leadFirst = false;
      setEndInternal(context, index);
      border.setLast(context, self.children.get(self.visualIndex(index)).getLastBrick(context));
    }

    public void setPosition(final Context context, final int index) {
      setEndInternal(context, index);
      setBeginInternal(context, index);
      border.setFirst(context, self.children.get(self.visualIndex(index)).getFirstBrick(context));
      border.setLast(context, self.children.get(self.visualIndex(index)).getLastBrick(context));
    }

    @Override
    public void clear(final Context context) {
      border.destroy(context);
      self.selection = null;
      context.removeActions(actions);
    }

    @Override
    public Visual getVisual() {
      return self.children.get(beginIndex);
    }

    @Override
    public SelectionState saveState() {
      return new ArraySelectionState(self.value, leadFirst, beginIndex, endIndex);
    }

    @Override
    public Path getSyntaxPath() {
      return self.value.getSyntaxPath().add(String.valueOf(beginIndex));
    }

    @Override
    public void tagsChanged(final Context context) {
      border.setStyle(context, getBorderStyle(context).obbox);
      super.tagsChanged(context);
    }

    @Override
    public ROSet<String> getTags(final Context context) {
      return self.getTags(context).add("selection").ro();
    }

    @Override
    public void dispatch(Dispatcher dispatcher) {
      dispatcher.handle(this);
    }

    @Action.StaticID(id = "enter")
    private class ActionEnter extends Action {
      @Override
      public boolean run(final Context context) {

        return self.value.data.get(beginIndex).visual.selectAnyChild(context);
      }
    }

    @Action.StaticID(id = "exit")
    private class ActionExit extends Action {
      @Override
      public boolean run(final Context context) {
        return self.value.atomParentRef.selectAtomParent(context);
      }
    }

    @Action.StaticID(id = "next")
    private class ActionNext extends Action {
      @Override
      public boolean run(final Context context) {
        return self.parent.selectNext(context);
      }
    }

    @Action.StaticID(id = "previous")
    private class ActionPrevious extends Action {
      @Override
      public boolean run(final Context context) {
        return self.parent.selectPrevious(context);
      }
    }

    @Action.StaticID(id = "next_element")
    private class ActionNextElement extends Action {
      @Override
      public boolean run(final Context context) {

        ArrayCursor.this.leadFirst = true;
        final int newIndex = Math.min(self.value.data.size() - 1, endIndex + 1);
        if (newIndex == beginIndex && newIndex == endIndex) return false;
        setPosition(context, newIndex);
        return true;
      }
    }

    @Action.StaticID(id = "previous_element")
    private class ActionPreviousElement extends Action {
      @Override
      public boolean run(final Context context) {

        ArrayCursor.this.leadFirst = true;
        final int newIndex = Math.max(0, beginIndex - 1);
        if (newIndex == beginIndex && newIndex == endIndex) return false;
        setPosition(context, newIndex);
        return true;
      }
    }

    @Action.StaticID(id = "copy")
    private class ActionCopy extends Action {
      @Override
      public boolean run(final Context context) {
        context.copy(self.value.data.sublist(beginIndex, endIndex + 1));
        return true;
      }
    }

    @Action.StaticID(id = "gather_next")
    private class ActionGatherNext extends Action {
      @Override
      public boolean run(final Context context) {

        final int newIndex = Math.min(self.value.data.size() - 1, endIndex + 1);
        if (endIndex == newIndex) return false;
        setEnd(context, newIndex);
        return true;
      }
    }

    @Action.StaticID(id = "release_next")
    private class ActionReleaseNext extends Action {
      @Override
      public boolean run(final Context context) {

        final int newIndex = Math.max(beginIndex, endIndex - 1);
        if (endIndex == newIndex) return false;
        setEnd(context, newIndex);
        return true;
      }
    }

    @Action.StaticID(id = "gather_previous")
    private class ActionGatherPrevious extends Action {
      @Override
      public boolean run(final Context context) {

        final int newIndex = Math.max(0, beginIndex - 1);
        if (beginIndex == newIndex) return false;
        setBegin(context, newIndex);
        return true;
      }
    }

    @Action.StaticID(id = "release_previous")
    private class ActionReleasePrevious extends Action {

      @Override
      public boolean run(final Context context) {

        final int newIndex = Math.min(endIndex, beginIndex + 1);
        if (beginIndex == newIndex) return false;
        setBegin(context, newIndex);
        return true;
      }
    }

    @Action.StaticID(id = "window")
    private class ActionWindow extends Action {

      @Override
      public boolean run(final Context context) {
        final Atom root = self.value.data.get(beginIndex);
        if (root.visual.selectAnyChild(context)) {
          context.windowExact(root);
          context.triggerIdleLayBricksOutward();
          return true;
        }
        return false;
      }
    }
  }

  private static class ArraySelectionState implements SelectionState {
    private final ValueArray value;
    private final int start;
    private final int end;
    private final boolean leadFirst;

    private ArraySelectionState(
        final ValueArray value, final boolean leadFirst, final int start, final int end) {
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

  private abstract class ArrayHoverable extends Hoverable {
    final BorderAttachment border;

    ArrayHoverable(final Context context) {
      border = new BorderAttachment(context, getBorderStyle(context, getTags(context)).obbox);
    }

    @Override
    protected void clear(final Context context) {
      border.destroy(context);
      if (hoverable == this) hoverable = null;
    }

    @Override
    public Visual visual() {
      return VisualFrontArray.this;
    }

    @Override
    public void tagsChanged(final Context context) {
      border.setStyle(context, getBorderStyle(context, getTags(context)).obbox);
    }

    public abstract void notifyRangeAdjusted(Context context, int index, int removed, int added);

    public abstract void notifySelected(Context context, int start, int end);
  }

  private class ElementHoverable extends ArrayHoverable {
    private int index;

    ElementHoverable(final Context context) {
      super(context);
    }

    @Override
    public void click(final Context context) {
      select(context, true, index, index);
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

  private class PlaceholderHoverable extends ArrayHoverable {

    private PlaceholderHoverable(final Context context, final Brick brick) {
      super(context);
      border.setFirst(context, brick);
      border.setLast(context, brick);
    }

    @Override
    public void click(final Context context) {
      select(context, true, 0, 0);
    }

    @Override
    public VisualAtom atom() {
      return VisualFrontArray.this.parent.atomVisual();
    }

    @Override
    public void tagsChanged(final Context context) {
      border.setStyle(context, getBorderStyle(context, getTags(context)).obbox);
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

  private class ArrayVisualParent extends Parent {

    private final boolean selectable;

    public ArrayVisualParent(final int index, final boolean selectable) {
      super(VisualFrontArray.this, index);
      this.selectable = selectable;
    }

    @Override
    public Hoverable hover(final Context context, final Vector point) {
      if (!selectable) {
        if (parent != null) return parent.hover(context, point);
        return null;
      }
      if (selection != null
          && selection.beginIndex == selection.endIndex
          && selection.beginIndex == valueIndex()) return null;
      if (hoverable == null) {
        hoverable = new ElementHoverable(context);
      }
      ((ElementHoverable) hoverable).setIndex(context, valueIndex());
      return hoverable;
    }

    private int valueIndex() {
      if (getSeparator().isEmpty()) return index;
      return index / 2;
    }

    @Override
    public void firstBrickChanged(final Context context, final Brick firstBrick) {
      if (selection != null && valueIndex() == selection.beginIndex)
        selection.border.setFirst(context, firstBrick);
      if (hoverable != null && valueIndex() == ((ElementHoverable) hoverable).index)
        ((ElementHoverable) hoverable).border.setFirst(context, firstBrick);
    }

    @Override
    public void lastBrickChanged(final Context context, final Brick lastBrick) {
      if (selection != null && valueIndex() == selection.endIndex)
        selection.border.setLast(context, lastBrick);
      if (hoverable != null && valueIndex() == ((ElementHoverable) hoverable).index)
        ((ElementHoverable) hoverable).border.setLast(context, lastBrick);
    }
  }
}
