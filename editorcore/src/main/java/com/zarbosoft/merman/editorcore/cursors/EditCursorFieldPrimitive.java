package com.zarbosoft.merman.editorcore.cursors;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.document.fields.FieldArray;
import com.zarbosoft.merman.core.document.fields.FieldPrimitive;
import com.zarbosoft.merman.core.syntax.GapAtomType;
import com.zarbosoft.merman.core.syntax.SuffixGapAtomType;
import com.zarbosoft.merman.core.visual.visuals.VisualFieldPrimitive;
import com.zarbosoft.merman.editorcore.Editor;
import com.zarbosoft.merman.editorcore.gap.EditGapCursorFieldPrimitive;
import com.zarbosoft.merman.editorcore.history.History;
import com.zarbosoft.merman.editorcore.history.changes.ChangePrimitive;
import com.zarbosoft.rendaw.common.TSList;

import java.util.function.Consumer;

public class EditCursorFieldPrimitive extends BaseEditCursorFieldPrimitive {
  public EditCursorFieldPrimitive(
      Context context,
      VisualFieldPrimitive visualPrimitive,
      boolean leadFirst,
      int beginOffset,
      int endOffset) {
    super(context, visualPrimitive, leadFirst, beginOffset, endOffset);
  }

  @Override
  public void editHandleTyping(Editor editor, History.Recorder recorder, String text) {
    FieldPrimitive value = visualPrimitive.value;
    if (value.back.matcher != null
        && editor.suffixOnPatternMismatch.contains(value.atomParentRef.atom().type.id)) {
      String preview = value.get();
      String after = preview.substring(range.endOffset, preview.length());
      preview = preview.substring(0, range.beginOffset) + text + after;
      if (!value.back.matcher.match(editor.context.env, preview)) {
        Consumer<History.Recorder> apply =
            recorder1 -> {
              // Remove everything after inserted text
              recorder1.apply(
                  editor,
                  new ChangePrimitive(
                      value, range.beginOffset, value.length() - range.beginOffset, ""));

              // Replace with suffix
              final Atom old = value.atomParentRef.atom();
              final Atom gap = editor.createEmptyGap(editor.context.syntax.suffixGap);
              editor.replaceInParent(editor, recorder1, old, gap);
              Editor.arrayChange(
                  editor,
                  recorder1,
                  (FieldArray) gap.namedFields.get(SuffixGapAtomType.PRECEDING_KEY),
                  0,
                  0,
                  TSList.of(old));
              gap.namedFields.get(GapAtomType.PRIMITIVE_KEY).selectInto(editor.context);

              // Send new text + chopped text to suffix
              ((EditGapCursorFieldPrimitive) editor.context.cursor)
                  .editHandleTyping(editor, recorder1, text + after);
            };
        if (recorder != null) apply.accept(recorder);
        else editor.history.record(editor, null, apply);
        return;
      }
    }
    super.editHandleTyping(editor, recorder, text);
  }
}
