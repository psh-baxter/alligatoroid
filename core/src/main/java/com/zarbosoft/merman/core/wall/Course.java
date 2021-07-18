package com.zarbosoft.merman.core.wall;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.IterationContext;
import com.zarbosoft.merman.core.IterationTask;
import com.zarbosoft.merman.core.display.Group;
import com.zarbosoft.merman.core.visual.VisualLeaf;
import com.zarbosoft.merman.core.visual.alignment.Alignment;
import com.zarbosoft.merman.core.visual.visuals.VisualAtom;
import com.zarbosoft.merman.core.visual.visuals.VisualFieldPrimitive;
import com.zarbosoft.rendaw.common.ChainComparator;
import com.zarbosoft.rendaw.common.EnumerateIterable;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSSet;

import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

import static com.zarbosoft.rendaw.common.Common.isOrdered;

public class Course {
  private static final Comparator<VisualAtom> compactComparator =
      new ChainComparator<VisualAtom>()
          .greaterFirst(VisualAtom::spacePriority)
          .lesserFirst(a -> a.depthScore)
          .build();
  private static final Comparator<VisualAtom> expandComparator = compactComparator.reversed();
  final Group visual;
  public int index;
  public Wall parent;
  public double transverseStart;
  public double ascent = 0;
  public double descent = 0;
  public TSList<Brick> children = new TSList<>();
  public Alignment alignment;
  public Brick alignmentBrick;
  double lastExpandCheckConverse = 0;
  private IterationPlaceTask idlePlace;
  private IterationCompactTask idleCompact;
  private IterationExpandTask idleExpand;

  Course(final Context context, final double transverseStart) {
    visual = context.display.group();
    this.transverseStart = transverseStart;
    visual.setTransverse(transverseStart, false);
  }

  /**
   * When adjusting bricks in a course, calculate the state after the current brick and return this
   * brick's new converse
   *
   * @param calcContext
   * @param converse
   * @param brick
   * @param properties
   * @return converse, minConverse (without alignment)
   */
  private static ROPair<Double, Double> calculateNextBrickAdvance(
      CalculateCourseConverseContext calcContext, Brick brick) {
    double out = calcContext.converse;
    double out1 = calcContext.preAlignConverse;
    if (calcContext.alignment == null && brick.alignment != null) {
      calcContext.alignment = brick.alignment;
      calcContext.alignedBrick = brick;
      if (brick.alignment.converse > out) out = brick.alignment.converse;
    }
    calcContext.preAlignConverse += brick.converseSpan;
    calcContext.converse = out + brick.converseSpan;
    return new ROPair<>(out, out1);
  }

  public double transverseEdge() {
    return transverseStart + ascent + descent;
  }

  void setTransverse(final Context context, final double transverse) {
    transverseStart = transverse;
    visual.setTransverse(transverseStart, context.animateCoursePlacement);
    for (Brick child : children.mut()) {
      for (Attachment a : child.attachments.mut()) {
        a.setTransverse(context, transverseStart);
      }
    }
  }

  void changed(final Context context, final int at) {
    final Brick brick = children.get(at);
    if (at > 0 && brick.isSplit()) {
      breakCourse(context, at);
      return;
    } else if (at == 0 && !brick.isSplit() && this.index > 0) {
      joinPreviousCourse(context);
      return;
    }
    getIdlePlace(context);
    idlePlace.changed.add(brick);
  }

  private void joinPreviousCourse(final Context context) {
    for (Brick child : children) {
      if (alignmentBrick == child) {
        alignment.removeBrick(context, alignmentBrick);
        alignment = null;
        alignmentBrick = null;
      }
    }
    visual.clear();
    final boolean resetCornerstone = parent.cornerstoneCourse == this;
    final Course previous = parent.children.get(this.index - 1);
    previous.add(context, previous.children.size(), children);
    destroyInner(context);
    if (resetCornerstone) parent.setCornerstone(context, parent.cornerstone, null, null);
  }

  Course breakCourse(final Context context, final int index) {
    if (index == 0) throw new AssertionError("Breaking course at 0.");
    boolean resetCornerstone = false;
    final Course next = new Course(context, transverseStart + transverseSpan());
    parent.add(context, this.index + 1, TSList.of(next));
    if (index < children.size()) {
      final TSList<Brick> transplantRemove = children.sublist(index, children.size());
      ROList<Brick> transportAdd = transplantRemove.mut();
      getIdlePlace(context);
      for (final Brick brick : transportAdd) {
        if (alignmentBrick == brick) {
          alignment.removeBrick(context,alignmentBrick);
          alignment = null;
          alignmentBrick = null;
        }
        idlePlace.removeMaxAscent = Math.max(idlePlace.removeMaxAscent, brick.ascent());
        idlePlace.removeMaxDescent = Math.max(idlePlace.removeMaxDescent, brick.descent());
        idlePlace.changed.remove(brick);
        if (brick == parent.cornerstone) resetCornerstone = true;
      }
      transplantRemove.clear();
      visual.remove(index, visual.size() - index);
      next.add(context, 0, transportAdd);
    }
    if (resetCornerstone) parent.setCornerstone(context, parent.cornerstone, null, null);
    return next;
  }

