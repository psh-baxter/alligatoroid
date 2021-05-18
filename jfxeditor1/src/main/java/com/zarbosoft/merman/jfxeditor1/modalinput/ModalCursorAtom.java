package com.zarbosoft.merman.jfxeditor1.modalinput;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.hid.ButtonEvent;
import com.zarbosoft.merman.core.visual.Visual;
import com.zarbosoft.merman.core.visual.visuals.VisualAtom;
import com.zarbosoft.merman.core.visual.visuals.VisualFieldArray;
import com.zarbosoft.merman.core.visual.visuals.VisualFieldAtom;
import com.zarbosoft.merman.core.visual.visuals.VisualFrontPrimitive;
import com.zarbosoft.merman.editorcore.Editor;
import com.zarbosoft.merman.editorcore.banner.BannerMessage;
import com.zarbosoft.merman.editorcore.cursors.EditCursorAtom;
import com.zarbosoft.merman.jfxeditor1.NotMain;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.Format;
import com.zarbosoft.rendaw.common.ROPair;

public class ModalCursorAtom extends EditCursorAtom {
  public final NotMain main;
  private BannerMessage info;

  public ModalCursorAtom(Context context, VisualAtom visual, int index, NotMain main) {
    super(context, visual, index);
    this.main = main;
  }

  @Override
  public void setIndex(Context context, int index) {
    super.setIndex(context, index);
    Editor editor = Editor.get(context);
    ROPair<String, Visual> indexVisual = visual.selectable.get(index);
    String indexVisType;
    if (indexVisual.second instanceof VisualFieldAtom) {
      indexVisType = "atom";
    } else if (indexVisual.second instanceof VisualFieldArray) {
      indexVisType = "array";
    } else if (indexVisual.second instanceof VisualFrontPrimitive) {
      indexVisType = "primitive";
    } else throw new Assertion();
    editor.banner.setMessage(
        editor,
        info =
            new BannerMessage(
                Format.format(
                    "%s - %s / field %s (%s)",
                    getSyntaxPath(), visual.atom.type.id, indexVisual.first, indexVisType)));
  }

  @Override
  public void destroy(Context context) {
    Editor.get(context).banner.removeMessage(context, info);
    super.destroy(context);
  }

  public boolean handleKey(Context context, ButtonEvent hidEvent) {
    if (hidEvent.press)
      switch (hidEvent.key) {
          // Mode changes
        case H:
          {
            actionExit(context);
            return true;
          }
        case L:
          {
            actionEnter(context);
            return true;
          }
        case J:
          {
            actionNextElement(context);
            return true;
          }
        case K:
          {
            actionPreviousElement(context);
            return true;
          }
        case X:
          {
            editCut(Editor.get(context));
            return true;
          }
        case C:
          {
            actionCopy(context);
            return true;
          }
        case V:
          {
            editPaste(Editor.get(context));
            return true;
          }
        case S:
          {
            editSuffix(Editor.get(context));
            return true;
          }
        case F:
          {
            main.flush(true);
            return true;
          }
      }
    return super.handleKey(context, hidEvent);
  }
}
