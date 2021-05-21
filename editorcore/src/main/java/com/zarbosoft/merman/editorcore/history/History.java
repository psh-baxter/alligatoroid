package com.zarbosoft.merman.editorcore.history;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.Environment;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.DeadCode;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSSet;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Consumer;

public class History {
  /*
  Implementation:
  Top level in past is the "in progress" change, may not exist
  To finish a change means to push a new empty change onto past
  Undo pops the last change in past, applies it, adds the reverse to end of future
  Redo pops the last change in future, applies it, adds the reverse to the end of past
   */
  private final Deque<ChangeLevel> past = new ArrayDeque<>();
  private final Deque<ChangeLevel> future = new ArrayDeque<>();
  private final TSSet<ModifiedStateListener> modifiedStateListeners = new TSSet<>();
  boolean locked = false;
  private Object lastChangeUnique;
  private Environment.Time lastChangeTime;
  private ChangeLevel clearLevel;

  public History() {}

  private Closeable lock() {
    if (locked) throw new Assertion("History callback is modifying history.");
    locked = true;
    return new Closeable() {
      @Override
      public void close() throws IOException {
        locked = false;
      }
    };
  }

  private ChangeLevel applyLevel(final Context context, final ChangeLevel group) {
    final ChangeLevel out = (ChangeLevel) group.apply(context);
    return out;
  }

  public boolean undo(final Context context) {
    final boolean wasModified = isModified();
    try (Closeable ignored = lock()) {
      if (past.isEmpty()) return false;
      if (past.getLast().isEmpty()) past.removeLast();
      if (past.isEmpty()) return false;
      future.addLast(applyLevel(context, past.removeLast()));
      past.addLast(new ChangeLevel());
    } catch (final IOException ignored) {
    }
    if (isModified() != wasModified) {
      for (ModifiedStateListener l : modifiedStateListeners) {
        l.changed(false);
      }
    }
    return true;
  }

  public boolean redo(final Context context) {
    final boolean wasModified = isModified();
    try (Closeable ignored = lock()) {
      if (future.isEmpty()) return false;
      past.addLast(applyLevel(context, future.removeLast()));
      past.addLast(new ChangeLevel());
    } catch (final IOException ignored) {
    }
    if (wasModified != isModified()) {
      for (ModifiedStateListener l : modifiedStateListeners) {
        l.changed(true);
      }
    }
    return true;
  }

  public void finishChange() {
    try (Closeable ignored = lock()) {
      finishChangeInner();
    } catch (final IOException ignored) {
    }
  }

  private void finishChangeInner() {
    if (!past.isEmpty() && past.getLast().isEmpty()) return;
    past.addLast(new ChangeLevel());
  }

  public void record(Context context, ROPair unique, Consumer<Recorder> c) {
    final boolean wasModified = isModified();
    try (Closeable ignored = lock()) {
      /// Demarcate change level based on conditions
      Environment.Time now = context.env.now();
      if (lastChangeUnique == null
          || unique == null
          || !unique.equals(lastChangeUnique)
          || lastChangeTime == null
          || lastChangeTime.plusMillis(2000).isBefore(now)) {
        lastChangeUnique = null;
        finishChangeInner();
      }
      lastChangeTime = now;
      lastChangeUnique = unique;

      future.clear();

      /// Record and apply changes as they're recorded
      ChangeLevel partialUndo = new ChangeLevel();
      partialUndo.select = past.getLast().select;
      try {
        c.accept(new Recorder(partialUndo));
      } catch (RuntimeException e) {
        /// Roll back partial changes
        partialUndo.apply(context);
        throw e;
      }
      past.getLast().subchanges.addAll(partialUndo.subchanges);
      if (past.getLast().select == null) past.getLast().select = partialUndo.select;
    } catch (final IOException e) {
      throw new DeadCode();
    }
    if (!wasModified) {
      for (ModifiedStateListener l : modifiedStateListeners) {
        l.changed(true);
      }
    }
  }

  /**
   * The last not in-progress change
   *
   * @return
   */
  private ChangeLevel fixedTop() {
    final Iterator<ChangeLevel> iter = past.descendingIterator();
    if (!iter.hasNext()) return null;
    ChangeLevel next = iter.next();
    if (next.isEmpty())
      if (iter.hasNext()) next = iter.next();
      else return null;
    return next;
  }

  public boolean isModified() {
    System.out.format("is mod?\n");
    System.out.format("clear lev = %s, top = %s\n", clearLevel, fixedTop());
    if (clearLevel!= fixedTop()) return true;
    System.out.format("past empty %s\n", past.isEmpty());
    if (!past.isEmpty()) System.out.format("past last empty %s\n", past.getLast().isEmpty());
    if (!past.isEmpty() && !past.getLast().isEmpty()) return true;
    System.out.format("-> not mod\n");
    return false;
  }

  public void clear() {
    past.clear();
    future.clear();
    clearLevel = null;
  }

  public void clearModified() {
    finishChange();
    final ChangeLevel oldClearLevel = clearLevel;
    clearLevel = fixedTop();
    if (clearLevel != oldClearLevel) {
      for (ModifiedStateListener l : modifiedStateListeners) {
        l.changed(false);
      }
    }
  }

  public void addModifiedStateListener(final ModifiedStateListener listener) {
    modifiedStateListeners.add(listener);
  }

  public static class Recorder {
    private final ChangeLevel partial;

    private Recorder(ChangeLevel partial) {
      this.partial = partial;
    }

    public void apply(final Context context, final Change change) {
      if (partial.select == null && context.cursor != null)
        partial.select = context.cursor.saveState();
      final Change reverse = change.apply(context);
      partial.merge(reverse);
    }
  }
}
