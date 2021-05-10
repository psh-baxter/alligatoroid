package com.zarbosoft.merman.jfxeditor1.modalinput;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.hid.ButtonEvent;
import com.zarbosoft.merman.core.visual.visuals.VisualFrontPrimitive;
import com.zarbosoft.merman.editorcore.Editor;
import com.zarbosoft.merman.editorcore.cursors.EditPrimitiveCursor;
import com.zarbosoft.merman.jfxeditor1.NotMain;
import com.zarbosoft.rendaw.common.Assertion;

import static com.zarbosoft.merman.jfxeditor1.NotMain.controlKeys;

public class ModalPrimitiveCursor extends EditPrimitiveCursor {
  public Mode mode = Mode.NAV;
  public final NotMain main;

  public ModalPrimitiveCursor(
          Context context,
          VisualFrontPrimitive visualPrimitive,
          boolean leadFirst,
          int beginOffset,
          int endOffset, NotMain main) {
    super(context, visualPrimitive, leadFirst, beginOffset, endOffset);
    this.main = main;
    if (beginOffset != endOffset) mode = Mode.SELECT;
  }

  public void setMode(Mode mode) {
    this.mode = mode;
  }

  @Override
  public boolean handleKey(Context context, ButtonEvent hidEvent) {
    if (hidEvent.press)
      switch (mode) {
        case NAV:
          {
            switch (hidEvent.key) {
                // Mode changes
              case ESCAPE:
                {
                  actionExit(context);
                  return true;
                }
              case T:
                {
                  setMode(Mode.TEXT);
                  return true;
                }
              case S:
                {
                  setMode(Mode.SELECT);
                  return true;
                }

                // Actions
              case H:
                {
                  actionPreviousGlyph(context);
                  return true;
                }
              case J:
                {
                  actionNextLine(context);
                  return true;
                }
              case K:
                {
                  actionPreviousLine(context);
                  return true;
                }
              case L:
                {
                  actionNextGlyph(context);
                  return true;
                }
              case Y:
                {
                  actionPreviousWord(context);
                  return true;
                }
              case O:
                {
                  actionNextWord(context);
                  return true;
                }
              case U:
                {
                  actionLastGlyph(context);
                  return true;
                }
              case I:
                {
                  actionFirstGlyph(context);
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
              case BACK_SPACE:
                {
                  editDeletePrevious(Editor.get(context));
                  return true;
                }
              case DELETE:
              case X:
                {
                  if (range.beginOffset != range.endOffset) editCut(Editor.get(context));
                  else editDeleteNext(Editor.get(context));
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
                  editSplitLines(Editor.get(context));
                  return true;
                }
              case G:
                {
                  editJoinLines(Editor.get(context));
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
                // Mode changes
              case ESCAPE:
                {
                  actionReleaseAll(context);
                  setMode(Mode.NAV);
                  return true;
                }
                // Actions
              case H:
                {
                  if (range.leadFirst) actionGatherPreviousGlyph(context);
                  else actionReleaseNextGlyph(context);
                  return true;
                }
              case Y:
                {
                  if (range.leadFirst) actionGatherPreviousWord(context);
                  else actionReleaseNextWord(context);
                  return true;
                }
              case J:
                {
                  if (range.leadFirst) actionReleasePreviousLine(context);
                  else actionGatherNextLine(context);
                  return true;
                }
              case K:
                {
                  if (range.leadFirst) actionGatherPreviousLine(context);
                  else actionReleaseNextLine(context);
                  return true;
                }
              case L:
                {
                  if (range.leadFirst) actionReleasePreviousGlyph(context);
                  else actionGatherNextGlyph(context);
                  return true;
                }
              case O:
                {
                  if (range.leadFirst) actionReleasePreviousWord(context);
                  else actionGatherNextWord(context);
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
                  if (range.beginOffset != range.endOffset) editCut(Editor.get(context));
                  else editDeleteNext(Editor.get(context));
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
              case F:
              {
                main.flush(true);
                return true;
              }
            }
            break;
          }
        case TEXT:
          {
            switch (hidEvent.key) {
                // Mode changes
              case ESCAPE:
                {
                  setMode(Mode.NAV);
                  return true;
                }
                // Actions
              case BACK_SPACE:
                {
                  editDeletePrevious(Editor.get(context));
                  return true;
                }
              case DELETE:
                {
                  editDeleteNext(Editor.get(context));
                  return true;
                }
              case ENTER:
                {
                  editSplitLines(Editor.get(context));
                  return true;
                }
              case J:
                {
                  if (hidEvent.modifiers.containsAny(controlKeys)) {
                    editJoinLines(Editor.get(context));
                    return true;
                  }
                }
              case V:
                {
                  if (hidEvent.modifiers.containsAny(controlKeys)) {
                    editPaste(Editor.get(context));
                    return true;
                  }
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
    TEXT
  }
}
