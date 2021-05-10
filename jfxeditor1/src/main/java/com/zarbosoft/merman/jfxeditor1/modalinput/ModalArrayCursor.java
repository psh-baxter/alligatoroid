package com.zarbosoft.merman.jfxeditor1.modalinput;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.hid.ButtonEvent;
import com.zarbosoft.merman.core.visual.visuals.VisualFrontArray;
import com.zarbosoft.merman.editorcore.Editor;
import com.zarbosoft.merman.editorcore.cursors.EditArrayCursor;
import com.zarbosoft.merman.jfxeditor1.NotMain;
import com.zarbosoft.rendaw.common.Assertion;

public class ModalArrayCursor extends EditArrayCursor {
  public final NotMain main;
  public Mode mode = Mode.NAV;

  public ModalArrayCursor(
      Context context,
      VisualFrontArray visual,
      boolean leadFirst,
      int start,
      int end,
      NotMain main) {
    super(context, visual, leadFirst, start, end);
    this.main = main;
    if (start != end) mode = Mode.SELECT;
  }

  public void setMode(Mode mode) {
    this.mode = mode;
  }

  public boolean handleKey(Context context, ButtonEvent hidEvent) {
    if (hidEvent.press)
      switch (mode) {
        case NAV:
          {
            switch (hidEvent.key) {
              case G:
                {
                  setMode(Mode.SELECT);
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
              case M:
                {
                  actionNext(context);
                  return true;
                }
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
                  setMode(Mode.NAV);
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
              case M:
                {
                  actionNext(context);
                  return true;
                }
              case COMMA:
                {
                  actionPrevious(context);
                  return true;
                }
              case DELETE:
              case X:
                {
                  if (beginIndex != endIndex) editCut(Editor.get(context));
                  else editDelete(Editor.get(context));
                  setMode(Mode.NAV);
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
                  setMode(Mode.NAV);
                  return true;
                }
              case B:
                {
                  editInsertBefore(Editor.get(context));
                  setMode(Mode.NAV);
                  return true;
                }
              case A:
                {
                  editInsertAfter(Editor.get(context));
                  setMode(Mode.NAV);
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
