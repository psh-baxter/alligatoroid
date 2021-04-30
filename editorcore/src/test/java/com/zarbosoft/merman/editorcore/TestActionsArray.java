package com.zarbosoft.merman.editorcore;

import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.document.fields.FieldArray;
import com.zarbosoft.merman.core.syntax.FreeAtomType;
import com.zarbosoft.merman.core.syntax.SuffixGapAtomType;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.core.visual.visuals.VisualFrontArray;
import com.zarbosoft.merman.editorcore.helper.FrontDataArrayBuilder;
import com.zarbosoft.merman.editorcore.helper.FrontMarkBuilder;
import com.zarbosoft.merman.editorcore.helper.GeneralTestWizard;
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

public class TestActionsArray {
  private static final Syntax syntax;
  private static final FreeAtomType one;
  private static final FreeAtomType two;
  private static final FreeAtomType three;
  private static final FreeAtomType four;
  private static final FreeAtomType five;
  private static final FreeAtomType array;

  static {
    one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .autoComplete(false)
            .build();
    two =
        new TypeBuilder("two")
            .back(Helper.buildBackPrimitive("two"))
            .front(new FrontMarkBuilder("two").build())
            .autoComplete(false)
            .build();
    three =
        new TypeBuilder("three")
            .back(Helper.buildBackPrimitive("three"))
            .front(new FrontMarkBuilder("three").build())
            .autoComplete(false)
            .build();
    four =
        new TypeBuilder("four")
            .back(Helper.buildBackPrimitive("four"))
            .front(new FrontMarkBuilder("four").build())
            .autoComplete(false)
            .build();
    five =
        new TypeBuilder("five")
            .back(Helper.buildBackPrimitive("five"))
            .front(new FrontMarkBuilder("five").build())
            .autoComplete(false)
            .build();
    array =
        new TypeBuilder("array")
            .back(Helper.buildBackDataArray("value", "any"))
            .frontMark("[")
            .front(
                new FrontDataArrayBuilder("value")
                    .addSeparator(new FrontMarkBuilder(", ").build())
                    .build())
            .frontMark("]")
            .autoComplete(true)
            .build();
    syntax =
        new SyntaxBuilder("any")
            .type(one)
            .type(two)
            .type(three)
            .type(four)
            .type(five)
            .type(array)
            .group(
                "any",
                new GroupBuilder()
                    .type(one)
                    .type(two)
                    .type(three)
                    .type(four)
                    .type(five)
                    .type(array)
                    .build())
            .build();
  }

  public static VisualFrontArray visual(final Editor editor) {
    return (VisualFrontArray) editor.context.cursor.getVisual().parent().visual();
  }

  public static void assertSelection(final Editor editor, final int begin, final int end) {
    final VisualFrontArray.Cursor selection = (VisualFrontArray.Cursor) editor.context.cursor;
    assertThat(selection.beginIndex, equalTo(begin));
    assertThat(selection.endIndex, equalTo(end));
  }

  public Editor build(final Atom... atoms) {
    final Editor editor = buildDoc(syntax, new TreeBuilder(array).addArray("value", atoms).build());
    ((FieldArray) Helper.rootArray(editor.context.document).data.get(0).fields.getOpt("value"))
        .selectInto(editor.context);
    return editor;
  }

  public Editor buildFive() {
    return build(
        new TreeBuilder(one).build(),
        new TreeBuilder(two).build(),
        new TreeBuilder(three).build(),
        new TreeBuilder(four).build(),
        new TreeBuilder(five).build());
  }

  @Test
  public void testDelete() {
    final Editor editor = buildFive();
    (((VisualFrontArray)
            ((FieldArray)
                    ((Atom) editor.context.syntaxLocate(new SyntaxPath("value", "0")))
                        .fields.getOpt("value"))
                .visual))
        .select(editor.context, true, 1, 2);
    Helper.cursorArray(editor.context).editDelete(editor);
    assertTreeEqual(
        editor.context,
        new TreeBuilder(array)
            .addArray(
                "value",
                new TreeBuilder(one).build(),
                new TreeBuilder(four).build(),
                new TreeBuilder(five).build())
            .build(),
        Helper.rootArray(editor.context.document));
    assertThat(
        editor.context.cursor.getSyntaxPath(), equalTo(new SyntaxPath("value", "0", "value", "1")));
  }

