package com.zarbosoft.merman.editorcore.cursors;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.document.fields.FieldArray;
import com.zarbosoft.merman.core.syntax.RootAtomType;
import com.zarbosoft.merman.core.syntax.SuffixGapAtomType;
import com.zarbosoft.merman.core.visual.visuals.CursorFieldArray;
import com.zarbosoft.merman.core.visual.visuals.VisualFieldArray;
import com.zarbosoft.merman.editorcore.Editor;
import com.zarbosoft.merman.editorcore.history.History;
import com.zarbosoft.merman.editorcore.history.changes.ChangeArray;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSList;

public class EditCursorFieldArray extends CursorFieldArray {
  public EditCursorFieldArray(
      Context context, VisualFieldArray visual, boolean leadFirst, int start, int end) {
    super(context, visual, leadFirst, start, end);
  }

  public void editCut(Editor editor) {
    visual.value.back().copy(editor.context, visual.value.data.sublist(beginIndex, endIndex + 1));
    editor.history.record(
        editor.context,
        null,
        recorder -> {
          editDeleteInner(editor, recorder);
        });
  }

  private void editDeleteInner(Editor editor, History.Recorder recorder) {
    ROList<Atom> replacement = ROList.empty;
    if (visual.value.atomParentRef.atom().type instanceof RootAtomType
        && visual.value.data.size() == endIndex - beginIndex + 1)
      replacement = TSList.of(Editor.createEmptyGap(editor.context.syntax.gap));
    recorder.apply(
        editor.context,
        new ChangeArray(visual.value, beginIndex, endIndex - beginIndex + 1, replacement));
  }

  public void editDelete(Editor editor) {
    editor.history.record(
        editor.context,
        new ROPair(visual.value, "delete"),
        recorder -> {
          editDeleteInner(editor, recorder);
        });
  }

  public static void editInsertAfter(Editor editor, FieldArray field, int index) {
      Atom[] created = new Atom[1];
      editor.history.record(
              editor.context,
              new ROPair(field, "insert_after"),
              recorder -> {
                  created[0] = editor.arrayInsertNewDefault(recorder, field, index + 1);
              });
      created[0].selectInto(editor.context);
  }

  public void editInsertAfter(Editor editor) {
      editInsertAfter(editor, visual.value, endIndex);
  }

    public void editInsertBefore(Editor editor) {
        editInsertBefore(editor, visual.value, beginIndex);
    }
    public static void editInsertBefore(Editor editor, FieldArray field, int index) {
        Atom[] created = new Atom[1];
        editor.history.record(
                editor.context,
                new ROPair(field, "insert_before"),
                recorder -> {
                    created[0] = editor.arrayInsertNewDefault(recorder, field, index);
                });
        created[0].selectInto(editor.context);
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
    visual
        .value
        .back()
        .uncopy(
            editor.context,
            atoms -> {
              if (atoms.isEmpty()) return;
              editor.history.record(
                  editor.context,
                  null,
                  recorder -> {
                    recorder.apply(
                        editor.context,
                        new ChangeArray(
                            visual.value, beginIndex, endIndex - beginIndex + 1, atoms));
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
