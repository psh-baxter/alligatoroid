package com.zarbosoft.merman.jfxeditor1.modalinput;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.hid.ButtonEvent;
import com.zarbosoft.merman.core.visual.visuals.VisualFieldPrimitive;
import com.zarbosoft.merman.editorcore.Editor;
import com.zarbosoft.merman.editorcore.gap.EditGapCursorFieldPrimitive;
import com.zarbosoft.merman.jfxeditor1.NotMain;

public class ModalGapCursorFieldPrimitive extends EditGapCursorFieldPrimitive {
  public final NotMain main;
  private SyntaxPath syntaxPath;

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
  }

  public boolean handleKey(Context context, ButtonEvent hidEvent) {
    return NotMain.handlePrimitiveNavigation(
        context,
        main,
        this,
        hidEvent,
        new NotMain.PrimitiveKeyHandler() {
          @Override
          public boolean prev(ButtonEvent hidEvent) {
            if (choicePage != null) choicePage.previousChoice(context);
            return true;
          }

          @Override
          public boolean next(ButtonEvent hidEvent) {
            if (choicePage != null) choicePage.nextChoice(context);
            return true;
          }

          @Override
          public boolean enter(ButtonEvent hidEvent) {
            if (choicePage != null) choicePage.choose(Editor.get(context));
            return true;
          }
        });
  }
}