  @Test
  public void testInsertBefore() {
    final Editor editor = buildFive();
    (((VisualFrontArray)
            ((FieldArray)
                    ((Atom) editor.context.syntaxLocate(new SyntaxPath("value", "0")))
                        .fields.getOpt("value"))
                .visual))
        .select(editor.context, true, 1, 2);
    Helper.cursorArray(editor.context).editInsertBefore(editor);
    assertTreeEqual(
        editor.context,
        new TreeBuilder(array)
            .addArray(
                "value",
                new TreeBuilder(one).build(),
                editor.createEmptyGap(editor.context.syntax.gap),
                new TreeBuilder(two).build(),
                new TreeBuilder(three).build(),
                new TreeBuilder(four).build(),
                new TreeBuilder(five).build())
            .build(),
        Helper.rootArray(editor.context.document));
    assertThat(
        editor.context.cursor.getSyntaxPath(),
        equalTo(new SyntaxPath("value", "0", "value", "1", "gap", "0")));
  }

  @Test
  public void testInsertBeforeDefault() {
    FreeAtomType quoted =
        new TypeBuilder("quoted")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("value")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(true)
            .build();
    FreeAtomType restrictedArray =
        new TypeBuilder("restricted_array")
            .back(Helper.buildBackDataArray("value", "restricted_array_group"))
            .frontMark("_")
            .front(new FrontDataArrayBuilder("value").build())
            .autoComplete(true)
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(quoted)
            .type(restrictedArray)
            .group("restricted_array_group", new GroupBuilder().type(quoted).build())
            .group("any", new GroupBuilder().type(quoted).type(restrictedArray).build())
            .build();

    final Atom atom =
        new TreeBuilder(restrictedArray)
            .addArray("value", new TreeBuilder(quoted).add("value", "").build())
            .build();
    final FieldArray value = (FieldArray) atom.fields.getOpt("value");
    new GeneralTestWizard(syntax, atom)
        .run(editor -> value.selectInto(editor.context, true, 0, 0))
        .editInsertBefore()
        .checkArrayTree(
            new TreeBuilder(restrictedArray)
                .addArray(
                    "value",
                    new TreeBuilder(quoted).add("value", "").build(),
                    new TreeBuilder(quoted).add("value", "").build())
                .build())
        .run(
            editor ->
                assertThat(
                    editor.context.cursor.getSyntaxPath(),
                    equalTo(new SyntaxPath("value", "0", "value", "0", "value", "0"))));
  }

  @Test
  public void testInsertAfter() {
    final Editor editor = buildFive();
    (((VisualFrontArray)
            ((FieldArray)
                    ((Atom) editor.context.syntaxLocate(new SyntaxPath("value", "0")))
                        .fields.getOpt("value"))
                .visual))
        .select(editor.context, true, 1, 2);
    Helper.cursorArray(editor.context).editInsertAfter(editor);
    assertTreeEqual(
        editor.context,
        new TreeBuilder(array)
            .addArray(
                "value",
                new TreeBuilder(one).build(),
                new TreeBuilder(two).build(),
                new TreeBuilder(three).build(),
                editor.createEmptyGap(editor.context.syntax.gap),
                new TreeBuilder(four).build(),
                new TreeBuilder(five).build())
            .build(),
        Helper.rootArray(editor.context.document));
    assertThat(
        editor.context.cursor.getSyntaxPath(),
        equalTo(new SyntaxPath("value", "0", "value", "3", "gap", "0")));
  }

  @Test
  public void testInsertAfterDefault() {
    FreeAtomType quoted =
        new TypeBuilder("quoted")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("value")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(true)
            .build();
    FreeAtomType restrictedArray =
        new TypeBuilder("restricted_array")
            .back(Helper.buildBackDataArray("value", "restricted_array_group"))
            .frontMark("_")
            .front(new FrontDataArrayBuilder("value").build())
            .autoComplete(true)
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(quoted)
            .type(restrictedArray)
            .group("restricted_array_group", new GroupBuilder().type(quoted).build())
            .group("any", new GroupBuilder().type(quoted).type(restrictedArray).build())
            .build();

    final Atom atom =
        new TreeBuilder(restrictedArray)
            .addArray("value", new TreeBuilder(quoted).add("value", "").build())
            .build();
    final FieldArray value = (FieldArray) atom.fields.getOpt("value");
    new GeneralTestWizard(syntax, atom)
        .run(editor -> value.selectInto(editor.context, true, 0, 0))
        .editInsertAfter()
        .checkArrayTree(
            new TreeBuilder(restrictedArray)
                .addArray(
                    "value",
                    new TreeBuilder(quoted).add("value", "").build(),
                    new TreeBuilder(quoted).add("value", "").build())
                .build())
        .run(
            editor ->
                assertThat(
                    editor.context.cursor.getSyntaxPath(),
                    equalTo(new SyntaxPath("value", "0", "value", "1", "value", "0"))));
  }

