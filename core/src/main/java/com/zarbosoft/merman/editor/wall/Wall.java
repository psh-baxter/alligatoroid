package com.zarbosoft.merman.editor.wall;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.IterationContext;
import com.zarbosoft.merman.editor.IterationTask;
import com.zarbosoft.merman.editor.display.Group;
import com.zarbosoft.merman.editor.visual.visuals.VisualFrontPrimitive;
import com.zarbosoft.rendaw.common.ChainComparator;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSSet;

import java.util.PriorityQueue;
import java.util.function.Supplier;

public class Wall {
  public final Group visual;
  public TSList<Course> children = new TSList<>();
  private IterationAdjustTask idleAdjust;
  private IterationCompactTask idleCompact;
  private IterationExpandTask idleExpand;
  /** Cornerstone may be null. Cornerstone course is only null in transition. */
  public Brick cornerstone;

  public Course cornerstoneCourse;
  TSSet<Bedding> bedding = new TSSet<>();
  public int beddingBefore = 0;
  int beddingAfter = 0;
  TSSet<BeddingListener> beddingListeners = new TSSet<>();
  TSSet<CornerstoneListener> cornerstoneListeners = new TSSet<>();

  public abstract static class CornerstoneListener {
    public abstract void cornerstoneChanged(Context context, Brick brick);
  }

  public abstract static class BeddingListener {

    public abstract void beddingChanged(Context context, int beddingBefore, int beddingAfter);
  }

  public Wall(final Context context) {
    visual = context.display.group();
    context.addConverseEdgeListener(
            new Context.ContextIntListener() {
              int modOldValue = Integer.MAX_VALUE;
              @Override
              public void changed(Context context1, int oldValue, int newValue) {
                if (newValue < modOldValue) {
                  Wall.this.idleCompact(context1);
                  modOldValue = newValue;
                } else if (newValue > modOldValue * context.retryExpandFactor) {
                  Wall.this.idleExpand(context1);
                  modOldValue = newValue;
                }
              }
            });
  }

  public void clear(final Context context) {
    while (!children.isEmpty()) children.last().destroy(context);
    if (idleCompact != null) idleCompact.destroy();
    if (idleExpand != null) idleExpand.destroy();
    if (idleAdjust != null) idleAdjust.destroy();
  }

  private void renumber(final int at) {
    for (int index = at; index < children.size(); ++index) {
      children.get(index).index = index;
    }
  }

  private void getIdle(final Context context) {
    if (idleAdjust == null) {
      idleAdjust = new IterationAdjustTask(context);
      context.addIteration(idleAdjust);
    }
  }

  void add(final Context context, final int at, final ROList<Course> courses) {
    children.insertAll(at, courses);
    for (Course course : courses) {
      course.parent = this;
    }
    renumber(at);
    for (Course course : courses) {
      visual.add(course.visual);
    }
    getIdle(context);
    if (children.size() > 1) {
      if (idleAdjust.backward >= at) idleAdjust.backward += 1;
      if (idleAdjust.forward >= at && idleAdjust.forward < Integer.MAX_VALUE)
        idleAdjust.forward += 1;
      idleAdjust.at(at);
    }
  }

  void remove(final Context context, final int at) {
    if (cornerstoneCourse != null && cornerstoneCourse.index == at) {
      cornerstoneCourse = null;
    }
    children.remove(at);
    visual.remove(at);
    if (at < children.size()) {
      renumber(at);
      getIdle(context);
      if (at < idleAdjust.backward) idleAdjust.backward -= 1;
      if (at < idleAdjust.forward && idleAdjust.forward < Integer.MAX_VALUE)
        idleAdjust.forward -= 1;
      idleAdjust.at(at);
    }
  }

  public void idleCompact(final Context context) {
    if (idleCompact == null) {
      idleCompact = new IterationCompactTask(context);
      context.addIteration(idleCompact);
    }
    idleCompact.at = 0;
  }

  public void idleExpand(final Context context) {
    if (idleExpand == null) {
      idleExpand = new IterationExpandTask(context);
      context.addIteration(idleExpand);
    }
    idleExpand.at = 0;
  }

  public TSSet<BeddingListener> getBeddingListeners() {
    return beddingListeners;
  }

  public void addBeddingListener(final Context context, final BeddingListener listener) {
    beddingListeners.add(listener);
    listener.beddingChanged(context, beddingBefore, beddingAfter);
  }

  public void removeBeddingListener(final BeddingListener listener) {
    beddingListeners.remove(listener);
  }

  public void addCornerstoneListener(final Context context, final CornerstoneListener listener) {
    cornerstoneListeners.add(listener);
    if (cornerstone != null) listener.cornerstoneChanged(context, cornerstone);
  }

  public void removeCornerstoneListener(final CornerstoneListener listener) {
    cornerstoneListeners.remove(listener);
  }

  public void addBedding(final Context context, final Bedding bedding) {
    this.bedding.add(bedding);
    beddingChanged(context);
  }

  public void removeBedding(final Context context, final Bedding bedding) {
    this.bedding.remove(bedding);
    beddingChanged(context);
  }

