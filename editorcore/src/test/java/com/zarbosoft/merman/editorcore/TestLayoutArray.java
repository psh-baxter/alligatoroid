package com.zarbosoft.merman.editorcore;

import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.document.fields.FieldArray;
import com.zarbosoft.merman.core.syntax.FreeAtomType;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.editorcore.helper.FrontDataArrayBuilder;
import com.zarbosoft.merman.editorcore.helper.FrontMarkBuilder;
import com.zarbosoft.merman.editorcore.helper.GeneralTestWizard;
import com.zarbosoft.merman.editorcore.helper.GroupBuilder;
import com.zarbosoft.merman.editorcore.helper.Helper;
import com.zarbosoft.merman.editorcore.helper.SyntaxBuilder;
import com.zarbosoft.merman.editorcore.helper.TreeBuilder;
import com.zarbosoft.merman.editorcore.helper.TypeBuilder;
import com.zarbosoft.merman.editorcore.history.changes.ChangeArray;
import com.zarbosoft.rendaw.common.TSList;
import org.junit.Test;

public class TestLayoutArray {
  @Test
  public void testDynamicAddFirst() {
    FreeAtomType one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .autoComplete(false)
            .build();
    FreeAtomType arrayType =
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
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(one)
            .type(arrayType)
            .group("any", new GroupBuilder().type(one).type(arrayType).build())
            .build();
    final Atom arrayAtom = new TreeBuilder(arrayType).addArray("value").build();
    final FieldArray array = (FieldArray) arrayAtom.fields.getOpt("value");
    new GeneralTestWizard(syntax, arrayAtom)
        .run(
            editor ->
                editor.history.record(
                    editor,
                    null,
                    r ->
                        r.apply(
                            editor,
                            new ChangeArray(array, 0, 0, TSList.of(new TreeBuilder(one).build())))))
        .checkTextBrick(0, 0, "[")
        .checkTextBrick(0, 1, "one")
        .checkTextBrick(0, 2, "]");
  }

  @Test
  public void testDynamicAddSecond() {
    FreeAtomType one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .autoComplete(false)
            .build();
    FreeAtomType arrayType =
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
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(one)
            .type(arrayType)
            .group("any", new GroupBuilder().type(one).type(arrayType).build())
            .build();
    final Atom arrayAtom =
        new TreeBuilder(arrayType).addArray("value", new TreeBuilder(one).build()).build();
    final FieldArray array = (FieldArray) arrayAtom.fields.getOpt("value");
    new GeneralTestWizard(syntax, arrayAtom)
        .run(
            editor ->
                editor.history.record(
                    editor,
                    null,
                    r ->
                        r.apply(
                            editor,
                            new ChangeArray(array, 0, 0, TSList.of(new TreeBuilder(one).build())))))
        .checkTextBrick(0, 0, "[")
        .checkTextBrick(0, 1, "one")
        .checkTextBrick(0, 2, ", ")
        .checkTextBrick(0, 3, "one")
        .checkTextBrick(0, 4, "]");
  }

  @Test
  public void testDynamicAddSecondAfter() {
    FreeAtomType one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .autoComplete(false)
            .build();
    FreeAtomType arrayType =
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
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(one)
            .type(arrayType)
            .group("any", new GroupBuilder().type(one).type(arrayType).build())
            .build();
    final Atom arrayAtom =
        new TreeBuilder(arrayType).addArray("value", new TreeBuilder(one).build()).build();
    final FieldArray array = (FieldArray) arrayAtom.fields.getOpt("value");
    new GeneralTestWizard(syntax, arrayAtom)
        .run(
            editor ->
                editor.history.record(
                    editor,
                    null,
                    r ->
                        r.apply(
                            editor,
                            new ChangeArray(array, 1, 0, TSList.of(new TreeBuilder(one).build())))))
        .checkTextBrick(0, 0, "[")
        .checkTextBrick(0, 1, "one")
        .checkTextBrick(0, 2, ", ")
        .checkTextBrick(0, 3, "one")
        .checkTextBrick(0, 4, "]");
  }

