package com.zarbosoft.merman.core.visual.visuals;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.Hoverable;
import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.document.fields.FieldArray;
import com.zarbosoft.merman.core.syntax.front.FrontArraySpecBase;
import com.zarbosoft.merman.core.syntax.front.FrontSymbolSpec;
import com.zarbosoft.merman.core.visual.Vector;
import com.zarbosoft.merman.core.visual.Visual;
import com.zarbosoft.merman.core.visual.VisualLeaf;
import com.zarbosoft.merman.core.visual.VisualParent;
import com.zarbosoft.merman.core.visual.alignment.Alignment;
import com.zarbosoft.merman.core.wall.Brick;
import com.zarbosoft.merman.core.wall.BrickInterface;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSList;

import java.util.function.Consumer;

public class VisualFieldArray extends VisualGroup implements VisualLeaf {
  public final FieldArray value;
  private final FieldArray.Listener dataListener;
  private final FrontArraySpecBase front;
  public CursorFieldArray cursor;
  public HoverableFieldArrayBase hoverable;
  private Brick ellipsis = null;
  private Brick empty = null;

  public VisualFieldArray(
      final FrontArraySpecBase front,
      final VisualParent parent,
      final Atom atom,
      final int visualDepth) {
    super(visualDepth);
    this.front = front;
    this.value = front.field.get(atom.namedFields);
    dataListener = new DataListener(value, parent);
    value.addListener(dataListener);
    value.visual = this;
  }

