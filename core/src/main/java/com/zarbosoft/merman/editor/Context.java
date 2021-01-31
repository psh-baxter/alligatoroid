package com.zarbosoft.merman.editor;

import com.zarbosoft.luxem.read.InvalidStream;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.Document;
import com.zarbosoft.merman.document.InvalidDocument;
import com.zarbosoft.merman.document.values.Value;
import com.zarbosoft.merman.editor.banner.Banner;
import com.zarbosoft.merman.editor.details.Details;
import com.zarbosoft.merman.editor.display.Display;
import com.zarbosoft.merman.editor.display.Group;
import com.zarbosoft.merman.editor.hid.HIDEvent;
import com.zarbosoft.merman.editor.serialization.Serializer;
import com.zarbosoft.merman.editor.visual.Vector;
import com.zarbosoft.merman.editor.visual.Visual;
import com.zarbosoft.merman.editor.visual.VisualParent;
import com.zarbosoft.merman.editor.visual.tags.Tags;
import com.zarbosoft.merman.editor.visual.tags.TagsChange;
import com.zarbosoft.merman.editor.visual.visuals.VisualAtom;
import com.zarbosoft.merman.editor.wall.Attachment;
import com.zarbosoft.merman.editor.wall.Brick;
import com.zarbosoft.merman.editor.wall.Wall;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.merman.syntax.style.Style;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.ChainComparator;
import com.zarbosoft.rendaw.common.Pair;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROMap;
import com.zarbosoft.rendaw.common.ROSet;
import com.zarbosoft.rendaw.common.ROSetRef;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;
import com.zarbosoft.rendaw.common.TSSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Context {
  public static Supplier<TSSet> createSet = () -> new TSSet();
  // Settings
  public boolean animateCoursePlacement;
  public boolean animateDetails;
  public int ellipsizeThreshold;
  public int layBrickBatchSize;
  public double retryExpandFactor;
  public double scrollFactor;
  public double scrollAlotFactor;

  // State
  /** Contains the cursor and other marks. Scrolls. */
  public final Group overlay;
  /** Contains the source code. Scrolls. */
  public final Wall foreground;

  public final Display display;
  public final Syntax syntax;
  public final Document document;
  public final Serializer serializer;
  public final I18nEngine i18n;
  public final TSSet<SelectionListener> cursorListeners = new TSSet<>();
  public final TSSet<HoverListener> hoverListeners = new TSSet<>();
  private final TSSet<TagsListener> selectionTagsChangeListeners = new TSSet<>();
  private final TSSet<TagsListener> globalTagsChangeListeners = new TSSet<>();
  private final TSSet<ActionChangeListener> actionChangeListeners = new TSSet<>();
  private final TSList<Action> actions = new TSList<>();
  private final Consumer<IterationTask> addIteration;
  private final Consumer<Integer> flushIteration;
  public boolean window;
    private Atom windowAtom;
  private final TSSet<String> globalTags = new TSSet<String>();
  public KeyListener keyListener;
  public TextListener textListener;
  public ClipboardEngine clipboardEngine;
  /** Contains banner/details and icons. Doesn't scroll. */
  public Group midground;
  /** Contains source borders. Scrolls. */
  public Group background;

  public Banner banner;
  public Details details;
  public int scroll = 0;
  public int peek = 0;
  public IterationLayBricks idleLayBricks = null;
  public int edge;
  public int transverseEdge;
  public Brick hoverBrick;
  public Hoverable hover;
  public HoverIteration hoverIdle;
  public Cursor cursor;
  TSMap<ROSet<String>, Style> styleCache = new TSMap<>();
  List<ContextIntListener> converseEdgeListeners = new ArrayList<>();
  List<ContextIntListener> transverseEdgeListeners = new ArrayList<>();
  int scrollStart;
  int scrollEnd;
  int scrollStartBeddingBefore;
  int scrollStartBeddingAfter;
  int selectToken = 0;
  boolean keyHandlingInProgress = false;
  boolean debugInHover = false;
  private IterationNotifyBricksCreated idleNotifyBricksCreated;

  public static class InitialConfig {
    public boolean animateCoursePlacement = false;
    public boolean animateDetails = false;
    public int ellipsizeThreshold = Integer.MAX_VALUE;
    public int layBrickBatchSize = 10;
    public double retryExpandFactor = 1.25;
    public double scrollFactor = 0.1;
    public double scrollAlotFactor = 0.8;
  }

  public Context(
          InitialConfig config,
          Syntax syntax,
          Document document,
          Display display,
          Consumer<IterationTask> addIteration,
          Consumer<Integer> flushIteration,
          ClipboardEngine clipboardEngine,
          Serializer serializer, boolean startWindowed, I18nEngine i18n) {
    this.serializer = serializer;
    this.i18n = i18n;
    actions.add(
        new ActionWindowClear(),
        new ActionWindowTowardsRoot(),
        new ActionWindowTowardsCursor(),
        new ActionScrollNext(),
        new ActionScrollNextAlot(),
        new ActionScrollPrevious(),
        new ActionScrollPreviousAlot(),
        new ActionScrollReset());
    this.syntax = syntax;
    this.document = document;
    this.display = display;
    this.animateCoursePlacement = config.animateCoursePlacement;
    this.animateDetails = config.animateDetails;
    this.ellipsizeThreshold = config.ellipsizeThreshold;
    this.layBrickBatchSize = config.layBrickBatchSize;
    this.retryExpandFactor = config.retryExpandFactor;
    this.scrollFactor = config.scrollFactor;
    this.scrollAlotFactor = config.scrollAlotFactor;
    display.setBackgroundColor(syntax.background);
    edge = display.edge();
    transverseEdge = display.transverseEdge();
    background = display.group();
    midground = display.group();
    this.foreground = new Wall(this);
    this.overlay = display.group();
    display.add(background);
    display.add(midground);
    display.add(foreground.visual);
    display.add(overlay);
    this.addIteration = addIteration;
    this.flushIteration = flushIteration;
    banner = new Banner(this);
    details = new Details(this);
    this.clipboardEngine = clipboardEngine;
    display.addConverseEdgeListener(
        (oldValue, newValue) -> {
          edge =
              Math.max(
                  0,
                  newValue - document.syntax.pad.converseStart - document.syntax.pad.converseEnd);
          converseEdgeListeners.forEach(listener -> listener.changed(this, oldValue, newValue));
        });
    display.addTransverseEdgeListener(
        ((oldValue, newValue) -> {
          transverseEdge = newValue;
          scrollVisible();
          transverseEdgeListeners.forEach(listener -> listener.changed(this, oldValue, newValue));
        }));
    display.addHIDEventListener(
        hidEvent -> {
          keyHandlingInProgress = false;
          if (keyListener != null && keyListener.handleKey(this, hidEvent)) {
            keyHandlingInProgress = true;
            flushIteration(100);
          }
        });
    display.addTypingListener(
        text -> {
          if (keyHandlingInProgress) {
            keyHandlingInProgress = false;
            return;
          }
          if (text.isEmpty()) return;
          textListener.handleText(this, text);
          flushIteration(100);
        });
    display.addMouseExitListener(
        () -> {
          if (hoverIdle != null) {
            hoverIdle.point = null;
          } else if (hover != null) {
            clearHover();
          }
        });
    display.addMouseMoveListener(
        vector -> {
          if (hoverIdle == null) {
            hoverIdle = new HoverIteration(this);
            addIteration.accept(hoverIdle);
          }
          hoverIdle.point = vector.add(new Vector(-syntax.pad.converseStart, scroll + peek));
        });
    foreground.addCornerstoneListener(
        this,
        new Wall.CornerstoneListener() {
          Brick cornerstone = null;
          private final Attachment selectionBrickAttachment =
              new Attachment() {
                @Override
                public void setTransverse(final Context context, final int transverse) {
                  final int oldScrollStart = scrollStart;
                  scrollStart = transverse;
                  scrollEnd += scrollStart - oldScrollStart;
                  scrollVisible();
                }

                @Override
                public void setTransverseSpan(
                    final Context context, final int ascent, final int descent) {
                  scrollEnd = scrollStart + ascent + descent;
                  scrollVisible();
                }

                @Override
                public void destroy(final Context context) {
                  cornerstone = null;
                }
              };

          @Override
          public void cornerstoneChanged(final Context context, final Brick brick) {
            if (cornerstone != null) {
              cornerstone.removeAttachment(selectionBrickAttachment);
            }
            this.cornerstone = brick;
            cornerstone.addAttachment(context, selectionBrickAttachment);
          }
        });
    foreground.addBeddingListener(
        this,
        new Wall.BeddingListener() {
          @Override
          public void beddingChanged(
              final Context context, final int beddingBefore, final int beddingAfter) {
            scrollStartBeddingBefore = beddingBefore;
            scrollStartBeddingAfter = beddingAfter;
            scrollVisible();
          }
        });
    if (!startWindowed) windowClear();
    else windowToSupertree(document.root);
    display.addHIDEventListener(
        event -> {
          clearHover();
        });
    document.root.visual.selectAnyChild(this);
    triggerIdleLayBricksOutward();
  }

  public void clearHover() {
    if (debugInHover) throw new AssertionError();
    if (hover != null) {
      hover.clear(this);
      hover = null;
    }
    if (hoverIdle != null) {
      hoverIdle.destroy();
    }
    hoverBrick = null;
  }

  private void scrollVisible() {
    final int minimum = scrollStart - scrollStartBeddingBefore - syntax.pad.transverseStart;
    final int maximum = scrollEnd + scrollStartBeddingAfter + syntax.pad.transverseEnd;

    // Change to scroll required to make it match the start of the window that ends at the max
    final int maxDiff = maximum - transverseEdge - scroll;

    Integer newScroll = null;
    if (minimum < scroll) {
      // Minimum is above scroll
      newScroll = minimum;
    } else if (maxDiff > 0) {
      // Maximum is below scroll window
      newScroll = Math.min(scroll + maxDiff, minimum);
    }
    if (newScroll != null) {
      scroll = newScroll;
      applyScroll();
    }
  }

  public void applyScroll() {
    final int newScroll = scroll + peek;
    foreground.visual.setPosition(
        this, new Vector(syntax.pad.converseStart, -newScroll), animateCoursePlacement);
    background.setPosition(
        this, new Vector(syntax.pad.converseStart, -newScroll), animateCoursePlacement);
    overlay.setPosition(
        this, new Vector(syntax.pad.converseStart, -newScroll), animateCoursePlacement);
    banner.setScroll(this, newScroll);
    details.setScroll(this, newScroll);
  }

  public void flushIteration(final int limit) {
    this.flushIteration.accept(limit);
  }

  public void addActions(final ROList<Action> actions) {
    this.actions.addAll(actions);
    for (ActionChangeListener l : actionChangeListeners.copy()) {
      l.actionsAdded(this);
    }
  }

  public void removeActions(final ROList<Action> key) {
    this.actions.removeAll(key);
    for (ActionChangeListener l : actionChangeListeners.copy()) {
      l.actionsRemoved(this);
    }
  }

  public void addActionChangeListener(final ActionChangeListener listener) {
    actionChangeListeners.add(listener);
  }

  public void removeActionChangeListener(final ActionChangeListener listener) {
    actionChangeListeners.remove(listener);
  }

  public ROList<Action> actions() {
    return actions;
  }

  public void addConverseEdgeListener(final ContextIntListener listener) {
    converseEdgeListeners.add(listener);
  }

  public void removeConverseEdgeListener(final ContextIntListener listener) {
    converseEdgeListeners.remove(listener);
  }

  public void addTransverseEdgeListener(final ContextIntListener listener) {
    transverseEdgeListeners.add(listener);
  }

  public void removeTransverseEdgeListener(final ContextIntListener listener) {
    transverseEdgeListeners.remove(listener);
  }

  public void copy(final ROList<Atom> atoms) {
    clipboardEngine.set(serializer.write(atoms));
  }

  public void copy(final String string) {
    clipboardEngine.setString(string);
  }

  public ROList<Atom> uncopy(final String type) {
    final byte[] bytes = clipboardEngine.get();
    if (bytes == null) return ROList.empty;
    try {
      return serializer.load(syntax, type, bytes);
    } catch (final InvalidStream ignored) {
    } catch (final InvalidDocument ignored) {
    }
    return ROList.empty;
  }

  public String uncopyString() {
    return clipboardEngine.getString();
  }

  public Object syntaxLocate(final Path path) {
    Object at = document.root;
    for (int i = 0; i < path.segments.size(); ++i) {
      String segment = path.segments.get(i);
      if (at instanceof Atom) {
        at = ((Atom) at).syntaxLocateStep(segment);
        if (at == null) throw new InvalidPath(path.segments.sublist(0, i), path.segments);
      } else if (at instanceof Value) {
        at = ((Value) at).syntaxLocateStep(segment);
        if (at == null) throw new InvalidPath(path.segments.sublist(0, i), path.segments);
      } else throw new Assertion();
    }
    return at;
  }

  public void addSelectionListener(final SelectionListener listener) {
    this.cursorListeners.add(listener);
  }

  public void removeSelectionListener(final SelectionListener listener) {
    this.cursorListeners.remove(listener);
  }

  public void addHoverListener(final HoverListener listener) {
    this.hoverListeners.add(listener);
  }

  public void removeHoverListener(final HoverListener listener) {
    this.hoverListeners.remove(listener);
  }

  public void addSelectionTagsChangeListener(final TagsListener listener) {
    this.selectionTagsChangeListeners.add(listener);
  }

  public void removeSelectionTagsChangeListener(final TagsListener listener) {
    this.selectionTagsChangeListeners.remove(listener);
  }

  public void addGlobalTagsChangeListener(final TagsListener listener) {
    this.globalTagsChangeListeners.add(listener);
  }

  public void removeGlobalTagsChangeListener(final TagsListener listener) {
    this.globalTagsChangeListeners.remove(listener);
  }

  public void triggerIdleLayBricks(
      final VisualParent parent,
      final int index,
      final int addCount,
      final int size,
      final Function<Integer, Brick> accessFirst,
      final Function<Integer, Brick> accessLast) {
    if (size == 0) throw new AssertionError();
    if (index > 0) {
      final Brick previousBrick = accessLast.apply(index - 1);
      if (previousBrick != null) {
        triggerIdleLayBricksAfterEnd(previousBrick);
        return;
      }
      if (index + addCount < size) {
        // Hits neither edge
        final Brick nextBrick = accessFirst.apply(index + addCount);
        if (nextBrick == null) return;
        triggerIdleLayBricksBeforeStart(nextBrick);
      } else {
        // Hits end edge
        if (parent == null) return;
        final Brick nextBrick = parent.getNextBrick(this);
        if (nextBrick == null) return;
        triggerIdleLayBricksBeforeStart(nextBrick);
      }
    } else {
      if (index + addCount < size) {
        // Hits index edge
        final Brick nextBrick = accessFirst.apply(index + addCount);
        if (nextBrick != null) {
          triggerIdleLayBricksBeforeStart(nextBrick);
          return;
        }
        final Brick previousBrick = parent.getPreviousBrick(this);
        if (previousBrick == null) return;
        triggerIdleLayBricksAfterEnd(previousBrick);
      } else {
        // Hits both edges
        if (parent == null) return;
        final Brick previousBrick = parent.getPreviousBrick(this);
        if (previousBrick != null) {
          triggerIdleLayBricksAfterEnd(previousBrick);
          return;
        }
        final Brick nextBrick = parent.getNextBrick(this);
        if (nextBrick == null) return;
        triggerIdleLayBricksBeforeStart(nextBrick);
      }
    }
  }

  public void triggerIdleLayBricksAfterEnd(final Brick end) {
    if (idleLayBricks == null) {
      idleLayBricks = new IterationLayBricks();
      addIteration(idleLayBricks);
    }
    idleLayBricks.ends.add(end);
  }

  public void addIteration(final IterationTask task) {
    this.addIteration.accept(task);
  }

  public void triggerIdleLayBricksBeforeStart(final Brick start) {
    if (idleLayBricks == null) {
      idleLayBricks = new IterationLayBricks();
      addIteration(idleLayBricks);
    }
    idleLayBricks.starts.add(start);
  }

  public void bricksCreated(final Visual visual, final Brick brick) {
    final ArrayList<Brick> out = new ArrayList<>();
    out.add(brick);
    bricksCreated(visual, out);
  }

  public void bricksCreated(final Visual visual, final ArrayList<Brick> bricks) {
    if (idleNotifyBricksCreated == null) {
      idleNotifyBricksCreated = new IterationNotifyBricksCreated();
    }
    idleNotifyBricksCreated.newBricks.add(new Pair<>(visual, bricks));
  }

  public void windowAdjustMinimalTo(final Value value) {
    // Check if the selection is a supertree of the current window
    if (isSubtree(windowAtom, value.atomParentRef.atom())) {
      windowToSupertree(value.atomParentRef.atom());
      return;
    }

    // Otherwise from the selection go towards the root to find the last parent where the selection
    // is still visible
    Atom nextWindow = value.atomParentRef.atom();
    int depth = 0;
    while (true) {
      if (nextWindow == windowAtom) return;
      if (nextWindow.valueParentRef == null) break;
      depth += nextWindow.type.depthScore();
      if (depth >= ellipsizeThreshold) break;
      nextWindow = nextWindow.valueParentRef.value.atomParentRef.atom();
    }

    if (isSubtree(windowAtom, nextWindow)) windowToSupertree(nextWindow);
    else windowToNonSupertree(nextWindow);
  }

  public void windowClear() {
    window = false;
    windowAtom = document.root;
    document.root.ensureVisual(this, null, ROMap.empty, 0, 0);
    changeGlobalTags(
        new TagsChange(
            TSSet.of(), TSSet.of(Tags.TAG_GLOBAL_WINDOWED, Tags.TAG_GLOBAL_ROOT_WINDOW)));
  }

  /**
   * Window tree is subtree or non-overlapping (or equal to) current window tree
   *
   * @param tree
   */
  private void windowToNonSupertree(Atom tree) {
    window = true;
    boolean wasRoot = windowAtom == document.root;
    final Visual oldWindow = windowAtom.visual;
    windowAtom = tree;
    Visual windowVisual = windowAtom.ensureVisual(this, null, ROMap.empty, 0, 0);
    oldWindow.uproot(this, windowVisual);
    if (wasRoot) {
      changeGlobalTags(
          new TagsChange(TSSet.of(Tags.TAG_GLOBAL_WINDOWED, Tags.TAG_GLOBAL_WINDOWED), TSSet.of()));
    } else {
      changeGlobalTags(
          new TagsChange(TSSet.of(Tags.TAG_GLOBAL_WINDOWED), TSSet.of(Tags.TAG_GLOBAL_WINDOWED)));
    }
  }

  /**
   * New window is the supertree (including equal to) of the current window tree
   *
   * @param supertree
   */
  private void windowToSupertree(Atom supertree) {
    window = true;
    windowAtom = supertree;
    windowAtom.ensureVisual(this, null, ROMap.empty, 0, 0);
    if (supertree == document.root)
      changeGlobalTags(
          new TagsChange(
              TSSet.of(Tags.TAG_GLOBAL_WINDOWED, Tags.TAG_GLOBAL_ROOT_WINDOW), TSSet.of()));
    else changeGlobalTags(new TagsChange(TSSet.of(Tags.TAG_GLOBAL_WINDOWED), TSSet.of()));
  }

  public boolean isSubtree(Atom subtree, Atom supertree) {
    Atom at = subtree;
    while (true) {
      if (at == supertree) return true;
      if (at.valueParentRef == null) break;
      at = at.valueParentRef.value.atomParentRef.atom();
    }
    return false;
  }

  public void windowExact(final Atom atom) {
    if (isSubtree(windowAtom, atom)) {
      windowToSupertree(atom);
    } else {
      windowToNonSupertree(atom);
    }
  }

  public void changeGlobalTags(final TagsChange change) {
    if (!change.apply(globalTags)) return;
    banner.tagsChanged(this);
    details.tagsChanged(this);
    windowAtom.visual.tagsChanged(this);
    for (TagsListener l : globalTagsChangeListeners.copy()) {
      l.tagsChanged(this);
    }
  }

  public void triggerIdleLayBricksOutward() {
    triggerIdleLayBricksBeforeStart(foreground.children.get(0).children.get(0));
    triggerIdleLayBricksAfterEnd(foreground.children.last().children.last());
  }

  public void clearSelection() {
    cursor.clear(this);
    cursor = null;
  }

  public void setCursor(final Cursor cursor) {
    final int localToken = ++selectToken;
    final Cursor oldCursor = this.cursor;
    this.cursor = cursor;
    if (oldCursor != null) {
      oldCursor.clear(this);
    }
    if (localToken != selectToken) return;
    for (SelectionListener l : cursorListeners.copy()) {
      l.cursorChanged(this, cursor);
    }
    selectionTagsChanged();
    triggerIdleLayBricksOutward();
  }

  public void selectionTagsChanged() {
    if (cursor == null) return;
    banner.tagsChanged(this);
    details.tagsChanged(this);
    for (TagsListener l : selectionTagsChangeListeners.copy()) {
      l.tagsChanged(this);
    }
  }

  public Style getStyle(final ROSet<String> tags) {
    return styleCache.getCreate(
        tags.own(),
        () -> {
          TSList<Style.Spec> toMerge = new TSList<Style.Spec>();
          for (final Style.Spec spec : syntax.styles) {
            if (!tags.containsAll(spec.with) || tags.intersect(spec.without).some()) continue;
            toMerge.add(spec);
          }
          return Style.create(toMerge);
        });
  }

  public ROSetRef<String> getGlobalTags() {
    return globalTags;
  }

  public Atom windowAtom() {
    return windowAtom;
  }

  public static interface ContextIntListener {
    void changed(Context context, int oldValue, int newValue);
  }

  public static interface ActionChangeListener {
    void actionsAdded(Context context);

    void actionsRemoved(Context context);
  }

  @FunctionalInterface
  public interface KeyListener {
    boolean handleKey(Context context, HIDEvent event);
  }

  @FunctionalInterface
  public interface TextListener {
    void handleText(Context context, String text);
  }

  public interface SelectionListener {
    public abstract void cursorChanged(Context context, Cursor cursor);
  }

  public abstract static class HoverListener {
    public abstract void hoverChanged(Context context, Hoverable selection);
  }

  public abstract static class TagsListener {
    public abstract void tagsChanged(Context context);
  }

  public class IterationLayBricks extends IterationTask {
    public Set<Brick> ends = new HashSet<>();
    public Set<Brick> starts = new HashSet<>();

    @Override
    protected double priority() {
      return P.layBricks;
    }

    @Override
    public boolean runImplementation(final IterationContext iterationContext) {
      for (int i = 0; i < layBrickBatchSize; ++i) {
        if (ends.isEmpty() && starts.isEmpty()) {
          return false;
        }
        if (!ends.isEmpty()) {
          final Brick next = ends.iterator().next();
          ends.remove(next);
          if (next.parent != null) {
            final Brick created = next.createNext(Context.this);
            if (created != null) {
              next.addAfter(Context.this, created);
              ends.add(created);
            }
          }
        }
        if (!starts.isEmpty()) {
          final Brick previous = starts.iterator().next();
          starts.remove(previous);
          if (previous.parent != null) {
            final Brick created = previous.createPrevious(Context.this);
            if (created != null) {
              previous.addBefore(Context.this, created);
              starts.add(created);
            }
          }
        }
      }
      if (idleNotifyBricksCreated != null) {
        idleNotifyBricksCreated.run(iterationContext);
      }
      return true;
    }

    @Override
    protected void destroyed() {
      idleLayBricks = null;
    }
  }

  private class IterationNotifyBricksCreated extends IterationTask {
    private final Queue<Pair<Visual, ArrayList<Brick>>> newBricks =
        new PriorityQueue<>(
            11,
            new ChainComparator<Pair<Visual, ArrayList<Brick>>>()
                .greaterFirst(pair -> pair.first.visualDepth)
                .build());

    @Override
    protected double priority() {
      // After laying bricks.  May be prematurely flushed after a brick laying batch.
      return P.notifyBricks;
    }

    @Override
    protected boolean runImplementation(final IterationContext iterationContext) {
      final List<Pair<Visual, ArrayList<Brick>>> level = new ArrayList<>();
      while (!newBricks.isEmpty()) {
        // Find all bricks at next highest depth
        level.clear();
        Integer levelDepth = null;
        while (!newBricks.isEmpty()) {
          final Pair<Visual, ArrayList<Brick>> next = newBricks.poll();
          final int depth = next.first.visualDepth;
          if (levelDepth == null) {
            levelDepth = depth;
          } else if (depth == levelDepth) {

          } else {
            newBricks.add(next);
            break;
          }
        }

        // Group by visual and pass to bricksCreated in that visual
        level.stream()
            .collect(
                Collectors.groupingBy(
                    pair -> pair.first,
                    Collectors.reducing(
                        null,
                        pair -> pair.second,
                        (a, b) -> {
                          a.addAll(b);
                          return a;
                        })))
            .entrySet()
            .stream()
            .forEach(
                entry -> {
                  final Visual visual = entry.getKey();
                  if (visual.parent() == null) return;
                  visual.parent().bricksCreated(Context.this, entry.getValue());
                });
      }
      return false;
    }

    @Override
    protected void destroyed() {
      idleNotifyBricksCreated = null;
    }
  }

  public class HoverIteration extends IterationTask {
    public Vector point = null;
    Context context;
    Brick at;

    public HoverIteration(final Context context) {
      this.context = context;
      at = hoverBrick == null ? context.foreground.children.get(0).children.get(0) : hoverBrick;
    }

    @Override
    protected double priority() {
      return 500;
    }

    @Override
    public boolean runImplementation(final IterationContext iterationContext) {
      debugInHover = true;
      if (at == null || at.parent == null) {
        debugInHover = false;
        return false;
      }
      if (point == null) {
        hoverBrick = null;
        debugInHover = false;
        return false;
      }
      if (point.transverse < at.parent.transverseStart && at.parent.index > 0) {
        at = context.foreground.children.get(at.parent.index - 1).children.get(0);
      } else if (point.transverse > at.parent.transverseEdge()
          && at.parent.index < foreground.children.size() - 1) {
        at = context.foreground.children.get(at.parent.index + 1).children.get(0);
      } else {
        while (point.converse < at.getConverse(context) && at.index > 0) {
          at = at.parent.children.get(at.index - 1);
        }
        while (point.converse >= at.converseEdge(context)
            && at.index < at.parent.children.size() - 1) {
          at = at.parent.children.get(at.index + 1);
        }
        final Hoverable old = hover;
        hover = at.hover(context, point);
        if (hover != old) {
          if (old != null) old.clear(context);
          for (HoverListener l : hoverListeners.copy()) {
            l.hoverChanged(context, hover);
          }
        }
        hoverBrick = at;
        hoverIdle = null;
        debugInHover = false;
        return false;
      }
      debugInHover = false;
      return true;
    }

    @Override
    protected void destroyed() {
      hoverIdle = null;
    }
  }

  @Action.StaticID(id = "window_clear")
  private class ActionWindowClear extends Action {
    @Override
    public boolean run(final Context context) {
      if (!window) return false;
      windowClear();
      triggerIdleLayBricksOutward();
      return true;
    }
  }

  @Action.StaticID(id = "window_up")
  private class ActionWindowTowardsRoot extends Action {
    public ActionWindowTowardsRoot() {}

    @Override
    public boolean run(final Context context) {
      if (!window) return false;
      if (windowAtom == document.root) return false;
      windowToSupertree(windowAtom.valueParentRef.value.atomParentRef.atom());
      triggerIdleLayBricksOutward();
      return true;
    }
  }

  @Action.StaticID(id = "window_down")
  private class ActionWindowTowardsCursor extends Action {
    @Override
    public boolean run(final Context context) {
      if (!window) return false;
      VisualAtom windowNext = null;
      final VisualAtom stop = windowAtom.visual;
      if (cursor.getVisual().parent() == null) return false;
      VisualAtom at = cursor.getVisual().parent().atomVisual();
      while (at != null) {
        if (at == stop) break;
        if (at.parent() == null) break;
        windowNext = at;
        at = at.parent().atomVisual();
      }
      if (windowNext == null) return false;
      context.windowToNonSupertree(windowNext.atom);
      triggerIdleLayBricksOutward();
      return true;
    }
  }

  @Action.StaticID(id = "scroll_previous")
  private class ActionScrollNext extends Action {

    @Override
    public boolean run(final Context context) {
      scroll -= scrollFactor * transverseEdge;
      applyScroll();
      return false;
    }
  }

  @Action.StaticID(id = "scroll_previous_alot")
  private class ActionScrollNextAlot extends Action {
    @Override
    public boolean run(final Context context) {
      scroll -= scrollAlotFactor * transverseEdge;
      applyScroll();
      return false;
    }
  }

  @Action.StaticID(id = "scroll_next")
  private class ActionScrollPrevious extends Action {
    @Override
    public boolean run(final Context context) {
      scroll += scrollFactor * transverseEdge;
      applyScroll();
      return false;
    }
  }

  @Action.StaticID(id = "scroll_next_alot")
  private class ActionScrollPreviousAlot extends Action {
    @Override
    public boolean run(final Context context) {
      scroll += scrollAlotFactor * transverseEdge;
      applyScroll();
      return false;
    }
  }

  @Action.StaticID(id = "scroll_reset")
  private class ActionScrollReset extends Action {

    @Override
    public boolean run(final Context context) {
      scrollVisible();
      return false;
    }
  }
}