  void add(final Context context, final int at, final ROList<Brick> bricks) {
    if (bricks.size() == 0) throw new AssertionError("Adding no bricks");
    children.insertAll(at, bricks);
    for (int i = 0; i < bricks.size(); ++i) {
      final Brick brick = bricks.get(i);
      brick.setParent(this, at + i);
    }
    renumber(at + bricks.size());
    for (EnumerateIterable.El<Brick> brick : new EnumerateIterable<>(bricks)) {
      visual.add(at + brick.index, brick.value.getDisplayNode());
    }
    for (Brick brick : bricks) {
      brick.allocateTransverse(context, ascent, descent);
      for (Attachment a : brick.getAttachments().copy()) {
        a.setTransverse(context, transverseStart);
        a.setTransverseSpan(context, ascent, descent);
      }
    }
    getIdlePlace(context);
    idlePlace.changed.addAll(bricks);
  }

  void removeFromSystem(final Context context, final int at) {
    final Brick brick = children.get(at);
    if (brick == alignmentBrick) {
      alignment.removeBrick(context, brick);
      alignment = null;
      alignmentBrick = null;
    }
    if (parent.cornerstone == brick) parent.cornerstone = null;
    if (context.hoverBrick == brick) {
      context.clearHover();
    }
    brick.setParent(null, 0);
    children.remove(at);
    if (index - 1 >= 0) parent.children.get(index - 1).getIdleExpand(context);
    if (index + 1 < parent.children.size()) parent.children.get(index + 1).getIdleExpand(context);
    if (children.isEmpty()) {
      destroyInner(context);
    } else {
      if (at == 0 && this.index > 0) {
        joinPreviousCourse(context);
      } else {
        visual.remove(at);
        renumber(at);
        getIdlePlace(context);
        idlePlace.removeMaxAscent = Math.max(idlePlace.removeMaxAscent, brick.ascent());
        idlePlace.removeMaxDescent = Math.max(idlePlace.removeMaxDescent, brick.descent());
        idlePlace.changed.remove(brick);
      }
    }
  }

  private void destroyInner(final Context context) {
    if (idlePlace != null) idlePlace.destroy();
    if (idleCompact != null) idleCompact.destroy();
    if (idleExpand != null) idleExpand.destroy();
    parent.remove(context, index);
  }

  void destroy(final Context context) {
    while (!children.isEmpty()) children.last().destroy(context);
  }

  private void renumber(final int at) {
    for (int index = at; index < children.size(); ++index) {
      children.get(index).index = index;
    }
  }

  private void getIdlePlace(final Context context) {
    if (idlePlace == null) {
      idlePlace = new IterationPlaceTask(context);
      context.addIteration(idlePlace);
    }
  }

  private void getIdleCompact(final Context context) {
    if (idleCompact == null) {
      idleCompact = new IterationCompactTask(context);
      context.addIteration(idleCompact);
    }
  }

  private void getIdleExpand(final Context context) {
    if (idleExpand == null) {
      idleExpand = new IterationExpandTask(context);
      context.addIteration(idleExpand);
    }
  }

  public double transverseSpan() {
    return ascent + descent;
  }

  /** State of layout on a single course */
  private static class CalculateCourseConverseContext {
    /** Current converse offset */
    public double converse;
    /** The alignment for this course, if encountered */
    public Alignment alignment;
    /** The brick from which the alignment hails */
    public Brick alignedBrick;

    public double preAlignConverse;
  }

  class IterationPlaceTask extends IterationTask {
    private final Context context;
    TSSet<Brick> changed = new TSSet<>();
    double removeMaxAscent = 0;
    double removeMaxDescent = 0;

    public IterationPlaceTask(final Context context) {
      this.context = context;
    }

    @Override
    protected double priority() {
      return P.coursePlace;
    }

