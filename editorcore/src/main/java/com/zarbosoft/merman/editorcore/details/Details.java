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
import com.zarbosoft.merman.editorcore.Editor;
import com.zarbosoft.merman.editorcore.displayderived.Box;

public class Details {
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

  private double pageTransverse(Editor editor) {
    final double padStart = editor.detailPad.transverseStart * editor.context.toPixels;
    final double padEnd = editor.detailPad.transverseEnd * editor.context.toPixels;
    return Math.min(
        editor.context.transverseEdge - padStart - current.node.transverseSpan() - padEnd,
        transverse + transverseSpan + padStart);
  }

  private void place(Editor editor, final boolean animate) {
    final double transverse = pageTransverse(editor);
    current.node.setPosition(new Vector(editor.detailPad.converseStart, transverse), animate);
    if (background != null) background.setPosition(new Vector(0, transverse), animate);
  }

  private void resizeBackground(final Context context) {
    if (background == null) return;
    background.setSize(context, context.edge * 2, current.node.transverseSpan());
  }

  public void setPage(Editor editor, final DetailsPage page) {
    Context context = editor.context;
    if (current != null) {
      context.midground.remove(current.node);
      context.wall.removeBedding(context, bedding);
    }
    current = page;
    if (current != null) {
      if (background == null && (style.obbox.line || style.obbox.fill)) {
        background = new Box(context);
        background.setStyle(style.obbox);
        context.midground.add(background.drawing);
      }
      resizeBackground(context);
      place(editor, false);
      context.midground.add(current.node);
      bedding =
          new Bedding(
              0,
              editor.detailPad.transverseStart * context.toPixels
                  + current.node.transverseSpan()
                  + editor.detailPad.transverseEnd * context.toPixels);
      context.wall.addBedding(context, bedding);
    }
  }

  public void removePage(final Context context, final DetailsPage page) {
    if (page != current) return;
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
        place(Editor.get(context), animate);
      }
      return false;
    }

    @Override
    protected void destroyed() {
      idle = null;
    }
  }
}
