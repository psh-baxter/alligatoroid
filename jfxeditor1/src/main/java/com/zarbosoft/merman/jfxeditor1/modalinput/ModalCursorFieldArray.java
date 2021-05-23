package com.zarbosoft.merman.jfxeditor1.modalinput;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.hid.ButtonEvent;
import com.zarbosoft.merman.core.visual.visuals.VisualFieldArray;
import com.zarbosoft.merman.editorcore.Editor;
import com.zarbosoft.merman.editorcore.banner.BannerMessage;
import com.zarbosoft.merman.editorcore.cursors.EditCursorFieldArray;
import com.zarbosoft.merman.jfxeditor1.NotMain;
import com.zarbosoft.rendaw.common.Assertion;

public class ModalCursorFieldArray extends EditCursorFieldArray {
  public final NotMain main;
  public Mode mode = Mode.NAV;
  private BannerMessage info;

  public ModalCursorFieldArray(
      Context context,
      VisualFieldArray visual,
      boolean leadFirst,
      int start,
      int end,
      NotMain main) {
    super(context, visual, leadFirst, start, end);
    this.main = main;
    if (start != end) setMode(Editor.get(context), Mode.SELECT);
    else updateInfo(Editor.get(context));
  }

  public void updateInfo(Editor editor) {
    /*
    editor.banner.setMessage(
        editor,
        info =
            new BannerMessage(
                Format.format(
                    "%s - %s / %s (array) %s",
                    getSyntaxPath(),
                    visual.atomVisual().atom.type.id,
                    visual.value.back().id,
                    mode)));

     */
  }

  @Override
  public void destroy(Context context) {
    if (info != null) Editor.get(context).banner.removeMessage(context, info);
    super.destroy(context);
  }

  public void setMode(Editor editor, Mode mode) {
    this.mode = mode;
    updateInfo(editor);
  }

  public boolean handleKey(Context context, ButtonEvent hidEvent) {
    if (hidEvent.press)
      switch (mode) {
        case NAV:
          {
            switch (hidEvent.key) {
              case G:
                {
                  setMode(Editor.get(context), Mode.SELECT);
                  return true;
                }
              case H:
                {
                  actionExit(context);
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
              case L:
                {
                  actionEnter(context);
                  return true;
                }
              case U:
                {
                  actionLastElement(context);
                  return true;
                }
              case I:
                {
                  actionFirstElement(context);
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
              case F:
                {
                  main.flush(true);
                  return true;
                }
            }
            break;
          }
        case SELECT:
          {
            switch (hidEvent.key) {
              case G:
                {
                  actionReleaseAll(context);
                  setMode(Editor.get(context), Mode.NAV);
                  return true;
                }
              case H:
                {
                  actionExit(context);
                  return true;
                }
              case J:
                {
                  if (leadFirst) actionReleasePrevious(context);
                  else actionGatherNext(context);
                  return true;
                }
              case K:
                {
                  if (leadFirst) actionGatherPrevious(context);
                  else actionReleaseNext(context);
                  return true;
                }
              case L:
                {
                  if (beginIndex == endIndex) {
                    actionEnter(context);
                  }
                  return true;
                }
              case U:
                {
                  actionGatherLast(context);
                  return true;
                }
              case I:
                {
                  actionGatherFirst(context);
                  return true;
                }
              case DELETE:
              case X:
                {
                  if (beginIndex != endIndex) editCut(Editor.get(context));
                  else editDelete(Editor.get(context));
                  setMode(Editor.get(context), Mode.NAV);
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
                  setMode(Editor.get(context), Mode.NAV);
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
              case F:
                {
                  main.flush(true);
                  return true;
                }
            }
            break;
          }
        default:
          throw new Assertion();
      }
    return super.handleKey(context, hidEvent);
  }

  public static enum Mode {
    NAV,
    SELECT,
  }
}
