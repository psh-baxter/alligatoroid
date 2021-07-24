package com.zarbosoft.merman.jfxeditor1.modalinput;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.hid.ButtonEvent;
import com.zarbosoft.merman.core.hid.Key;
import com.zarbosoft.merman.core.visual.visuals.VisualFieldPrimitive;
import com.zarbosoft.merman.editorcore.Editor;
import com.zarbosoft.merman.editorcore.banner.BannerMessage;
import com.zarbosoft.merman.editorcore.gap.EditGapCursorFieldPrimitive;
import com.zarbosoft.merman.jfxeditor1.NotMain;
import com.zarbosoft.rendaw.common.Format;
import com.zarbosoft.rendaw.common.ROSetRef;
import com.zarbosoft.rendaw.common.TSSet;

import static com.zarbosoft.merman.jfxeditor1.NotMain.controlKeys;

public class ModalGapCursorFieldPrimitive extends EditGapCursorFieldPrimitive {
  public static ROSetRef<Key> shiftKeys = TSSet.of(Key.SHIFT, Key.SHIFT_LEFT, Key.SHIFT_RIGHT);

  public final NotMain main;
  private SyntaxPath syntaxPath;
  private BannerMessage info;

  public ModalGapCursorFieldPrimitive(
      Editor editor,
      VisualFieldPrimitive visualPrimitive,
      boolean leadFirst,
      int beginOffset,
      int endOffset,
      NotMain main) {
    super(editor, visualPrimitive, leadFirst, beginOffset, endOffset);
    this.main = main;
    this.syntaxPath = syntaxPath;
    updateInfo(editor);
  }

  public void updateInfo(Editor editor) {
    /*
    editor.banner.setMessage(
        editor,
        info =
            new BannerMessage(
                Format.format(
                    "%s - %s / %s (gap)",
                    syntaxPath,
                    visualPrimitive.atomVisual().atom.type.id,
                    visualPrimitive.value.back().id)));
     */
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
        case F:
          {
            if (hidEvent.modifiers.containsAny(controlKeys)) {
              main.flush(true);
              return true;
            }
          }
      }
    }
    return false;
  }
}
