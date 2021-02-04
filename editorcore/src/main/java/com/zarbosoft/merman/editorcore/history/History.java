package com.zarbosoft.merman.editorcore.history;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.SelectionState;
import com.zarbosoft.merman.editor.visual.tags.GlobalTag;
import com.zarbosoft.merman.editor.visual.tags.Tag;
import com.zarbosoft.merman.editor.visual.tags.TagsChange;
import com.zarbosoft.rendaw.common.Common;
import com.zarbosoft.rendaw.common.DeadCode;
import com.zarbosoft.rendaw.common.TSList;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class History {
  private int levelId = 0;
  boolean locked = false;
  private final Deque<Level> past = new ArrayDeque<>();
  private final Deque<Level> future = new ArrayDeque<>();
  private Integer clearLevel;

  public History() {
    addModifiedStateListener(
        modified -> {
          final Tag tag = new GlobalTag("modified");
          if (modified) changeGlobalTags(new TagsChange(add, remove).add(tag));
          else changeGlobalTags(new TagsChange(add, remove).remove(tag));
        });
    addListener(
            new History.Listener() {
              @Override
              public void applied(final Context context, final Change change) {
                if (hoverIdle != null) {
                  hoverIdle.destroy();
                }
              }
            });
  }

  private static class Level extends Change {
    private final int id;
    public final TSList<Change> subchanges = new TSList<>();
    private SelectionState select;

    private Level(final int id) {
      this.id = id;
    }

    @Override
    public boolean merge(final Change other) {
      if (subchanges.isEmpty()) {
        subchanges.add(other);
      } else if (subchanges.last().merge(other)) {
      } else subchanges.add(other);
      return true;
    }

    @Override
    public Change apply(final Context context) {
      final Level out = new Level(id);
      out.select = context.cursor.saveState();
      for (int i = 0; i < subchanges.size(); ++i) {
        Change change = subchanges.getRev(i);
        out.subchanges.add(change.apply(context));
      }
      if (select != null) select.select(context);
      return out;
    }

    public boolean isEmpty() {
      return subchanges.isEmpty();
    }
  }

  public abstract static class Listener {
    public abstract void applied(Context context, Change change);
  }

  @FunctionalInterface
  public interface ModifiedStateListener {
    void changed(boolean modified);
  }

  private final Set<Listener> listeners = new HashSet<>();
  private final Set<ModifiedStateListener> modifiedStateListeners = new HashSet<>();

  private Closeable lock() {
    if (locked) throw new AssertionError("History callback is modifying history.");
    locked = true;
    return new Closeable() {
      @Override
      public void close() throws IOException {
        locked = false;
      }
    };
  }

  private Level applyLevel(final Context context, final Level group) {
    final Level out = (Level) group.apply(context);
    for (final Listener listener : listeners) listener.applied(context, group);
    return out;
  }

  public boolean undo(final Context context) {
    final boolean wasModified = isModified();
    try (Closeable lock = lock()) {
      if (past.isEmpty()) return false;
      if (past.getLast().isEmpty()) past.removeLast();
      if (past.isEmpty()) return false;
      future.addLast(applyLevel(context, past.removeLast()));
      past.addLast(new Level(levelId++));
    } catch (final IOException e) {
    }
    if (isModified() != wasModified) modifiedStateListeners.forEach(l -> l.changed(false));
    return true;
  }

  public boolean redo(final Context context) {
    final boolean wasModified = isModified();
    try (Closeable ignored = lock()) {
      if (future.isEmpty()) return false;
      past.addLast(applyLevel(context, future.removeLast()));
      past.addLast(new Level(levelId++));
    } catch (final IOException ignored) {
    }
    if (wasModified != isModified()) modifiedStateListeners.forEach(l -> l.changed(true));
    return true;
  }

  public void finishChange(final Context context) {
    try (Closeable ignored = lock()) {
      if (!past.isEmpty() && past.getLast().isEmpty()) return;
      past.addLast(new Level(levelId++));
    } catch (final IOException ignored) {
    }
  }

  public void apply(final Context context, final Change change) {
    final boolean wasModified = isModified();
    try (Closeable ignored = lock()) {
      future.clear();
      final Level reverseLevel;
      if (past.isEmpty()) {
        reverseLevel = new Level(levelId++);
        past.addLast(reverseLevel);
      } else reverseLevel = past.getLast();
      if (reverseLevel.select == null && context.cursor != null)
        reverseLevel.select = context.cursor.saveState();
      final Change reverse = change.apply(context);
      reverseLevel.merge(reverse);
      for (final Listener listener : ImmutableList.copyOf(listeners))
        listener.applied(context, change);
    } catch (final IOException e) {
      throw new DeadCode();
    }
    if (!wasModified) modifiedStateListeners.forEach(l -> l.changed(true));
  }

  private Integer fixedTop() {
    final Iterator<Level> iter = past.descendingIterator();
    if (!iter.hasNext()) return null;
    Level next = iter.next();
    if (next.isEmpty())
      if (iter.hasNext()) next = iter.next();
      else return null;
    return next.id;
  }

  public boolean isModified() {
    if (clearLevel == null) {
      if (past.isEmpty()) return false;
      return past.size() > 1 || !past.getLast().isEmpty();
    } else {
      return !clearLevel.equals(fixedTop());
    }
  }

  public void clear() {
    past.clear();
    future.clear();
    clearLevel = null;
  }

  public void clearModified(final Context context) {
    finishChange(context);
    final Integer oldClearLevel = clearLevel;
    clearLevel = fixedTop();
    if (!Objects.equals(clearLevel, oldClearLevel))
      modifiedStateListeners.forEach(l -> l.changed(false));
  }

  public void addListener(final Listener listener) {
    listeners.add(listener);
  }

  public void removeListener(final Listener listener) {
    listeners.remove(listener);
  }

  public void addModifiedStateListener(final ModifiedStateListener listener) {
    modifiedStateListeners.add(listener);
  }
}
