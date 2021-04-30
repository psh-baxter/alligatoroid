package com.zarbosoft.merman.editorcore.details;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.IterationContext;
import com.zarbosoft.merman.core.IterationTask;
import com.zarbosoft.merman.core.syntax.style.Style;
import com.zarbosoft.merman.core.visual.Vector;
import com.zarbosoft.merman.core.wall.Attachment;
import com.zarbosoft.merman.core.wall.Bedding;
import com.zarbosoft.merman.core.wall.Brick;
import com.zarbosoft.merman.core.wall.Wall;
import com.zarbosoft.merman.editorcore.displayderived.Box;
import com.zarbosoft.rendaw.common.ChainComparator;

import java.util.PriorityQueue;

public class Details {
  private final PriorityQueue<DetailsPage> queue =
      new PriorityQueue<>(
          11, new ChainComparator<DetailsPage>().greaterFirst(m -> m.priority).build());
  private final Style style;
  public DetailsPage current;
  public Box background;
  private Brick brick;
  private double transverse;
  private double transverseSpan;
  private Bedding bedding;
  private IterationPlace idle;
  private final Attachment attachment =
      new Attachment() {
        @Override
        public void setTransverse(final Context context, final double transverse) {
          Details.this.transverse = transverse;
          iterationPlace(context, false);
        }

        @Override
        public void destroy(final Context context) {
          brick = null;
        }

        @Override
        public void setTransverseSpan(
            final Context context, final double ascent, final double descent) {
          Details.this.transverseSpan = ascent + descent;
          iterationPlace(context, false);
        }
      };

  public Details(final Context context, Style style) {
    this.style = style;
    context.wall.addCornerstoneListener(
        context,
        new Wall.CornerstoneListener() {
          @Override
          public void cornerstoneChanged(final Context context, final Brick cornerstone) {
            if (brick != null) {
              brick.removeAttachment(attachment);
            }
            brick = cornerstone;
            brick.addAttachment(context, attachment);
          }
        });
    context.addConverseEdgeListener(
        new Context.ContextDoubleListener() {
          @Override
          public void changed(final Context context, final double oldValue, final double newValue) {
            resizeBackground(context);
          }
        });
  }

  private void iterationPlace(final Context context, final boolean animate) {
    if (current == null) return;
    if (idle == null) {
      idle = new IterationPlace(context);
      context.addIteration(idle);
    }
    idle.animate = idle.animate && animate;
  }

  public void tagsChanged(final Context context) {
    if (current == null) return;
    updateStyle(context);
    place(context, false);
  }

  private void updateStyle(final Context context) {
    current.tagsChanged(context);
    if (background == null) {
      background = new Box(context);
      context.midground.add(background.drawing);
    }
    background.setStyle(style.obbox);
    resizeBackground(context);
  }

  private double pageTransverse(final Context context) {
    final double padStart = context.syntax.detailPad.transverseStart;
    final double padEnd = context.syntax.detailPad.transverseEnd;
    return Math.min(
        context.transverseEdge - padStart - current.node.transverseSpan() - padEnd,
        transverse + transverseSpan + padStart);
  }

  private void place(final Context context, final boolean animate) {
    final double transverse = pageTransverse(context);
    current.node.setPosition(
        new Vector(context.syntax.detailPad.converseStart, transverse), animate);
    if (background != null) background.setPosition(new Vector(0, transverse), animate);
  }

  private void resizeBackground(final Context context) {
    if (background == null) return;
    background.setSize(context, context.edge * 2, current.node.transverseSpan());
  }

  public void addPage(final Context context, final DetailsPage page) {
    queue.add(page);
    update(context);
  }

  private void update(final Context context) {
    if (queue.isEmpty()) {
      if (current != null) {
        context.wall.removeBedding(context, bedding);
        bedding = null;
        context.midground.remove(current.node);
        current = null;
        if (background != null) {
          context.midground.remove(background.drawing);
          background = null;
        }
      }
    } else if (queue.peek() != current) {
      if (current != null) {
        context.midground.remove(current.node);
        context.wall.removeBedding(context, bedding);
      } else {

      }
      current = queue.peek();
      updateStyle(context);
      place(context, false);
      context.midground.add(current.node);
      bedding =
          new Bedding(
              0,
              context.syntax.detailPad.transverseStart
                  + current.node.transverseSpan()
                  + context.syntax.detailPad.transverseEnd);
      context.wall.addBedding(context, bedding);
    }
  }

  public void removePage(final Context context, final DetailsPage page) {
    if (queue.isEmpty()) return;
    queue.remove(page);
    update(context);
  }

  private class IterationPlace extends IterationTask {
    private final Context context;
    private boolean animate;

    private IterationPlace(final Context context) {
      this.context = context;
      this.animate = context.animateCoursePlacement;
    }

    @Override
    protected boolean runImplementation(final IterationContext iterationContext) {
      if (current != null) {
        place(context, animate);
      }
      return false;
    }

    @Override
    protected void destroyed() {
      idle = null;
    }
  }
}
