package com.zarbosoft.merman.jfxeditor1.modalinput;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.hid.ButtonEvent;
import com.zarbosoft.merman.core.visual.visuals.VisualFrontPrimitive;
import com.zarbosoft.merman.editorcore.Editor;
import com.zarbosoft.merman.editorcore.gap.EditGapCursorFieldPrimitive;
import com.zarbosoft.merman.jfxeditor1.NotMain;

public class ModalGapCursorFieldPrimitive extends EditGapCursorFieldPrimitive {
  private final ModalPrimitiveInner inner;

  public ModalGapCursorFieldPrimitive(
      Editor editor,
      VisualFrontPrimitive visualPrimitive,
      boolean leadFirst,
      int beginOffset,
      int endOffset,
      NotMain main) {
    super(editor, visualPrimitive, leadFirst, beginOffset, endOffset);
    inner = new ModalPrimitiveInner(editor.context, main, this, getSyntaxPath());
  }

  @Override
  public boolean handleKey(Context context, ButtonEvent hidEvent) {
    if (inner.handleKey(context, hidEvent)) return true;
    return super.handleKey(context, hidEvent);
  }
}
