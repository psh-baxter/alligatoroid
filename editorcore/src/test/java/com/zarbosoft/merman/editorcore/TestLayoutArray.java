package com.zarbosoft.merman.editorcore;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.FieldArray;
import com.zarbosoft.merman.editorcore.helper.GeneralTestWizard;
import com.zarbosoft.merman.editorcore.helper.TreeBuilder;
import com.zarbosoft.merman.editorcore.history.changes.ChangeArray;
import org.junit.Test;

public class TestLayoutArray {
  @Test
  public void testDynamicAddFirst() {
    final Atom arrayAtom = new TreeBuilder(MiscSyntax.array).addArray("value").build();
    final FieldArray array = (FieldArray) arrayAtom.fields.getOpt("value");
    new GeneralTestWizard(MiscSyntax.syntax, arrayAtom)
        .run(
            context ->
                context.history.apply(
                    context,
                    new ChangeArray(
                        array, 0, 0, ImmutableList.of(new TreeBuilder(MiscSyntax.one).build()))))
        .checkTextBrick(0, 0, "[")
        .checkTextBrick(0, 1, "one")
        .checkTextBrick(0, 2, "]");
  }

  @Test
  public void testDynamicAddSecond() {
    final Atom arrayAtom =
        new TreeBuilder(MiscSyntax.array)
            .addArray("value", new TreeBuilder(MiscSyntax.one).build())
            .build();
    final FieldArray array = (FieldArray) arrayAtom.fields.getOpt("value");
    new GeneralTestWizard(MiscSyntax.syntax, arrayAtom)
        .run(
            context ->
                context.history.apply(
                    context,
                    new ChangeArray(
                        array, 0, 0, ImmutableList.of(new TreeBuilder(MiscSyntax.one).build()))))
        .checkTextBrick(0, 0, "[")
        .checkTextBrick(0, 1, "one")
        .checkTextBrick(0, 2, ", ")
        .checkTextBrick(0, 3, "one")
        .checkTextBrick(0, 4, "]");
  }

  @Test
  public void testDynamicAddSecondAfter() {
    final Atom arrayAtom =
        new TreeBuilder(MiscSyntax.array)
            .addArray("value", new TreeBuilder(MiscSyntax.one).build())
            .build();
    final FieldArray array = (FieldArray) arrayAtom.fields.getOpt("value");
    new GeneralTestWizard(MiscSyntax.syntax, arrayAtom)
        .run(
            context ->
                context.history.apply(
                    context,
                    new ChangeArray(
                        array, 1, 0, ImmutableList.of(new TreeBuilder(MiscSyntax.one).build()))))
        .checkTextBrick(0, 0, "[")
        .checkTextBrick(0, 1, "one")
        .checkTextBrick(0, 2, ", ")
        .checkTextBrick(0, 3, "one")
        .checkTextBrick(0, 4, "]");
  }

  @Test
  public void testDynamicAddSecondAfterArray() {
    final Atom arrayAtom =
        new TreeBuilder(MiscSyntax.array)
            .addArray("value", new TreeBuilder(MiscSyntax.array).addArray("value").build())
            .build();
    final FieldArray array = (FieldArray) arrayAtom.fields.getOpt("value");
    final int index = 0;
    int index2 = 0;
    new GeneralTestWizard(MiscSyntax.syntax, arrayAtom)
        .run(
            context ->
                context.history.apply(
                    context,
                    new ChangeArray(
                        array, 1, 0, ImmutableList.of(new TreeBuilder(MiscSyntax.one).build()))))
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
    final Atom arrayAtom =
        new TreeBuilder(MiscSyntax.array)
            .addArray(
                "value",
                new TreeBuilder(MiscSyntax.one).build(),
                new TreeBuilder(MiscSyntax.one).build())
            .build();
    final FieldArray array = (FieldArray) arrayAtom.fields.getOpt("value");
    new GeneralTestWizard(MiscSyntax.syntax, arrayAtom)
        .run(
            context ->
                context.history.apply(context, new ChangeArray(array, 0, 1, ImmutableList.of())))
        .checkTextBrick(0, 0, "[")
        .checkTextBrick(0, 1, "one")
        .checkTextBrick(0, 2, "]");
  }

  @Test
  public void testDynamicDeleteSecondPart() {
    final Atom arrayAtom =
        new TreeBuilder(MiscSyntax.array)
            .addArray(
                "value",
                new TreeBuilder(MiscSyntax.one).build(),
                new TreeBuilder(MiscSyntax.one).build())
            .build();
    final FieldArray array = (FieldArray) arrayAtom.fields.getOpt("value");
    new GeneralTestWizard(MiscSyntax.syntax, arrayAtom)
        .run(
            context ->
                context.history.apply(context, new ChangeArray(array, 1, 1, ImmutableList.of())))
        .checkTextBrick(0, 0, "[")
        .checkTextBrick(0, 1, "one")
        .checkTextBrick(0, 2, "]");
  }

  @Test
  public void testDynamicDeleteLast() {
    final Atom arrayAtom =
        new TreeBuilder(MiscSyntax.array)
            .addArray("value", new TreeBuilder(MiscSyntax.one).build())
            .build();
    final FieldArray array = (FieldArray) arrayAtom.fields.getOpt("value");
    new GeneralTestWizard(MiscSyntax.syntax, arrayAtom)
        .run(
            context ->
                context.history.apply(context, new ChangeArray(array, 0, 1, ImmutableList.of())))
        .checkTextBrick(0, 0, "[")
        .checkSpaceBrick(0, 1)
        .checkTextBrick(0, 2, "]");
  }

  @Test
  public void testDynamicGapDeselectLast() {
    new GeneralTestWizard(
            MiscSyntax.syntax, new TreeBuilder(MiscSyntax.array).addArray("value").build())
        .act("enter")
        .act("exit")
        .checkTextBrick(0, 0, "[")
        .checkSpaceBrick(0, 1)
        .checkTextBrick(0, 2, "]");
  }
}