    @Override
    public boolean runImplementation(final IterationContext iterationContext) {
      /// Update transverse space
      boolean newAscent = false, newDescent = false;
      for (final Brick brick : changed) {
        if (brick.ascent() > ascent) {
          ascent = brick.ascent();
          newAscent = true;
        }
        if (brick.descent() > descent) {
          descent = brick.descent();
          newDescent = true;
        }
      }
      if (!(newAscent && newDescent) && removeMaxAscent == ascent && removeMaxDescent == descent) {
        ascent = 0;
        descent = 0;
        {
          for (final Brick brick : children) {
            ascent = Math.max(ascent, brick.ascent());
            descent = Math.max(descent, brick.descent());
          }
        }
        newAscent = true;
        newDescent = true;
      }
      if (newAscent || newDescent) {
        for (Brick b : children) {
          b.allocateTransverse(context, ascent, descent);
          for (Attachment attachment : b.getAttachments()) {
            attachment.setTransverseSpan(context, ascent, descent);
          }
        }
      } else
        for (Brick b : changed) {
          b.allocateTransverse(context, ascent, descent);
          for (Attachment a : b.getAttachments()) {
            a.setTransverseSpan(context, ascent, descent);
          }
        }

      /// Do converse placement
      CalculateCourseConverseContext calcContext = new CalculateCourseConverseContext();
      for (int index = 0; index < children.size(); ++index) {
        final Brick brick = children.get(index);
        if (calcContext.alignment == null && brick.alignment != null) {
          if (alignment != null) alignment.removeBrick(context, alignmentBrick);
          alignment = brick.alignment;
          alignmentBrick = brick;
          alignment.addBrick(context, alignmentBrick);
          alignment.feedback(context, calcContext.converse);
        }
        ROPair<Double, Double> brickPlacement = calculateNextBrickAdvance(calcContext, brick);
        brick.setConverse(context, brickPlacement.second, brickPlacement.first);
        for (final Attachment attachment : brick.getAttachments())
          attachment.setConverse(context, brickPlacement.first);
      }
      if (calcContext.alignment == null && alignment != null) {
        alignment.removeBrick(context, alignmentBrick);
      }
      if (calcContext.converse > context.edge) getIdleCompact(context);
      if (calcContext.converse * context.retryExpandFactor < lastExpandCheckConverse)
        getIdleExpand(context);
      if (calcContext.converse > lastExpandCheckConverse)
        lastExpandCheckConverse = calcContext.converse;

      // Propagate changes up
      if (newAscent || newDescent) parent.adjust(context, index);

      return false;
    }

    @Override
    protected void destroyed() {
      if (idlePlace == this) idlePlace = null;
    }
  }

  class IterationCompactTask extends IterationTask {
    private final Context context;
    private final Set<VisualAtom> skip = new HashSet<>();
    private final Set<VisualFieldPrimitive> skipPrimitives = new HashSet<>();

    IterationCompactTask(final Context context) {
      this.context = context;
    }

    @Override
    protected double priority() {
      return P.courseCompact;
    }

    @Override
    public boolean runImplementation(final IterationContext iterationContext) {
      // Find higest priority brick in this course
      final PriorityQueue<VisualAtom> priorities = new PriorityQueue<>(11, compactComparator);
      VisualFieldPrimitive lastPrimitive = null;
      double converse = 0;
      for (int index = 0; index < children.size(); ++index) {
        final Brick brick = children.get(index);
        final VisualLeaf visual = brick.getVisual();
        final VisualAtom atomVisual = visual.parent().atomVisual();
        if (visual instanceof VisualFieldPrimitive && !skipPrimitives.contains(visual))
          lastPrimitive = (VisualFieldPrimitive) visual;
        if ((!visual.atomVisual().compact && !skip.contains(atomVisual)))
          priorities.add(atomVisual);
        converse = brick.converseEdge();
        if (!priorities.isEmpty() && converse > context.edge) break;
      }
      if (converse <= context.edge) {
        return false;
      }
      if (priorities.isEmpty()) {
        if (lastPrimitive != null) {
          lastPrimitive.primitiveReflow(context);
          skipPrimitives.add(lastPrimitive);
          return true;
        } else {
          return false;
        }
      } else {
        final VisualAtom top = priorities.poll();
        top.compact(context);
        skip.add(top);
        return true;
      }
    }

    @Override
    protected void destroyed() {
      idleCompact = null;
    }
  }

  class IterationExpandTask extends IterationTask {
    private final Context context;

    IterationExpandTask(final Context context) {
      this.context = context;
    }

    @Override
    protected double priority() {
      return P.courseExpand;
    }

    @Override
    public boolean runImplementation(final IterationContext iterationContext) {
      return expand(context, iterationContext);
    }

