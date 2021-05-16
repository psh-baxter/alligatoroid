package com.zarbosoft.merman.jfxeditor1.modalinput;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.hid.ButtonEvent;
import com.zarbosoft.merman.core.visual.visuals.VisualFieldAtomBase;
import com.zarbosoft.merman.editorcore.Editor;
import com.zarbosoft.merman.editorcore.banner.BannerMessage;
import com.zarbosoft.merman.editorcore.cursors.EditCursorAtom;
import com.zarbosoft.merman.jfxeditor1.NotMain;
import com.zarbosoft.rendaw.common.Format;

public class ModalCursorAtom extends EditCursorAtom {
  public final NotMain main;
  private BannerMessage info;

  public ModalCursorAtom(Context context, VisualFieldAtomBase base, NotMain main) {
    super(context, base);
    this.main = main;
    Editor editor = Editor.get(context);
    editor.banner.setMessage(
        editor.context,
        info =
            new BannerMessage(
                Format.format(
                    "%s - %s / %s (atom)",
                    getSyntaxPath(), base.atomVisual().atom.type.id, base.backId())));
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
