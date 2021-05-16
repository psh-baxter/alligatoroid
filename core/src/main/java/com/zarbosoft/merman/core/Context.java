package com.zarbosoft.merman.core;

import com.zarbosoft.merman.core.display.Display;
import com.zarbosoft.merman.core.display.Font;
import com.zarbosoft.merman.core.display.Group;
import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.document.Document;
import com.zarbosoft.merman.core.document.InvalidDocument;
import com.zarbosoft.merman.core.document.fields.Field;
import com.zarbosoft.merman.core.hid.ButtonEvent;
import com.zarbosoft.merman.core.serialization.Serializer;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.core.syntax.style.Padding;
import com.zarbosoft.merman.core.syntax.style.Style;
import com.zarbosoft.merman.core.visual.Vector;
import com.zarbosoft.merman.core.visual.Visual;
import com.zarbosoft.merman.core.visual.VisualParent;
import com.zarbosoft.merman.core.visual.visuals.VisualAtom;
import com.zarbosoft.merman.core.wall.Attachment;
import com.zarbosoft.merman.core.wall.Brick;
import com.zarbosoft.merman.core.wall.Wall;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Invariants and inner workings - Bricks are only created -- outward from the cornerstone when the
 * selection changes -- when the window expands -- when the model changes, at the visual level where
 * the change occurs
 *
 * <p>The whole document is always loaded. Visuals exist for everything in the window. Bricks
 * eventually exist for everything on screen.
 *
 * <p>The selection may be null within a transaction but always exists afterwards. The initial
 * selection is set by default in context.
 */
public class Context {
  public static Supplier<TSSet> createSet = () -> new TSSet();
  /** Contains the cursor and other marks. Scrolls. */
  public final Group overlay;
  /** Contains the source code. Scrolls. */
  public final Wall wall;

  public final Syntax syntax;
  public final Document document;
  public final Serializer serializer;
  public final TSSet<CursorListener> cursorListeners = new TSSet<>();
  public final TSSet<HoverListener> hoverListeners = new TSSet<>();
  public final WallUsageListener wallUsageListener;
  public final double toPixels;
  public final double fromPixelsToMM;
  public final CursorFactory cursorFactory;
  public final Environment env;
  public final Display display;
  public final PriorityQueue<IterationTask> iterationQueue = new PriorityQueue<>();
  private final Padding pad;
  public boolean animateCoursePlacement;
  public boolean animateDetails;
  public int ellipsizeThreshold;
  public int layBrickBatchSize;
  public double retryExpandFactor;
  public double scrollFactor;
  public double scrollAlotFactor;
  public boolean window;
  public KeyListener mouseButtonEventListener;
  /** Contains banner/details and icons. Scrolls. */
  public Group midground;
  /** Contains source borders. Scrolls. */
  public Group background;

  public double scroll = 0;
  public double peek = 0;
  public IterationLayBricks idleLayBricks = null;
  public double edge;
  public double transverseEdge;
  public Brick hoverBrick;
  public Hoverable hover;
  public HoverIteration hoverIdle;
  public Cursor cursor;
  List<ContextDoubleListener> converseEdgeListeners = new ArrayList<>();
  List<ContextDoubleListener> transverseEdgeListeners = new ArrayList<>();
  double scrollStart;
  double scrollEnd;
  double scrollStartBeddingBefore;
  double scrollStartBeddingAfter;
  int selectToken = 0;
  boolean keyHandlingInProgress = false;
  boolean debugInHover = false;
  private Atom windowAtom;
  private boolean iterationPending = false;
  private Environment.HandleDelay iterationTimer = null;
  private IterationContext iterationContext = null;

