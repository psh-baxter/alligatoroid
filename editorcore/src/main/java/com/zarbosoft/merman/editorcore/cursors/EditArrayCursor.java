package com.zarbosoft.merman.editorcore.cursors;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.document.fields.FieldArray;
import com.zarbosoft.merman.core.syntax.SuffixGapAtomType;
import com.zarbosoft.merman.core.visual.visuals.VisualFrontArray;
import com.zarbosoft.merman.editorcore.Editor;
import com.zarbosoft.merman.editorcore.history.changes.ChangeArray;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSList;

public class EditArrayCursor extends VisualFrontArray.Cursor {
  public EditArrayCursor(
      Context context, VisualFrontArray visual, boolean leadFirst, int start, int end) {
    super(context, visual, leadFirst, start, end);
  }

  public void editCut(Editor editor) {
    editor.history.record(
        editor.context,
        null,
        recorder -> {
          editor.context.copy(visual.value.data.sublist(beginIndex, endIndex + 1));
          recorder.apply(
              editor.context,
              new ChangeArray(visual.value, beginIndex, endIndex - beginIndex + 1, ROList.empty));
        });
  }

  public void editDelete(Editor editor) {
    editor.history.record(
        editor.context,
        new ROPair(visual.value, "delete"),
        recorder -> {
          recorder.apply(
              editor.context,
              new ChangeArray(visual.value, beginIndex, endIndex - beginIndex + 1, ROList.empty));
        });
  }

  public void editInsertAfter(Editor editor) {
    editor.history.record(
        editor.context,
        new ROPair(visual.value, "insert_after"),
        recorder -> {
          final Atom created = editor.arrayInsertNewDefault(recorder, visual.value, endIndex + 1);
          if (!created.visual.selectAnyChild(editor.context))
            setPosition(editor.context, endIndex + 1);
        });
  }

  public void editInsertBefore(Editor editor) {
    editor.history.record(
        editor.context,
        new ROPair(visual.value, "insert_before"),
        recorder -> {
          final Atom created = editor.arrayInsertNewDefault(recorder, visual.value, beginIndex);
          if (!created.visual.selectAnyChild(editor.context))
            setPosition(editor.context, beginIndex);
        });
  }

  public void editMoveAfter(Editor editor) {
    if (endIndex == visual.value.data.size() - 1) return;
    editor.history.record(
        editor.context,
        new ROPair(visual.value, "move"),
        recorder -> {
          int index = beginIndex;
          final TSList<Atom> atoms = visual.value.data.sublist(index, endIndex + 1).mut();
          recorder.apply(
              editor.context, new ChangeArray(visual.value, index, atoms.size(), ROList.empty));
          setPosition(editor.context, ++index);
          recorder.apply(editor.context, new ChangeArray(visual.value, index, 0, atoms));
          leadFirst = false;
          setRange(editor.context, index, index + atoms.size() - 1);
        });
  }

  public void editMoveBefore(Editor editor) {
    if (beginIndex == 0) return;
    editor.history.record(
        editor.context,
        new ROPair(visual.value, "move"),
        recorder -> {
          int index = beginIndex;
          final TSList<Atom> atoms = visual.value.data.sublist(index, endIndex + 1).mut();
          recorder.apply(
              editor.context, new ChangeArray(visual.value, index, atoms.size(), ROList.empty));
          setBegin(editor.context, --index);
          recorder.apply(editor.context, new ChangeArray(visual.value, index, 0, atoms));
          leadFirst = true;
          setRange(editor.context, index, index + atoms.size() - 1);
        });
  }

  public void editPaste(Editor editor) {
    editor.context.uncopy(
        visual.value.back().elementAtomType(),
        atoms -> {
          if (atoms.isEmpty()) return;
          editor.history.record(
              editor.context,
              null,
              recorder -> {
                recorder.apply(
                    editor.context,
                    new ChangeArray(visual.value, beginIndex, endIndex - beginIndex + 1, atoms));
              });
        });
  }

  public void editSuffix(Editor editor) {
    editor.history.record(
        editor.context,
        null,
        recorder -> {
          final Atom gap = editor.createEmptyGap(editor.context.syntax.suffixGap);
          TSList<Atom> transplant = visual.value.data.sublist(beginIndex, endIndex + 1).mut();
          recorder.apply(
              editor.context,
              new ChangeArray(visual.value, beginIndex, transplant.size(), TSList.of(gap)));
          recorder.apply(
              editor.context,
              new ChangeArray(
                  (FieldArray) gap.fields.get(SuffixGapAtomType.PRECEDING_KEY), 0, 0, transplant));
          gap.fields.getOpt("gap").selectInto(editor.context);
        });
  }
}