    private boolean expand(final Context context, final IterationContext iterationContext) {
      // Try to unwrap any soft-wrapped primitives first
      for (Brick child : children) {
        VisualLeaf visual = child.getVisual();
        if (!(visual instanceof VisualFieldPrimitive)
            || !((VisualFieldPrimitive) visual).softWrapped()) continue;
        int oldLines = ((VisualFieldPrimitive) visual).hardLineCount;
        ((VisualFieldPrimitive) visual).primitiveReflow(context);
        int newLines = ((VisualFieldPrimitive) visual).hardLineCount;
        return oldLines != newLines;
      }

      // Unwrap nodes based on priority
      final PriorityQueue<VisualAtom> priorities = new PriorityQueue<>(11, expandComparator);

      // Find next atom that can be expanded
      for (int index = 0; index < children.size(); ++index) {
        final Brick brick = children.get(index);
        final VisualLeaf visual = brick.getVisual();
        if (visual.atomVisual().compact) priorities.add(visual.parent().atomVisual());
      }
      if (priorities.isEmpty()) return false;
      final VisualAtom top = priorities.poll();

      // Check that it's the most precedent in all courses it has bricks (preserve: all higher
      // precedent atoms are
      // expanded)
      {
        TSList<Brick> bricks = new TSList<>();
        top.getLeafBricks(context, bricks);
        for (Brick brick : bricks) {
          if (brick.parent.index == index) continue;
          for (Brick otherCourseBrick : parent.children.get(brick.parent.index).children) {
            VisualAtom otherAtom = otherCourseBrick.getVisual().atomVisual();
            if (otherAtom == top) continue;
            if (!otherAtom.compact) continue;
            if (isOrdered(expandComparator, top, otherAtom)) return false;
          }
        }
      }

      // Check that all parents are either expanded or have lower expand priority
      {
        VisualAtom parentAtom = top;
        while (parentAtom.parent() != null) {
          parentAtom = parentAtom.parent().atomVisual();
          if (isOrdered(expandComparator, parentAtom, top) && parentAtom.compact) return false;
        }
      }

      // Check that all children are either expanded or have lower expand priority
      {
        final Brick first = top.getFirstBrick(context);
        final Brick last = top.getLastBrick(context);
        for (int courseI = first.parent.index; courseI <= last.parent.index; ++courseI) {
          Course course = parent.children.get(courseI);
          for (int brickI = courseI == first.parent.index ? first.index : 0;
              courseI == last.parent.index ? brickI <= last.index : brickI < course.children.size();
              ++brickI) {
            Brick brick = course.children.get(brickI);
            final VisualLeaf visual = brick.getVisual();
            final VisualAtom atom = visual.atomVisual();
            if (!atom.compact) continue;
            if (atom == top) continue;
            if (isOrdered(expandComparator, top, atom)) continue;
            return false;
          }
        }
      }

      // Check if we actually can expand
      {
        final TSList<Brick> brickProperties = new TSList<>();
        top.getLeafBricks(context, brickProperties);
        Course course = null;
        int courseLastBrick = 0;
        CalculateCourseConverseContext courseCalc = null;
        for (final Brick brick : brickProperties) {

          // Line changed (or first brick)
          if (brick.parent != course) {
            // If jumping to a new non-consecutive line and unbreaking, reset to calculate previous
            // line
            boolean unsplit = brick.index == 0 && !brick.isSplit(false) && brick.parent.index > 0;
            if (unsplit) {
              if (course != null && brick.parent.index - 1 == course.index) {
                // On next line, continue calculation
              } else {
                // Reset to previous line to calculate unsplit
                courseCalc = new CalculateCourseConverseContext();
                course = brick.parent.parent.children.get(brick.parent.index - 1);
                courseLastBrick = 0;
              }
            }

            // Finish previous line, see if it goes over
            if (course != null) {
              for (; courseLastBrick < course.children.size(); ++courseLastBrick) {
                Brick brick1 = course.children.get(courseLastBrick);
                calculateNextBrickAdvance(courseCalc, brick1);
                if (courseCalc.converse > context.edge) return false;
              }
            }

            courseLastBrick = 0;
            course = brick.parent;
            if (!unsplit) {
              courseCalc = new CalculateCourseConverseContext();
            }
          }

          // Sum bricks leading up to modified brick
          for (; courseLastBrick < brick.index; ++courseLastBrick) {
            Brick brick1 = course.children.get(courseLastBrick);
            calculateNextBrickAdvance(courseCalc, brick1);
            if (courseCalc.converse > context.edge) return false;
          }

          // Sum modified brick
          calculateNextBrickAdvance(courseCalc, brick);
          if (courseCalc.converse > context.edge) return false;
          courseLastBrick += 1;
        }

        // Finish final line
        if (course != null)
          for (; courseLastBrick < course.children.size(); ++courseLastBrick) {
            Brick brick1 = course.children.get(courseLastBrick);
            calculateNextBrickAdvance(courseCalc, brick1);
            if (courseCalc.converse > context.edge) return false;
          }
      }

      // Avoid bouncing
      if (iterationContext.expanded.contains(top)) return false;
      iterationContext.expanded.add(top);

      // Expand
      top.expand(context);
      lastExpandCheckConverse = 0;
      return true;
    }

    @Override
    protected void destroyed() {
      idleExpand = null;
    }
  }
}
