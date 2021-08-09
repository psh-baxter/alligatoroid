package com.zarbosoft.merman.core.visual.visuals;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.CursorState;
import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.syntax.front.FrontArraySpecBase;
import com.zarbosoft.merman.core.syntax.front.FrontAtomSpec;
import com.zarbosoft.merman.core.syntax.front.FrontPrimitiveSpec;
import com.zarbosoft.merman.core.syntax.front.FrontSpec;
import com.zarbosoft.merman.core.visual.Visual;
import com.zarbosoft.merman.core.visual.attachments.BorderAttachment;
import com.zarbosoft.merman.core.wall.Brick;
import com.zarbosoft.rendaw.common.Assertion;

public class CursorAtom extends com.zarbosoft.merman.core.Cursor {
  public final VisualAtom visual;
  public int index;
  BorderAttachment border;

  public CursorAtom(final Context context, final VisualAtom visual, final int index) {
    this.visual = visual;
    border = new BorderAttachment(context, context.syntax.cursorStyle.obbox);
    setIndex(context, index);
  }

  public void resetCornerstone(Context context) {
    Visual fieldVisual = this.visual.selectable.get(index).second;
    int visualIndex = ((VisualAtom.ChildParent) visual.selectable.get(index).second.parent()).index;
    context.wall.setCornerstone(
            context,
            fieldVisual.createOrGetCornerstoneCandidate(context).brick,
            () -> {
              for (int at = visualIndex - 1; at >= 0; --at) {
                final Brick found = visual.children.get(at).getLastBrick(context);
                if (found != null) return found;
              }
              if (visual.parent() == null) return null;
              return visual.parent().getPreviousBrick(context);
            },
            () -> {
              for (int at = visualIndex + 1; at < visual.selectable.size(); ++at) {
                final Brick found = visual.children.get(at).getFirstBrick(context);
                if (found != null) return found;
              }
              if (visual.parent() == null) return null;
              return visual.parent().getNextBrick(context);
            });
    border.setFirst(context, fieldVisual.getFirstBrick(context));
    border.setLast(context, fieldVisual.getLastBrick(context));
  }

  public void setIndex(Context context, int index) {
    this.index = index;
    resetCornerstone(context);
  }

  @Override
  public void destroy(final Context context) {
    border.destroy(context);
    visual.cursor = null;
  }

  @Override
  public Visual getVisual() {
    return this.visual;
  }

  @Override
  public CursorState saveState() {
    return new CursorState() {
      private final int index;
      private final Atom value;

      {
        this.index = CursorAtom.this.index;
        this.value = visual.atom;
      }

      @Override
      public void select(final Context context) {
        value.visual.select(context, index);
      }
    };
  }

  @Override
  public SyntaxPath getSyntaxPath() {
    return visual.atom.namedFields.get(visual.selectable.get(index).first).getSyntaxPath();
  }

  public void actionEnter(final Context context) {
    Visual selectable = this.visual.selectable.get(index).second;
    if (selectable instanceof VisualFieldAtomBase) {
      ((VisualFieldAtomBase) selectable).atomGet().selectInto(context);
    } else {
      selectable.selectIntoAnyChild(context);
    }
  }

  public void actionExit(final Context context) {
    if (visual.atom.fieldParentRef == null) return;
    visual.atom.fieldParentRef.selectField(context);
  }

  public void actionNextElement(final Context context) {
    setIndex(context, (index + 1) % visual.selectable.size());
  }

  public void actionPreviousElement(final Context context) {
    setIndex(context, (index + visual.selectable.size() - 1) % visual.selectable.size());
  }

  public void actionCopy(final Context context) {
    Visual selectable = this.visual.selectable.get(index).second;
    if (selectable instanceof VisualFieldAtomBase) {
      ((VisualFieldAtomBase) selectable).copy(context);
    } else if (selectable instanceof VisualFieldArray) {
      ((VisualFieldArray) selectable)
          .copy(context, 0, ((VisualFieldArray) selectable).value.data.size() - 1);
    } else if (selectable instanceof VisualFieldPrimitive) {
      ((VisualFieldPrimitive) selectable)
          .copy(context, 0, ((VisualFieldPrimitive) selectable).value.data.length());
    } else throw new Assertion();
  }

  public void actionWindow(final Context context) {
    Visual selectable = this.visual.selectable.get(index).second;
    if (selectable instanceof VisualFieldAtomBase) {
      boolean editable = ((VisualFieldAtomBase) selectable).atomGet().type.front().some();
      for (FrontSpec frontSpec : ((VisualFieldAtomBase) selectable).atomGet().type.front()) {
        if (!((frontSpec instanceof FrontArraySpecBase)
            || (frontSpec instanceof FrontPrimitiveSpec)
            || (frontSpec instanceof FrontAtomSpec))) {
          editable = false;
          break;
        }
      }
      if (editable) {
        context.windowExact(((VisualFieldAtomBase) selectable).atomGet());
        context.triggerIdleLayBricksOutward();
        ((VisualFieldAtomBase) selectable).atomGet().visual.selectIntoAnyChild(context);
      }
    } else if (selectable instanceof VisualFieldArray
        || selectable instanceof VisualFieldPrimitive) {
      context.windowExact(visual.atom);
      context.triggerIdleLayBricksOutward();
    } else throw new Assertion();
  }
}
