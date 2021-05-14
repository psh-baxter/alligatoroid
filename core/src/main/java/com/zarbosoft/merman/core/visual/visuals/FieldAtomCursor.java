package com.zarbosoft.merman.core.visual.visuals;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.CursorState;
import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.document.fields.Field;
import com.zarbosoft.merman.core.document.fields.FieldAtom;
import com.zarbosoft.merman.core.visual.Visual;
import com.zarbosoft.merman.core.visual.attachments.BorderAttachment;
import com.zarbosoft.merman.core.wall.Brick;
import com.zarbosoft.rendaw.common.TSList;

public class FieldAtomCursor extends com.zarbosoft.merman.core.Cursor {
  public VisualFrontAtomBase visual;
  public BorderAttachment border;

  public FieldAtomCursor(final Context context, VisualFrontAtomBase visual) {
    this.visual = visual;
    border = new BorderAttachment(context, context.syntax.cursorStyle.obbox);
    final Brick first = nudgeCreation(context);
    border.setFirst(context, first);
    border.setLast(context, visual.body.getLastBrick(context));
  }

  @Override
  public void destroy(final Context context) {
    border.destroy(context);
    border = null;
    visual.cursor = null;
  }

  @Override
  public Visual getVisual() {
    return visual;
  }

  @Override
  public CursorState saveState() {
    return new VisualNodeCursorState(visual.value());
  }

  @Override
  public SyntaxPath getSyntaxPath() {
    return visual.getBackPath().add(FieldAtom.SYNTAX_PATH_KEY);
  }

  @Override
  public void dispatch(Dispatcher dispatcher) {
    dispatcher.handle(this);
  }

  public Brick nudgeCreation(final Context context) {
    final Visual.CreateBrickResult first = visual.body.createOrGetCornerstoneCandidate(context);
    context.wall.setCornerstone(
        context,
        first.brick,
        () -> visual.parent.getPreviousBrick(context),
        () -> visual.parent.getNextBrick(context));
    return first.brick;
  }

  public void actionCopy(Context context) {
    context.copy(Context.CopyContext.ARRAY, TSList.of(visual.atomGet()));
  }

  public void actionEnter(final Context context) {
    visual.body.selectAnyChild(context);
  }

  public void actionExit(final Context context) {
    if (visual.value().atomParentRef == null) return;
    visual.value().atomParentRef.selectAtomParent(context);
  }

  public void actionNext(final Context context) {
    visual.parent.selectNext(context);
  }

  public void actionPrevious(final Context context) {
    visual.parent.selectPrevious(context);
  }

  public void actionWindow(final Context context) {
    final Atom root = visual.atomGet();
    if (!root.visual.selectAnyChild(context)) return;
    context.windowExact(root);
    context.triggerIdleLayBricksOutward();
  }

  public static class VisualNodeCursorState implements CursorState {
    private final Field field;

    private VisualNodeCursorState(final Field field) {
      this.field = field;
    }

    @Override
    public void select(final Context context) {
      field.selectInto(context);
    }
  }
}
