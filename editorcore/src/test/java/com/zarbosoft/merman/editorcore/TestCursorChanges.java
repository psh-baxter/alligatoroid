package com.zarbosoft.merman.editorcore;

import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.document.fields.Field;
import com.zarbosoft.merman.core.document.fields.FieldArray;
import com.zarbosoft.merman.core.document.fields.FieldAtom;
import com.zarbosoft.merman.core.syntax.AtomType;
import com.zarbosoft.merman.core.syntax.FreeAtomType;
import com.zarbosoft.merman.core.syntax.GapAtomType;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.core.visual.visuals.CursorFieldArray;
import com.zarbosoft.merman.core.visual.visuals.VisualFieldArray;
import com.zarbosoft.merman.editorcore.cursors.EditCursorFieldArray;
import com.zarbosoft.merman.editorcore.helper.BackRecordBuilder;
import com.zarbosoft.merman.editorcore.helper.FrontDataArrayBuilder;
import com.zarbosoft.merman.editorcore.helper.FrontMarkBuilder;
import com.zarbosoft.merman.editorcore.helper.GroupBuilder;
import com.zarbosoft.merman.editorcore.helper.Helper;
import com.zarbosoft.merman.editorcore.helper.SyntaxBuilder;
import com.zarbosoft.merman.editorcore.helper.TreeBuilder;
import com.zarbosoft.merman.editorcore.helper.TypeBuilder;
import com.zarbosoft.merman.editorcore.history.Change;
import com.zarbosoft.merman.editorcore.history.changes.ChangeArray;
import com.zarbosoft.merman.editorcore.history.changes.ChangeAtom;
import com.zarbosoft.rendaw.common.TSList;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

/** Test changes to the selection when a change affects the selected nodes (or nearby nodes). */
public class TestCursorChanges {

  public static void arrayParentDelete(Editor editor, FieldArray.Parent parent) {
    editor.history.record(
        editor,
        null,
        r -> r.apply(editor, new ChangeArray(parent.field, parent.index, 1, TSList.of())));
  }

  public static void parentDelete(Editor editor, Field.Parent<?> parent) {
    parent.dispatch(
        new Field.ParentDispatcher() {
          @Override
          public void handle(FieldArray.Parent parent) {
            arrayParentDelete(editor, parent);
          }

          @Override
          public void handle(FieldAtom.Parent parent) {
            editor.history.record(
                editor,
                null,
                r ->
                    r.apply(
                        editor,
                        new ChangeAtom(
                            parent.field,
                            new TreeBuilder(editor.context.syntax.gap)
                                .add(GapAtomType.PRIMITIVE_KEY, "")
                                .build())));
          }
        });
  }

  public static AtomType t(Syntax syntax, String name) {
    return syntax.splayedTypes.get(name).iterator().next();
  }

