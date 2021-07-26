package com.zarbosoft.merman.jfxeditor1.modalinput;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.hid.ButtonEvent;
import com.zarbosoft.merman.core.visual.visuals.VisualAtom;
import com.zarbosoft.merman.editorcore.Editor;
import com.zarbosoft.merman.editorcore.cursors.EditCursorAtom;
import com.zarbosoft.merman.jfxeditor1.NotMain;

public class ModalCursorAtom extends EditCursorAtom {
  public final NotMain main;

  public ModalCursorAtom(Context context, VisualAtom visual, int index, NotMain main) {
    super(context, visual, index);
    this.main = main;
  }

  public boolean handleKey(Context context, ButtonEvent hidEvent) {
    if (NotMain.handleCommonNavigation(context, main, hidEvent)) return true;
    if (hidEvent.press)
      switch (hidEvent.key) {
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
        case B:
        {
          editInsertBefore(Editor.get(context));
          return true;
        }
        case A:
        {
          editInsertAfter(Editor.get(context));
          return true;
        }
      }
    return super.handleKey(context, hidEvent);
  }
}
