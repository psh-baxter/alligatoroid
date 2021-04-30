package com.zarbosoft.merman.editorcore.cursors;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.document.fields.FieldArray;
import com.zarbosoft.merman.core.syntax.BaseGapAtomType;
import com.zarbosoft.merman.core.syntax.GapAtomType;
import com.zarbosoft.merman.core.syntax.SuffixGapAtomType;
import com.zarbosoft.merman.core.visual.visuals.VisualFrontAtomBase;
import com.zarbosoft.merman.editorcore.Editor;
import com.zarbosoft.merman.editorcore.history.changes.ChangeArray;
import com.zarbosoft.rendaw.common.TSList;

public class EditAtomCursor extends VisualFrontAtomBase.Cursor {
  public EditAtomCursor(Context context, VisualFrontAtomBase base) {
    super(context, base);
  }

  public void editCut(Editor editor) {
    editor.history.record(editor.context, null,
        recorder -> {
          editor.context.copy(TSList.of(base.atomGet()));
          editor.atomSet(editor.context, recorder, base, editor.createEmptyGap(editor.context.syntax.gap));
        });
  }

  public void editDelete(Editor editor) {
    editor.history.record(editor.context, null,
        recorder -> {
          editor.atomSet(editor.context, recorder, base, editor.createEmptyGap(editor.context.syntax.gap));
        });
  }

  public void editPaste(Editor editor) {
    editor.context.uncopy(
        base.nodeType(),
        atoms -> {
          if (atoms.isEmpty()) return;
          editor.history.record(editor.context, null,
              recorder -> {
                if (atoms.size() == 1) {
                  editor.atomSet(editor.context, recorder, base, atoms.get(0));
                } else {
                  Atom gap = editor.createEmptyGap(editor.context.syntax.suffixGap);
                  editor.atomSet(editor.context, recorder, base, gap);
                  recorder.apply(
                      editor.context,
                      new ChangeArray(
                          (FieldArray) gap.fields.get(BaseGapAtomType.GAP_PRIMITIVE_KEY),
                          0,
                          0,
                          atoms));
                }
              });
        });
  }

  public void editSuffix(Editor editor) {
    editor.history.record(editor.context, null,
        recorder -> {
          final Atom old = base.atomGet();
          final Atom gap = editor.createEmptyGap(editor.context.syntax.suffixGap);
          editor.atomSet(editor.context, recorder, base, gap);
          recorder.apply(
              editor.context,
              new ChangeArray((FieldArray) gap.fields.get(SuffixGapAtomType.PRECEDING_KEY), 0, 0, TSList.of(old)));
          gap.fields.get(GapAtomType.GAP_PRIMITIVE_KEY).selectInto(editor.context);
        });
  }
}