  @Test
  public void testDynamicAddSecondAfterArray() {
    FreeAtomType one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .autoComplete(false)
            .build();
    FreeAtomType arrayType =
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
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(one)
            .type(arrayType)
            .group("any", new GroupBuilder().type(one).type(arrayType).build())
            .build();
    final Atom arrayAtom =
        new TreeBuilder(arrayType)
            .addArray("value", new TreeBuilder(arrayType).addArray("value").build())
            .build();
    final FieldArray array = (FieldArray) arrayAtom.fields.getOpt("value");
    int index2 = 0;
    new GeneralTestWizard(syntax, arrayAtom)
        .run(
            editor ->
                editor.history.record(
                    editor,
                    null,
                    r ->
                        r.apply(
                            editor,
                            new ChangeArray(array, 1, 0, TSList.of(new TreeBuilder(one).build())))))
        .checkTextBrick(0, index2++, "[")
        .checkTextBrick(0, index2++, "[")
        .checkSpaceBrick(0, index2++)
        .checkTextBrick(0, index2++, "]")
        .checkTextBrick(0, index2++, ", ")
        .checkTextBrick(0, index2++, "one")
        .checkTextBrick(0, index2++, "]");
  }

  @Test
  public void testDynamicDeleteFirstPart() {
    FreeAtomType one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .autoComplete(false)
            .build();
    FreeAtomType arrayType =
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
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(one)
            .type(arrayType)
            .group("any", new GroupBuilder().type(one).type(arrayType).build())
            .build();
    final Atom arrayAtom =
        new TreeBuilder(arrayType)
            .addArray("value", new TreeBuilder(one).build(), new TreeBuilder(one).build())
            .build();
    final FieldArray array = (FieldArray) arrayAtom.fields.getOpt("value");
    new GeneralTestWizard(syntax, arrayAtom)
        .run(
            editor ->
                editor.history.record(
                    editor, null, r -> r.apply(editor, new ChangeArray(array, 0, 1, TSList.of()))))
        .checkTextBrick(0, 0, "[")
        .checkTextBrick(0, 1, "one")
        .checkTextBrick(0, 2, "]");
  }

  @Test
  public void testDynamicDeleteSecondPart() {
    FreeAtomType one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .autoComplete(false)
            .build();
    FreeAtomType multiback =
        new TypeBuilder("multiback")
            .back(Helper.buildBackDataPrimitive("a"))
            .back(Helper.buildBackDataPrimitive("b"))
            .frontDataPrimitive("a")
            .frontMark("^")
            .frontDataPrimitive("b")
            .autoComplete(false)
            .build();
    FreeAtomType arrayType =
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
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(one)
            .type(multiback)
            .type(arrayType)
            .group("any", new GroupBuilder().type(one).type(arrayType).build())
            .build();
    final Atom arrayAtom =
        new TreeBuilder(arrayType)
            .addArray("value", new TreeBuilder(one).build(), new TreeBuilder(one).build())
            .build();
    final FieldArray array = (FieldArray) arrayAtom.fields.getOpt("value");
    new GeneralTestWizard(syntax, arrayAtom)
        .run(
            editor ->
                editor.history.record(
                    editor, null, r -> r.apply(editor, new ChangeArray(array, 1, 1, TSList.of()))))
        .checkTextBrick(0, 0, "[")
        .checkTextBrick(0, 1, "one")
        .checkTextBrick(0, 2, "]");
  }

  @Test
  public void testDynamicDeleteLast() {
    FreeAtomType one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .autoComplete(false)
            .build();
    FreeAtomType multiback =
        new TypeBuilder("multiback")
            .back(Helper.buildBackDataPrimitive("a"))
            .back(Helper.buildBackDataPrimitive("b"))
            .frontDataPrimitive("a")
            .frontMark("^")
            .frontDataPrimitive("b")
            .autoComplete(false)
            .build();
    FreeAtomType arrayType =
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
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(one)
            .type(multiback)
            .type(arrayType)
            .group("any", new GroupBuilder().type(one).type(arrayType).build())
            .build();
    final Atom arrayAtom =
        new TreeBuilder(arrayType).addArray("value", new TreeBuilder(one).build()).build();
    final FieldArray array = (FieldArray) arrayAtom.fields.getOpt("value");
    new GeneralTestWizard(syntax, arrayAtom)
        .run(
            editor ->
                editor.history.record(
                    editor, null, r -> r.apply(editor, new ChangeArray(array, 0, 1, TSList.of()))))
        .checkTextBrick(0, 0, "[")
        .checkSpaceBrick(0, 1)
        .checkTextBrick(0, 2, "]");
  }
}
