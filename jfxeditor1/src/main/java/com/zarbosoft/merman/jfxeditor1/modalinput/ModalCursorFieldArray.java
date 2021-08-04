package com.zarbosoft.merman.jfxeditor1.modalinput;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.hid.ButtonEvent;
import com.zarbosoft.merman.core.visual.visuals.VisualFieldArray;
import com.zarbosoft.merman.editorcore.Editor;
import com.zarbosoft.merman.editorcore.cursors.EditCursorFieldArray;
import com.zarbosoft.merman.jfxeditor1.NotMain;
import com.zarbosoft.rendaw.common.Assertion;

import static com.zarbosoft.merman.jfxeditor1.NotMain.controlKeys;
import static com.zarbosoft.merman.jfxeditor1.NotMain.shiftKeys;

public class ModalCursorFieldArray extends EditCursorFieldArray {
  public final NotMain main;

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

  public boolean handleKey(Context context, ButtonEvent hidEvent) {
    if (NotMain.handleCommonNavigation(context, main, hidEvent)) return true;
    if (hidEvent.press)
            switch (hidEvent.key) {
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
                  if (hidEvent.modifiers.containsAny(controlKeys)) {
                    editCut(Editor.get(context));
                  } else {
                    editDelete(Editor.get(context));
                  }
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