  private void beddingChanged(final Context context) {
    beddingBefore = 0;
    beddingAfter = 0;
    for (Bedding b : bedding) {
      beddingBefore += b.before;
      beddingAfter += b.after;
    }
    for (BeddingListener l : beddingListeners.copy()) {
      l.beddingChanged(context, beddingBefore, beddingAfter);
    }
    if (cornerstoneCourse != null) adjust(context, cornerstoneCourse.index);
  }

  public void setCornerstone(
      final Context context,
      final Brick cornerstone,
      final Supplier<Brick> findPrevious,
      final Supplier<Brick> findNext) {
    this.cornerstone = cornerstone;
    if (cornerstone == null) {
      this.cornerstoneCourse = null;
    } else {
      if (cornerstone.parent == null) {
        Brick found = findPrevious.get();
        if (found != null) {
          found.addAfter(context, cornerstone);
        } else {
          found = findNext.get();
          if (found != null) {
            found.addBefore(context, cornerstone);
          } else {
            clear(context);
            final Course course = new Course(context, 0);
            add(context, 0, TSList.of(course));
            course.add(context, 0, TSList.of(cornerstone));
          }
        }
      }
      this.cornerstoneCourse = cornerstone.parent;
      if (beddingBefore > 0 || beddingAfter > 0) adjust(context, cornerstoneCourse.index);
      for (CornerstoneListener l : cornerstoneListeners.copy()) {
        l.cornerstoneChanged(context, cornerstone);
      }
      context.triggerIdleLayBricksBeforeStart(cornerstone);
      context.triggerIdleLayBricksAfterEnd(cornerstone);
    }
  }

  class IterationCompactTask extends IterationTask {
    private final Context context;
    Course.IterationCompactTask compactTask;
    int at = 0;

    IterationCompactTask(final Context context) {
      this.context = context;
    }

    @Override
    protected double priority() {
      return P.wallCompact;
    }

    @Override
    public boolean runImplementation(final IterationContext iterationContext) {
      if (at >= children.size()) {
        return false;
      }
      if (compactTask == null) {
        compactTask = children.get(at).new IterationCompactTask(context);
      }
      if (!compactTask.run(iterationContext)) {
        compactTask = null;
        at++;
      }
      return true;
    }

    @Override
    protected void destroyed() {
      idleCompact = null;
    }
  }

  class IterationExpandTask extends IterationTask {
    private final Context context;
    Course.IterationExpandTask expandTask;
    int at = 0;

    IterationExpandTask(final Context context) {
      this.context = context;
    }

    @Override
    protected double priority() {
      return P.wallExpand;
    }

    @Override
    public boolean runImplementation(final IterationContext iterationContext) {
      if (at >= children.size()) {
        return false;
      }
      if (expandTask == null) {
        expandTask = children.get(at).new IterationExpandTask(context);
        at++;
      }
      final int oldCourseCount = children.size();
      if (!expandTask.run(iterationContext)) {
        expandTask = null;
      }
      if (oldCourseCount != children.size()) at = Math.max(0, at - 2);
      return true;
    }

    @Override
    protected void destroyed() {
      idleExpand = null;
    }
  }

  class IterationAdjustTask extends IterationTask {
    private final Context context;
    int forward = Integer.MAX_VALUE;
    int backward = Integer.MIN_VALUE;

    IterationAdjustTask(final Context context) {
      this.context = context;
    }

    @Override
    protected double priority() {
      return P.wallAdjust;
    }

    @Override
    public boolean runImplementation(final IterationContext iterationContext) {
      boolean modified = false;
      if (cornerstoneCourse.index <= backward || cornerstoneCourse.index >= forward) {
        backward = cornerstoneCourse.index - 1;
        forward = cornerstoneCourse.index + 1;
      }
      if (backward >= 0) {
        // Always < children size because of cornerstone
        final Course child = children.get(backward);
        final Course preceding = children.get(backward + 1);
        int transverse = preceding.transverseStart - child.transverseSpan();
        if (preceding == cornerstoneCourse) transverse -= beddingBefore;
        child.setTransverse(context, transverse);
        backward -= 1;
        modified = true;
      }
      if (forward < children.size()) {
        // Always > 0 because of cornerstone
        int transverse = children.get(forward - 1).transverseEdge();
        if (forward - 1 == cornerstoneCourse.index) transverse += beddingAfter;
        children.get(forward).setTransverse(context, transverse);
        forward += 1;
        modified = true;
      }
      return modified;
    }

    @Override
    protected void destroyed() {
      idleAdjust = null;
    }

    public void at(final int at) {
      if (cornerstoneCourse == null) return;
      if (at <= cornerstoneCourse.index && at > backward)
        backward = Math.min(cornerstoneCourse.index - 1, at);
      if (at >= cornerstoneCourse.index && at < forward)
        forward = Math.max(cornerstoneCourse.index + 1, at);
    }
  }

  void adjust(final Context context, final int at) {
    getIdle(context);
    idleAdjust.at(at);
  }
}