  @Test
  public void testMoveBefore() {
    final Editor editor = buildFive();
    (((VisualFrontArray)
            ((FieldArray)
                    ((Atom) editor.context.syntaxLocate(new SyntaxPath("value", "0")))
                        .fields.getOpt("value"))
                .visual))
        .select(editor.context, true, 1, 2);
    Helper.cursorArray(editor.context).editMoveBefore(editor);
    assertTreeEqual(
        editor.context,
        new TreeBuilder(array)
            .addArray(
                "value",
                new TreeBuilder(two).build(),
                new TreeBuilder(three).build(),
                new TreeBuilder(one).build(),
                new TreeBuilder(four).build(),
                new TreeBuilder(five).build())
            .build(),
        Helper.rootArray(editor.context.document));
    final VisualFrontArray.Cursor selection = (VisualFrontArray.Cursor) editor.context.cursor;
    assertThat(selection.beginIndex, equalTo(0));
    assertThat(selection.endIndex, equalTo(1));
  }

  @Test
  public void testMoveBeforeStart() {
    final Editor editor = buildFive();
    (((VisualFrontArray)
            ((FieldArray)
                    ((Atom) editor.context.syntaxLocate(new SyntaxPath("value", "0")))
                        .fields.getOpt("value"))
                .visual))
        .select(editor.context, true, 0, 1);
    Helper.cursorArray(editor.context).editMoveBefore(editor);
    assertTreeEqual(
        editor.context,
        new TreeBuilder(array)
            .addArray(
                "value",
                new TreeBuilder(one).build(),
                new TreeBuilder(two).build(),
                new TreeBuilder(three).build(),
                new TreeBuilder(four).build(),
                new TreeBuilder(five).build())
            .build(),
        Helper.rootArray(editor.context.document));
    final VisualFrontArray.Cursor selection = (VisualFrontArray.Cursor) editor.context.cursor;
    assertThat(selection.beginIndex, equalTo(0));
    assertThat(selection.endIndex, equalTo(1));
  }

  @Test
  public void testMoveAfter() {
    final Editor editor = buildFive();
    (((VisualFrontArray)
            ((FieldArray)
                    ((Atom) editor.context.syntaxLocate(new SyntaxPath("value", "0")))
                        .fields.getOpt("value"))
                .visual))
        .select(editor.context, true, 1, 2);
    Helper.cursorArray(editor.context).editMoveAfter(editor);
    assertTreeEqual(
        editor.context,
        new TreeBuilder(array)
            .addArray(
                "value",
                new TreeBuilder(one).build(),
                new TreeBuilder(four).build(),
                new TreeBuilder(two).build(),
                new TreeBuilder(three).build(),
                new TreeBuilder(five).build())
            .build(),
        Helper.rootArray(editor.context.document));
    final VisualFrontArray.Cursor selection = (VisualFrontArray.Cursor) editor.context.cursor;
    assertThat(selection.beginIndex, equalTo(2));
    assertThat(selection.endIndex, equalTo(3));
  }

  @Test
  public void testMoveAfterEnd() {
    final Editor editor = buildFive();
    (((VisualFrontArray)
            ((FieldArray)
                    ((Atom) editor.context.syntaxLocate(new SyntaxPath("value", "0")))
                        .fields.getOpt("value"))
                .visual))
        .select(editor.context, true, 3, 4);
    Helper.cursorArray(editor.context).editMoveAfter(editor);
    assertTreeEqual(
        editor.context,
        new TreeBuilder(array)
            .addArray(
                "value",
                new TreeBuilder(one).build(),
                new TreeBuilder(two).build(),
                new TreeBuilder(three).build(),
                new TreeBuilder(four).build(),
                new TreeBuilder(five).build())
            .build(),
        Helper.rootArray(editor.context.document));
    final VisualFrontArray.Cursor selection = (VisualFrontArray.Cursor) editor.context.cursor;
    assertThat(selection.beginIndex, equalTo(3));
    assertThat(selection.endIndex, equalTo(4));
  }

