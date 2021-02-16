package com.zarbosoft.merman.editor.wall;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.IterationContext;
import com.zarbosoft.merman.editor.IterationTask;
import com.zarbosoft.merman.editor.display.Group;
import com.zarbosoft.merman.editor.visual.VisualLeaf;
import com.zarbosoft.merman.editor.visual.alignment.Alignment;
import com.zarbosoft.merman.editor.visual.tags.Tags;
import com.zarbosoft.merman.editor.visual.tags.TagsChange;
import com.zarbosoft.merman.editor.visual.visuals.VisualAtom;
import com.zarbosoft.merman.editor.visual.visuals.VisualFrontPrimitive;
import com.zarbosoft.rendaw.common.ChainComparator;
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
  public int transverseStart;
  public int ascent = 0;
  public int descent = 0;
  public TSList<Brick> children = new TSList<>();
  public Alignment alignment;
  public Brick alignmentBrick;
  int lastExpandCheckConverse = 0;
  private IterationPlaceTask idlePlace;
  private IterationCompactTask idleCompact;
  private IterationExpandTask idleExpand;

  Course(final Context context, final int transverseStart) {
    visual = context.display.group();
    this.transverseStart = transverseStart;
    visual.setTransverse(transverseStart);
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
  private static ROPair<Integer, Integer> calculateNextBrickAdvance(
      CalculateCourseConverseContext calcContext, Brick brick, Brick.Properties properties) {
    int out = calcContext.converse;
    int out1 = calcContext.preAlignConverse;
    if (calcContext.alignment == null && properties.alignment != null) {
      calcContext.alignment = properties.alignment;
      calcContext.alignedBrick = brick;
      if (properties.alignment.converse > out) out = properties.alignment.converse;
    }
    calcContext.preAlignConverse += properties.converseSpan;
    calcContext.converse = out + properties.converseSpan;
    return new ROPair<>(out, out1);
  }

  public int transverseEdge() {
    return transverseStart + ascent + descent;
  }

  void setTransverse(final Context context, final int transverse) {
    transverseStart = transverse;
    visual.setPosition(
        new com.zarbosoft.merman.editor.visual.Vector(0, transverseStart),
        context.animateCoursePlacement);
    for (Brick child : children.mut()) {
      for (Attachment a : child.attachments.mut()) {
        a.setTransverse(context, transverseStart);
      }
    }
  }

  void changed(final Context context, final int at) {
    final Brick brick = children.get(at);
    final Brick.Properties properties = brick.properties(context);
    if (at > 0 && properties.split) {
      breakCourse(context, at);
      return;
    } else if (at == 0 && !properties.split && this.index > 0) {
      joinPreviousCourse(context);
      return;
    }
    getIdlePlace(context);
    idlePlace.changed.add(brick);
  }

  private void joinPreviousCourse(final Context context) {
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
        idlePlace.removeMaxAscent =
            Math.max(idlePlace.removeMaxAscent, brick.properties(context).ascent);
        idlePlace.removeMaxDescent =
            Math.max(idlePlace.removeMaxDescent, brick.properties(context).descent);
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
    for (Brick brick : bricks) {
      visual.add(brick.getDisplayNode());
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
    if (children.isEmpty()) {
      if (index - 1 >= 0) parent.children.get(index - 1).getIdleExpand(context);
      if (index + 1 < parent.children.size()) parent.children.get(index + 1).getIdleExpand(context);
      destroyInner(context);
    } else {
      if (at == 0 && this.index > 0) {
        joinPreviousCourse(context);
      } else {
        visual.remove(at);
        renumber(at);
        getIdlePlace(context);
        idlePlace.removeMaxAscent =
            Math.max(idlePlace.removeMaxAscent, brick.properties(context).ascent);
        idlePlace.removeMaxDescent =
            Math.max(idlePlace.removeMaxDescent, brick.properties(context).descent);
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

  public int transverseSpan() {
    return ascent + descent;
  }

  /** State of layout on a single course */
  private static class CalculateCourseConverseContext {
    /** Current converse offset */
    public int converse;
    /** The alignment for this course, if encountered */
    public Alignment alignment;
    /** The brick from which the alignment hails */
    public Brick alignedBrick;

    public int preAlignConverse;
  }

  class IterationPlaceTask extends IterationTask {
    private final Context context;
    TSSet<Brick> changed = new TSSet<>();
    int removeMaxAscent = 0;
    int removeMaxDescent = 0;

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
        final Brick.Properties properties = brick.properties(context);
        if (properties.ascent > ascent) {
          ascent = properties.ascent;
          newAscent = true;
        }
        if (properties.descent > descent) {
          descent = properties.descent;
          newDescent = true;
        }
      }
      if (!(newAscent && newDescent) && removeMaxAscent == ascent && removeMaxDescent == descent) {
        ascent = 0;
        descent = 0;
        {
          for (final Brick brick : children) {
            final Brick.Properties properties = brick.properties(context);
            ascent = Math.max(ascent, properties.ascent);
            descent = Math.max(descent, properties.descent);
          }
        }
        newAscent = true;
        newDescent = true;
      }
      if (newAscent || newDescent) {
        for (Brick b : children) {
          b.allocateTransverse(context, ascent, descent);
          for (Attachment attachment : b.getAttachments()) {
            attachment.setTransverseSpan(context,ascent,descent);
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
        final Brick.Properties properties = brick.properties(context);
        if (calcContext.alignment == null && properties.alignment != null) {
          if (alignment != null) alignment.removeBrick(context, alignmentBrick);
          alignment = properties.alignment;
          alignmentBrick = brick;
          alignment.addBrick(context, alignmentBrick);
          alignment.feedback(context, calcContext.converse);
        }
        ROPair<Integer, Integer> brickPlacement =
            calculateNextBrickAdvance(calcContext, brick, properties);
        brick.setConverse(context, brickPlacement.second, brickPlacement.first);
        for (final Attachment attachment : brick.getAttachments())
          attachment.setConverse(context, brickPlacement.first);
      }
      if (calcContext.alignment == null && alignment != null) {
        if (alignment != null) alignment.removeBrick(context, alignmentBrick);
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
      int converse = 0;
      {
        for (int index = 0; index < children.size(); ++index) {
          final Brick brick = children.get(index);
          final VisualLeaf visual = brick.getVisual();
          final VisualAtom atomVisual = visual.parent().atomVisual();
          if (skip.contains(atomVisual)) continue;
          if (!visual.atomVisual().compact || visual instanceof VisualFrontPrimitive)
            priorities.add(atomVisual);
          converse = brick.converseEdge();
          if (!priorities.isEmpty() && converse > context.edge) break;
        }
      }
      if (converse <= context.edge) {
        return false;
      }
      if (priorities.isEmpty()) {
        return false;
      }
      final VisualAtom top = priorities.poll();
      top.compact(context);
      skip.add(top);
      return true;
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
      final PriorityQueue<VisualAtom> priorities = new PriorityQueue<>(11, expandComparator);

      // Find next atom that can be expanded
      for (int index = 0; index < children.size(); ++index) {
        final Brick brick = children.get(index);
        final VisualLeaf visual = brick.getVisual();
        if (visual.atomVisual().compact) priorities.add(visual.parent().atomVisual());
      }
      if (priorities.isEmpty()) return false;
      final VisualAtom top = priorities.poll();

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
        final TSList<ROPair<Brick, Brick.Properties>> brickProperties = new TSList<>();
        top.getLeafPropertiesForTagsChange(
            context, brickProperties, TagsChange.remove(Tags.TAG_COMPACT));
        Course course = null;
        int courseLastBrick = 0;
        CalculateCourseConverseContext courseCalc = null;
        for (final ROPair<Brick, Brick.Properties> pair : brickProperties) {
          final Brick brick = pair.first;
          Brick.Properties properties = pair.second;

          // Line changed (or first brick)
          if (brick.parent != course) {
            // If jumping to a new non-consecutive line and unbreaking, reset to calculate previous
            // line
            boolean unsplit = brick.index == 0 && !properties.split && brick.parent.index > 0;
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
                Brick.Properties properties1 = brick1.properties(context);
                calculateNextBrickAdvance(courseCalc, brick1, properties1);
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
            Brick.Properties properties1 = brick1.properties(context);
            calculateNextBrickAdvance(courseCalc, brick1, properties1);
            if (courseCalc.converse > context.edge) return false;
          }

          // Sum modified brick
          calculateNextBrickAdvance(courseCalc, brick, properties);
          if (courseCalc.converse > context.edge) return false;
          courseLastBrick += 1;
        }

        // Finish final line
        if (course != null)
          for (; courseLastBrick < course.children.size(); ++courseLastBrick) {
            Brick brick1 = course.children.get(courseLastBrick);
            Brick.Properties properties1 = brick1.properties(context);
            calculateNextBrickAdvance(courseCalc, brick1, properties1);
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
