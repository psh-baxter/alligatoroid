package com.zarbosoft.merman.jfxeditor1.modalinput;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.hid.ButtonEvent;
import com.zarbosoft.merman.core.visual.visuals.VisualFrontPrimitive;
import com.zarbosoft.merman.editorcore.Editor;
import com.zarbosoft.merman.editorcore.banner.BannerMessage;
import com.zarbosoft.merman.editorcore.cursors.EditCursorFieldPrimitive;
import com.zarbosoft.merman.jfxeditor1.NotMain;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.Format;

import static com.zarbosoft.merman.jfxeditor1.NotMain.controlKeys;

public class ModalCursorFieldPrimitive extends EditCursorFieldPrimitive {
  public final NotMain main;
  public Mode mode = Mode.NAV;
  private SyntaxPath syntaxPath;
  private BannerMessage info;

  public ModalCursorFieldPrimitive(
      Context context,
      VisualFrontPrimitive visualPrimitive,
      boolean leadFirst,
      int beginOffset,
      int endOffset,
      NotMain main) {
    super(context, visualPrimitive, leadFirst, beginOffset, endOffset);
    this.main = main;
    this.syntaxPath = syntaxPath;
    if (range.beginOffset != range.endOffset) setMode(Editor.get(context), Mode.SELECT);
    else setMode(Editor.get(context), Mode.TEXT);
  }

  public void setMode(Editor editor, Mode mode) {
    this.mode = mode;
    updateInfo(editor);
  }

  public void updateInfo(Editor editor) {
    /*
    editor.banner.setMessage(
        editor,
        info =
            new BannerMessage(
                Format.format(
                    "%s - %s / %s (primitive) %s",
                    syntaxPath,
                    visualPrimitive.atomVisual().atom.type.id,
                    visualPrimitive.value.back().id,
                    mode)));

     */
  }

  public void destroy(Context context) {
    if (info != null)
    Editor.get(context).banner.removeMessage(context, info);
    super.destroy(context);
  }

