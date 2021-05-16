package com.zarbosoft.merman.editorcore;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.syntax.FreeAtomType;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.core.visual.visuals.CursorFieldPrimitive;
import com.zarbosoft.merman.core.visual.visuals.VisualFrontPrimitive;
import com.zarbosoft.merman.editorcore.cursors.BaseEditCursorFieldPrimitive;
import com.zarbosoft.merman.editorcore.helper.FrontMarkBuilder;
import com.zarbosoft.merman.editorcore.helper.GroupBuilder;
import com.zarbosoft.merman.editorcore.helper.Helper;
import com.zarbosoft.merman.editorcore.helper.SyntaxBuilder;
import com.zarbosoft.merman.editorcore.helper.TreeBuilder;
import com.zarbosoft.merman.editorcore.helper.TypeBuilder;
import org.junit.Test;

import static com.zarbosoft.merman.editorcore.helper.Helper.assertTreeEqual;
import static com.zarbosoft.merman.editorcore.helper.Helper.buildDoc;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class TestActionsPrimitive {
  public static Editor buildFive() {
    return build("12345");
  }

  public static Editor build(final String string) {
    FreeAtomType quoted =
        new TypeBuilder("quoted")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("value")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(true)
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(quoted)
            .group("any", new GroupBuilder().type(quoted).build())
            .build();
    final Editor editor = buildDoc(syntax, new TreeBuilder(quoted).add("value", string).build());
    Helper.rootArray(editor.context.document)
        .data
        .get(0)
        .fields
        .getOpt("value")
        .selectInto(editor.context);
    return editor;
  }

  public static VisualFrontPrimitive visual(final Context context) {
    return (VisualFrontPrimitive) context.cursor.getVisual();
  }

  public static void assertSelection(final Context context, final int begin, final int end) {
    final CursorFieldPrimitive selection = (CursorFieldPrimitive) context.cursor;
    assertThat(selection.range.beginOffset, equalTo(begin));
    assertThat(selection.range.endOffset, equalTo(end));
  }

  @Test
  public void testSplitSingleStart() {
    final Editor editor = build("ab");
    visual(editor.context).select(editor.context, true, 0, 0);
    ((BaseEditCursorFieldPrimitive) editor.context.cursor).editSplitLines(editor);
    assertText(editor, "\nab");
    assertSelection(editor.context, 1, 1);
  }

  @Test
  public void testSplitSingleMid() {
    final Editor editor = build("ab");
    visual(editor.context).select(editor.context, true, 1, 1);
    ((BaseEditCursorFieldPrimitive) editor.context.cursor).editSplitLines(editor);
    assertText(editor, "a\nb");
    assertSelection(editor.context, 2, 2);
  }

  @Test
  public void testSplitSingleEnd() {
    final Editor editor = build("ab");
    visual(editor.context).select(editor.context, true, 2, 2);
    ((BaseEditCursorFieldPrimitive) editor.context.cursor).editSplitLines(editor);
    assertText(editor, "ab\n");
    assertSelection(editor.context, 3, 3);
  }

  @Test
  public void testSplitMultipleStart() {
    final Editor editor = build("1\nab\n2");
    visual(editor.context).select(editor.context, true, 2, 2);
    ((BaseEditCursorFieldPrimitive) editor.context.cursor).editSplitLines(editor);
    assertText(editor, "1\n\nab\n2");
    assertSelection(editor.context, 3, 3);
  }

  @Test
  public void testSplitMultipleMid() {
    final Editor editor = build("1\nab\n2");
    visual(editor.context).select(editor.context, true, 3, 3);
    ((BaseEditCursorFieldPrimitive) editor.context.cursor).editSplitLines(editor);
    assertText(editor, "1\na\nb\n2");
    assertSelection(editor.context, 4, 4);
  }

  @Test
  public void testSplitMultipleEnd() {
    final Editor editor = build("1\nab\n2");
    visual(editor.context).select(editor.context, true, 4, 4);
    ((BaseEditCursorFieldPrimitive) editor.context.cursor).editSplitLines(editor);
    assertText(editor, "1\nab\n\n2");
    assertSelection(editor.context, 5, 5);
  }

  @Test
  public void testSplitEmpty() {
    final Editor editor = build("");
    visual(editor.context).select(editor.context, true, 0, 0);
    ((BaseEditCursorFieldPrimitive) editor.context.cursor).editSplitLines(editor);
    assertText(editor, "\n");
    assertSelection(editor.context, 1, 1);
  }

  @Test
  public void testSplitRange() {
    final Editor editor = build("abcd");
    visual(editor.context).select(editor.context, true, 1, 3);
    ((BaseEditCursorFieldPrimitive) editor.context.cursor).editSplitLines(editor);
    assertText(editor, "a\nd");
    assertSelection(editor.context, 2, 2);
  }

  @Test
  public void testJoinEmpty() {
    final Editor editor = build("");
    visual(editor.context).select(editor.context, true, 0, 0);
    ((BaseEditCursorFieldPrimitive) editor.context.cursor).editJoinLines(editor);
    assertSelection(editor.context, 0, 0);
  }

  @Test
  public void testJoinMinimal() {
    final Editor editor = build("\n");
    visual(editor.context).select(editor.context, true, 0, 0);
    ((BaseEditCursorFieldPrimitive) editor.context.cursor).editJoinLines(editor);
    assertText(editor, "");
    assertSelection(editor.context, 0, 0);
  }

  @Test
  public void testJoin() {
    final Editor editor = build("a\nb");
    visual(editor.context).select(editor.context, true, 0, 0);
    ((BaseEditCursorFieldPrimitive) editor.context.cursor).editJoinLines(editor);
    assertText(editor, "ab");
    assertSelection(editor.context, 1, 1);
  }

  @Test
  public void testJoinRange() {
    final Editor editor = build("ab\nc\nde");
    visual(editor.context).select(editor.context, true, 1, 6);
    ((BaseEditCursorFieldPrimitive) editor.context.cursor).editJoinLines(editor);
    assertText(editor, "abcde");
    assertSelection(editor.context, 1, 4);
  }

  @Test
  public void testDeletePrevious() {
    final Editor editor = buildFive();
    visual(editor.context).select(editor.context, true, 2, 2);
    ((BaseEditCursorFieldPrimitive) editor.context.cursor).editDeletePrevious(editor);
    assertText(editor, "1345");
    assertSelection(editor.context, 1, 1);
  }

  @Test
  public void testDeleteBOL() {
    final Editor editor = build("a\nb");
    visual(editor.context).select(editor.context, true, 2, 2);
    ((BaseEditCursorFieldPrimitive) editor.context.cursor).editDeletePrevious(editor);
    assertText(editor, "ab");
    assertSelection(editor.context, 1, 1);
  }

  @Test
  public void testDeleteBOF() {
    final Editor editor = build("a");
    visual(editor.context).select(editor.context, true, 0, 0);
    ((BaseEditCursorFieldPrimitive) editor.context.cursor).editDeletePrevious(editor);
    assertText(editor, "a");
    assertSelection(editor.context, 0, 0);
  }

  @Test
  public void testDeletePreviousRange() {
    final Editor editor = buildFive();
    visual(editor.context).select(editor.context, true, 1, 2);
    ((BaseEditCursorFieldPrimitive) editor.context.cursor).editDeletePrevious(editor);
    assertText(editor, "1345");
    assertSelection(editor.context, 1, 1);
  }

  @Test
  public void testDeletePreviousRangeLines() {
    final Editor editor = build("ab\ncd");
    visual(editor.context).select(editor.context, true, 1, 4);
    ((BaseEditCursorFieldPrimitive) editor.context.cursor).editDeletePrevious(editor);
    assertText(editor, "ad");
    assertSelection(editor.context, 1, 1);
  }

  @Test
  public void testDeleteNext() {
    final Editor editor = buildFive();
    visual(editor.context).select(editor.context, true, 2, 2);
    ((BaseEditCursorFieldPrimitive) editor.context.cursor).editDeleteNext(editor);
    assertText(editor, "1245");
    assertSelection(editor.context, 2, 2);
  }

  @Test
  public void testDeleteEOL() {
    final Editor editor = build("a\nb");
    visual(editor.context).select(editor.context, true, 1, 1);
    ((BaseEditCursorFieldPrimitive) editor.context.cursor).editDeleteNext(editor);
    assertText(editor, "ab");
    assertSelection(editor.context, 1, 1);
  }

  @Test
  public void testDeleteEOF() {
    final Editor editor = build("a");
    visual(editor.context).select(editor.context, true, 1, 1);
    ((BaseEditCursorFieldPrimitive) editor.context.cursor).editDeleteNext(editor);
    assertText(editor, "a");
    assertSelection(editor.context, 1, 1);
  }

  @Test
  public void testDeleteNextRange() {
    final Editor editor = buildFive();
    visual(editor.context).select(editor.context, true, 1, 2);
    ((BaseEditCursorFieldPrimitive) editor.context.cursor).editDeleteNext(editor);
    assertText(editor, "1345");
    assertSelection(editor.context, 1, 1);
  }

  @Test
  public void testDeleteNextRangeLines() {
    final Editor editor = build("ab\ncd");
    visual(editor.context).select(editor.context, true, 1, 4);
    ((BaseEditCursorFieldPrimitive) editor.context.cursor).editDeleteNext(editor);
    assertText(editor, "ad");
    assertSelection(editor.context, 1, 1);
  }

  @Test
  public void testDeleteNextLongRangeLines() {
    final Editor editor = build("ab\ncognate\nefg");
    visual(editor.context).select(editor.context, true, 1, 13);
    ((BaseEditCursorFieldPrimitive) editor.context.cursor).editDeleteNext(editor);
    assertText(editor, "ag");
    assertSelection(editor.context, 1, 1);
  }

  @Test
  public void testCopyPasteSingle() {
    final Editor editor = buildFive();
    visual(editor.context).select(editor.context, true, 1, 3);
    ((BaseEditCursorFieldPrimitive) editor.context.cursor).actionCopy(editor.context);
    visual(editor.context).select(editor.context, true, 4, 4);
    ((BaseEditCursorFieldPrimitive) editor.context.cursor).editPaste(editor);
    assertText(editor, "1234235");
    assertSelection(editor.context, 6, 6);
  }

  @Test
  public void testCopyPasteRange() {
    final Editor editor = buildFive();
    visual(editor.context).select(editor.context, true, 1, 3);
    ((BaseEditCursorFieldPrimitive) editor.context.cursor).actionCopy(editor.context);
    visual(editor.context).select(editor.context, true, 4, 5);
    ((BaseEditCursorFieldPrimitive) editor.context.cursor).editPaste(editor);
    assertText(editor, "123423");
    assertSelection(editor.context, 6, 6);
  }

  @Test
  public void testCutPaste() {
    final Editor editor = buildFive();
    visual(editor.context).select(editor.context, true, 1, 3);
    ((BaseEditCursorFieldPrimitive) editor.context.cursor).editCut(editor);
    assertSelection(editor.context, 1, 1);
    visual(editor.context).select(editor.context, true, 2, 3);
    ((BaseEditCursorFieldPrimitive) editor.context.cursor).editPaste(editor);
    assertText(editor, "1423");
    assertSelection(editor.context, 4, 4);
  }

  public void assertText(Editor editor, String s) {
    assertTreeEqual(
        editor.context,
        new TreeBuilder(editor.context.syntax.splayedTypes.get("quoted").iterator().next())
            .add("value", s)
            .build(),
        Helper.rootArray(editor.context.document));
  }
}
