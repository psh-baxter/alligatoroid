package com.zarbosoft.merman;

import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.document.values.FieldPrimitive;
import com.zarbosoft.merman.core.editor.Context;
import com.zarbosoft.merman.core.editor.Path;
import com.zarbosoft.merman.core.editor.visual.visuals.VisualFrontArray;
import com.zarbosoft.merman.core.editor.visual.visuals.VisualFrontPrimitive;
import com.zarbosoft.merman.helper.GeneralTestWizard;
import com.zarbosoft.merman.helper.Helper;
import com.zarbosoft.merman.helper.MiscSyntax;
import com.zarbosoft.merman.helper.TreeBuilder;
import org.junit.Test;

import static com.zarbosoft.merman.helper.Helper.buildDoc;
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

  @Test
  public void testNext() {
    final Atom target =
        new TreeBuilder(MiscSyntax.doubleQuoted).add("first", "").add("second", "").build();
    final FieldPrimitive value = (FieldPrimitive) target.fields.getOpt("first");
    new GeneralTestWizard(MiscSyntax.syntax,  target)
        .run(context -> value.selectInto(context))
        .act("next")
        .run(
            context ->
                assertThat(
                    context.cursor.getSyntaxPath(),
                    equalTo(new Path("value", "0", "second", "0"))));
  }

  @Test
  public void testPrevious() {
    final Atom target =
        new TreeBuilder(MiscSyntax.doubleQuoted).add("first", "").add("second", "").build();
    final FieldPrimitive value = (FieldPrimitive) target.fields.getOpt("second");
    new GeneralTestWizard(MiscSyntax.syntax,  target)
        .run(context -> value.selectInto(context))
        .act("previous")
        .run(
            context ->
                assertThat(
                    context.cursor.getSyntaxPath(),
                    equalTo(new Path("value", "0", "first", "0"))));
  }

  @Test
  public void testNextElement() {
    final Context context = buildFive();
    visual(context).select(context, true, 2, 2);
    Helper.act(context, "next_element");
    assertSelection(context, 3, 3);
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
  public void testNextEOL() {
    final Context context = build("1\n2");
    visual(context).select(context, true, 1, 1);
    Helper.act(context, "next_element");
    assertSelection(context, 2, 2);
  }

  @Test
  public void testNextDeselect() {
    final Context context = buildFive();
    visual(context).select(context, true, 1, 2);
    Helper.act(context, "next_element");
    assertSelection(context, 3, 3);
  }

  @Test
  public void testNextEnd() {
    final Context context = buildFive();
    visual(context).select(context, true, 5, 5);
    Helper.act(context, "next_element");
    assertSelection(context, 5, 5);
  }

  @Test
  public void testNextDeselectEnd() {
    final Context context = buildFive();
    visual(context).select(context, true, 4, 5);
    Helper.act(context, "next_element");
    assertSelection(context, 5, 5);
  }

  @Test
  public void testPreviousElement() {
    final Context context = buildFive();
    visual(context).select(context, true, 2, 2);
    Helper.act(context, "previous_element");
    assertSelection(context, 1, 1);
  }

  @Test
  public void testPreviousBOL() {
    final Context context = build("a\n2");
    visual(context).select(context, true, 2, 2);
    Helper.act(context, "previous_element");
    assertSelection(context, 1, 1);
  }

  @Test
  public void testPreviousDeselect() {
    final Context context = buildFive();
    visual(context).select(context, true, 2, 3);
    Helper.act(context, "previous_element");
    assertSelection(context, 1, 1);
  }

  @Test
  public void testPreviousStart() {
    final Context context = buildFive();
    visual(context).select(context, true, 0, 0);
    Helper.act(context, "previous_element");
    assertSelection(context, 0, 0);
  }

  @Test
  public void testPreviousDeselectStart() {
    final Context context = buildFive();
    visual(context).select(context, true, 0, 1);
    Helper.act(context, "previous_element");
    assertSelection(context, 0, 0);
  }

  @Test
  public void testNextLineLast() {
    final Context context = build("12");
    visual(context).select(context, true, 1, 1);
    Helper.act(context, "next_line");
    assertSelection(context, 2, 2);
  }

  @Test
  public void testNextLineStart() {
    final Context context = build("12\n34");
    visual(context).select(context, true, 0, 0);
    Helper.act(context, "next_line");
    assertSelection(context, 3, 3);
  }

  @Test
  public void testNextLineMid() {
    final Context context = build("12\n34");
    visual(context).select(context, true, 1, 1);
    Helper.act(context, "next_line");
    assertSelection(context, 4, 4);
  }

  @Test
  public void testNextLineEnd() {
    final Context context = build("12\n34");
    visual(context).select(context, true, 2, 2);
    Helper.act(context, "next_line");
    assertSelection(context, 5, 5);
  }

  @Test
  public void testNextLineLimit() {
    final Context context = build("12\n3");
    visual(context).select(context, true, 2, 2);
    Helper.act(context, "next_line");
    assertSelection(context, 4, 4);
  }

  @Test
  public void testPreviousLineFirst() {
    final Context context = build("12");
    visual(context).select(context, true, 1, 1);
    Helper.act(context, "previous_line");
    assertSelection(context, 0, 0);
  }

  @Test
  public void testPreviousLineStart() {
    final Context context = build("12\n34");
    visual(context).select(context, true, 3, 3);
    Helper.act(context, "previous_line");
    assertSelection(context, 0, 0);
  }

  @Test
  public void testPreviousLineMid() {
    final Context context = build("12\n34");
    visual(context).select(context, true, 4, 4);
    Helper.act(context, "previous_line");
    assertSelection(context, 1, 1);
  }

  @Test
  public void testPreviousLineEnd() {
    final Context context = build("12\n34");
    visual(context).select(context, true, 5, 5);
    Helper.act(context, "previous_line");
    assertSelection(context, 2, 2);
  }

  @Test
  public void testPreviousLineLimit() {
    final Context context = build("1\n34");
    visual(context).select(context, true, 4, 4);
    Helper.act(context, "previous_line");
    assertSelection(context, 1, 1);
  }

  @Test
  public void testLineBegin() {
    final Context context = build("01\n23\n45");
    visual(context).select(context, true, 4, 4);
    Helper.act(context, "line_begin");
    assertSelection(context, 3, 3);
  }

  @Test
  public void testLineEnd() {
    final Context context = build("01\n23\n45");
    visual(context).select(context, true, 4, 4);
    Helper.act(context, "line_end");
    assertSelection(context, 5, 5);
  }

  @Test
  public void testLastLineEnd() {
    final Context context = build("01");
    visual(context).select(context, true, 1, 1);
    Helper.act(context, "line_end");
    assertSelection(context, 2, 2);
  }

  @Test
  public void testFirstLineBegin() {
    final Context context = build("01");
    visual(context).select(context, true, 1, 1);
    Helper.act(context, "line_begin");
    assertSelection(context, 0, 0);
  }

  @Test
  public void testNextWordMid() {
    final Context context = build("the dog");
    visual(context).select(context, true, 1, 1);
    Helper.act(context, "next_word");
    assertSelection(context, 3, 3);
  }

  @Test
  public void testNextWordBoundary() {
    final Context context = build("the dog");
    visual(context).select(context, true, 3, 3);
    Helper.act(context, "next_word");
    assertSelection(context, 4, 4);
  }

  @Test
  public void testPreviousWordMid() {
    final Context context = build("the dog");
    visual(context).select(context, true, 5, 5);
    Helper.act(context, "previous_word");
    assertSelection(context, 4, 4);
  }

  @Test
  public void testPreviousWordBoundary() {
    final Context context = build("the dog");
    visual(context).select(context, true, 4, 4);
    Helper.act(context, "previous_word");
    assertSelection(context, 3, 3);
  }

  @Test
  public void testGatherNext() {
    final Context context = buildFive();
    visual(context).select(context, true, 2, 2);
    Helper.act(context, "gather_next");
    assertSelection(context, 2, 3);
  }

  @Test
  public void testGatherNextEnd() {
    final Context context = buildFive();
    visual(context).select(context, true, 5, 5);
    Helper.act(context, "gather_next");
    assertSelection(context, 5, 5);
  }

  @Test
  public void testGatherNextNewline() {
    final Atom primitiveAtom = new TreeBuilder(MiscSyntax.quoted).add("value", "abc\n123").build();
    new GeneralTestWizard(MiscSyntax.syntax,  primitiveAtom)
        .run(
            context ->
                ((FieldPrimitive) primitiveAtom.fields.getOpt("value"))
                    .visual.select(context, false, 2, 2))
        .act("gather_next")
        .act("gather_next")
        .run(context -> assertSelection(context, 2, 4));
  }

  @Test
  public void testGatherNextNewlineShorter() {
    final Atom primitiveAtom = new TreeBuilder(MiscSyntax.quoted).add("value", "abc\n1").build();
    new GeneralTestWizard(MiscSyntax.syntax,  primitiveAtom)
        .run(
            context ->
                ((FieldPrimitive) primitiveAtom.fields.getOpt("value"))
                    .visual.select(context, false, 2, 2))
        .act("gather_next")
        .act("gather_next")
        .run(context -> assertSelection(context, 2, 4));
  }

  @Test
  public void testGatherNextWord() {
    final Context context = build("dog hat chair");
    visual(context).select(context, true, 3, 4);
    Helper.act(context, "gather_next_word");
    assertSelection(context, 3, 7);
  }

  @Test
  public void testGatherNextLineEnd() {
    final Context context = build("dog hat\n chair");
    visual(context).select(context, true, 3, 3);
    Helper.act(context, "gather_next_line_end");
    assertSelection(context, 3, 7);
  }

  @Test
  public void testGatherNextLine() {
    final Context context = build("dog hat\n chair");
    visual(context).select(context, true, 3, 3);
    Helper.act(context, "gather_next_line");
    assertSelection(context, 3, 11);
  }

  @Test
  public void testGatherPrevious() {
    final Context context = buildFive();
    visual(context).select(context, true, 2, 2);
    Helper.act(context, "gather_previous");
    assertSelection(context, 1, 2);
  }

  @Test
  public void testGatherPreviousWord() {
    final Context context = build("chair hat pan");
    visual(context).select(context, true, 9, 9);
    Helper.act(context, "gather_previous_word");
    assertSelection(context, 6, 9);
  }

  @Test
  public void testGatherPreviousLineStart() {
    final Context context = build("chair\nhat pan");
    visual(context).select(context, true, 9, 9);
    Helper.act(context, "gather_previous_line_start");
    assertSelection(context, 6, 9);
  }

  @Test
  public void testGatherPreviousNewline() {
    final Context context = build("chair\nhat pan");
    visual(context).select(context, true, 9, 9);
    Helper.act(context, "gather_previous_line");
    assertSelection(context, 3, 9);
  }

  @Test
  public void testGatherPreviousNewlineShorter() {
    final Atom primitiveAtom = new TreeBuilder(MiscSyntax.quoted).add("value", "a\n1234").build();
    new GeneralTestWizard(MiscSyntax.syntax,  primitiveAtom)
        .run(
            context ->
                ((FieldPrimitive) primitiveAtom.fields.getOpt("value"))
                    .visual.select(context, false, 5, 5))
        .act("gather_previous_line")
        .run(context -> assertSelection(context, 1, 5));
  }

  @Test
  public void testGatherPreviousNewlineStart() {
    final Atom primitiveAtom = new TreeBuilder(MiscSyntax.quoted).add("value", "abc\ndef").build();
    new GeneralTestWizard(MiscSyntax.syntax,  primitiveAtom)
        .run(
            context ->
                ((FieldPrimitive) primitiveAtom.fields.getOpt("value"))
                    .visual.select(context, false, 4, 4))
        .act("gather_previous_line")
        .run(context -> assertSelection(context, 0, 4));
  }

  @Test
  public void testGatherPreviousStart() {
    final Context context = buildFive();
    visual(context).select(context, true, 0, 0);
    Helper.act(context, "gather_previous");
    assertSelection(context, 0, 0);
  }

  @Test
  public void testReleaseNext() {
    final Context context = buildFive();
    visual(context).select(context, true, 2, 3);
    Helper.act(context, "release_next");
    assertSelection(context, 2, 2);
  }

  @Test
  public void testReleaseNextMinimum() {
    final Context context = buildFive();
    visual(context).select(context, true, 2, 2);
    Helper.act(context, "release_next");
    assertSelection(context, 2, 2);
  }

  @Test
  public void testReleaseNextWord() {
    final Context context = build("kettle rubarb");
    visual(context).select(context, true, 6, 13);
    Helper.act(context, "release_next_word");
    assertSelection(context, 6, 7);
  }

  @Test
  public void testReleaseNextLineEnd() {
    final Context context = build("one\ntwo three");
    visual(context).select(context, true, 1, 8);
    Helper.act(context, "release_next_line_end");
    assertSelection(context, 1, 4);
  }

  @Test
  public void testReleaseNextLine() {
    final Context context = build("one\ntwo three");
    visual(context).select(context, true, 1, 7);
    Helper.act(context, "release_next_line");
    assertSelection(context, 1, 3);
  }

  @Test
  public void testReleaseNextLineReversed() {
    final Context context = build("one two\nthree");
    visual(context).select(context, true, 6, 9);
    Helper.act(context, "release_next_line");
    assertSelection(context, 6, 6);
  }

  @Test
  public void testReleasePrevious() {
    final Context context = buildFive();
    visual(context).select(context, true, 1, 2);
    Helper.act(context, "release_previous");
    assertSelection(context, 2, 2);
  }

  @Test
  public void testReleasePreviousMinimum() {
    final Context context = buildFive();
    visual(context).select(context, true, 2, 2);
    Helper.act(context, "release_previous");
    assertSelection(context, 2, 2);
  }

  @Test
  public void testReleasePreviousWord() {
    final Context context = build("truck frypan");
    visual(context).select(context, true, 0, 10);
    Helper.act(context, "release_previous_word");
    assertSelection(context, 5, 10);
  }

  @Test
  public void testReleasePreviousLineStart() {
    final Context context = build("no\nyes");
    visual(context).select(context, true, 0, 5);
    Helper.act(context, "release_previous_line_start");
    assertSelection(context, 2, 5);
  }

  @Test
  public void testReleasePreviousLine() {
    final Context context = build("no\nyes");
    visual(context).select(context, true, 0, 5);
    Helper.act(context, "release_previous_line");
    assertSelection(context, 3, 5);
  }

  @Test
  public void testReleasePreviousLineReversed() {
    final Context context = build("no ultimatum\nyes");
    visual(context).select(context, true, 7, 15);
    Helper.act(context, "release_previous_line");
    assertSelection(context, 15, 15);
  }
}
