package com.zarbosoft.merman.jfxeditor1.modalinput;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.hid.ButtonEvent;
import com.zarbosoft.merman.editorcore.Editor;
import com.zarbosoft.merman.editorcore.banner.BannerMessage;
import com.zarbosoft.merman.editorcore.cursors.BaseEditPrimitiveCursor;
import com.zarbosoft.merman.jfxeditor1.NotMain;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.Format;

import static com.zarbosoft.merman.jfxeditor1.NotMain.controlKeys;

public class ModalPrimitiveInner {
  public final NotMain main;
  private final BaseEditPrimitiveCursor cursor;
  public Mode mode = Mode.NAV;
  private SyntaxPath syntaxPath;
  private BannerMessage info;

  public ModalPrimitiveInner(
      Context context, NotMain main, BaseEditPrimitiveCursor cursor, SyntaxPath syntaxPath) {
    this.main = main;
    this.cursor = cursor;
    this.syntaxPath = syntaxPath;
    if (cursor.range.beginOffset != cursor.range.endOffset)
      setMode(Editor.get(context), Mode.SELECT);
    else updateInfo(Editor.get(context));
  }

  public void setMode(Editor editor, Mode mode) {
    this.mode = mode;
    updateInfo(editor);
  }

  public void updateInfo(Editor editor) {
    editor.banner.addMessage(
        editor.context,
        info =
            new BannerMessage(
                Format.format(
                    "%s - %s / %s (primitive) %s",
                    syntaxPath,
                    cursor.visualPrimitive.atomVisual().atom.type.id,
                    cursor.visualPrimitive.value.back().id,
                    mode)));
  }

  public void destroy(Context context) {
    Editor.get(context).banner.removeMessage(context, info);
  }

  public boolean handleKey(Context context, ButtonEvent hidEvent) {
    if (hidEvent.press)
      switch (mode) {
        case NAV:
          {
            switch (hidEvent.key) {
                // Mode changes
              case ESCAPE:
                {
                  cursor.actionExit(context);
                  return true;
                }
              case T:
                {
                  setMode(Editor.get(context), Mode.TEXT);
                  return true;
                }
              case S:
                {
                  setMode(Editor.get(context), Mode.SELECT);
                  return true;
                }

                // Actions
              case H:
                {
                  cursor.actionPreviousGlyph(context);
                  return true;
                }
              case J:
                {
                  cursor.actionNextLine(context);
                  return true;
                }
              case K:
                {
                  cursor.actionPreviousLine(context);
                  return true;
                }
              case L:
                {
                  cursor.actionNextGlyph(context);
                  return true;
                }
              case Y:
                {
                  cursor.actionPreviousWord(context);
                  return true;
                }
              case O:
                {
                  cursor.actionNextWord(context);
                  return true;
                }
              case U:
                {
                  cursor.actionLastGlyph(context);
                  return true;
                }
              case I:
                {
                  cursor.actionFirstGlyph(context);
                  return true;
                }
              case M:
                {
                  cursor.actionNext(context);
                  return true;
                }
              case COMMA:
                {
                  cursor.actionPrevious(context);
                  return true;
                }
              case BACK_SPACE:
                {
                  cursor.editDeletePrevious(Editor.get(context));
                  return true;
                }
              case DELETE:
              case X:
                {
                  if (cursor.range.beginOffset != cursor.range.endOffset)
                    cursor.editCut(Editor.get(context));
                  else cursor.editDeleteNext(Editor.get(context));
                  return true;
                }
              case C:
                {
                  cursor.actionCopy(context);
                  return true;
                }
              case V:
                {
                  cursor.editPaste(Editor.get(context));
                  return true;
                }
              case B:
                {
                  cursor.editSplitLines(Editor.get(context));
                  return true;
                }
              case G:
                {
                  cursor.editJoinLines(Editor.get(context));
                  return true;
                }
              case F:
                {
                  main.flush(true);
                  return true;
                }
            }
            return true; // Prevent typing in this mode
          }
        case SELECT:
          {
            switch (hidEvent.key) {
                // Mode changes
              case ESCAPE:
                {
                  cursor.actionReleaseAll(context);
                  setMode(Editor.get(context), Mode.NAV);
                  return true;
                }
                // Actions
              case H:
                {
                  if (cursor.range.leadFirst) cursor.actionGatherPreviousGlyph(context);
                  else cursor.actionReleaseNextGlyph(context);
                  return true;
                }
              case Y:
                {
                  if (cursor.range.leadFirst) cursor.actionGatherPreviousWord(context);
                  else cursor.actionReleaseNextWord(context);
                  return true;
                }
              case J:
                {
                  if (cursor.range.leadFirst) cursor.actionReleasePreviousLine(context);
                  else cursor.actionGatherNextLine(context);
                  return true;
                }
              case K:
                {
                  if (cursor.range.leadFirst) cursor.actionGatherPreviousLine(context);
                  else cursor.actionReleaseNextLine(context);
                  return true;
                }
              case L:
                {
                  if (cursor.range.leadFirst) cursor.actionReleasePreviousGlyph(context);
                  else cursor.actionGatherNextGlyph(context);
                  return true;
                }
              case O:
                {
                  if (cursor.range.leadFirst) cursor.actionReleasePreviousWord(context);
                  else cursor.actionGatherNextWord(context);
                  return true;
                }
              case U:
                {
                  cursor.actionGatherLast(context);
                  return true;
                }
              case I:
                {
                  cursor.actionGatherFirst(context);
                  return true;
                }
              case M:
                {
                  cursor.actionNext(context);
                  return true;
                }
              case COMMA:
                {
                  cursor.actionPrevious(context);
                  return true;
                }
              case DELETE:
              case X:
                {
                  if (cursor.range.beginOffset != cursor.range.endOffset)
                    cursor.editCut(Editor.get(context));
                  else cursor.editDeleteNext(Editor.get(context));
                  setMode(Editor.get(context), Mode.NAV);
                  return true;
                }
              case C:
                {
                  cursor.actionCopy(context);
                  return true;
                }
              case V:
                {
                  cursor.editPaste(Editor.get(context));
                  return true;
                }
              case F:
                {
                  main.flush(true);
                  return true;
                }
            }
            return true; // Prevent typing in this mode
          }
        case TEXT:
          {
            switch (hidEvent.key) {
                // Mode changes
              case ESCAPE:
                {
                  setMode(Editor.get(context), Mode.NAV);
                  return true;
                }
                // Actions
              case BACK_SPACE:
                {
                  cursor.editDeletePrevious(Editor.get(context));
                  return true;
                }
              case DELETE:
                {
                  cursor.editDeleteNext(Editor.get(context));
                  return true;
                }
              case ENTER:
                {
                  cursor.editSplitLines(Editor.get(context));
                  return true;
                }
              case J:
                {
                  if (hidEvent.modifiers.containsAny(controlKeys)) {
                    cursor.editJoinLines(Editor.get(context));
                    return true;
                  }
                }
              case V:
                {
                  if (hidEvent.modifiers.containsAny(controlKeys)) {
                    cursor.editPaste(Editor.get(context));
                    return true;
                  }
                }
            }
            break;
          }
        default:
          throw new Assertion();
      }
    return false;
  }

  public static enum Mode {
    NAV,
    SELECT,
    TEXT
  }
}
