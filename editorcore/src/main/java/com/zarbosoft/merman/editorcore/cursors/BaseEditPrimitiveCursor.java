package com.zarbosoft.merman.editorcore.cursors;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.document.fields.FieldPrimitive;
import com.zarbosoft.merman.core.visual.visuals.VisualFrontPrimitive;
import com.zarbosoft.merman.editorcore.Editor;
import com.zarbosoft.merman.editorcore.history.History;
import com.zarbosoft.merman.editorcore.history.changes.ChangePrimitive;
import com.zarbosoft.rendaw.common.ROPair;

import java.util.function.Consumer;

public class BaseEditPrimitiveCursor extends VisualFrontPrimitive.Cursor {
  // TODO check pattern against more edits: split, join, newline, paste, etc

  public BaseEditPrimitiveCursor(
      Context context,
      VisualFrontPrimitive visualPrimitive,
      boolean leadFirst,
      int beginOffset,
      int endOffset) {
    super(context, visualPrimitive, leadFirst, beginOffset, endOffset);
  }

  @Override
  public void handleTyping(Context context, String text) {
    editHandleTyping(Editor.get(context), null, text);
  }

  public void editHandleTyping(Editor editor, History.Recorder recorder, String text) {
    FieldPrimitive value = visualPrimitive.value;
    Consumer<History.Recorder> apply =
        recorder1 -> {
          recorder1.apply(
              editor.context,
              new ChangePrimitive(
                  value, range.beginOffset, range.endOffset - range.beginOffset, text));
        };
    if (recorder != null) apply.accept(recorder);
    else editor.history.record(editor.context, new ROPair(visualPrimitive.value, "text"), apply);
  }

  public void editCut(Editor editor) {
    editor.history.record(
        editor.context,
        null,
        recorder -> {
          editor.context.copy(
              visualPrimitive.value.get().substring(range.beginOffset, range.endOffset));
          recorder.apply(
              editor.context,
              new ChangePrimitive(
                  visualPrimitive.value,
                  range.beginOffset,
                  range.endOffset - range.beginOffset,
                  ""));
        });
  }

  public void editDeleteNext(Editor editor) {
    if (range.beginOffset == range.endOffset) {
      if (range.endOffset == visualPrimitive.value.length()) return;
      editor.history.record(
          editor.context,
          new ROPair(visualPrimitive.value, "text"),
          recorder -> {
            final int following = followingStart();
            recorder.apply(
                editor.context,
                new ChangePrimitive(
                    visualPrimitive.value, range.beginOffset, following - range.beginOffset, ""));
          });
    } else
      editor.history.record(
          editor.context,
          new ROPair(visualPrimitive.value, "text"),
          recorder -> {
            recorder.apply(
                editor.context,
                new ChangePrimitive(
                    visualPrimitive.value,
                    range.beginOffset,
                    range.endOffset - range.beginOffset,
                    ""));
          });
  }

  public void editDeletePrevious(Editor editor) {
    if (range.beginOffset == range.endOffset) {
      if (range.beginOffset == 0) return;
      editor.history.record(
          editor.context,
          new ROPair(visualPrimitive.value, "text"),
          recorder -> {
            final int preceding = precedingStart();
            recorder.apply(
                editor.context,
                new ChangePrimitive(
                    visualPrimitive.value, preceding, range.beginOffset - preceding, ""));
          });
    } else
      editor.history.record(
          editor.context,
          new ROPair(visualPrimitive.value, "text"),
          recorder -> {
            recorder.apply(
                editor.context,
                new ChangePrimitive(
                    visualPrimitive.value,
                    range.beginOffset,
                    range.endOffset - range.beginOffset,
                    ""));
          });
  }

  public void editJoinLines(Editor editor) {
    int beginOffset = range.beginOffset;
    int endOffset = range.endOffset;
    VisualFrontPrimitive.Line beginLine = range.beginLine;
    VisualFrontPrimitive.Line endLine = range.endLine;
    if (beginOffset == endOffset) {
      if (beginLine.index + 1 >= visualPrimitive.lines.size()) return;
      editor.history.record(
          editor.context,
          null,
          recorder -> {
            final int select = endLine.offset + endLine.text.length();
            recorder.apply(
                editor.context,
                new ChangePrimitive(
                    visualPrimitive.value,
                    visualPrimitive.lines.get(beginLine.index + 1).offset - 1,
                    1,
                    ""));
            visualPrimitive.select(editor.context, true, select, select);
          });
    } else {
      if (beginLine == endLine) return;
      editor.history.record(
          editor.context,
          null,
          recorder -> {
            final StringBuilder replace = new StringBuilder();
            replace.append(beginLine.text.substring(beginOffset - beginLine.offset));
            final int selectBegin = beginOffset;
            int selectEnd = endOffset - 1;
            for (int index = beginLine.index + 1; index <= endLine.index - 1; ++index) {
              replace.append(visualPrimitive.lines.get(index).text);
              selectEnd -= 1;
            }
            replace.append(endLine.text.substring(0, endOffset - endLine.offset));
            recorder.apply(
                editor.context,
                new ChangePrimitive(
                    visualPrimitive.value,
                    beginOffset,
                    endOffset - beginOffset,
                    replace.toString()));
            visualPrimitive.select(editor.context, true, selectBegin, selectEnd);
          });
    }
  }

  public void editPaste(Editor editor) {
    editor.context.uncopyString(
        text ->
            editor.history.record(
                editor.context,
                null,
                recorder -> {
                  if (text == null) return;
                  FieldPrimitive value = visualPrimitive.value;
                  recorder.apply(
                      editor.context,
                      new ChangePrimitive(
                          value, range.beginOffset, range.endOffset - range.beginOffset, text));
                }));
  }

  public void editSplitLines(Editor editor) {
    editor.history.record(
        editor.context,
        null,
        recorder -> {
          recorder.apply(
              editor.context,
              new ChangePrimitive(
                  visualPrimitive.value,
                  range.beginOffset,
                  range.endOffset - range.beginOffset,
                  "\n"));
        });
  }
}
