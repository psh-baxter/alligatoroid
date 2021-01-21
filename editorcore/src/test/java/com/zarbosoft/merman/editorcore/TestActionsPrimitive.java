package com.zarbosoft.merman.editorcore;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.visual.visuals.VisualFrontArray;
import com.zarbosoft.merman.editor.visual.visuals.VisualFrontPrimitive;
import com.zarbosoft.merman.editorcore.helper.GeneralTestWizard;
import com.zarbosoft.merman.editorcore.helper.Helper;
import com.zarbosoft.merman.editorcore.helper.TreeBuilder;
import org.junit.Test;

import static com.zarbosoft.merman.editorcore.helper.Helper.assertTreeEqual;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class TestActionsPrimitive {
  @Test
  public void testExit() {
    final Context context = buildFive();
    assertThat(context.cursor.getVisual(), instanceOf(VisualFrontPrimitive.class));
    Helper.act(context, "exit");
    assertNotNull(((VisualFrontArray) Helper.rootArray(context.document).visual).selection);
  }

  public static Context buildFive() {
    return build("12345");
  }

  public static Context build(final String string) {
    final Context context =
        buildDoc(
            MiscSyntax.syntax, new TreeBuilder(MiscSyntax.quoted).add("value", string).build());
    Helper.rootArray(context.document).data.get(0).fields.getOpt("value").selectInto(context);
    return context;
  }

  public static VisualFrontPrimitive visual(final Context context) {
    return (VisualFrontPrimitive) context.cursor.getVisual();
  }

  public static void assertSelection(final Context context, final int begin, final int end) {
    final VisualFrontPrimitive.PrimitiveCursor selection =
        (VisualFrontPrimitive.PrimitiveCursor) context.cursor;
    assertThat(selection.range.beginOffset, equalTo(begin));
    assertThat(selection.range.endOffset, equalTo(end));
  }

  @Test
  public void testSplitSingleStart() {
    final Context context = build("ab");
    visual(context).select(context, true, 0, 0);
    Helper.act(context, "split");
    assertTreeEqual(
        context,
        new TreeBuilder(MiscSyntax.quoted).add("value", "\nab").build(),
        Helper.rootArray(context.document));
    assertSelection(context, 1, 1);
  }

  @Test
  public void testSplitSingleMid() {
    final Context context = build("ab");
    visual(context).select(context, true, 1, 1);
    Helper.act(context, "split");
    assertTreeEqual(
        context,
        new TreeBuilder(MiscSyntax.quoted).add("value", "a\nb").build(),
        Helper.rootArray(context.document));
    assertSelection(context, 2, 2);
  }

  @Test
  public void testSplitSingleEnd() {
    final Context context = build("ab");
    visual(context).select(context, true, 2, 2);
    Helper.act(context, "split");
    assertTreeEqual(
        context,
        new TreeBuilder(MiscSyntax.quoted).add("value", "ab\n").build(),
        Helper.rootArray(context.document));
    assertSelection(context, 3, 3);
  }

  @Test
  public void testSplitMultipleStart() {
    final Context context = build("1\nab\n2");
    visual(context).select(context, true, 2, 2);
    Helper.act(context, "split");
    assertTreeEqual(
        context,
        new TreeBuilder(MiscSyntax.quoted).add("value", "1\n\nab\n2").build(),
        Helper.rootArray(context.document));
    assertSelection(context, 3, 3);
  }

  @Test
  public void testSplitMultipleMid() {
    final Context context = build("1\nab\n2");
    visual(context).select(context, true, 3, 3);
    Helper.act(context, "split");
    assertTreeEqual(
        context,
        new TreeBuilder(MiscSyntax.quoted).add("value", "1\na\nb\n2").build(),
        Helper.rootArray(context.document));
    assertSelection(context, 4, 4);
  }

  @Test
  public void testSplitMultipleEnd() {
    final Context context = build("1\nab\n2");
    visual(context).select(context, true, 4, 4);
    Helper.act(context, "split");
    assertTreeEqual(
        context,
        new TreeBuilder(MiscSyntax.quoted).add("value", "1\nab\n\n2").build(),
        Helper.rootArray(context.document));
    assertSelection(context, 5, 5);
  }

  @Test
  public void testSplitEmpty() {
    final Context context = build("");
    visual(context).select(context, true, 0, 0);
    Helper.act(context, "split");
    assertTreeEqual(
        context,
        new TreeBuilder(MiscSyntax.quoted).add("value", "\n").build(),
        Helper.rootArray(context.document));
    assertSelection(context, 1, 1);
  }

  @Test
  public void testSplitRange() {
    final Context context = build("abcd");
    visual(context).select(context, true, 1, 3);
    Helper.act(context, "split");
    assertTreeEqual(
        context,
        new TreeBuilder(MiscSyntax.quoted).add("value", "a\nd").build(),
        Helper.rootArray(context.document));
    assertSelection(context, 2, 2);
  }

  @Test
  public void testJoinEmpty() {
    final Context context = build("");
    visual(context).select(context, true, 0, 0);
    Helper.act(context, "join");
    assertSelection(context, 0, 0);
  }

  @Test
  public void testJoinMinimal() {
    final Context context = build("\n");
    visual(context).select(context, true, 0, 0);
    Helper.act(context, "join");
    assertTreeEqual(
        context,
        new TreeBuilder(MiscSyntax.quoted).add("value", "").build(),
        Helper.rootArray(context.document));
    assertSelection(context, 0, 0);
  }

  @Test
  public void testJoin() {
    final Context context = build("a\nb");
    visual(context).select(context, true, 0, 0);
    Helper.act(context, "join");
    assertTreeEqual(
        context,
        new TreeBuilder(MiscSyntax.quoted).add("value", "ab").build(),
        Helper.rootArray(context.document));
    assertSelection(context, 1, 1);
  }

  @Test
  public void testJoinRange() {
    final Context context = build("ab\nc\nde");
    visual(context).select(context, true, 1, 6);
    Helper.act(context, "join");
    assertTreeEqual(
        context,
        new TreeBuilder(MiscSyntax.quoted).add("value", "abcde").build(),
        Helper.rootArray(context.document));
    assertSelection(context, 1, 4);
  }

  @Test
  public void testDeletePrevious() {
    final Context context = buildFive();
    visual(context).select(context, true, 2, 2);
    Helper.act(context, "delete_previous");
    assertTreeEqual(
        context,
        new TreeBuilder(MiscSyntax.quoted).add("value", "1345").build(),
        Helper.rootArray(context.document));
    assertSelection(context, 1, 1);
  }

  @Test
  public void testDeleteBOL() {
    final Context context = build("a\nb");
    visual(context).select(context, true, 2, 2);
    Helper.act(context, "delete_previous");
    assertTreeEqual(
        context,
        new TreeBuilder(MiscSyntax.quoted).add("value", "ab").build(),
        Helper.rootArray(context.document));
    assertSelection(context, 1, 1);
  }

  @Test
  public void testDeleteBOF() {
    final Context context = build("a");
    visual(context).select(context, true, 0, 0);
    Helper.act(context, "delete_previous");
    assertTreeEqual(
        context,
        new TreeBuilder(MiscSyntax.quoted).add("value", "a").build(),
        Helper.rootArray(context.document));
    assertSelection(context, 0, 0);
  }

  @Test
  public void testDeletePreviousRange() {
    final Context context = buildFive();
    visual(context).select(context, true, 1, 2);
    Helper.act(context, "delete_previous");
    assertTreeEqual(
        context,
        new TreeBuilder(MiscSyntax.quoted).add("value", "1345").build(),
        Helper.rootArray(context.document));
    assertSelection(context, 1, 1);
  }

  @Test
  public void testDeletePreviousRangeLines() {
    final Context context = build("ab\ncd");
    visual(context).select(context, true, 1, 4);
    Helper.act(context, "delete_previous");
    assertTreeEqual(
        context,
        new TreeBuilder(MiscSyntax.quoted).add("value", "ad").build(),
        Helper.rootArray(context.document));
    assertSelection(context, 1, 1);
  }

  @Test
  public void testDeleteNext() {
    final Context context = buildFive();
    visual(context).select(context, true, 2, 2);
    Helper.act(context, "delete_next");
    assertTreeEqual(
        context,
        new TreeBuilder(MiscSyntax.quoted).add("value", "1245").build(),
        Helper.rootArray(context.document));
    assertSelection(context, 2, 2);
  }

  @Test
  public void testDeleteEOL() {
    final Context context = build("a\nb");
    visual(context).select(context, true, 1, 1);
    Helper.act(context, "delete_next");
    assertTreeEqual(
        context,
        new TreeBuilder(MiscSyntax.quoted).add("value", "ab").build(),
        Helper.rootArray(context.document));
    assertSelection(context, 1, 1);
  }

  @Test
  public void testDeleteEOF() {
    final Context context = build("a");
    visual(context).select(context, true, 1, 1);
    Helper.act(context, "delete_next");
    assertTreeEqual(
        context,
        new TreeBuilder(MiscSyntax.quoted).add("value", "a").build(),
        Helper.rootArray(context.document));
    assertSelection(context, 1, 1);
  }

  @Test
  public void testDeleteNextRange() {
    final Context context = buildFive();
    visual(context).select(context, true, 1, 2);
    Helper.act(context, "delete_next");
    assertTreeEqual(
        context,
        new TreeBuilder(MiscSyntax.quoted).add("value", "1345").build(),
        Helper.rootArray(context.document));
    assertSelection(context, 1, 1);
  }

  @Test
  public void testDeleteNextRangeLines() {
    final Context context = build("ab\ncd");
    visual(context).select(context, true, 1, 4);
    Helper.act(context, "delete_next");
    assertTreeEqual(
        context,
        new TreeBuilder(MiscSyntax.quoted).add("value", "ad").build(),
        Helper.rootArray(context.document));
    assertSelection(context, 1, 1);
  }

  @Test
  public void testDeleteNextLongRangeLines() {
    new GeneralTestWizard(
            MiscSyntax.syntax,
             new TreeBuilder(MiscSyntax.quoted).add("value", "ab\ncognate\nefg").build())
        .run(
            context ->
                Helper.rootArray(context.document)
                    .data
                    .get(0)
                    .fields
                    .getOpt("value")
                    .selectInto(context))
        .run(context -> visual(context).select(context, true, 1, 13))
        .act("delete_next")
        .checkTextBrick(0, 1, "ag")
        .checkCourseCount(1)
        .checkArrayTree(new TreeBuilder(MiscSyntax.quoted).add("value", "ag").build())
        .run(context -> assertSelection(context, 1, 1));
  }

  @Test
  public void testCopyPasteSingle() {
    final Context context = buildFive();
    visual(context).select(context, true, 1, 3);
    Helper.act(context, "copy");
    visual(context).select(context, true, 4, 4);
    Helper.act(context, "paste");
    assertTreeEqual(
        context,
        new TreeBuilder(MiscSyntax.quoted).add("value", "1234235").build(),
        Helper.rootArray(context.document));
    assertSelection(context, 6, 6);
  }

  @Test
  public void testCopyPasteRange() {
    final Context context = buildFive();
    visual(context).select(context, true, 1, 3);
    Helper.act(context, "copy");
    visual(context).select(context, true, 4, 5);
    Helper.act(context, "paste");
    assertTreeEqual(
        context,
        new TreeBuilder(MiscSyntax.quoted).add("value", "123423").build(),
        Helper.rootArray(context.document));
    assertSelection(context, 6, 6);
  }

  @Test
  public void testCutPaste() {
    final Context context = buildFive();
    visual(context).select(context, true, 1, 3);
    Helper.act(context, "cut");
    assertSelection(context, 1, 1);
    visual(context).select(context, true, 2, 3);
    Helper.act(context, "paste");
    assertTreeEqual(
        context,
        new TreeBuilder(MiscSyntax.quoted).add("value", "1423").build(),
        Helper.rootArray(context.document));
    assertSelection(context, 4, 4);
  }
}
