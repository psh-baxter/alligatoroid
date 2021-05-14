package com.zarbosoft.merman.editorcore;

import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.document.fields.FieldAtom;
import com.zarbosoft.merman.core.syntax.FreeAtomType;
import com.zarbosoft.merman.core.syntax.GapAtomType;
import com.zarbosoft.merman.core.syntax.SuffixGapAtomType;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.editorcore.cursors.EditFieldAtomCursor;
import com.zarbosoft.merman.editorcore.helper.BackRecordBuilder;
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

public class TestActionsNested {
  @Test
  public void testDelete() {
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
    new GeneralTestWizard(
            syntax,
            new TreeBuilder(snooze)
                .add(
                    "value",
                    new TreeBuilder(snooze).add("value", new TreeBuilder(infinity).build()).build())
                .build())
        .run(
            editor -> {
              ((Atom) editor.context.syntaxLocate(new SyntaxPath("value", "0", "value", "atom")))
                  .fieldParentRef.selectValue(editor.context);
            })
        .editDelete()
        .checkArrayTree(
            new TreeBuilder(snooze)
                .add(
                    "value",
                    new TreeBuilder(syntax.gap).add(GapAtomType.PRIMITIVE_KEY, "").build())
                .build());
  }

  @Test
  public void testCopyPaste() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(false)
            .build();
    FreeAtomType plus =
        new TypeBuilder("plus")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontDataAtom("first")
            .frontMark("+")
            .frontDataAtom("second")
            .precedence(10)
            .associateForward()
            .autoComplete(true)
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(infinity)
            .type(plus)
            .group("any", new GroupBuilder().type(plus).type(infinity).build())
            .build();

    final Editor editor =
        buildDoc(
            syntax,
            new TreeBuilder(plus)
                .add("first", new TreeBuilder(infinity).build())
                .add(
                    "second",
                    new TreeBuilder(syntax.gap).add(GapAtomType.PRIMITIVE_KEY, "").build())
                .build());
    ((FieldAtom) editor.context.syntaxLocate(new SyntaxPath("value", "0", "first")))
        .visual.select(editor.context);
    ((EditFieldAtomCursor) editor.context.cursor).actionCopy(editor.context);
    ((FieldAtom) editor.context.syntaxLocate(new SyntaxPath("value", "0", "second")))
        .visual.select(editor.context);
    ((EditFieldAtomCursor) editor.context.cursor).editPaste(editor);
    assertTreeEqual(
        editor.context,
        new TreeBuilder(plus)
            .add("first", new TreeBuilder(infinity).build())
            .add("second", new TreeBuilder(infinity).build())
            .build(),
        Helper.rootArray(editor.context.document));
  }

  @Test
  public void testCutPaste() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(false)
            .build();
    FreeAtomType factorial =
        new TypeBuilder("factorial")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "any"))
                    .build())
            .frontDataAtom("value")
            .frontMark("!")
            .autoComplete(false)
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(infinity)
            .type(factorial)
            .group("any", new GroupBuilder().type(factorial).type(infinity).build())
            .build();

    final Editor editor =
        buildDoc(
            syntax,
            new TreeBuilder(factorial).add("value", new TreeBuilder(infinity).build()).build());
    ((FieldAtom) editor.context.syntaxLocate(new SyntaxPath("value", "0", "value")))
        .visual.select(editor.context);
    ((EditFieldAtomCursor) editor.context.cursor).editCut(editor);
    assertTreeEqual(
        editor.context,
        new TreeBuilder(factorial)
            .add(
                "value", new TreeBuilder(syntax.gap).add(GapAtomType.PRIMITIVE_KEY, "").build())
            .build(),
        Helper.rootArray(editor.context.document));
    ((EditFieldAtomCursor) editor.context.cursor).editPaste(editor);
    assertTreeEqual(
        editor.context,
        new TreeBuilder(factorial).add("value", new TreeBuilder(infinity).build()).build(),
        Helper.rootArray(editor.context.document));
  }

  @Test
  public void testSuffix() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(false)
            .build();
    FreeAtomType factorial =
        new TypeBuilder("factorial")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "any"))
                    .build())
            .frontDataAtom("value")
            .frontMark("!")
            .autoComplete(false)
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(infinity)
            .type(factorial)
            .group("any", new GroupBuilder().type(factorial).build())
            .build();
    final Editor editor =
        buildDoc(
            syntax,
            new TreeBuilder(factorial).add("value", new TreeBuilder(infinity).build()).build());
    ((FieldAtom) editor.context.syntaxLocate(new SyntaxPath("value", "0", "value")))
        .visual.select(editor.context);
    ((EditFieldAtomCursor) editor.context.cursor).editSuffix(editor);
    assertTreeEqual(
        editor.context,
        new TreeBuilder(factorial)
            .add(
                "value",
                new TreeBuilder(editor.context.syntax.suffixGap)
                    .add(SuffixGapAtomType.PRIMITIVE_KEY, "")
                    .addArray(SuffixGapAtomType.PRECEDING_KEY, new TreeBuilder(infinity).build())
                    .build())
            .build(),
        Helper.rootArray(editor.context.document));
    assertThat(
        editor.context.cursor.getSyntaxPath(),
        equalTo(new SyntaxPath("value", "0", "value", "atom", "gap", "0")));
  }
}
