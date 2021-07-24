package com.zarbosoft.merman.editorcore.banner;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.IterationContext;
import com.zarbosoft.merman.core.IterationTask;
import com.zarbosoft.merman.core.display.Text;
import com.zarbosoft.merman.core.syntax.style.Style;
import com.zarbosoft.merman.core.visual.Vector;
import com.zarbosoft.merman.core.wall.Attachment;
import com.zarbosoft.merman.core.wall.Bedding;
import com.zarbosoft.merman.core.wall.Brick;
import com.zarbosoft.merman.core.wall.Wall;
import com.zarbosoft.merman.editorcore.Editor;
import com.zarbosoft.merman.editorcore.displayderived.Box;

public class Banner {
  private final Attachment attachment = new TransverseListener(this);
  private final Style style;
  public Text text;
  public Box background;
  private BannerMessage current;
  private Brick brick;
  private double transverse;
  private Bedding bedding;
  private IterationPlace idle;

  public Banner(final Context context, Style style) {
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

  private void idlePlace(final Context context) {
    if (text == null) return;
    if (idle == null) {
      idle = new IterationPlace(context);
      context.addIteration(idle);
    }
  }

  private void place(final Editor editor) {
    if (text == null) return;
    final double calculatedTransverse =
        transverse - text.descent() - editor.bannerPad.transverseEnd * editor.context.toPixels;
    text.setBaselinePosition(
        new Vector(editor.bannerPad.converseStart * editor.context.toPixels, calculatedTransverse),
        false);
    if (background != null)
      background.setPosition(new Vector(0, calculatedTransverse - text.ascent()), false);
  }

  private void resizeBackground(final Context context) {
    if (background == null) return;
    background.setSize(context, context.edge, text.descent() + text.ascent());
  }

  public void setMessage(final Editor editor, final BannerMessage message) {
    if (current == null) {
      if (style.obbox.line || style.obbox.fill) {
        background = new Box(editor.context);
        editor.context.midground.add(background.drawing);
      }
      text = editor.context.display.text();
      editor.context.midground.add(text);
      updateStyle(editor);
      resizeBackground(editor.context);
    }
    current = message;
    text.setText(editor.context, current.text);
  }

  private void updateStyle(final Editor editor) {
    if (text == null) return;
    if (background != null) background.setStyle(style.obbox);
    text.setFont(editor.context, Context.getFont(editor.context, style));
    text.setColor(editor.context, style.color);
    if (bedding != null) editor.context.wall.removeBedding(editor.context, bedding);
    bedding =
        new Bedding(
            text.transverseSpan()
                + (editor.bannerPad.transverseStart + editor.bannerPad.transverseEnd)
                    * editor.context.toPixels,
            0);
    editor.context.wall.addBedding(editor.context, bedding);
    idlePlace(editor.context);
  }

  public void removeMessage(final Context context, final BannerMessage message) {
    if (current != message) return;
    current = null;
    context.midground.remove(text);
    text = null;
    if (background != null) {
      context.midground.remove(background.drawing);
      background = null;
    }
    context.wall.removeBedding(context, bedding);
    bedding = null;
  }

  private static class TransverseListener extends Attachment {
    private final Banner banner;

    public TransverseListener(Banner banner) {
      this.banner = banner;
    }

    @Override
    public void setTransverse(final Context context, final double transverse) {
      banner.transverse = transverse;
      banner.idlePlace(context);
    }

    @Override
    public void destroy(final Context context) {
      banner.brick = null;
    }
  }

  private class IterationPlace extends IterationTask {
    private final Context context;

    private IterationPlace(final Context context) {
      this.context = context;
    }

    @Override
    protected boolean runImplementation(final IterationContext iterationContext) {
      place(Editor.get(context));
      return false;
    }

    @Override
    protected void destroyed() {
      idle = null;
    }
  }
}
