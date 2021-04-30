package com.zarbosoft.merman.editorcore.cursors;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.document.fields.FieldPrimitive;
import com.zarbosoft.merman.core.visual.visuals.VisualFrontPrimitive;

public class EditPrimitiveCursor extends BaseEditPrimitiveCursor {
  public EditPrimitiveCursor(
      Context context,
      VisualFrontPrimitive visualPrimitive,
      boolean leadFirst,
      int beginOffset,
      int endOffset) {
    super(context, visualPrimitive, leadFirst, beginOffset, endOffset);
  }

  @Override
  public void handleTyping(Context context, String text) {
    FieldPrimitive value = visualPrimitive.value;
    if (value.middle.matcher != null) {
      String preview = value.get();
      preview =
          preview.substring(0, range.beginOffset)
              + text
              + preview.substring(range.endOffset, preview.length());
      if (!value.middle.matcher.match(context.env, preview)) {
        return;
      }
    }
    super.handleTyping(context, text);
  }
}
