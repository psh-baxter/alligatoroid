package com.zarbosoft.merman.editorcore.cursors;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.document.fields.FieldPrimitive;
import com.zarbosoft.merman.core.visual.visuals.VisualFrontPrimitive;
import com.zarbosoft.merman.editorcore.Editor;
import com.zarbosoft.merman.editorcore.history.History;

public class EditCursorFieldPrimitive extends BaseEditCursorFieldPrimitive {
  public EditCursorFieldPrimitive(
      Context context,
      VisualFrontPrimitive visualPrimitive,
      boolean leadFirst,
      int beginOffset,
      int endOffset) {
    super(context, visualPrimitive, leadFirst, beginOffset, endOffset);
  }

  @Override
  public void editHandleTyping(Editor editor, History.Recorder recorder, String text) {
    FieldPrimitive value = visualPrimitive.value;
    if (value.back.matcher != null) {
      String preview = value.get();
      preview =
              preview.substring(0, range.beginOffset)
                      + text
                      + preview.substring(range.endOffset, preview.length());
      if (!value.back.matcher.match(editor.context.env, preview)) {
        return;
      }
    }
    super.editHandleTyping(editor, recorder, text);
  }
}