  public Context(
      InitialConfig config,
      Syntax syntax,
      Document document,
      Display display,
      Environment env,
      Serializer serializer,
      CursorFactory cursorFactory) {
    this.serializer = serializer;
    this.syntax = syntax;
    this.document = document;
    this.env = env;
    this.animateCoursePlacement = config.animateCoursePlacement;
    this.animateDetails = config.animateDetails;
    this.ellipsizeThreshold = config.ellipsizeThreshold;
    this.layBrickBatchSize = config.layBrickBatchSize;
    this.retryExpandFactor = config.retryExpandFactor;
    this.scrollFactor = config.scrollFactor;
    this.scrollAlotFactor = config.scrollAlotFactor;
    this.cursorFactory = cursorFactory;
    this.display = display;
    display.setBackgroundColor(syntax.background);
    edge = display.edge();
    transverseEdge = display.transverseEdge();
    background = display.group();
    midground = display.group();
    this.wall = new Wall(this);
    this.overlay = display.group();
    this.pad =
        new Padding(
            (config.pad == null ? 0 : config.pad.converseStart) + syntax.pad.converseStart,
            (config.pad == null ? 0 : config.pad.converseEnd) + syntax.pad.converseEnd,
            (config.pad == null ? 0 : config.pad.transverseStart) + syntax.pad.transverseStart,
            (config.pad == null ? 0 : config.pad.transverseEnd) + syntax.pad.transverseEnd);
    display.add(background);
    display.add(midground);
    display.add(wall.visual);
    display.add(overlay);
    toPixels = display.toPixels(syntax.displayUnit);
    fromPixelsToMM = 1.0 / display.toPixels(Syntax.DisplayUnit.MM);
    display.addConverseEdgeListener(
        (oldValue, newValue) -> {
          edge = Math.max(0, newValue - pad.converseStart * toPixels - pad.converseEnd * toPixels);
          for (ContextDoubleListener l : converseEdgeListeners) {
            l.changed(this, oldValue, newValue);
          }
        });
    display.addTransverseEdgeListener(
        ((oldValue, newValue) -> {
          transverseEdge = newValue;
          scrollVisible();
          for (ContextDoubleListener l : transverseEdgeListeners) {
            l.changed(this, oldValue, newValue);
          }
        }));
    display.setKeyEventListener(
        hidEvent -> {
          keyHandlingInProgress = false;
          if (cursor.handleKey(this, hidEvent)) {
            keyHandlingInProgress = true;
            flushIteration(100);
          }
          clearHover();
          return true;
        });
    display.setMouseButtonEventListener(
        hidEvent -> {
          keyHandlingInProgress = false;
          if (mouseButtonEventListener != null
              && mouseButtonEventListener.handleKey(this, hidEvent)) {
            keyHandlingInProgress = true;
            flushIteration(100);
          }
          clearHover();
          return true;
        });
    display.setTypingListener(
        text -> {
          if (keyHandlingInProgress) {
            keyHandlingInProgress = false;
            return false;
          }
          if (text.isEmpty()) return false;
          cursor.handleTyping(this, text);
          flushIteration(100);
          return true;
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
            addIteration(hoverIdle);
          }
          hoverIdle.point =
              vector.add(new Vector(-syntax.pad.converseStart * toPixels, scroll + peek));
        });
    wall.addCornerstoneListener(
        this,
        new Wall.CornerstoneListener() {
          Brick cornerstone = null;
          private final Attachment selectionBrickAttachment =
              new Attachment() {
                @Override
                public void setTransverse(final Context context, final double transverse) {
                  final double oldScrollStart = scrollStart;
                  scrollStart = transverse;
                  scrollEnd += scrollStart - oldScrollStart;
                  scrollVisible();
                }

                @Override
                public void setTransverseSpan(
                    final Context context, final double ascent, final double descent) {
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
    wall.addBeddingListener(
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
    wallUsageListener = config.wallUsageListener;
    if (!config.startWindowed) windowClear();
    else windowToSupertree(document.root);
    wall.setCornerstone(
        this,
        document.root.visual.createOrGetCornerstoneCandidate(this).brick,
        () -> null,
        () -> null);
    triggerIdleLayBricksOutward();
  }

  public static Font getFont(final Context context, Style style) {
    double toPt = context.toPixels * context.fromPixelsToMM * 72.0 / 25.4;
    if (style.font == null) return context.display.font(null, style.fontSize * toPt);
    return context.display.font(style.font, style.fontSize * toPt);
  }

  public void flushIteration(final int limit) {
    final Environment.Time start = env.now();
    // TODO measure pending event backlog, adjust batch size to accomodate
    // by proxy? time since last invocation?
    for (int i = 0; i < limit; ++i) {
      {
        Environment.Time now = start;
        if (i % 100 == 0) {
          now = env.now();
        }
        if (start.plusMillis(500).isBefore(now)) {
          iterationContext = null;
          break;
        }
      }
      final IterationTask top = iterationQueue.poll();
      if (top == null) {
        iterationContext = null;
        break;
      } else {
        if (iterationContext == null) iterationContext = new IterationContext();
        if (top.run(iterationContext)) addIteration(top);
      }
    }
  }

  public void addIteration(final IterationTask task) {
    iterationQueue.add(task);
    if (iterationTimer == null) {
      iterationTimer =
          env.delay(
              50,
              () -> {
                handleTimer();
              });
    }
  }

  private void handleTimer() {
    if (iterationPending) return;
    iterationPending = true;
    try {
      flushIteration(1000);
    } finally {
      iterationPending = false;
      iterationTimer = null;
    }
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
    final double minimum = scrollStart - scrollStartBeddingBefore - pad.transverseStart * toPixels;
    final double maximum = scrollEnd + scrollStartBeddingAfter + pad.transverseEnd * toPixels;

    // Change to scroll required to make it match the start of the window that ends at the max
    final double maxDiff = maximum - transverseEdge - scroll;

    Double newScroll = null;
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
    final double newScroll = scroll + peek;
    double conversePad = pad.converseStart * toPixels;
    wall.visual.setPosition(new Vector(conversePad, -newScroll), animateCoursePlacement);
    midground.setPosition(new Vector(conversePad, -newScroll), animateCoursePlacement);
    background.setPosition(new Vector(conversePad, -newScroll), animateCoursePlacement);
    overlay.setPosition(new Vector(conversePad, -newScroll), animateCoursePlacement);
  }

  public void addConverseEdgeListener(final ContextDoubleListener listener) {
    converseEdgeListeners.add(listener);
  }

  public void removeConverseEdgeListener(final ContextDoubleListener listener) {
    converseEdgeListeners.remove(listener);
  }

  public void addTransverseEdgeListener(final ContextDoubleListener listener) {
    transverseEdgeListeners.add(listener);
  }

  public void removeTransverseEdgeListener(final ContextDoubleListener listener) {
    transverseEdgeListeners.remove(listener);
  }

  public void copy(CopyContext copyContext, final ROList<Atom> atoms) {
    env.clipboardSet(syntax.backType.mime(), serializer.write(copyContext, atoms));
  }

  public void copy(final String string) {
    env.clipboardSetString(string);
  }

  public void uncopy(final String type, UncopyContext uncopyContext, Consumer<ROList<Atom>> cb) {
    env.clipboardGet(
        syntax.backType.mime(),
        data -> {
          if (data == null) {
            cb.accept(ROList.empty);
            return;
          }
          try {
            cb.accept(serializer.loadFromClipboard(syntax, uncopyContext, type, data));
            return;
          } catch (final InvalidDocument ignored) {
          }
          cb.accept(ROList.empty);
        });
  }

  public void uncopyString(Consumer<String> cb) {
    env.clipboardGetString(cb);
  }

  /**
   * Note that the paths for atom fields and atoms are the same. There's no way to get an Atom back if the atom's in an
   * atom field, you'll always get the field.
   * @param path
   * @return an atom or field
   */
  public Object syntaxLocate(final SyntaxPath path) {
    Object at = document.root;
    for (int i = 0; i < path.segments.size(); ++i) {
      String segment = path.segments.get(i);
      if (at instanceof Atom) {
        at = ((Atom) at).syntaxLocateStep(segment);
        if (at == null) throw new InvalidPath(path.segments.sublist(0, i), path.segments);
      } else if (at instanceof Field) {
        at = ((Field) at).syntaxLocateStep(segment);
        if (at == null) throw new InvalidPath(path.segments.sublist(0, i), path.segments);
      } else throw new Assertion();
    }
    return at;
  }

  public void addCursorListener(final CursorListener listener) {
    this.cursorListeners.add(listener);
  }

  public void removeSelectionListener(final CursorListener listener) {
    this.cursorListeners.remove(listener);
  }

  public void addHoverListener(final HoverListener listener) {
    this.hoverListeners.add(listener);
  }

  public void removeHoverListener(final HoverListener listener) {
    this.hoverListeners.remove(listener);
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

  public void triggerIdleLayBricksBeforeStart(final Brick start) {
    if (idleLayBricks == null) {
      idleLayBricks = new IterationLayBricks();
      addIteration(idleLayBricks);
    }
    idleLayBricks.starts.add(start);
  }

  public void windowAdjustMinimalTo(final Atom atom) {
    // Check if the selection is a supertree of the current window
    if (isSubtree(windowAtom, atom)) {
      windowToSupertree(atom);
      return;
    }

    // Otherwise from the selection go towards the root to find the last parent where the selection
    // is still visible
    Atom nextWindow = atom;
    int depth = 0;
    while (true) {
      if (nextWindow == windowAtom) return;
      if (nextWindow.fieldParentRef == null) break;
      depth += nextWindow.type.depthScore();
      if (depth >= ellipsizeThreshold) break;
      nextWindow = nextWindow.fieldParentRef.field.atomParentRef.atom();
    }

    if (isSubtree(windowAtom, nextWindow)) windowToSupertree(nextWindow);
    else windowToNonSupertree(nextWindow);
  }

  public void windowAdjustMinimalTo(final Field field) {
    windowAdjustMinimalTo(field.atomParentRef.atom());
  }

  public void windowClear() {
    window = false;
    windowAtom = document.root;
    document.root.ensureVisual(this, null, 0, 0);
  }

  /**
   * Window tree is subtree or non-overlapping (or equal to) current window tree
   *
   * @param tree
   */
  private void windowToNonSupertree(Atom tree) {
    window = true;
    final Visual oldWindow = windowAtom.visual;
    windowAtom = tree;
    Visual windowVisual = windowAtom.ensureVisual(this, null, 0, 0);
    oldWindow.uproot(this, windowVisual);
  }

  /**
   * New window is the supertree (including equal to) of the current window tree
   *
   * @param supertree
   */
  private void windowToSupertree(Atom supertree) {
    window = true;
    windowAtom = supertree;
    windowAtom.ensureVisual(this, null, 0, 0);
  }

  public boolean isSubtree(Atom subtree, Atom supertree) {
    Atom at = subtree;
    while (true) {
      if (at == supertree) return true;
      if (at.fieldParentRef == null) break;
      at = at.fieldParentRef.field.atomParentRef.atom();
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

  public void triggerIdleLayBricksOutward() {
    if (wall.children.none()) return;
    triggerIdleLayBricksBeforeStart(wall.children.get(0).children.get(0));
    triggerIdleLayBricksAfterEnd(wall.children.last().children.last());
  }

  public void clearCursor() {
    if (cursor == null) return;
    cursor.destroy(this);
    cursor = null;
  }

  public void setCursor(final Cursor cursor) {
    final int localToken = ++selectToken;
    final Cursor oldCursor = this.cursor;
    this.cursor = cursor;
    if (oldCursor != null) {
      oldCursor.destroy(this);
    }
    if (localToken != selectToken) return;
    for (CursorListener l : cursorListeners.copy()) {
      l.cursorChanged(this, cursor);
    }
    triggerIdleLayBricksOutward();
  }

  public Atom windowAtom() {
    return windowAtom;
  }

  public void actionClearWindow() {
    if (!window) return;
    windowClear();
    triggerIdleLayBricksOutward();
  }

  public void actionWindowTowardsRoot() {
    if (!window) return;
    if (windowAtom == document.root) return;
    windowToSupertree(windowAtom.fieldParentRef.field.atomParentRef.atom());
    triggerIdleLayBricksOutward();
  }

  public void actionWindowTowardsCursor() {
    if (!window) return;
    VisualAtom windowNext = null;
    final VisualAtom stop = windowAtom.visual;
    if (cursor.getVisual().parent() == null) return;
    VisualAtom at = cursor.getVisual().parent().atomVisual();
    while (at != null) {
      if (at == stop) break;
      if (at.parent() == null) break;
      windowNext = at;
      at = at.parent().atomVisual();
    }
    if (windowNext == null) return;
    windowToNonSupertree(windowNext.atom);
    triggerIdleLayBricksOutward();
    return;
  }

  public void actionScrollNext() {
    scroll -= scrollFactor * transverseEdge;
    applyScroll();
  }

  public void actionScrollNextAlot() {
    scroll -= scrollAlotFactor * transverseEdge;
    applyScroll();
  }

  public void actionScrollPrevious() {
    scroll += scrollFactor * transverseEdge;
    applyScroll();
  }

  public void actionScrollPreviousAlot() {
    scroll += scrollAlotFactor * transverseEdge;
    applyScroll();
  }

  public void actionScrollReset() {
    scrollVisible();
  }

  public static enum CopyContext {
    NONE,
    RECORD,
    ARRAY
  }

  public static enum UncopyContext {
    NONE,
    RECORD,
    MAYBE_ARRAY
  }

  public static interface ContextDoubleListener {
    void changed(Context context, double oldValue, double newValue);
  }

  @FunctionalInterface
  public interface KeyListener {
    boolean handleKey(Context context, ButtonEvent event);
  }

  @FunctionalInterface
  public interface TextListener {
    void handleText(Context context, String text);
  }

  public interface CursorListener {
    public abstract void cursorChanged(Context context, Cursor cursor);
  }

  public interface WallUsageListener {
    /**
     * Min and max will always have the same axis
     *
     * @param min
     * @param max
     */
    public void usageChanged(Display.UnconvertAxis min, Display.UnconvertAxis max);
  }

  public static class InitialConfig {
    public boolean animateCoursePlacement = false;
    public boolean animateDetails = false;
    public int ellipsizeThreshold = Integer.MAX_VALUE;
    public int layBrickBatchSize = 10;
    /**
     * Split courses are unwrapped when their length passes below the threshold of the last failed
     * unwrap-length check. This is applied to the current length to reduce the number of unwrap
     * checks/reduce wrap/unwrap bouncing.
     */
    public double retryExpandFactor = 1.25;

    public double scrollFactor = 0.1;
    public double scrollAlotFactor = 0.8;
    public boolean startWindowed = false;
    public WallUsageListener wallUsageListener = null;
    /** Additional padding, builds on syntax padding */
    public Padding pad;

    public InitialConfig startWindowed(boolean b) {
      startWindowed = b;
      return this;
    }

    public InitialConfig wallTransverseUsageListener(WallUsageListener l) {
      this.wallUsageListener = l;
      return this;
    }

    public InitialConfig pad(Padding pad) {
      this.pad = pad;
      return this;
    }
  }

  public abstract static class HoverListener {
    public abstract void hoverChanged(Context context, Hoverable hover);
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
            final Brick created = next.createNext(Context.this).brick;
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
            final Brick created = previous.createPrevious(Context.this).brick;
            if (created != null) {
              previous.addBefore(Context.this, created);
              starts.add(created);
            }
          }
        }
      }
      return true;
    }

    @Override
    protected void destroyed() {
      idleLayBricks = null;
    }
  }

  public class HoverIteration extends IterationTask {
    public Vector point = null;
    Context context;
    Brick at;

    public HoverIteration(final Context context) {
      this.context = context;
      at = hoverBrick == null ? context.wall.children.get(0).children.get(0) : hoverBrick;
    }

    @Override
    protected double priority() {
      return 500;
    }

    /** @return nothing left to do, hover changed */
    public ROPair<Boolean, Boolean> inner() {
      if (at == null || at.parent == null) {
        return new ROPair<>(false, false);
      }
      if (point == null) {
        hoverBrick = null;
        return new ROPair<>(false, false);
      }
      if (point.transverse < at.parent.transverseStart && at.parent.index > 0) {
        at = context.wall.children.get(at.parent.index - 1).children.get(0);
      } else if (point.transverse > at.parent.transverseEdge()
          && at.parent.index < wall.children.size() - 1) {
        at = context.wall.children.get(at.parent.index + 1).children.get(0);
      } else {
        while (point.converse < at.getConverse() && at.index > 0) {
          at = at.parent.children.get(at.index - 1);
        }
        while (point.converse >= at.converseEdge() && at.index < at.parent.children.size() - 1) {
          at = at.parent.children.get(at.index + 1);
        }
        final Hoverable old = hover;
        ROPair<Hoverable, Boolean> hover0 = at.hover(context, point);
        boolean hoverChanged;
        if (hover0 == null) {
          hoverChanged = old != null;
          Context.this.hover = null;
        } else {
          hoverChanged = hover0.second;
          Context.this.hover = hover0.first;
        }
        if (hoverChanged) {
          if (old != null) old.clear(context);
        }
        hoverBrick = at;
        hoverIdle = null;
        return new ROPair<>(false, hoverChanged);
      }
      return new ROPair<>(true, false);
    }

    @Override
    public boolean runImplementation(final IterationContext iterationContext) {
      debugInHover = true;
      ROPair<Boolean, Boolean> res = inner();
      debugInHover = false;
      if (res.second) {
        for (HoverListener l : hoverListeners.copy()) {
          l.hoverChanged(context, hover);
        }
      }
      return res.first;
    }

    @Override
    protected void destroyed() {
      hoverIdle = null;
    }
  }
}
