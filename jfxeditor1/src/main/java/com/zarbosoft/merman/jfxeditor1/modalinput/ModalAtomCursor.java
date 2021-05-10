package com.zarbosoft.merman.jfxeditor1.modalinput;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.hid.ButtonEvent;
import com.zarbosoft.merman.core.visual.visuals.VisualFrontAtomBase;
import com.zarbosoft.merman.editorcore.Editor;
import com.zarbosoft.merman.editorcore.cursors.EditAtomCursor;
import com.zarbosoft.merman.jfxeditor1.NotMain;

public class ModalAtomCursor extends EditAtomCursor {
  public final NotMain main;

  public ModalAtomCursor(Context context, VisualFrontAtomBase base, NotMain main) {
    super(context, base);
    this.main = main;
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
        case M:
          {
            actionNext(context);
            return true;
          }
        case K:
        case COMMA:
          {
            actionPrevious(context);
            return true;
          }
        case DELETE:
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