  public boolean handleKey(Context context, ButtonEvent hidEvent) {
    if (hidEvent.press)
      switch (mode) {
        case NAV:
          {
            switch (hidEvent.key) {
              case S:
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
                  setMode(Editor.get(context), Mode.TEXT);
                  return true;
                }
              case N:
                {
                  actionPreviousWord(context);
                  return true;
                }
              case M:
                {
                  actionPreviousGlyph(context);
                  return true;
                }
              case COMMA:
                {
                  actionNextGlyph(context);
                  return true;
                }
              case PERIOD:
                {
                  actionNextWord(context);
                  return true;
                }
              case Y:
                {
                  actionFirstGlyph(context);
                  return true;
                }
              case U:
                {
                  Editor.get(context).undo(context);
                  return true;
                }
              case O:
                {
                  actionLastGlyph(context);
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
            return true; // Prevent typing in this mode
          }
        case SELECT:
          {
            switch (hidEvent.key) {
                // Mode changes
                // Actions
              case H:
                {
                  actionReleaseAll(context);
                  setMode(Editor.get(context), Mode.NAV);
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
                  // nop
                  return true;
                }
              case N:
                {
                  if (range.leadFirst) actionGatherPreviousWord(context);
                  else actionReleaseNextWord(context);
                  return true;
                }
              case M:
                {
                  if (range.leadFirst) actionGatherPreviousGlyph(context);
                  else actionReleaseNextGlyph(context);
                  return true;
                }
              case COMMA:
                {
                  if (range.leadFirst) actionReleasePreviousGlyph(context);
                  else actionGatherNextGlyph(context);
                  return true;
                }
              case PERIOD:
                {
                  if (range.leadFirst) actionReleasePreviousWord(context);
                  else actionGatherNextWord(context);
                  return true;
                }
              case Y:
                {
                  actionGatherLast(context);
                  return true;
                }
              case O:
                {
                  actionGatherFirst(context);
                  return true;
                }
              case DELETE:
              case X:
                {
                  if (range.beginOffset != range.endOffset) editCut(Editor.get(context));
                  else editDeleteNext(Editor.get(context));
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
              case DOWN:
                {
                  actionNextLine(context);
                  return true;
                }
              case UP:
                {
                  actionPreviousLine(context);
                  return true;
                }
              case LEFT:
                {
                  actionPreviousGlyph(context);
                  return true;
                }
              case RIGHT:
                {
                  actionNextGlyph(context);
                  return true;
                }
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
    return false;
  }

  public static enum Mode {
    NAV,
    SELECT,
    TEXT
  }
  /*
  non-modal

  package com.zarbosoft.merman.jfxeditor1.modalinput;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.hid.ButtonEvent;
import com.zarbosoft.merman.core.hid.Key;
import com.zarbosoft.merman.core.visual.visuals.VisualFrontPrimitive;
import com.zarbosoft.merman.editorcore.Editor;
import com.zarbosoft.merman.editorcore.banner.BannerMessage;
import com.zarbosoft.merman.editorcore.cursors.EditCursorFieldPrimitive;
import com.zarbosoft.merman.jfxeditor1.NotMain;
import com.zarbosoft.rendaw.common.Format;
import com.zarbosoft.rendaw.common.ROSetRef;
import com.zarbosoft.rendaw.common.TSSet;

import static com.zarbosoft.merman.jfxeditor1.NotMain.controlKeys;

public class ModalCursorFieldPrimitive extends EditCursorFieldPrimitive {
  public static ROSetRef<Key> shiftKeys = TSSet.of(Key.SHIFT, Key.SHIFT_LEFT, Key.SHIFT_RIGHT);

  public final NotMain main;
  private SyntaxPath syntaxPath;
  private BannerMessage info;

  public ModalCursorFieldPrimitive(
      Context context,
      VisualFrontPrimitive visualPrimitive,
      boolean leadFirst,
      int beginOffset,
      int endOffset,
      NotMain main) {
    super(context, visualPrimitive, leadFirst, beginOffset, endOffset);
    this.main = main;
    this.syntaxPath = syntaxPath;
    updateInfo(Editor.get(context));
  }

  public void updateInfo(Editor editor) {
    editor.banner.setMessage(
        editor,
        info =
            new BannerMessage(
                Format.format(
                    "%s - %s / %s (primitive)",
                    syntaxPath,
                    visualPrimitive.atomVisual().atom.type.id,
                    visualPrimitive.value.back().id)));
  }

  public void destroy(Context context) {
    Editor.get(context).banner.removeMessage(context, info);
    super.destroy(context);
  }

  public boolean handleKey(Context context, ButtonEvent hidEvent) {
    if (hidEvent.press) {
      switch (hidEvent.key) {
        case ESCAPE:
          {
            actionExit(context);
            return true;
          }
        case DOWN:
          {
            if (hidEvent.modifiers.containsAll(shiftKeys)) {
              if (range.leadFirst) actionReleasePreviousLine(context);
              else actionGatherNextLine(context);
            } else actionNextLine(context);
            return true;
          }
        case UP:
          {
            if (hidEvent.modifiers.containsAll(shiftKeys)) {
              if (range.leadFirst) actionGatherPreviousLine(context);
              else actionReleaseNextLine(context);
            } else actionPreviousLine(context);
            return true;
          }
        case LEFT:
          {
            if (hidEvent.modifiers.containsAll(shiftKeys)) {
              if (range.leadFirst) actionGatherPreviousGlyph(context);
              else actionReleaseNextGlyph(context);
            } else actionPreviousGlyph(context);
            return true;
          }
        case RIGHT:
          {
            if (hidEvent.modifiers.containsAll(shiftKeys)) {
              if (range.leadFirst) actionReleasePreviousGlyph(context);
              else actionGatherNextGlyph(context);
            } else actionNextGlyph(context);
            return true;
          }
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
        case X:
          {
            if (hidEvent.modifiers.containsAny(controlKeys)) {
              if (range.beginOffset != range.endOffset) editCut(Editor.get(context));
              return true;
            }
          }
        case ENTER:
          {
            editSplitLines(Editor.get(context));
            return true;
          }
        case V:
          {
            if (hidEvent.modifiers.containsAny(controlKeys)) {
              editPaste(Editor.get(context));
              return true;
            }
          }
      }
    }
    return false;
  }
}

   */
}