  public void copy(Context context, int beginIndex, int endIndex) {
    value.back().copy(context, value.data.sublist(beginIndex, endIndex + 1));
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
                    new FrontArrayParent(this, addAt, false),
                    visualDepth + 1,
                    depthScore());
            for (int fixIndex = 0; fixIndex < front.separator.size(); ++fixIndex) {
              final FrontSymbolSpec fix = front.separator.get(fixIndex);
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
        final VisualGroup elementGroup =
            new VisualGroup(
                context, new FrontArrayParent(this, addIndex, true), visualDepth + 1, depthScore());
        int groupIndex = 0;
        for (final FrontSymbolSpec fix : front.prefix)
          elementGroup.add(
              context,
              fix.createVisual(
                  context,
                  elementGroup.createParent(groupIndex++),
                  elementGroup.visualDepth + 1,
                  depthScore()));
        final VisualAtom nodeVisual =
            (VisualAtom)
                atom.ensureVisual(
                    context,
                    new Parent(elementGroup, groupIndex++) {
                      @Override
                      public void notifyLastBrickCreated(Context context, Brick brick) {
                        if (cursor != null
                            && cursor.endIndex == ((FieldArray.Parent) atom.fieldParentRef).index)
                          cursor.border.setLast(context, brick);
                        if (hoverable != null
                            && hoverable instanceof HoverableFieldArray
                            && ((HoverableFieldArray) hoverable).index
                                == ((FieldArray.Parent) atom.fieldParentRef).index)
                          hoverable.border.setLast(context, brick);
                        super.notifyLastBrickCreated(context, brick);
                      }

                      @Override
                      public void notifyFirstBrickCreated(Context context, Brick brick) {
                        if (cursor != null
                            && cursor.beginIndex == ((FieldArray.Parent) atom.fieldParentRef).index)
                          cursor.border.setFirst(context, brick);
                        if (hoverable != null
                            && hoverable instanceof HoverableFieldArray
                            && ((HoverableFieldArray) hoverable).index
                                == ((FieldArray.Parent) atom.fieldParentRef).index)
                          hoverable.border.setFirst(context, brick);
                        super.notifyFirstBrickCreated(context, brick);
                      }
                    },
                    elementGroup.visualDepth + 2,
                    depthScore());
        elementGroup.add(
            context,
            new Visual(elementGroup.visualDepth + 1) {
              @Override
              public VisualParent parent() {
                return nodeVisual.parent();
              }

              @Override
              public CreateBrickResult createOrGetCornerstoneCandidate(final Context context) {
                return nodeVisual.createOrGetCornerstoneCandidate(context);
              }

              @Override
              public ExtendBrickResult createFirstBrick(final Context context) {
                return nodeVisual.createFirstBrick(context);
              }

              @Override
              public ExtendBrickResult createLastBrick(final Context context) {
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
              public boolean selectIntoAnyChild(final Context context) {
                return nodeVisual.selectIntoAnyChild(context);
              }

              @Override
              public void notifyLastBrickCreated(Context context, Brick brick) {
                throw new Assertion();
              }

              @Override
              public void notifyFirstBrickCreated(Context context, Brick brick) {
                throw new Assertion();
              }
            });
        for (final FrontSymbolSpec fix : front.suffix)
          elementGroup.add(
              context,
              fix.createVisual(
                  context,
                  elementGroup.createParent(groupIndex++),
                  elementGroup.visualDepth + 1,
                  depthScore()));
        if (atomVisual().compact) elementGroup.compact(context);
        super.add(context, elementGroup, addIndex++);
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
                return VisualFieldArray.this;
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

  public int visualIndex(final int valueIndex) {
    if (front.separator.isEmpty()) return valueIndex;
    else return valueIndex * 2;
  }

  public void select(
      final Context context, final boolean leadFirst, final int start, final int end) {
    if (hoverable != null) hoverable.notifySelected(context, start, end);
    if (cursor == null) {
      cursor = context.cursorFactory.createFieldArrayCursor(context, this, leadFirst, start, end);
      context.setCursor(cursor);
    } else {
      cursor.setRange(context, start, end);
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
  public boolean selectIntoAnyChild(final Context context) {
    value.selectInto(context, true, 0, 0);
    return true;
  }

  @Override
  public CreateBrickResult createOrGetCornerstoneCandidate(final Context context) {
    if (value.data.isEmpty()) {
      if (empty != null) return CreateBrickResult.brick(empty);
      else return CreateBrickResult.brick(createEmpty(context));
    } else if (ellipsize(context)) {
      if (ellipsis != null) return CreateBrickResult.brick(ellipsis);
      else return CreateBrickResult.brick(createEllipsis(context));
    } else return super.createOrGetCornerstoneCandidate(context);
  }

  @Override
  public ExtendBrickResult createFirstBrick(final Context context) {
    if (value.data.isEmpty()) {
      if (empty != null) return ExtendBrickResult.exists();
      return ExtendBrickResult.brick(createEmpty(context));
    }
    if (ellipsize(context)) {
      if (ellipsis != null) return ExtendBrickResult.exists();
      return ExtendBrickResult.brick(createEllipsis(context));
    }
    return super.createFirstBrick(context);
  }

  @Override
  public ExtendBrickResult createLastBrick(final Context context) {
    if (value.data.isEmpty()) {
      if (empty != null) return ExtendBrickResult.exists();
      return ExtendBrickResult.brick(createEmpty(context));
    }
    if (ellipsize(context)) {
      if (ellipsis != null) return ExtendBrickResult.exists();
      return ExtendBrickResult.brick(createEllipsis(context));
    }
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
                return VisualFieldArray.this;
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
    return ellipsis;
  }

  @Override
  public void uproot(final Context context, final Visual root) {
    if (root == this) {
      // Only root array, which should never be uprooted with itself as the stop point
      throw new AssertionError();
    }
    if (cursor != null) context.clearCursor();
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
      hoverable = new HoverableFieldArrayPlaceholder(context, empty, this);
      return new ROPair<>(hoverable, true);
    } else if (ellipsis != null) {
      hoverable = new HoverableFieldArrayPlaceholder(context, ellipsis, this);
      return new ROPair<>(hoverable, true);
    } else return super.hover(context, point);
  }

  private static class FrontArrayParent extends Parent {
    private final boolean selectable;
    private final VisualFieldArray visual;

    public FrontArrayParent(VisualFieldArray visual, final int index, final boolean selectable) {
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
      if (visual.cursor != null
          && visual.cursor.beginIndex == visual.cursor.endIndex
          && visual.cursor.beginIndex == newIndex) return null;
      if (visual.hoverable == null) {
        visual.hoverable = new HoverableFieldArray(context, visual);
        changed = true;
      }
      HoverableFieldArray elementHoverable = (HoverableFieldArray) visual.hoverable;
      if (elementHoverable.index != newIndex) changed = true;
      elementHoverable.setIndex(context, newIndex);
      return new ROPair<>(visual.hoverable, changed);
    }

    private int valueIndex() {
      if (visual.front.separator.isEmpty()) return index;
      return index / 2;
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
      if (cursor != null) {
        oldSelectionBeginIndex = cursor.beginIndex;
        oldSelectionEndIndex = cursor.endIndex;
      } else if (context.cursor != null) {
        VisualParent parent = context.cursor.getVisual().parent();
        while (parent != null) {
          final Visual visual = parent.visual();
          if (visual == VisualFieldArray.this) {
            fixDeepSelectionIndex = ((FrontArrayParent) parent).valueIndex();
            break;
          }
          parent = visual.parent();
        }
      }
      if (hoverable == null && context.hover != null) {
        VisualParent parent = context.hover.visual().parent();
        while (parent != null) {
          final Visual visual = parent.visual();
          if (visual == VisualFieldArray.this) {
            fixDeepHoverIndex = ((FrontArrayParent) parent).valueIndex();
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
        if (value.data.isEmpty()) value.atomParentRef.selectParent(context);
        else {
          if (oldSelectionBeginIndex >= index + remove)
            cursor.setBegin(context, oldSelectionBeginIndex - remove + add.size());
          else if (oldSelectionBeginIndex >= index)
            cursor.setBegin(
                context, Math.min(value.data.size() - 1, index + Math.max(0, add.size() - 1)));
          if (oldSelectionEndIndex >= index + remove)
            cursor.setEnd(context, oldSelectionEndIndex - remove + add.size());
          else if (oldSelectionEndIndex >= index)
            cursor.setEnd(
                context, Math.min(value.data.size() - 1, index + Math.max(0, add.size() - 1)));
        }
      } else if (fixDeepSelectionIndex != null) {
        if (value.data.isEmpty()) value.atomParentRef.selectParent(context);
        else if (fixDeepSelectionIndex >= index && fixDeepSelectionIndex < index + remove) {
          final int newIndex = Math.min(value.data.size() - 1, index + Math.max(0, add.size() - 1));
          select(context, true, newIndex, newIndex);
        }
      }
    }
  }
}
