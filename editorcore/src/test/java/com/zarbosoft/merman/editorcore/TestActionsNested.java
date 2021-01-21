package com.zarbosoft.merman.editorcore;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.ValueAtom;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.editorcore.helper.FrontMarkBuilder;
import com.zarbosoft.merman.editorcore.helper.GeneralTestWizard;
import com.zarbosoft.merman.editorcore.helper.GroupBuilder;
import com.zarbosoft.merman.editorcore.helper.Helper;
import com.zarbosoft.merman.editorcore.helper.TreeBuilder;
import com.zarbosoft.merman.editorcore.helper.TypeBuilder;
import com.zarbosoft.merman.syntax.FreeAtomType;
import com.zarbosoft.merman.syntax.Syntax;
import org.junit.Test;

import static com.zarbosoft.merman.editorcore.helper.Helper.buildDoc;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class TestActionsNested {
  @Test
  public void testDelete() {
    /*
    final Context context = buildDoc(MiscSyntax.syntax, new TreeBuilder(MiscSyntax.snooze).add("value",
    		new TreeBuilder(MiscSyntax.snooze).add("value", new TreeBuilder(MiscSyntax.infinity).build()).build()
    ).build());
    ((Atom) context.locateLong(new Path("0", "value"))).parent.selectUp(context);
    Helper.act(context, "delete");
    assertTreeEqual(context,
    		new TreeBuilder(MiscSyntax.snooze).add("value", MiscSyntax.syntax.gap.create()).build(),
    		Helper.rootArray(context.document)
    );
    */
    new GeneralTestWizard(
            MiscSyntax.syntax,
            new TreeBuilder(MiscSyntax.snooze)
                .add(
                    "value",
                    new TreeBuilder(MiscSyntax.snooze)
                        .add("value", new TreeBuilder(MiscSyntax.infinity).build())
                        .build())
                .build())
        .run(
            context -> {
              ((Atom) context.syntaxLocate(new Path("value", "0", "value", "atom")))
                  .parent.selectChild(context);
            })
        .act("delete")
        .checkArrayTree(
            new TreeBuilder(MiscSyntax.snooze)
                .add("value", MiscSyntax.syntax.gap.create())
                .build());
  }

  @Test
  public void testCopyPaste() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(99)
            .build();
    FreeAtomType factorial =
        new TypeBuilder("factorial")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "any"))
                    .build())
            .frontDataNode("value")
            .frontMark("!")
            .autoComplete(99)
            .build();
    FreeAtomType plus =
        new TypeBuilder("plus")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("+")
            .frontDataNode("second")
            .precedence(10)
            .associateForward()
            .autoComplete(99)
            .build();
    FreeAtomType minus =
        new TypeBuilder("minus")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("-")
            .frontDataNode("second")
            .precedence(10)
            .associateBackward()
            .autoComplete(99)
            .build();
    FreeAtomType multiply =
        new TypeBuilder("multiply")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("*")
            .frontDataNode("second")
            .precedence(20)
            .associateForward()
            .autoComplete(99)
            .build();
    FreeAtomType divide =
        new TypeBuilder("divide")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("/")
            .frontDataNode("second")
            .precedence(20)
            .associateForward()
            .autoComplete(99)
            .build();
    FreeAtomType subscript =
        new TypeBuilder("subscript")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "name"))
                    .add("second", Helper.buildBackDataAtom("second", "name"))
                    .build())
            .frontDataNode("first")
            .frontMark("_")
            .frontDataNode("second")
            .precedence(0)
            .autoComplete(99)
            .build();
    FreeAtomType inclusiveRange =
        new TypeBuilder("inclusiveRange")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontMark("[")
            .frontDataNode("first")
            .frontMark(", ")
            .frontDataNode("second")
            .frontMark("]")
            .precedence(50)
            .autoComplete(99)
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(infinity)
            .type(factorial)
            .type(plus)
            .type(minus)
            .type(multiply)
            .type(divide)
            .type(subscript)
            .type(inclusiveRange)
            .group("name", new GroupBuilder().type(infinity).type(subscript).build())
            .group(
                "any",
                new GroupBuilder()
                    .type(factorial)
                    .type(plus)
                    .type(minus)
                    .type(multiply)
                    .type(divide)
                    .group("name")
                    .type(inclusiveRange)
                    .build())
            .build();

    final Context context =
        buildDoc(
            syntax,
            new TreeBuilder(plus)
                .add("first", new TreeBuilder(infinity).build())
                .add("second", syntax.gap.create())
                .build());
    ((ValueAtom) context.syntaxLocate(new Path("value", "0", "first"))).visual.select(context);
    Helper.act(context, "copy");
    ((ValueAtom) context.syntaxLocate(new Path("value", "0", "second"))).visual.select(context);
    Helper.act(context, "paste");
    assertTreeEqual(
        context,
        new TreeBuilder(plus)
            .add("first", new TreeBuilder(infinity).build())
            .add("second", new TreeBuilder(infinity).build())
            .build(),
        Helper.rootArray(context.document));
  }

  @Test
  public void testCutPaste() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(99)
            .build();
    FreeAtomType factorial =
        new TypeBuilder("factorial")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "any"))
                    .build())
            .frontDataNode("value")
            .frontMark("!")
            .autoComplete(99)
            .build();
    FreeAtomType plus =
        new TypeBuilder("plus")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("+")
            .frontDataNode("second")
            .precedence(10)
            .associateForward()
            .autoComplete(99)
            .build();
    FreeAtomType minus =
        new TypeBuilder("minus")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("-")
            .frontDataNode("second")
            .precedence(10)
            .associateBackward()
            .autoComplete(99)
            .build();
    FreeAtomType multiply =
        new TypeBuilder("multiply")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("*")
            .frontDataNode("second")
            .precedence(20)
            .associateForward()
            .autoComplete(99)
            .build();
    FreeAtomType divide =
        new TypeBuilder("divide")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("/")
            .frontDataNode("second")
            .precedence(20)
            .associateForward()
            .autoComplete(99)
            .build();
    FreeAtomType subscript =
        new TypeBuilder("subscript")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "name"))
                    .add("second", Helper.buildBackDataAtom("second", "name"))
                    .build())
            .frontDataNode("first")
            .frontMark("_")
            .frontDataNode("second")
            .precedence(0)
            .autoComplete(99)
            .build();
    FreeAtomType inclusiveRange =
        new TypeBuilder("inclusiveRange")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontMark("[")
            .frontDataNode("first")
            .frontMark(", ")
            .frontDataNode("second")
            .frontMark("]")
            .precedence(50)
            .autoComplete(99)
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(infinity)
            .type(factorial)
            .type(plus)
            .type(minus)
            .type(multiply)
            .type(divide)
            .type(subscript)
            .type(inclusiveRange)
            .group("name", new GroupBuilder().type(infinity).type(subscript).build())
            .group(
                "any",
                new GroupBuilder()
                    .type(factorial)
                    .type(plus)
                    .type(minus)
                    .type(multiply)
                    .type(divide)
                    .group("name")
                    .type(inclusiveRange)
                    .build())
            .build();

    final Context context =
        buildDoc(
            syntax,
            new TreeBuilder(factorial).add("value", new TreeBuilder(infinity).build()).build());
    ((ValueAtom) context.syntaxLocate(new Path("value", "0", "value"))).visual.select(context);
    Helper.act(context, "cut");
    assertTreeEqual(
        context,
        new TreeBuilder(factorial).add("value", syntax.gap.create()).build(),
        Helper.rootArray(context.document));
    Helper.act(context, "paste");
    assertTreeEqual(
        context,
        new TreeBuilder(factorial).add("value", new TreeBuilder(infinity).build()).build(),
        Helper.rootArray(context.document));
  }

  @Test
  public void testPrefix() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(99)
            .build();
    FreeAtomType factorial =
        new TypeBuilder("factorial")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "any"))
                    .build())
            .frontDataNode("value")
            .frontMark("!")
            .autoComplete(99)
            .build();
    FreeAtomType plus =
        new TypeBuilder("plus")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("+")
            .frontDataNode("second")
            .precedence(10)
            .associateForward()
            .autoComplete(99)
            .build();
    FreeAtomType minus =
        new TypeBuilder("minus")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("-")
            .frontDataNode("second")
            .precedence(10)
            .associateBackward()
            .autoComplete(99)
            .build();
    FreeAtomType multiply =
        new TypeBuilder("multiply")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("*")
            .frontDataNode("second")
            .precedence(20)
            .associateForward()
            .autoComplete(99)
            .build();
    FreeAtomType divide =
        new TypeBuilder("divide")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("/")
            .frontDataNode("second")
            .precedence(20)
            .associateForward()
            .autoComplete(99)
            .build();
    FreeAtomType subscript =
        new TypeBuilder("subscript")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "name"))
                    .add("second", Helper.buildBackDataAtom("second", "name"))
                    .build())
            .frontDataNode("first")
            .frontMark("_")
            .frontDataNode("second")
            .precedence(0)
            .autoComplete(99)
            .build();
    FreeAtomType inclusiveRange =
        new TypeBuilder("inclusiveRange")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontMark("[")
            .frontDataNode("first")
            .frontMark(", ")
            .frontDataNode("second")
            .frontMark("]")
            .precedence(50)
            .autoComplete(99)
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(infinity)
            .type(factorial)
            .type(plus)
            .type(minus)
            .type(multiply)
            .type(divide)
            .type(subscript)
            .type(inclusiveRange)
            .group("name", new GroupBuilder().type(infinity).type(subscript).build())
            .group(
                "any",
                new GroupBuilder()
                    .type(factorial)
                    .type(plus)
                    .type(minus)
                    .type(multiply)
                    .type(divide)
                    .group("name")
                    .type(inclusiveRange)
                    .build())
            .build();
    final Context context =
        buildDoc(
            syntax,
            new TreeBuilder(factorial).add("value", new TreeBuilder(infinity).build()).build());
    ((ValueAtom) context.syntaxLocate(new Path("value", "0", "value"))).visual.select(context);
    Helper.act(context, "prefix");
    assertTreeEqual(
        context,
        new TreeBuilder(factorial)
            .add("value", syntax.prefixGap.create(new TreeBuilder(infinity).build()))
            .build(),
        Helper.rootArray(context.document));
    assertThat(
        context.cursor.getSyntaxPath(),
        equalTo(new Path("value", "0", "value", "atom", "gap", "0")));
  }

  @Test
  public void testSuffix() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(99)
            .build();
    FreeAtomType factorial =
        new TypeBuilder("factorial")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "any"))
                    .build())
            .frontDataNode("value")
            .frontMark("!")
            .autoComplete(99)
            .build();
    FreeAtomType plus =
        new TypeBuilder("plus")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("+")
            .frontDataNode("second")
            .precedence(10)
            .associateForward()
            .autoComplete(99)
            .build();
    FreeAtomType minus =
        new TypeBuilder("minus")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("-")
            .frontDataNode("second")
            .precedence(10)
            .associateBackward()
            .autoComplete(99)
            .build();
    FreeAtomType multiply =
        new TypeBuilder("multiply")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("*")
            .frontDataNode("second")
            .precedence(20)
            .associateForward()
            .autoComplete(99)
            .build();
    FreeAtomType divide =
        new TypeBuilder("divide")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("/")
            .frontDataNode("second")
            .precedence(20)
            .associateForward()
            .autoComplete(99)
            .build();
    FreeAtomType subscript =
        new TypeBuilder("subscript")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "name"))
                    .add("second", Helper.buildBackDataAtom("second", "name"))
                    .build())
            .frontDataNode("first")
            .frontMark("_")
            .frontDataNode("second")
            .precedence(0)
            .autoComplete(99)
            .build();
    FreeAtomType inclusiveRange =
        new TypeBuilder("inclusiveRange")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontMark("[")
            .frontDataNode("first")
            .frontMark(", ")
            .frontDataNode("second")
            .frontMark("]")
            .precedence(50)
            .autoComplete(99)
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(infinity)
            .type(factorial)
            .type(plus)
            .type(minus)
            .type(multiply)
            .type(divide)
            .type(subscript)
            .type(inclusiveRange)
            .group("name", new GroupBuilder().type(infinity).type(subscript).build())
            .group(
                "any",
                new GroupBuilder()
                    .type(factorial)
                    .type(plus)
                    .type(minus)
                    .type(multiply)
                    .type(divide)
                    .group("name")
                    .type(inclusiveRange)
                    .build())
            .build();
    final Context context =
        buildDoc(
            syntax,
            new TreeBuilder(factorial).add("value", new TreeBuilder(infinity).build()).build());
    ((ValueAtom) context.syntaxLocate(new Path("value", "0", "value"))).visual.select(context);
    Helper.act(context, "suffix");
    assertTreeEqual(
        context,
        new TreeBuilder(factorial)
            .add("value", syntax.suffixGap.create(false, new TreeBuilder(infinity).build()))
            .build(),
        Helper.rootArray(context.document));
    assertThat(
        context.cursor.getSyntaxPath(),
        equalTo(new Path("value", "0", "value", "atom", "gap", "0")));
  }
}
