package com.zarbosoft.merman.editor;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.Document;
import com.zarbosoft.merman.document.InvalidDocument;
import com.zarbosoft.merman.document.values.Field;
import com.zarbosoft.merman.editor.banner.Banner;
import com.zarbosoft.merman.editor.details.Details;
import com.zarbosoft.merman.editor.display.Display;
import com.zarbosoft.merman.editor.display.Font;
import com.zarbosoft.merman.editor.display.Group;
import com.zarbosoft.merman.editor.hid.HIDEvent;
import com.zarbosoft.merman.editor.serialization.Serializer;
import com.zarbosoft.merman.editor.visual.Vector;
import com.zarbosoft.merman.editor.visual.Visual;
import com.zarbosoft.merman.editor.visual.VisualParent;
import com.zarbosoft.merman.editor.visual.visuals.VisualAtom;
import com.zarbosoft.merman.editor.wall.Attachment;
import com.zarbosoft.merman.editor.wall.Brick;
import com.zarbosoft.merman.editor.wall.Wall;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.merman.syntax.style.Style;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Context {
  public static Supplier<TSSet> createSet = () -> new TSSet();
  /** Contains the cursor and other marks. Scrolls. */
  public final Group overlay;
  /** Contains the source code. Scrolls. */
  public final Wall foreground;

  public final Display display;
  public final Syntax syntax;
  public final Document document;
  public final Serializer serializer;
  public final I18nEngine i18n;

  // State
  public final TSSet<SelectionListener> cursorListeners = new TSSet<>();
  public final TSSet<HoverListener> hoverListeners = new TSSet<>();
  // Settings
  public final DelayEngine delayEngine;
  public final Style cursorStyle;
  public final Style hoverStyle;
  public final WallUsageListener wallUsageListener;
  private final TSSet<ActionChangeListener> actionChangeListeners = new TSSet<>();
  private final TSList<Action> actions = new TSList<>();
  private final Consumer<IterationTask> addIteration;
  private final Consumer<Integer> flushIteration;
  public boolean animateCoursePlacement;
  public boolean animateDetails;
  public int ellipsizeThreshold;
  public int layBrickBatchSize;
  public double retryExpandFactor;
  public double scrollFactor;
  public double scrollAlotFactor;
  public boolean window;
  public KeyListener keyListener;
  public TextListener textListener;
  public ClipboardEngine clipboardEngine;
  /** Contains banner/details and icons. Doesn't scroll. */
  public Group midground;
  /** Contains source borders. Scrolls. */
  public Group background;

  public Banner banner;
  public Details details;
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

  public Context(
      InitialConfig config,
      Syntax syntax,
      Document document,
      Display display,
      Consumer<IterationTask> addIteration,
      Consumer<Integer> flushIteration,
      DelayEngine delayEngine,
      ClipboardEngine clipboardEngine,
      Serializer serializer,
      I18nEngine i18n) {
    this.serializer = serializer;
    this.i18n = i18n;
    actions.addVar(
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
    this.delayEngine = delayEngine;
    this.cursorStyle = config.cursorStyle.create();
    this.hoverStyle = config.hoverStyle.create();
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
    banner = new Banner(this, config.bannerStyle.create());
    details = new Details(this, config.detailsStyle.create());
    this.clipboardEngine = clipboardEngine;
    display.addConverseEdgeListener(
        (oldValue, newValue) -> {
          edge =
              Math.max(
                  0,
                  newValue - document.syntax.pad.converseStart - document.syntax.pad.converseEnd);
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
    display.addHIDEventListener(
        hidEvent -> {
          keyHandlingInProgress = false;
          if (keyListener != null && keyListener.handleKey(this, hidEvent)) {
            keyHandlingInProgress = true;
            flushIteration(100);
          }
          clearHover();
        });
    display.addTypingListener(
        text -> {
          if (keyHandlingInProgress) {
            keyHandlingInProgress = false;
            return;
          }
          if (text.isEmpty()) return;
          if (textListener != null) {
            textListener.handleText(this, text);
            flushIteration(100);
          }
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
    wallUsageListener = config.wallUsageListener;
    if (!config.startWindowed) windowClear();
    else windowToSupertree(document.root);
    if (config.startSelected) document.root.visual.selectAnyChild(this);
    else {
      foreground.setCornerstone(
          this, document.root.visual.createOrGetFirstBrick(this), () -> null, () -> null);
    }
    triggerIdleLayBricksOutward();
  }

  public static Font getFont(Style style, final Context context) {
    if (style.font == null) return context.display.font(null, style.fontSize);
    return context.display.font(style.font, style.fontSize);
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
    final double minimum = scrollStart - scrollStartBeddingBefore - syntax.pad.transverseStart;
    final double maximum = scrollEnd + scrollStartBeddingAfter + syntax.pad.transverseEnd;

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
    foreground.visual.setPosition(
        new Vector(syntax.pad.converseStart, -newScroll), animateCoursePlacement);
    background.setPosition(
        new Vector(syntax.pad.converseStart, -newScroll), animateCoursePlacement);
    overlay.setPosition(new Vector(syntax.pad.converseStart, -newScroll), animateCoursePlacement);
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

  public void copy(final ROList<Atom> atoms) {
    clipboardEngine.set(serializer.write(atoms));
  }

  public void copy(final String string) {
    clipboardEngine.setString(string);
  }

  public void uncopy(final String type, Consumer<ROList<Atom>> cb) {
    clipboardEngine.get(
        data -> {
          if (data == null) {
            cb.accept(ROList.empty);
            return;
          }
          try {
            cb.accept(serializer.loadFromClipboard(syntax, type, data));
            return;
          } catch (final InvalidDocument ignored) {
          }
          cb.accept(ROList.empty);
        });
  }

  public void uncopyString(Consumer<String> cb) {
    clipboardEngine.getString(cb);
  }

  /**
   * @param path
   * @return an atom or field
   */
  public Object syntaxLocate(final Path path) {
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

  public void windowAdjustMinimalTo(final Field field) {
    // Check if the selection is a supertree of the current window
    if (isSubtree(windowAtom, field.atomParentRef.atom())) {
      windowToSupertree(field.atomParentRef.atom());
      return;
    }

    // Otherwise from the selection go towards the root to find the last parent where the selection
    // is still visible
    Atom nextWindow = field.atomParentRef.atom();
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

  public void triggerIdleLayBricksOutward() {
    triggerIdleLayBricksBeforeStart(foreground.children.get(0).children.get(0));
    triggerIdleLayBricksAfterEnd(foreground.children.last().children.last());
  }

  public void clearSelection() {
    if (cursor == null) return;
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
    triggerIdleLayBricksOutward();
  }

  public Atom windowAtom() {
    return windowAtom;
  }

  public static interface ContextDoubleListener {
    void changed(Context context, double oldValue, double newValue);
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
    public final Style.Config cursorStyle = new Style.Config();
    public final Style.Config hoverStyle = new Style.Config();
    public final Style.Config bannerStyle = new Style.Config();
    public final Style.Config detailsStyle = new Style.Config();
    public boolean animateCoursePlacement = false;
    public boolean animateDetails = false;
    public int ellipsizeThreshold = Integer.MAX_VALUE;
    public int layBrickBatchSize = 10;
    public double retryExpandFactor = 1.25;
    public double scrollFactor = 0.1;
    public double scrollAlotFactor = 0.8;
    public boolean startSelected = true;
    public boolean startWindowed = false;
    public WallUsageListener wallUsageListener = null;

    public InitialConfig startSelected(boolean b) {
      startSelected = b;
      return this;
    }

    public InitialConfig startWindowed(boolean b) {
      startWindowed = b;
      return this;
    }

    public InitialConfig wallTransverseUsageListener(WallUsageListener l) {
      this.wallUsageListener = l;
      return this;
    }

    public InitialConfig hoverStyle(Consumer<Style.Config> c) {
      c.accept(this.hoverStyle);
      return this;
    }

    public InitialConfig cursorStyle(Consumer<Style.Config> c) {
      c.accept(this.cursorStyle);
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
      at = hoverBrick == null ? context.foreground.children.get(0).children.get(0) : hoverBrick;
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
        at = context.foreground.children.get(at.parent.index - 1).children.get(0);
      } else if (point.transverse > at.parent.transverseEdge()
          && at.parent.index < foreground.children.size() - 1) {
        at = context.foreground.children.get(at.parent.index + 1).children.get(0);
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

  private class ActionWindowClear implements Action {
    public String id() {
      return "window_clear";
    }

    @Override
    public void run(final Context context) {
      if (!window) return;
      windowClear();
      triggerIdleLayBricksOutward();
    }
  }

  private class ActionWindowTowardsRoot implements Action {
    public ActionWindowTowardsRoot() {}

    public String id() {
      return "window_up";
    }

    @Override
    public void run(final Context context) {
      if (!window) return;
      if (windowAtom == document.root) return;
      windowToSupertree(windowAtom.valueParentRef.value.atomParentRef.atom());
      triggerIdleLayBricksOutward();
    }
  }

  private class ActionWindowTowardsCursor implements Action {
    public String id() {
      return "window_down";
    }

    @Override
    public void run(final Context context) {
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
      context.windowToNonSupertree(windowNext.atom);
      triggerIdleLayBricksOutward();
      return;
    }
  }

  private class ActionScrollNext implements Action {
    public String id() {
      return "scroll_previous";
    }

    @Override
    public void run(final Context context) {
      scroll -= scrollFactor * transverseEdge;
      applyScroll();
    }
  }

  private class ActionScrollNextAlot implements Action {
    public String id() {
      return "scroll_previous_alot";
    }

    @Override
    public void run(final Context context) {
      scroll -= scrollAlotFactor * transverseEdge;
      applyScroll();
    }
  }

  private class ActionScrollPrevious implements Action {
    public String id() {
      return "scroll_next";
    }

    @Override
    public void run(final Context context) {
      scroll += scrollFactor * transverseEdge;
      applyScroll();
    }
  }

  private class ActionScrollPreviousAlot implements Action {
    public String id() {
      return "scroll_next_alot";
    }

    @Override
    public void run(final Context context) {
      scroll += scrollAlotFactor * transverseEdge;
      applyScroll();
    }
  }

  private class ActionScrollReset implements Action {
    public String id() {
      return "scroll_reset";
    }

    @Override
    public void run(final Context context) {
      scrollVisible();
    }
  }
}