  @Test
  public void testCopyPaste() {
    final Editor editor = buildFive();
    final VisualFrontArray visual =
        ((VisualFrontArray)
            ((FieldArray)
                    ((Atom) editor.context.syntaxLocate(new SyntaxPath("value", "0")))
                        .fields.getOpt("value"))
                .visual);
    visual.select(editor.context, true, 1, 2);
    Helper.cursorArray(editor.context).actionCopy(editor.context);
    visual.select(editor.context, true, 4, 4);
    Helper.cursorArray(editor.context).editPaste(editor);
    assertTreeEqual(
        editor.context,
        new TreeBuilder(array)
            .addArray(
                "value",
                new TreeBuilder(one).build(),
                new TreeBuilder(two).build(),
                new TreeBuilder(three).build(),
                new TreeBuilder(four).build(),
                new TreeBuilder(two).build(),
                new TreeBuilder(three).build())
            .build(),
        Helper.rootArray(editor.context.document));
    final VisualFrontArray.Cursor selection = (VisualFrontArray.Cursor) editor.context.cursor;
    assertThat(selection.beginIndex, equalTo(5));
    assertThat(selection.endIndex, equalTo(5));
    assertThat(
        editor.context.cursor.getSyntaxPath(), equalTo(new SyntaxPath("value", "0", "value", "5")));
  }

  @Test
  public void testCutPaste() {
    final Editor editor = buildFive();
    (((VisualFrontArray)
            ((FieldArray)
                    ((Atom) editor.context.syntaxLocate(new SyntaxPath("value", "0")))
                        .fields.getOpt("value"))
                .visual))
        .select(editor.context, true, 1, 2);
    Helper.cursorArray(editor.context).editCut(editor);
    {
      final VisualFrontArray.Cursor selection = (VisualFrontArray.Cursor) editor.context.cursor;
      assertThat(selection.beginIndex, equalTo(1));
      assertThat(selection.endIndex, equalTo(1));
    }
    (((VisualFrontArray)
            ((FieldArray)
                    ((Atom) editor.context.syntaxLocate(new SyntaxPath("value", "0")))
                        .fields.getOpt("value"))
                .visual))
        .select(editor.context, true, 2, 2);
    Helper.cursorArray(editor.context).editPaste(editor);
    assertTreeEqual(
        editor.context,
        new TreeBuilder(array)
            .addArray(
                "value",
                new TreeBuilder(one).build(),
                new TreeBuilder(four).build(),
                new TreeBuilder(two).build(),
                new TreeBuilder(three).build())
            .build(),
        Helper.rootArray(editor.context.document));
    {
      final VisualFrontArray.Cursor selection = (VisualFrontArray.Cursor) editor.context.cursor;
      assertThat(selection.beginIndex, equalTo(3));
      assertThat(selection.endIndex, equalTo(3));
    }
    assertThat(
        editor.context.cursor.getSyntaxPath(), equalTo(new SyntaxPath("value", "0", "value", "3")));
  }

  @Test
  public void testSuffix() {
    final Editor editor = buildFive();
    final VisualFrontArray visual =
        ((VisualFrontArray)
            ((FieldArray)
                    ((Atom) editor.context.syntaxLocate(new SyntaxPath("value", "0")))
                        .fields.getOpt("value"))
                .visual);
    visual.select(editor.context, true, 1, 1);
    Helper.cursorArray(editor.context).editSuffix(editor);
    assertTreeEqual(
        editor.context,
        new TreeBuilder(array)
            .addArray(
                "value",
                new TreeBuilder(one).build(),
                new TreeBuilder(editor.context.syntax.suffixGap)
                    .add(SuffixGapAtomType.GAP_PRIMITIVE_KEY, "")
                    .addArray(SuffixGapAtomType.PRECEDING_KEY, new TreeBuilder(two).build())
                    .build(),
                new TreeBuilder(three).build(),
                new TreeBuilder(four).build(),
                new TreeBuilder(five).build())
            .build(),
        Helper.rootArray(editor.context.document));
    assertThat(
        editor.context.cursor.getSyntaxPath(),
        equalTo(new SyntaxPath("value", "0", "value", "1", "gap", "0")));
  }
}
