package com.zarbosoft.merman.jfxeditor1.modalinput;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.hid.ButtonEvent;
import com.zarbosoft.merman.core.visual.visuals.VisualFrontPrimitive;
import com.zarbosoft.merman.editorcore.Editor;
import com.zarbosoft.merman.editorcore.banner.BannerMessage;
import com.zarbosoft.merman.editorcore.gap.EditGapCursorFieldPrimitive;
import com.zarbosoft.merman.jfxeditor1.NotMain;
import com.zarbosoft.rendaw.common.Assertion;

import static com.zarbosoft.merman.jfxeditor1.NotMain.controlKeys;

public class ModalGapCursorFieldPrimitive extends EditGapCursorFieldPrimitive {
  public final NotMain main;
  public ModalCursorFieldPrimitive.Mode mode = ModalCursorFieldPrimitive.Mode.NAV;
  private SyntaxPath syntaxPath;
  private BannerMessage info;

  public ModalGapCursorFieldPrimitive(
      Editor editor,
      VisualFrontPrimitive visualPrimitive,
      boolean leadFirst,
      int beginOffset,
      int endOffset,
      NotMain main) {
    super(editor, visualPrimitive, leadFirst, beginOffset, endOffset);
    this.main = main;
    this.syntaxPath = syntaxPath;
    if (range.beginOffset != range.endOffset)
      setMode(editor, ModalCursorFieldPrimitive.Mode.SELECT);
    else setMode(editor, ModalCursorFieldPrimitive.Mode.TEXT);
  }

  public void setMode(Editor editor, ModalCursorFieldPrimitive.Mode mode) {
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
    if (info != null) Editor.get(context).banner.removeMessage(context, info);
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
                  setMode(Editor.get(context), ModalCursorFieldPrimitive.Mode.SELECT);
                  return true;
                }

              case H:
                {
                  editExit(Editor.get(context));
                  return true;
                }
              case J:
                {
                  if (choicePage != null) choicePage.nextChoice(context);
                  return true;
                }
              case K:
                {
                  if (choicePage != null) choicePage.previousChoice(context);
                  return true;
                }
              case ENTER:
                {
                  if (choicePage != null) choicePage.choose(Editor.get(context));
                  return true;
                }
              case L:
                {
                  setMode(Editor.get(context), ModalCursorFieldPrimitive.Mode.TEXT);
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
                  setMode(Editor.get(context), ModalCursorFieldPrimitive.Mode.NAV);
                  return true;
                }
              case J:
                {
                  if (choicePage != null) choicePage.nextChoice(context);
                  return true;
                }
              case K:
                {
                  if (choicePage != null) choicePage.previousChoice(context);
                  return true;
                }
              case ENTER:
                {
                  if (choicePage != null) choicePage.choose(Editor.get(context));
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
                  setMode(Editor.get(context), ModalCursorFieldPrimitive.Mode.NAV);
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
                  setMode(Editor.get(context), ModalCursorFieldPrimitive.Mode.NAV);
                  return true;
                }
                // Actions
              case DOWN:
                {
                  if (choicePage != null) choicePage.nextChoice(context);
                  return true;
                }
              case UP:
                {
                  if (choicePage != null) choicePage.previousChoice(context);
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
                  if (choicePage != null) choicePage.choose(Editor.get(context));
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
}
