package com.zarbosoft.merman.jfxeditor1.modalinput;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.hid.ButtonEvent;
import com.zarbosoft.merman.core.visual.visuals.VisualFieldArray;
import com.zarbosoft.merman.editorcore.Editor;
import com.zarbosoft.merman.editorcore.cursors.EditCursorFieldArray;
import com.zarbosoft.merman.jfxeditor1.NotMain;
import com.zarbosoft.rendaw.common.Assertion;

import static com.zarbosoft.merman.jfxeditor1.NotMain.shiftKeys;

public class ModalCursorFieldArray extends EditCursorFieldArray {
  public final NotMain main;
  public Mode mode = Mode.NAV;

  public ModalCursorFieldArray(
      Context context,
      VisualFieldArray visual,
      boolean leadFirst,
      int start,
      int end,
      NotMain main) {
    super(context, visual, leadFirst, start, end);
    this.main = main;
  }

  public void setMode(Mode mode) {
    this.mode = mode;
  }

  public boolean handleKey(Context context, ButtonEvent hidEvent) {
    if (NotMain.handleCommonNavigation(context, main, hidEvent)) return true;
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
              case DIR_SURFACE:
              case H:
                {
                  actionExit(context);
                  return true;
                }
              case DIR_NEXT:
              case J:
                {
                  if (hidEvent.modifiers.containsAny(shiftKeys)) {
                    if (leadFirst && beginIndex != endIndex) actionReleasePrevious(context);
                    else actionGatherNext(context);
                  } else {
                    actionNextElement(context);
                  }
                  return true;
                }
              case DIR_PREV:
              case K:
                {
                  if (hidEvent.modifiers.containsAny(shiftKeys)) {
                    if (!leadFirst && beginIndex != endIndex) actionReleaseNext(context);
                    else actionGatherPrevious(context);
                  } else {
                    actionPreviousElement(context);
                  }
                  return true;
                }
              case DIR_DIVE:
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