  @Test
  public void removeRootOnly() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(true)
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(infinity)
            .group("any", new GroupBuilder().type(infinity).build())
            .build();
    innerTestTransform(
        syntax,
        new TreeBuilder(infinity).build(),
        new SyntaxPath("value", "0"),
        (editor, selected, changer) -> {
          ((EditCursorFieldArray) editor.context.cursor).editDelete(editor);
        },
        new TreeBuilder(syntax.gap).add(GapAtomType.PRIMITIVE_KEY, "").build(),
        new SyntaxPath("value", "0"));
  }

  private void innerTestTransform(
      final Syntax syntax,
      final Atom begin,
      SyntaxPath selectBegin,
      final TestConsumer transform,
      final Atom end,
      final SyntaxPath selectEnd) {
    final Editor editor = Helper.buildDoc(syntax, begin);

    // Initial selection and double checking
    Object located = editor.context.syntaxLocate(selectBegin);
    final Atom found;
    if (located instanceof Atom) {
      found = (Atom) located;
      found.fieldParentRef.selectField(editor.context);
    } else {
      FieldAtom foundField = (FieldAtom) located;
      found = foundField.data;
      foundField.selectInto(editor.context);
    }
    selectBegin = editor.context.cursor.getSyntaxPath();
    // assertThat(context.selection.getSyntaxPath(), equalTo(selectBegin));

    // Transform
    transform.accept(
        editor,
        found,
        c -> editor.history.record(editor, null, r -> r.apply(editor, c)));
    MatcherAssert.assertThat(Helper.rootArray(editor.context.document).data.size(), equalTo(1));
    Helper.assertTreeEqual(Helper.rootArray(editor.context.document).data.get(0), end);
    assertThat(editor.context.cursor.getSyntaxPath(), equalTo(selectEnd));

    // Undo
    editor.history.undo(editor);
    MatcherAssert.assertThat(Helper.rootArray(editor.context.document).data.size(), equalTo(1));
    Helper.assertTreeEqual(Helper.rootArray(editor.context.document).data.get(0), begin);
    assertThat(editor.context.cursor.getSyntaxPath(), equalTo(selectBegin));

    // Redo
    editor.history.redo(editor);
    MatcherAssert.assertThat(Helper.rootArray(editor.context.document).data.size(), equalTo(1));
    Helper.assertTreeEqual(Helper.rootArray(editor.context.document).data.get(0), end);
    assertThat(editor.context.cursor.getSyntaxPath(), equalTo(selectEnd));
  }

  @Test
  public void removeArrayOnly() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(true)
            .build();
    FreeAtomType array =
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
            .type(infinity)
            .type(array)
            .group("any", new GroupBuilder().type(infinity).type(array).build())
            .build();
    innerTestTransform(
        syntax,
        new TreeBuilder(array).addArray("value", new TreeBuilder(infinity).build()).build(),
        new SyntaxPath("value", "0", "value", "0"),
        (context, selected, changer) -> {
          changer.accept(
              new ChangeArray((FieldArray) selected.fieldParentRef.field, 0, 1, TSList.of()));
        },
        new TreeBuilder(array).addArray("value").build(),
        new SyntaxPath("value", "0"));
  }

  @Test
  public void removeArraySelectBefore() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(true)
            .build();
    FreeAtomType array =
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
            .type(infinity)
            .type(array)
            .group("any", new GroupBuilder().type(infinity).type(array).build())
            .build();
    innerTestTransform(
        syntax,
        new TreeBuilder(array)
            .addArray("value", new TreeBuilder(infinity).build(), new TreeBuilder(infinity).build())
            .build(),
        new SyntaxPath("value", "0", "value", "0"),
        (context, selected, changer) ->
            changer.accept(
                new ChangeArray((FieldArray) selected.fieldParentRef.field, 1, 1, TSList.of())),
        new TreeBuilder(array).addArray("value", new TreeBuilder(infinity).build()).build(),
        new SyntaxPath("value", "0", "value", "0"));
  }

  @Test
  public void removeArraySelectFollowing() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(true)
            .build();
    FreeAtomType array =
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
            .type(infinity)
            .type(array)
            .group("any", new GroupBuilder().type(infinity).type(array).build())
            .build();
    innerTestTransform(
        syntax,
        new TreeBuilder(array)
            .addArray("value", new TreeBuilder(infinity).build(), new TreeBuilder(infinity).build())
            .build(),
        new SyntaxPath("value", "0", "value", "1"),
        (context, selected, changer) ->
            changer.accept(
                new ChangeArray((FieldArray) selected.fieldParentRef.field, 0, 1, TSList.of())),
        new TreeBuilder(array).addArray("value", new TreeBuilder(infinity).build()).build(),
        new SyntaxPath("value", "0", "value", "0"));
  }

  @Test
  public void removeArraySelectWithin() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(true)
            .build();
    FreeAtomType array =
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
            .type(infinity)
            .type(array)
            .group("any", new GroupBuilder().type(infinity).type(array).build())
            .build();
    innerTestTransform(
        syntax,
        new TreeBuilder(array)
            .addArray(
                "value",
                new TreeBuilder(infinity).build(),
                new TreeBuilder(infinity).build(),
                new TreeBuilder(infinity).build())
            .build(),
        new SyntaxPath("value", "0", "value", "1"),
        (context, selected, changer) ->
            changer.accept(
                new ChangeArray((FieldArray) selected.fieldParentRef.field, 1, 1, TSList.of())),
        new TreeBuilder(array)
            .addArray("value", new TreeBuilder(infinity).build(), new TreeBuilder(infinity).build())
            .build(),
        new SyntaxPath("value", "0", "value", "1"));
  }

  @Test
  public void removeArraySelectWithinNoneAfter() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(true)
            .build();
    FreeAtomType array =
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
            .type(infinity)
            .type(array)
            .group("any", new GroupBuilder().type(infinity).type(array).build())
            .build();
    innerTestTransform(
        syntax,
        new TreeBuilder(array)
            .addArray("value", new TreeBuilder(infinity).build(), new TreeBuilder(infinity).build())
            .build(),
        new SyntaxPath("value", "0", "value", "1"),
        (context, selected, changer) ->
            changer.accept(
                new ChangeArray((FieldArray) selected.fieldParentRef.field, 1, 1, TSList.of())),
        new TreeBuilder(array).addArray("value", new TreeBuilder(infinity).build()).build(),
        new SyntaxPath("value", "0", "value", "0"));
  }

  @Test
  public void removeArraySelectDeep() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(true)
            .build();
    FreeAtomType array =
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
            .type(infinity)
            .type(array)
            .group("any", new GroupBuilder().type(infinity).type(array).build())
            .build();
    innerTestTransform(
        syntax,
        new TreeBuilder(array)
            .addArray(
                "value",
                new TreeBuilder(infinity).build(),
                new TreeBuilder(array).addArray("value", new TreeBuilder(infinity).build()).build(),
                new TreeBuilder(infinity).build())
            .build(),
        new SyntaxPath("value", "0", "value", "1", "value", "0"),
        (editor, selected, changer) -> {
          parentDelete(
              editor,
              ((Field)
                      editor.context.syntaxLocate(
                          new SyntaxPath("value", "0", "value", "1", "value")))
                  .atomParentRef.atom().fieldParentRef);
        },
        new TreeBuilder(array)
            .addArray("value", new TreeBuilder(infinity).build(), new TreeBuilder(infinity).build())
            .build(),
        new SyntaxPath("value", "0", "value", "1"));
  }

  @Test
  public void addArrayAfter() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(true)
            .build();
    FreeAtomType array =
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
            .type(infinity)
            .type(array)
            .group("any", new GroupBuilder().type(infinity).type(array).build())
            .build();
    innerTestTransform(
        syntax,
        new TreeBuilder(array)
            .addArray("value", new TreeBuilder(infinity).build(), new TreeBuilder(infinity).build())
            .build(),
        new SyntaxPath("value", "0", "value", "0"),
        (context, selected, changer) ->
            changer.accept(
                new ChangeArray(
                    (FieldArray) selected.fieldParentRef.field,
                    0,
                    0,
                    TSList.of(new TreeBuilder(infinity).build()))),
        new TreeBuilder(array)
            .addArray(
                "value",
                new TreeBuilder(infinity).build(),
                new TreeBuilder(infinity).build(),
                new TreeBuilder(infinity).build())
            .build(),
        new SyntaxPath("value", "0", "value", "1"));
  }

  @Test
  public void addArrayAfterEnd1() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(true)
            .build();
    FreeAtomType array =
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
            .type(infinity)
            .type(array)
            .group("any", new GroupBuilder().type(infinity).type(array).build())
            .build();
    innerTestTransform(
        syntax,
        new TreeBuilder(array)
            .addArray("value", new TreeBuilder(infinity).build(), new TreeBuilder(infinity).build())
            .build(),
        new SyntaxPath("value", "0", "value", "1"),
        (context, selected, changer) ->
            changer.accept(
                new ChangeArray(
                    (FieldArray) selected.fieldParentRef.field,
                    1,
                    0,
                    TSList.of(new TreeBuilder(infinity).build()))),
        new TreeBuilder(array)
            .addArray(
                "value",
                new TreeBuilder(infinity).build(),
                new TreeBuilder(infinity).build(),
                new TreeBuilder(infinity).build())
            .build(),
        new SyntaxPath("value", "0", "value", "2"));
  }

  @Test
  public void arrayBeginAtRemoveMultiple() {
    innerArrayTestTransform(
        0,
        0,
        (context, value, syntax, changer) -> {
          changer.accept(new ChangeArray(value, 0, 2, TSList.of()));
        },
        0,
        0);
  }

  private void innerArrayTestTransform(
      final int beginSelectBegin,
      final int beginSelectEnd,
      final ArrayTestConsumer transform,
      final int endSelectBegin,
      final int endSelectEnd) {
    FreeAtomType one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .autoComplete(false)
            .build();
    FreeAtomType two =
        new TypeBuilder("two")
            .back(Helper.buildBackPrimitive("two"))
            .front(new FrontMarkBuilder("two").build())
            .autoComplete(false)
            .build();
    FreeAtomType three =
        new TypeBuilder("three")
            .back(Helper.buildBackPrimitive("three"))
            .front(new FrontMarkBuilder("three").build())
            .autoComplete(false)
            .build();
    FreeAtomType four =
        new TypeBuilder("four")
            .back(Helper.buildBackPrimitive("four"))
            .front(new FrontMarkBuilder("four").build())
            .autoComplete(false)
            .build();
    FreeAtomType five =
        new TypeBuilder("five")
            .back(Helper.buildBackPrimitive("five"))
            .front(new FrontMarkBuilder("five").build())
            .autoComplete(false)
            .build();
    FreeAtomType array =
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
    final Editor editor =
        Helper.buildDoc(
            syntax,
            new TreeBuilder(array)
                .addArray(
                    "value",
                    new TreeBuilder(one).build(),
                    new TreeBuilder(two).build(),
                    new TreeBuilder(three).build(),
                    new TreeBuilder(four).build(),
                    new TreeBuilder(five).build())
                .build());

    final FieldArray value =
        (FieldArray) Helper.rootArray(editor.context.document).data.get(0).fields.getOpt("value");
    final VisualFieldArray visual = (VisualFieldArray) value.visual;
    visual.select(editor.context, true, beginSelectBegin, beginSelectEnd);
    final CursorFieldArray selection = visual.cursor;

    // Transform
    transform.accept(
        editor,
        value,
        syntax,
        c -> editor.history.record(editor, null, r -> r.apply(editor, c)));
    assertThat(selection.beginIndex, equalTo(endSelectBegin));
    assertThat(selection.endIndex, equalTo(endSelectEnd));

    // Undo
    editor.history.undo(editor);
    assertThat(selection.beginIndex, equalTo(beginSelectBegin));
    assertThat(selection.endIndex, equalTo(beginSelectEnd));

    // Redo
    editor.history.redo(editor);
    assertThat(selection.beginIndex, equalTo(endSelectBegin));
    assertThat(selection.endIndex, equalTo(endSelectEnd));
  }

  @Test
  public void arrayBeginRightBeforeRemoveMultiple() {
    innerArrayTestTransform(
        0,
        0,
        (context, value, syntax, changer) -> {
          changer.accept(new ChangeArray(value, 1, 2, TSList.of()));
        },
        0,
        0);
  }

  @Test
  public void arrayBeginFarBeforeRemoveMultiple() {
    innerArrayTestTransform(
        0,
        0,
        (context, value, syntax, changer) -> {
          changer.accept(new ChangeArray(value, 2, 2, TSList.of()));
        },
        0,
        0);
  }

  @Test
  public void arrayMidFarAfterRemoveMultiple() {
    innerArrayTestTransform(
        4,
        4,
        (context, value, syntax, changer) -> {
          changer.accept(new ChangeArray(value, 0, 2, TSList.of()));
        },
        2,
        2);
  }

  @Test
  public void arrayMidRightAfterRemoveMultiple() {
    innerArrayTestTransform(
        3,
        3,
        (context, value, syntax, changer) -> {
          changer.accept(new ChangeArray(value, 0, 2, TSList.of()));
        },
        1,
        1);
  }

  @Test
  public void arrayMidAtFirstRemoveMultiple() {
    innerArrayTestTransform(
        1,
        1,
        (context, value, syntax, changer) -> {
          changer.accept(new ChangeArray(value, 1, 2, TSList.of()));
        },
        1,
        1);
  }

  @Test
  public void arrayMidAtSecondRemoveMultiple() {
    innerArrayTestTransform(
        2,
        2,
        (context, value, syntax, changer) -> {
          changer.accept(new ChangeArray(value, 1, 2, TSList.of()));
        },
        1,
        1);
  }

  @Test
  public void arrayMidRightBeforeRemoveMultiple() {
    innerArrayTestTransform(
        1,
        1,
        (context, value, syntax, changer) -> {
          changer.accept(new ChangeArray(value, 2, 2, TSList.of()));
        },
        1,
        1);
  }

  @Test
  public void arrayMidFarBeforeRemoveMultiple() {
    innerArrayTestTransform(
        1,
        1,
        (context, value, syntax, changer) -> {
          changer.accept(new ChangeArray(value, 3, 2, TSList.of()));
        },
        1,
        1);
  }

  @Test
  public void arrayEndFarAfterRemoveMultiple() {
    innerArrayTestTransform(
        4,
        4,
        (context, value, syntax, changer) -> {
          changer.accept(new ChangeArray(value, 0, 2, TSList.of()));
        },
        2,
        2);
  }

  @Test
  public void arrayEndRightAfterRemoveMultiple() {
    innerArrayTestTransform(
        4,
        4,
        (context, value, syntax, changer) -> {
          changer.accept(new ChangeArray(value, 3, 2, TSList.of()));
        },
        2,
        2);
  }

  @Test
  public void arrayEndAtRemoveMultiple() {
    innerArrayTestTransform(
        3,
        3,
        (context, value, syntax, changer) -> {
          changer.accept(new ChangeArray(value, 3, 2, TSList.of()));
        },
        2,
        2);
  }

  @Test
  public void arrayMidAtAddRemove() {
    innerArrayTestTransform(
        3,
        3,
        (context, value, syntax, changer) -> {
          changer.accept(
              new ChangeArray(
                  value,
                  3,
                  1,
                  TSList.of(
                      new TreeBuilder(t(syntax, "one")).build(),
                      new TreeBuilder(t(syntax, "one")).build())));
        },
        4,
        4);
  }

  @Test
  public void arrayEndAtAddRemove() {
    innerArrayTestTransform(
        4,
        4,
        (context, value, syntax, changer) -> {
          changer.accept(
              new ChangeArray(
                  value,
                  4,
                  1,
                  TSList.of(
                      new TreeBuilder(t(syntax, "one")).build(),
                      new TreeBuilder(t(syntax, "one")).build())));
        },
        5,
        5);
  }

  @Test
  public void removeNode() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(true)
            .build();
    FreeAtomType snooze =
        new TypeBuilder("snooze")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "any"))
                    .build())
            .frontMark("#")
            .frontDataAtom("value")
            .autoComplete(true)
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(infinity)
            .type(snooze)
            .group("any", new GroupBuilder().type(infinity).type(snooze).build())
            .build();
    innerTestTransform(
        syntax,
        new TreeBuilder(snooze).add("value", new TreeBuilder(infinity).build()).build(),
        new SyntaxPath("value", "0", "value"),
        (context, selected, changer) -> {
          changer.accept(
              new ChangeAtom(
                  (FieldAtom) selected.fieldParentRef.field,
                  new TreeBuilder(syntax.gap).add(GapAtomType.PRIMITIVE_KEY, "").build()));
        },
        new TreeBuilder(snooze)
            .add("value", new TreeBuilder(syntax.gap).add(GapAtomType.PRIMITIVE_KEY, "").build())
            .build(),
        new SyntaxPath("value", "0", "value"));
  }

  @Test
  public void removeNodeSelectDeep() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(true)
            .build();
    FreeAtomType snooze =
        new TypeBuilder("snooze")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "any"))
                    .build())
            .frontMark("#")
            .frontDataAtom("value")
            .autoComplete(true)
            .build();
    FreeAtomType array =
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
            .type(infinity)
            .type(snooze)
            .type(array)
            .group("any", new GroupBuilder().type(infinity).type(snooze).type(array).build())
            .build();
    innerTestTransform(
        syntax,
        new TreeBuilder(snooze)
            .add(
                "value",
                new TreeBuilder(array).addArray("value", new TreeBuilder(infinity).build()).build())
            .build(),
        new SyntaxPath("value", "0", "value", "value", "0"),
        (editor, selected, changer) -> {
          parentDelete(
              editor,
              ((FieldArray)
                      editor.context.syntaxLocate(new SyntaxPath("value", "0", "value", "value")))
                  .atomParentRef.atom().fieldParentRef);
        },
        new TreeBuilder(snooze)
            .add("value", new TreeBuilder(syntax.gap).add(GapAtomType.PRIMITIVE_KEY, "").build())
            .build(),
        new SyntaxPath("value", "0", "value"));
  }

  @FunctionalInterface
  public static interface TestConsumer {
    void accept(Editor editor, Atom value, Changer changer);
  }

  @FunctionalInterface
  public static interface ArrayTestConsumer {
    void accept(Editor editor, FieldArray array, Syntax syntax, Changer changer);
  }

  @FunctionalInterface
  public static interface Changer {
    void accept(Change change);
  }
}
