package com.zarbosoft.merman;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.ValueArray;
import com.zarbosoft.merman.document.values.ValuePrimitive;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.editor.history.changes.ChangePrimitiveSet;
import com.zarbosoft.merman.editor.visual.visuals.VisualPrimitive;
import com.zarbosoft.merman.helper.BackArrayBuilder;
import com.zarbosoft.merman.helper.BackRecordBuilder;
import com.zarbosoft.merman.helper.ExpressionSyntax;
import com.zarbosoft.merman.helper.FrontDataArrayBuilder;
import com.zarbosoft.merman.helper.FrontMarkBuilder;
import com.zarbosoft.merman.helper.GeneralTestWizard;
import com.zarbosoft.merman.helper.GroupBuilder;
import com.zarbosoft.merman.helper.Helper;
import com.zarbosoft.merman.helper.SyntaxBuilder;
import com.zarbosoft.merman.helper.SyntaxRestrictedRoot;
import com.zarbosoft.merman.helper.TreeBuilder;
import com.zarbosoft.merman.helper.TypeBuilder;
import com.zarbosoft.merman.syntax.FreeAtomType;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.merman.syntax.front.FrontGapBase;
import org.junit.Test;

import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.zarbosoft.merman.helper.Helper.assertTreeEqual;
import static com.zarbosoft.merman.helper.Helper.buildDoc;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class TestDocumentGap {

  /** Confirm all concrete atom types are found */
  @Test
  public void syntaxLeafNodes() {
    // Find leaves
    // Of group
    // Of transitive group
    // No duplicates (first appearance only)
    // Nothing from unrelated groups
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .build();
    FreeAtomType one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .build();
    FreeAtomType two =
      new TypeBuilder("two")
        .back(Helper.buildBackPrimitive("two"))
        .front(new FrontMarkBuilder("two").build())
        .build();
    FreeAtomType three =
      new TypeBuilder("three")
        .back(Helper.buildBackPrimitive("three"))
        .front(new FrontMarkBuilder("three").build())
        .build();
    FreeAtomType four =
      new TypeBuilder("four")
        .back(Helper.buildBackPrimitive("four"))
        .front(new FrontMarkBuilder("four").build())
        .build();
    Syntax syntax =
        new SyntaxBuilder("test_group_1")
            .type(infinity)
            .type(one)
            .type(two)
            .type(three)
            .type(four)
            .group(
                "test_group_1",
                new GroupBuilder()
                    .type(infinity)
                    .type(one)
                    .type(two)
                    .group("test_group_2")
                    .build())
            .group("test_group_2", new GroupBuilder().type(three).type(one).build())
          .group("unused", new GroupBuilder().type(four).type(one).build())
          .build();

    final Context context = blank(syntax);
    assertThat(
        context.syntax.getLeafTypes("test_group_1").collect(Collectors.toSet()),
        equalTo(ImmutableSet.of(infinity, one, two, three)));
  }

  private Context blank(Syntax syntax) {
    final Atom gap = syntax.gap.create();
    final Context context = buildDoc(syntax, gap);
    gap.visual.selectDown(context);
    return context;
  }

  // ========================================================================
  // ========================================================================
  // Decision making and replacement
  @Test
  public void decisionMaking_choiceCount() {
    // TODO how does this work?
    FreeAtomType quoted =
        new TypeBuilder("quoted")
            .middlePrimitive("value")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("value")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(1)
            .build();
    FreeAtomType restricted =
        new TypeBuilder("restricted")
            .middleAtom("value", "restricted_group")
            .back(new BackRecordBuilder().add("value", Helper.buildBackDataAtom("value")).build())
            .frontDataNode("value")
            .build();
    Syntax syntax =
        new SyntaxBuilder("restricted_group")
            .type(quoted)
            .type(restricted)
            .group("restricted_group", new GroupBuilder().type(quoted).build())
            .build();

    final Atom gap = syntax.gap.create();
    new GeneralTestWizard(syntax, new TreeBuilder(restricted).add("value", gap).build())
        .run(context -> gap.data.get("gap").selectDown(context))
        .sendText("q")
        .checkChoices(1);
  }

  @Test
  public void decisionMaking_undecided() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(1)
            .build();
    FreeAtomType one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .autoComplete(-1)
            .build();
    FreeAtomType two =
        new TypeBuilder("two")
            .back(Helper.buildBackPrimitive("two"))
            .front(new FrontMarkBuilder("two").build())
            .autoComplete(-1)
            .build();
    FreeAtomType three =
        new TypeBuilder("three")
            .back(Helper.buildBackPrimitive("three"))
            .front(new FrontMarkBuilder("three").build())
            .autoComplete(-1)
            .build();
    FreeAtomType four =
        new TypeBuilder("four")
            .back(Helper.buildBackPrimitive("four"))
            .front(new FrontMarkBuilder("four").build())
            .autoComplete(-1)
            .build();
    FreeAtomType five =
        new TypeBuilder("five")
            .back(Helper.buildBackPrimitive("five"))
            .front(new FrontMarkBuilder("five").build())
            .autoComplete(-1)
            .build();
    FreeAtomType seven =
        new TypeBuilder("seven")
            .back(Helper.buildBackPrimitive("7"))
            .front(new FrontMarkBuilder("7").build())
            .autoComplete(-1)
            .build();
    FreeAtomType multiback =
        new TypeBuilder("multiback")
            .back(Helper.buildBackDataPrimitive("a"))
            .back(Helper.buildBackDataPrimitive("b"))
            .middlePrimitive("a")
            .middlePrimitive("b")
            .frontDataPrimitive("a")
            .frontMark("^")
            .frontDataPrimitive("b")
            .autoComplete(-1)
            .build();
    FreeAtomType quoted =
        new TypeBuilder("quoted")
            .middlePrimitive("value")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("value")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(1)
            .build();
    FreeAtomType digits =
        new TypeBuilder("digits")
            .middlePrimitiveDigits("value")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .autoComplete(1)
            .build();
    FreeAtomType doubleQuoted =
        new TypeBuilder("doubleuoted")
            .middlePrimitive("first")
            .middlePrimitive("second")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataPrimitive("first"))
                    .add("second", Helper.buildBackDataPrimitive("second"))
                    .build())
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("first")
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("second")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(-1)
            .build();
    FreeAtomType plus =
        new TypeBuilder("plus")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("+")
            .frontDataNode("second")
            .autoComplete(-1)
            .build();
    FreeAtomType plusEqual =
        new TypeBuilder("plusequal")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("+=")
            .frontDataNode("second")
            .autoComplete(-1)
            .build();
    FreeAtomType binaryBang =
        new TypeBuilder("bang")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("!")
            .frontDataNode("second")
            .autoComplete(1)
            .build();
    FreeAtomType waddle =
        new TypeBuilder("waddle")
            .middleAtom("first", "any")
            .back(new BackRecordBuilder().add("first", Helper.buildBackDataAtom("first")).build())
            .frontDataNode("first")
            .frontMark("?")
            .autoComplete(1)
            .build();
    FreeAtomType snooze =
        new TypeBuilder("snooze")
            .middleAtom("value", "any")
            .back(new BackRecordBuilder().add("value", Helper.buildBackDataAtom("value")).build())
            .frontMark("#")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType multiplier =
        new TypeBuilder("multiplier")
            .middlePrimitive("text")
            .middleAtom("value", "any")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value"))
                    .add("text", Helper.buildBackDataPrimitive("text"))
                    .build())
            .frontMark("x")
            .frontDataPrimitive("text")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType array =
        new TypeBuilder("array")
            .middleArray("value", "any")
            .back(Helper.buildBackDataArray("value"))
            .frontMark("[")
            .front(
                new FrontDataArrayBuilder("value")
                    .addSeparator(new FrontMarkBuilder(", ").build())
                    .build())
            .frontMark("]")
            .autoComplete(1)
            .build();
    FreeAtomType doubleArray =
        new TypeBuilder("doublearray")
            .middleArray("first", "any")
            .middleArray("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataArray("first"))
                    .add("second", Helper.buildBackDataArray("second"))
                    .build())
            .frontMark("[")
            .frontDataArray("first")
            .frontMark("?")
            .frontDataArray("second")
            .frontMark("]")
            .build();
    FreeAtomType record =
        new TypeBuilder("record")
            .middleRecord("value", "record_element")
            .back(Helper.buildBackDataRecord("value"))
            .frontMark("{")
            .frontDataArray("value")
            .frontMark("}")
            .autoComplete(1)
            .build();
    FreeAtomType recordElement =
        new TypeBuilder("record_element")
            .middlePrimitive("key")
            .middleAtom("value", "any")
            .back(Helper.buildBackDataKey("key"))
            .back(Helper.buildBackDataAtom("value"))
            .frontDataPrimitive("key")
            .frontMark(": ")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType pair =
        new TypeBuilder("pair")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackArrayBuilder()
                    .add(Helper.buildBackDataAtom("first"))
                    .add(Helper.buildBackDataAtom("second"))
                    .build())
            .frontMark("<")
            .frontDataNode("first")
            .frontMark(", ")
            .frontDataNode("second")
            .frontMark(">")
            .autoComplete(1)
            .build();
    FreeAtomType ratio =
        new TypeBuilder("ratio")
            .middlePrimitive("first")
            .middlePrimitive("second")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataPrimitive("first"))
                    .add("second", Helper.buildBackDataPrimitive("second"))
                    .build())
            .frontMark("<")
            .frontDataPrimitive("first")
            .frontMark(":")
            .frontDataPrimitive("second")
            .frontMark(">")
            .build();
    FreeAtomType restricted =
        new TypeBuilder("restricted")
            .middleAtom("value", "restricted_group")
            .back(new BackRecordBuilder().add("value", Helper.buildBackDataAtom("value")).build())
            .frontDataNode("value")
            .build();
    FreeAtomType restrictedArray =
        new TypeBuilder("restricted_array")
            .middleArray("value", "restricted_array_group")
            .back(Helper.buildBackDataArray("value"))
            .frontMark("_")
            .front(new FrontDataArrayBuilder("value").build())
            .autoComplete(1)
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(infinity)
            .type(one)
            .type(two)
            .type(three)
            .type(four)
            .type(five)
            .type(seven)
            .type(multiback)
            .type(quoted)
            .type(digits)
            .type(doubleQuoted)
            .type(plus)
            .type(plusEqual)
            .type(binaryBang)
            .type(waddle)
            .type(snooze)
            .type(multiplier)
            .type(array)
            .type(doubleArray)
            .type(record)
            .type(recordElement)
            .type(pair)
            .type(ratio)
            .type(restricted)
            .type(restrictedArray)
            .group(
                "test_group_1",
                new GroupBuilder()
                    .type(infinity)
                    .type(one)
                    .type(multiback)
                    .group("test_group_2")
                    .build())
            .group("test_group_2", new GroupBuilder().type(quoted).build())
            .group("restricted_group", new GroupBuilder().type(quoted).build())
            .group("restricted_array_group", new GroupBuilder().type(quoted).build())
            .group(
                "any",
                new GroupBuilder()
                    .type(infinity)
                    .type(one)
                    .type(two)
                    .type(three)
                    .type(four)
                    .type(five)
                    .type(quoted)
                    .type(digits)
                    .type(seven)
                    .type(plus)
                    .type(plusEqual)
                    .type(binaryBang)
                    .type(waddle)
                    .type(snooze)
                    .type(multiplier)
                    .type(array)
                    .type(restrictedArray)
                    .type(record)
                    .type(pair)
                    .type(ratio)
                    .build())
            .group("arrayChildren", new GroupBuilder().type(one).type(multiback).build())
            .build();

    final Context context = blank(syntax);
    context.selection.receiveText(context, "o");
    assertTreeEqual(
        context,
        new TreeBuilder(syntax.gap).add("gap", "o").build(),
        Helper.rootArray(context.document));
  }

  @Test
  public void decisionMaking_undecidedFull() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(1)
            .build();
    FreeAtomType one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .autoComplete(-1)
            .build();
    FreeAtomType two =
        new TypeBuilder("two")
            .back(Helper.buildBackPrimitive("two"))
            .front(new FrontMarkBuilder("two").build())
            .autoComplete(-1)
            .build();
    FreeAtomType three =
        new TypeBuilder("three")
            .back(Helper.buildBackPrimitive("three"))
            .front(new FrontMarkBuilder("three").build())
            .autoComplete(-1)
            .build();
    FreeAtomType four =
        new TypeBuilder("four")
            .back(Helper.buildBackPrimitive("four"))
            .front(new FrontMarkBuilder("four").build())
            .autoComplete(-1)
            .build();
    FreeAtomType five =
        new TypeBuilder("five")
            .back(Helper.buildBackPrimitive("five"))
            .front(new FrontMarkBuilder("five").build())
            .autoComplete(-1)
            .build();
    FreeAtomType seven =
        new TypeBuilder("seven")
            .back(Helper.buildBackPrimitive("7"))
            .front(new FrontMarkBuilder("7").build())
            .autoComplete(-1)
            .build();
    FreeAtomType multiback =
        new TypeBuilder("multiback")
            .back(Helper.buildBackDataPrimitive("a"))
            .back(Helper.buildBackDataPrimitive("b"))
            .middlePrimitive("a")
            .middlePrimitive("b")
            .frontDataPrimitive("a")
            .frontMark("^")
            .frontDataPrimitive("b")
            .autoComplete(-1)
            .build();
    FreeAtomType quoted =
        new TypeBuilder("quoted")
            .middlePrimitive("value")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("value")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(1)
            .build();
    FreeAtomType digits =
        new TypeBuilder("digits")
            .middlePrimitiveDigits("value")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .autoComplete(1)
            .build();
    FreeAtomType doubleQuoted =
        new TypeBuilder("doubleuoted")
            .middlePrimitive("first")
            .middlePrimitive("second")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataPrimitive("first"))
                    .add("second", Helper.buildBackDataPrimitive("second"))
                    .build())
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("first")
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("second")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(-1)
            .build();
    FreeAtomType plus =
        new TypeBuilder("plus")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("+")
            .frontDataNode("second")
            .autoComplete(-1)
            .build();
    FreeAtomType plusEqual =
        new TypeBuilder("plusequal")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("+=")
            .frontDataNode("second")
            .autoComplete(-1)
            .build();
    FreeAtomType binaryBang =
        new TypeBuilder("bang")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("!")
            .frontDataNode("second")
            .autoComplete(1)
            .build();
    FreeAtomType waddle =
        new TypeBuilder("waddle")
            .middleAtom("first", "any")
            .back(new BackRecordBuilder().add("first", Helper.buildBackDataAtom("first")).build())
            .frontDataNode("first")
            .frontMark("?")
            .autoComplete(1)
            .build();
    FreeAtomType snooze =
        new TypeBuilder("snooze")
            .middleAtom("value", "any")
            .back(new BackRecordBuilder().add("value", Helper.buildBackDataAtom("value")).build())
            .frontMark("#")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType multiplier =
        new TypeBuilder("multiplier")
            .middlePrimitive("text")
            .middleAtom("value", "any")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value"))
                    .add("text", Helper.buildBackDataPrimitive("text"))
                    .build())
            .frontMark("x")
            .frontDataPrimitive("text")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType array =
        new TypeBuilder("array")
            .middleArray("value", "any")
            .back(Helper.buildBackDataArray("value"))
            .frontMark("[")
            .front(
                new FrontDataArrayBuilder("value")
                    .addSeparator(new FrontMarkBuilder(", ").build())
                    .build())
            .frontMark("]")
            .autoComplete(1)
            .build();
    FreeAtomType doubleArray =
        new TypeBuilder("doublearray")
            .middleArray("first", "any")
            .middleArray("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataArray("first"))
                    .add("second", Helper.buildBackDataArray("second"))
                    .build())
            .frontMark("[")
            .frontDataArray("first")
            .frontMark("?")
            .frontDataArray("second")
            .frontMark("]")
            .build();
    FreeAtomType record =
        new TypeBuilder("record")
            .middleRecord("value", "record_element")
            .back(Helper.buildBackDataRecord("value"))
            .frontMark("{")
            .frontDataArray("value")
            .frontMark("}")
            .autoComplete(1)
            .build();
    FreeAtomType recordElement =
        new TypeBuilder("record_element")
            .middlePrimitive("key")
            .middleAtom("value", "any")
            .back(Helper.buildBackDataKey("key"))
            .back(Helper.buildBackDataAtom("value"))
            .frontDataPrimitive("key")
            .frontMark(": ")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType pair =
        new TypeBuilder("pair")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackArrayBuilder()
                    .add(Helper.buildBackDataAtom("first"))
                    .add(Helper.buildBackDataAtom("second"))
                    .build())
            .frontMark("<")
            .frontDataNode("first")
            .frontMark(", ")
            .frontDataNode("second")
            .frontMark(">")
            .autoComplete(1)
            .build();
    FreeAtomType ratio =
        new TypeBuilder("ratio")
            .middlePrimitive("first")
            .middlePrimitive("second")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataPrimitive("first"))
                    .add("second", Helper.buildBackDataPrimitive("second"))
                    .build())
            .frontMark("<")
            .frontDataPrimitive("first")
            .frontMark(":")
            .frontDataPrimitive("second")
            .frontMark(">")
            .build();
    FreeAtomType restricted =
        new TypeBuilder("restricted")
            .middleAtom("value", "restricted_group")
            .back(new BackRecordBuilder().add("value", Helper.buildBackDataAtom("value")).build())
            .frontDataNode("value")
            .build();
    FreeAtomType restrictedArray =
        new TypeBuilder("restricted_array")
            .middleArray("value", "restricted_array_group")
            .back(Helper.buildBackDataArray("value"))
            .frontMark("_")
            .front(new FrontDataArrayBuilder("value").build())
            .autoComplete(1)
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(infinity)
            .type(one)
            .type(two)
            .type(three)
            .type(four)
            .type(five)
            .type(seven)
            .type(multiback)
            .type(quoted)
            .type(digits)
            .type(doubleQuoted)
            .type(plus)
            .type(plusEqual)
            .type(binaryBang)
            .type(waddle)
            .type(snooze)
            .type(multiplier)
            .type(array)
            .type(doubleArray)
            .type(record)
            .type(recordElement)
            .type(pair)
            .type(ratio)
            .type(restricted)
            .type(restrictedArray)
            .group(
                "test_group_1",
                new GroupBuilder()
                    .type(infinity)
                    .type(one)
                    .type(multiback)
                    .group("test_group_2")
                    .build())
            .group("test_group_2", new GroupBuilder().type(quoted).build())
            .group("restricted_group", new GroupBuilder().type(quoted).build())
            .group("restricted_array_group", new GroupBuilder().type(quoted).build())
            .group(
                "any",
                new GroupBuilder()
                    .type(infinity)
                    .type(one)
                    .type(two)
                    .type(three)
                    .type(four)
                    .type(five)
                    .type(quoted)
                    .type(digits)
                    .type(seven)
                    .type(plus)
                    .type(plusEqual)
                    .type(binaryBang)
                    .type(waddle)
                    .type(snooze)
                    .type(multiplier)
                    .type(array)
                    .type(restrictedArray)
                    .type(record)
                    .type(pair)
                    .type(ratio)
                    .build())
            .group("arrayChildren", new GroupBuilder().type(one).type(multiback).build())
            .build();

    final Context context = blank(syntax);
    context.selection.receiveText(context, "o");
    context.selection.receiveText(context, "n");
    context.selection.receiveText(context, "e");
    assertTreeEqual(
        context,
        new TreeBuilder(syntax.gap).add("gap", "one").build(),
        Helper.rootArray(context.document));
  }

  @Test
  public void decisionMaking_immediate() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(1)
            .build();
    FreeAtomType one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .autoComplete(-1)
            .build();
    FreeAtomType two =
        new TypeBuilder("two")
            .back(Helper.buildBackPrimitive("two"))
            .front(new FrontMarkBuilder("two").build())
            .autoComplete(-1)
            .build();
    FreeAtomType three =
        new TypeBuilder("three")
            .back(Helper.buildBackPrimitive("three"))
            .front(new FrontMarkBuilder("three").build())
            .autoComplete(-1)
            .build();
    FreeAtomType four =
        new TypeBuilder("four")
            .back(Helper.buildBackPrimitive("four"))
            .front(new FrontMarkBuilder("four").build())
            .autoComplete(-1)
            .build();
    FreeAtomType five =
        new TypeBuilder("five")
            .back(Helper.buildBackPrimitive("five"))
            .front(new FrontMarkBuilder("five").build())
            .autoComplete(-1)
            .build();
    FreeAtomType seven =
        new TypeBuilder("seven")
            .back(Helper.buildBackPrimitive("7"))
            .front(new FrontMarkBuilder("7").build())
            .autoComplete(-1)
            .build();
    FreeAtomType multiback =
        new TypeBuilder("multiback")
            .back(Helper.buildBackDataPrimitive("a"))
            .back(Helper.buildBackDataPrimitive("b"))
            .middlePrimitive("a")
            .middlePrimitive("b")
            .frontDataPrimitive("a")
            .frontMark("^")
            .frontDataPrimitive("b")
            .autoComplete(-1)
            .build();
    FreeAtomType quoted =
        new TypeBuilder("quoted")
            .middlePrimitive("value")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("value")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(1)
            .build();
    FreeAtomType digits =
        new TypeBuilder("digits")
            .middlePrimitiveDigits("value")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .autoComplete(1)
            .build();
    FreeAtomType doubleQuoted =
        new TypeBuilder("doubleuoted")
            .middlePrimitive("first")
            .middlePrimitive("second")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataPrimitive("first"))
                    .add("second", Helper.buildBackDataPrimitive("second"))
                    .build())
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("first")
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("second")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(-1)
            .build();
    FreeAtomType plus =
        new TypeBuilder("plus")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("+")
            .frontDataNode("second")
            .autoComplete(-1)
            .build();
    FreeAtomType plusEqual =
        new TypeBuilder("plusequal")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("+=")
            .frontDataNode("second")
            .autoComplete(-1)
            .build();
    FreeAtomType binaryBang =
        new TypeBuilder("bang")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("!")
            .frontDataNode("second")
            .autoComplete(1)
            .build();
    FreeAtomType waddle =
        new TypeBuilder("waddle")
            .middleAtom("first", "any")
            .back(new BackRecordBuilder().add("first", Helper.buildBackDataAtom("first")).build())
            .frontDataNode("first")
            .frontMark("?")
            .autoComplete(1)
            .build();
    FreeAtomType snooze =
        new TypeBuilder("snooze")
            .middleAtom("value", "any")
            .back(new BackRecordBuilder().add("value", Helper.buildBackDataAtom("value")).build())
            .frontMark("#")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType multiplier =
        new TypeBuilder("multiplier")
            .middlePrimitive("text")
            .middleAtom("value", "any")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value"))
                    .add("text", Helper.buildBackDataPrimitive("text"))
                    .build())
            .frontMark("x")
            .frontDataPrimitive("text")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType array =
        new TypeBuilder("array")
            .middleArray("value", "any")
            .back(Helper.buildBackDataArray("value"))
            .frontMark("[")
            .front(
                new FrontDataArrayBuilder("value")
                    .addSeparator(new FrontMarkBuilder(", ").build())
                    .build())
            .frontMark("]")
            .autoComplete(1)
            .build();
    FreeAtomType doubleArray =
        new TypeBuilder("doublearray")
            .middleArray("first", "any")
            .middleArray("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataArray("first"))
                    .add("second", Helper.buildBackDataArray("second"))
                    .build())
            .frontMark("[")
            .frontDataArray("first")
            .frontMark("?")
            .frontDataArray("second")
            .frontMark("]")
            .build();
    FreeAtomType record =
        new TypeBuilder("record")
            .middleRecord("value", "record_element")
            .back(Helper.buildBackDataRecord("value"))
            .frontMark("{")
            .frontDataArray("value")
            .frontMark("}")
            .autoComplete(1)
            .build();
    FreeAtomType recordElement =
        new TypeBuilder("record_element")
            .middlePrimitive("key")
            .middleAtom("value", "any")
            .back(Helper.buildBackDataKey("key"))
            .back(Helper.buildBackDataAtom("value"))
            .frontDataPrimitive("key")
            .frontMark(": ")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType pair =
        new TypeBuilder("pair")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackArrayBuilder()
                    .add(Helper.buildBackDataAtom("first"))
                    .add(Helper.buildBackDataAtom("second"))
                    .build())
            .frontMark("<")
            .frontDataNode("first")
            .frontMark(", ")
            .frontDataNode("second")
            .frontMark(">")
            .autoComplete(1)
            .build();
    FreeAtomType ratio =
        new TypeBuilder("ratio")
            .middlePrimitive("first")
            .middlePrimitive("second")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataPrimitive("first"))
                    .add("second", Helper.buildBackDataPrimitive("second"))
                    .build())
            .frontMark("<")
            .frontDataPrimitive("first")
            .frontMark(":")
            .frontDataPrimitive("second")
            .frontMark(">")
            .build();
    FreeAtomType restricted =
        new TypeBuilder("restricted")
            .middleAtom("value", "restricted_group")
            .back(new BackRecordBuilder().add("value", Helper.buildBackDataAtom("value")).build())
            .frontDataNode("value")
            .build();
    FreeAtomType restrictedArray =
        new TypeBuilder("restricted_array")
            .middleArray("value", "restricted_array_group")
            .back(Helper.buildBackDataArray("value"))
            .frontMark("_")
            .front(new FrontDataArrayBuilder("value").build())
            .autoComplete(1)
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(infinity)
            .type(one)
            .type(two)
            .type(three)
            .type(four)
            .type(five)
            .type(seven)
            .type(multiback)
            .type(quoted)
            .type(digits)
            .type(doubleQuoted)
            .type(plus)
            .type(plusEqual)
            .type(binaryBang)
            .type(waddle)
            .type(snooze)
            .type(multiplier)
            .type(array)
            .type(doubleArray)
            .type(record)
            .type(recordElement)
            .type(pair)
            .type(ratio)
            .type(restricted)
            .type(restrictedArray)
            .group(
                "test_group_1",
                new GroupBuilder()
                    .type(infinity)
                    .type(one)
                    .type(multiback)
                    .group("test_group_2")
                    .build())
            .group("test_group_2", new GroupBuilder().type(quoted).build())
            .group("restricted_group", new GroupBuilder().type(quoted).build())
            .group("restricted_array_group", new GroupBuilder().type(quoted).build())
            .group(
                "any",
                new GroupBuilder()
                    .type(infinity)
                    .type(one)
                    .type(two)
                    .type(three)
                    .type(four)
                    .type(five)
                    .type(quoted)
                    .type(digits)
                    .type(seven)
                    .type(plus)
                    .type(plusEqual)
                    .type(binaryBang)
                    .type(waddle)
                    .type(snooze)
                    .type(multiplier)
                    .type(array)
                    .type(restrictedArray)
                    .type(record)
                    .type(pair)
                    .type(ratio)
                    .build())
            .group("arrayChildren", new GroupBuilder().type(one).type(multiback).build())
            .build();

    final Context context = blank(syntax);
    context.selection.receiveText(context, "i");
    assertTreeEqual(
        context,
        new TreeBuilder(syntax.suffixGap)
            .addArray("value", ImmutableList.of(new TreeBuilder(infinity).build()))
            .add("gap", "")
            .build(),
        Helper.rootArray(context.document));
  }

  @Test
  public void decisionMaking_binaryImmediate() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(1)
            .build();
    FreeAtomType one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .autoComplete(-1)
            .build();
    FreeAtomType two =
        new TypeBuilder("two")
            .back(Helper.buildBackPrimitive("two"))
            .front(new FrontMarkBuilder("two").build())
            .autoComplete(-1)
            .build();
    FreeAtomType three =
        new TypeBuilder("three")
            .back(Helper.buildBackPrimitive("three"))
            .front(new FrontMarkBuilder("three").build())
            .autoComplete(-1)
            .build();
    FreeAtomType four =
        new TypeBuilder("four")
            .back(Helper.buildBackPrimitive("four"))
            .front(new FrontMarkBuilder("four").build())
            .autoComplete(-1)
            .build();
    FreeAtomType five =
        new TypeBuilder("five")
            .back(Helper.buildBackPrimitive("five"))
            .front(new FrontMarkBuilder("five").build())
            .autoComplete(-1)
            .build();
    FreeAtomType seven =
        new TypeBuilder("seven")
            .back(Helper.buildBackPrimitive("7"))
            .front(new FrontMarkBuilder("7").build())
            .autoComplete(-1)
            .build();
    FreeAtomType multiback =
        new TypeBuilder("multiback")
            .back(Helper.buildBackDataPrimitive("a"))
            .back(Helper.buildBackDataPrimitive("b"))
            .middlePrimitive("a")
            .middlePrimitive("b")
            .frontDataPrimitive("a")
            .frontMark("^")
            .frontDataPrimitive("b")
            .autoComplete(-1)
            .build();
    FreeAtomType quoted =
        new TypeBuilder("quoted")
            .middlePrimitive("value")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("value")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(1)
            .build();
    FreeAtomType digits =
        new TypeBuilder("digits")
            .middlePrimitiveDigits("value")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .autoComplete(1)
            .build();
    FreeAtomType doubleQuoted =
        new TypeBuilder("doubleuoted")
            .middlePrimitive("first")
            .middlePrimitive("second")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataPrimitive("first"))
                    .add("second", Helper.buildBackDataPrimitive("second"))
                    .build())
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("first")
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("second")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(-1)
            .build();
    FreeAtomType plus =
        new TypeBuilder("plus")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("+")
            .frontDataNode("second")
            .autoComplete(-1)
            .build();
    FreeAtomType plusEqual =
        new TypeBuilder("plusequal")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("+=")
            .frontDataNode("second")
            .autoComplete(-1)
            .build();
    FreeAtomType binaryBang =
        new TypeBuilder("bang")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("!")
            .frontDataNode("second")
            .autoComplete(1)
            .build();
    FreeAtomType waddle =
        new TypeBuilder("waddle")
            .middleAtom("first", "any")
            .back(new BackRecordBuilder().add("first", Helper.buildBackDataAtom("first")).build())
            .frontDataNode("first")
            .frontMark("?")
            .autoComplete(1)
            .build();
    FreeAtomType snooze =
        new TypeBuilder("snooze")
            .middleAtom("value", "any")
            .back(new BackRecordBuilder().add("value", Helper.buildBackDataAtom("value")).build())
            .frontMark("#")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType multiplier =
        new TypeBuilder("multiplier")
            .middlePrimitive("text")
            .middleAtom("value", "any")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value"))
                    .add("text", Helper.buildBackDataPrimitive("text"))
                    .build())
            .frontMark("x")
            .frontDataPrimitive("text")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType array =
        new TypeBuilder("array")
            .middleArray("value", "any")
            .back(Helper.buildBackDataArray("value"))
            .frontMark("[")
            .front(
                new FrontDataArrayBuilder("value")
                    .addSeparator(new FrontMarkBuilder(", ").build())
                    .build())
            .frontMark("]")
            .autoComplete(1)
            .build();
    FreeAtomType doubleArray =
        new TypeBuilder("doublearray")
            .middleArray("first", "any")
            .middleArray("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataArray("first"))
                    .add("second", Helper.buildBackDataArray("second"))
                    .build())
            .frontMark("[")
            .frontDataArray("first")
            .frontMark("?")
            .frontDataArray("second")
            .frontMark("]")
            .build();
    FreeAtomType record =
        new TypeBuilder("record")
            .middleRecord("value", "record_element")
            .back(Helper.buildBackDataRecord("value"))
            .frontMark("{")
            .frontDataArray("value")
            .frontMark("}")
            .autoComplete(1)
            .build();
    FreeAtomType recordElement =
        new TypeBuilder("record_element")
            .middlePrimitive("key")
            .middleAtom("value", "any")
            .back(Helper.buildBackDataKey("key"))
            .back(Helper.buildBackDataAtom("value"))
            .frontDataPrimitive("key")
            .frontMark(": ")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType pair =
        new TypeBuilder("pair")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackArrayBuilder()
                    .add(Helper.buildBackDataAtom("first"))
                    .add(Helper.buildBackDataAtom("second"))
                    .build())
            .frontMark("<")
            .frontDataNode("first")
            .frontMark(", ")
            .frontDataNode("second")
            .frontMark(">")
            .autoComplete(1)
            .build();
    FreeAtomType ratio =
        new TypeBuilder("ratio")
            .middlePrimitive("first")
            .middlePrimitive("second")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataPrimitive("first"))
                    .add("second", Helper.buildBackDataPrimitive("second"))
                    .build())
            .frontMark("<")
            .frontDataPrimitive("first")
            .frontMark(":")
            .frontDataPrimitive("second")
            .frontMark(">")
            .build();
    FreeAtomType restricted =
        new TypeBuilder("restricted")
            .middleAtom("value", "restricted_group")
            .back(new BackRecordBuilder().add("value", Helper.buildBackDataAtom("value")).build())
            .frontDataNode("value")
            .build();
    FreeAtomType restrictedArray =
        new TypeBuilder("restricted_array")
            .middleArray("value", "restricted_array_group")
            .back(Helper.buildBackDataArray("value"))
            .frontMark("_")
            .front(new FrontDataArrayBuilder("value").build())
            .autoComplete(1)
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(infinity)
            .type(one)
            .type(two)
            .type(three)
            .type(four)
            .type(five)
            .type(seven)
            .type(multiback)
            .type(quoted)
            .type(digits)
            .type(doubleQuoted)
            .type(plus)
            .type(plusEqual)
            .type(binaryBang)
            .type(waddle)
            .type(snooze)
            .type(multiplier)
            .type(array)
            .type(doubleArray)
            .type(record)
            .type(recordElement)
            .type(pair)
            .type(ratio)
            .type(restricted)
            .type(restrictedArray)
            .group(
                "test_group_1",
                new GroupBuilder()
                    .type(infinity)
                    .type(one)
                    .type(multiback)
                    .group("test_group_2")
                    .build())
            .group("test_group_2", new GroupBuilder().type(quoted).build())
            .group("restricted_group", new GroupBuilder().type(quoted).build())
            .group("restricted_array_group", new GroupBuilder().type(quoted).build())
            .group(
                "any",
                new GroupBuilder()
                    .type(infinity)
                    .type(one)
                    .type(two)
                    .type(three)
                    .type(four)
                    .type(five)
                    .type(quoted)
                    .type(digits)
                    .type(seven)
                    .type(plus)
                    .type(plusEqual)
                    .type(binaryBang)
                    .type(waddle)
                    .type(snooze)
                    .type(multiplier)
                    .type(array)
                    .type(restrictedArray)
                    .type(record)
                    .type(pair)
                    .type(ratio)
                    .build())
            .group("arrayChildren", new GroupBuilder().type(one).type(multiback).build())
            .build();

    final Context context = blank(syntax);
    context.selection.receiveText(context, "one");
    context.selection.receiveText(context, "!");
    context.selection.receiveText(context, "t");
    assertTreeEqual(
        context,
        new TreeBuilder(binaryBang)
            .add("first", new TreeBuilder(one))
            .add("second", new TreeBuilder(syntax.gap).add("gap", "t"))
            .build(),
        Helper.rootArray(context.document));
  }

  @Test
  public void decisionMaking_binaryTextImmediate() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(1)
            .build();
    FreeAtomType one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .autoComplete(-1)
            .build();
    FreeAtomType two =
        new TypeBuilder("two")
            .back(Helper.buildBackPrimitive("two"))
            .front(new FrontMarkBuilder("two").build())
            .autoComplete(-1)
            .build();
    FreeAtomType three =
        new TypeBuilder("three")
            .back(Helper.buildBackPrimitive("three"))
            .front(new FrontMarkBuilder("three").build())
            .autoComplete(-1)
            .build();
    FreeAtomType four =
        new TypeBuilder("four")
            .back(Helper.buildBackPrimitive("four"))
            .front(new FrontMarkBuilder("four").build())
            .autoComplete(-1)
            .build();
    FreeAtomType five =
        new TypeBuilder("five")
            .back(Helper.buildBackPrimitive("five"))
            .front(new FrontMarkBuilder("five").build())
            .autoComplete(-1)
            .build();
    FreeAtomType seven =
        new TypeBuilder("seven")
            .back(Helper.buildBackPrimitive("7"))
            .front(new FrontMarkBuilder("7").build())
            .autoComplete(-1)
            .build();
    FreeAtomType multiback =
        new TypeBuilder("multiback")
            .back(Helper.buildBackDataPrimitive("a"))
            .back(Helper.buildBackDataPrimitive("b"))
            .middlePrimitive("a")
            .middlePrimitive("b")
            .frontDataPrimitive("a")
            .frontMark("^")
            .frontDataPrimitive("b")
            .autoComplete(-1)
            .build();
    FreeAtomType quoted =
        new TypeBuilder("quoted")
            .middlePrimitive("value")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("value")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(1)
            .build();
    FreeAtomType digits =
        new TypeBuilder("digits")
            .middlePrimitiveDigits("value")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .autoComplete(1)
            .build();
    FreeAtomType doubleQuoted =
        new TypeBuilder("doubleuoted")
            .middlePrimitive("first")
            .middlePrimitive("second")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataPrimitive("first"))
                    .add("second", Helper.buildBackDataPrimitive("second"))
                    .build())
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("first")
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("second")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(-1)
            .build();
    FreeAtomType plus =
        new TypeBuilder("plus")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("+")
            .frontDataNode("second")
            .autoComplete(-1)
            .build();
    FreeAtomType plusEqual =
        new TypeBuilder("plusequal")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("+=")
            .frontDataNode("second")
            .autoComplete(-1)
            .build();
    FreeAtomType binaryBang =
        new TypeBuilder("bang")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("!")
            .frontDataNode("second")
            .autoComplete(1)
            .build();
    FreeAtomType waddle =
        new TypeBuilder("waddle")
            .middleAtom("first", "any")
            .back(new BackRecordBuilder().add("first", Helper.buildBackDataAtom("first")).build())
            .frontDataNode("first")
            .frontMark("?")
            .autoComplete(1)
            .build();
    FreeAtomType snooze =
        new TypeBuilder("snooze")
            .middleAtom("value", "any")
            .back(new BackRecordBuilder().add("value", Helper.buildBackDataAtom("value")).build())
            .frontMark("#")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType multiplier =
        new TypeBuilder("multiplier")
            .middlePrimitive("text")
            .middleAtom("value", "any")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value"))
                    .add("text", Helper.buildBackDataPrimitive("text"))
                    .build())
            .frontMark("x")
            .frontDataPrimitive("text")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType array =
        new TypeBuilder("array")
            .middleArray("value", "any")
            .back(Helper.buildBackDataArray("value"))
            .frontMark("[")
            .front(
                new FrontDataArrayBuilder("value")
                    .addSeparator(new FrontMarkBuilder(", ").build())
                    .build())
            .frontMark("]")
            .autoComplete(1)
            .build();
    FreeAtomType doubleArray =
        new TypeBuilder("doublearray")
            .middleArray("first", "any")
            .middleArray("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataArray("first"))
                    .add("second", Helper.buildBackDataArray("second"))
                    .build())
            .frontMark("[")
            .frontDataArray("first")
            .frontMark("?")
            .frontDataArray("second")
            .frontMark("]")
            .build();
    FreeAtomType record =
        new TypeBuilder("record")
            .middleRecord("value", "record_element")
            .back(Helper.buildBackDataRecord("value"))
            .frontMark("{")
            .frontDataArray("value")
            .frontMark("}")
            .autoComplete(1)
            .build();
    FreeAtomType recordElement =
        new TypeBuilder("record_element")
            .middlePrimitive("key")
            .middleAtom("value", "any")
            .back(Helper.buildBackDataKey("key"))
            .back(Helper.buildBackDataAtom("value"))
            .frontDataPrimitive("key")
            .frontMark(": ")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType pair =
        new TypeBuilder("pair")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackArrayBuilder()
                    .add(Helper.buildBackDataAtom("first"))
                    .add(Helper.buildBackDataAtom("second"))
                    .build())
            .frontMark("<")
            .frontDataNode("first")
            .frontMark(", ")
            .frontDataNode("second")
            .frontMark(">")
            .autoComplete(1)
            .build();
    FreeAtomType ratio =
        new TypeBuilder("ratio")
            .middlePrimitive("first")
            .middlePrimitive("second")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataPrimitive("first"))
                    .add("second", Helper.buildBackDataPrimitive("second"))
                    .build())
            .frontMark("<")
            .frontDataPrimitive("first")
            .frontMark(":")
            .frontDataPrimitive("second")
            .frontMark(">")
            .build();
    FreeAtomType restricted =
        new TypeBuilder("restricted")
            .middleAtom("value", "restricted_group")
            .back(new BackRecordBuilder().add("value", Helper.buildBackDataAtom("value")).build())
            .frontDataNode("value")
            .build();
    FreeAtomType restrictedArray =
        new TypeBuilder("restricted_array")
            .middleArray("value", "restricted_array_group")
            .back(Helper.buildBackDataArray("value"))
            .frontMark("_")
            .front(new FrontDataArrayBuilder("value").build())
            .autoComplete(1)
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(infinity)
            .type(one)
            .type(two)
            .type(three)
            .type(four)
            .type(five)
            .type(seven)
            .type(multiback)
            .type(quoted)
            .type(digits)
            .type(doubleQuoted)
            .type(plus)
            .type(plusEqual)
            .type(binaryBang)
            .type(waddle)
            .type(snooze)
            .type(multiplier)
            .type(array)
            .type(doubleArray)
            .type(record)
            .type(recordElement)
            .type(pair)
            .type(ratio)
            .type(restricted)
            .type(restrictedArray)
            .group(
                "test_group_1",
                new GroupBuilder()
                    .type(infinity)
                    .type(one)
                    .type(multiback)
                    .group("test_group_2")
                    .build())
            .group("test_group_2", new GroupBuilder().type(quoted).build())
            .group("restricted_group", new GroupBuilder().type(quoted).build())
            .group("restricted_array_group", new GroupBuilder().type(quoted).build())
            .group(
                "any",
                new GroupBuilder()
                    .type(infinity)
                    .type(one)
                    .type(two)
                    .type(three)
                    .type(four)
                    .type(five)
                    .type(quoted)
                    .type(digits)
                    .type(seven)
                    .type(plus)
                    .type(plusEqual)
                    .type(binaryBang)
                    .type(waddle)
                    .type(snooze)
                    .type(multiplier)
                    .type(array)
                    .type(restrictedArray)
                    .type(record)
                    .type(pair)
                    .type(ratio)
                    .build())
            .group("arrayChildren", new GroupBuilder().type(one).type(multiback).build())
            .build();

    final Context context = blank(syntax);
    context.selection.receiveText(context, "7");
    context.selection.receiveText(context, "!");
    context.selection.receiveText(context, "t");
    assertTreeEqual(
        context,
        new TreeBuilder(binaryBang)
            .add("first", new TreeBuilder(digits).add("value", "7"))
            .add("second", new TreeBuilder(syntax.gap).add("gap", "t"))
            .build(),
        Helper.rootArray(context.document));
  }

  @Test
  public void decisionMaking_textPrimitiveImmediate() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(1)
            .build();
    FreeAtomType one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .autoComplete(-1)
            .build();
    FreeAtomType two =
        new TypeBuilder("two")
            .back(Helper.buildBackPrimitive("two"))
            .front(new FrontMarkBuilder("two").build())
            .autoComplete(-1)
            .build();
    FreeAtomType three =
        new TypeBuilder("three")
            .back(Helper.buildBackPrimitive("three"))
            .front(new FrontMarkBuilder("three").build())
            .autoComplete(-1)
            .build();
    FreeAtomType four =
        new TypeBuilder("four")
            .back(Helper.buildBackPrimitive("four"))
            .front(new FrontMarkBuilder("four").build())
            .autoComplete(-1)
            .build();
    FreeAtomType five =
        new TypeBuilder("five")
            .back(Helper.buildBackPrimitive("five"))
            .front(new FrontMarkBuilder("five").build())
            .autoComplete(-1)
            .build();
    FreeAtomType seven =
        new TypeBuilder("seven")
            .back(Helper.buildBackPrimitive("7"))
            .front(new FrontMarkBuilder("7").build())
            .autoComplete(-1)
            .build();
    FreeAtomType multiback =
        new TypeBuilder("multiback")
            .back(Helper.buildBackDataPrimitive("a"))
            .back(Helper.buildBackDataPrimitive("b"))
            .middlePrimitive("a")
            .middlePrimitive("b")
            .frontDataPrimitive("a")
            .frontMark("^")
            .frontDataPrimitive("b")
            .autoComplete(-1)
            .build();
    FreeAtomType quoted =
        new TypeBuilder("quoted")
            .middlePrimitive("value")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("value")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(1)
            .build();
    FreeAtomType digits =
        new TypeBuilder("digits")
            .middlePrimitiveDigits("value")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .autoComplete(1)
            .build();
    FreeAtomType doubleQuoted =
        new TypeBuilder("doubleuoted")
            .middlePrimitive("first")
            .middlePrimitive("second")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataPrimitive("first"))
                    .add("second", Helper.buildBackDataPrimitive("second"))
                    .build())
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("first")
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("second")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(-1)
            .build();
    FreeAtomType plus =
        new TypeBuilder("plus")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("+")
            .frontDataNode("second")
            .autoComplete(-1)
            .build();
    FreeAtomType plusEqual =
        new TypeBuilder("plusequal")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("+=")
            .frontDataNode("second")
            .autoComplete(-1)
            .build();
    FreeAtomType binaryBang =
        new TypeBuilder("bang")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("!")
            .frontDataNode("second")
            .autoComplete(1)
            .build();
    FreeAtomType waddle =
        new TypeBuilder("waddle")
            .middleAtom("first", "any")
            .back(new BackRecordBuilder().add("first", Helper.buildBackDataAtom("first")).build())
            .frontDataNode("first")
            .frontMark("?")
            .autoComplete(1)
            .build();
    FreeAtomType snooze =
        new TypeBuilder("snooze")
            .middleAtom("value", "any")
            .back(new BackRecordBuilder().add("value", Helper.buildBackDataAtom("value")).build())
            .frontMark("#")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType multiplier =
        new TypeBuilder("multiplier")
            .middlePrimitive("text")
            .middleAtom("value", "any")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value"))
                    .add("text", Helper.buildBackDataPrimitive("text"))
                    .build())
            .frontMark("x")
            .frontDataPrimitive("text")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType array =
        new TypeBuilder("array")
            .middleArray("value", "any")
            .back(Helper.buildBackDataArray("value"))
            .frontMark("[")
            .front(
                new FrontDataArrayBuilder("value")
                    .addSeparator(new FrontMarkBuilder(", ").build())
                    .build())
            .frontMark("]")
            .autoComplete(1)
            .build();
    FreeAtomType doubleArray =
        new TypeBuilder("doublearray")
            .middleArray("first", "any")
            .middleArray("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataArray("first"))
                    .add("second", Helper.buildBackDataArray("second"))
                    .build())
            .frontMark("[")
            .frontDataArray("first")
            .frontMark("?")
            .frontDataArray("second")
            .frontMark("]")
            .build();
    FreeAtomType record =
        new TypeBuilder("record")
            .middleRecord("value", "record_element")
            .back(Helper.buildBackDataRecord("value"))
            .frontMark("{")
            .frontDataArray("value")
            .frontMark("}")
            .autoComplete(1)
            .build();
    FreeAtomType recordElement =
        new TypeBuilder("record_element")
            .middlePrimitive("key")
            .middleAtom("value", "any")
            .back(Helper.buildBackDataKey("key"))
            .back(Helper.buildBackDataAtom("value"))
            .frontDataPrimitive("key")
            .frontMark(": ")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType pair =
        new TypeBuilder("pair")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackArrayBuilder()
                    .add(Helper.buildBackDataAtom("first"))
                    .add(Helper.buildBackDataAtom("second"))
                    .build())
            .frontMark("<")
            .frontDataNode("first")
            .frontMark(", ")
            .frontDataNode("second")
            .frontMark(">")
            .autoComplete(1)
            .build();
    FreeAtomType ratio =
        new TypeBuilder("ratio")
            .middlePrimitive("first")
            .middlePrimitive("second")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataPrimitive("first"))
                    .add("second", Helper.buildBackDataPrimitive("second"))
                    .build())
            .frontMark("<")
            .frontDataPrimitive("first")
            .frontMark(":")
            .frontDataPrimitive("second")
            .frontMark(">")
            .build();
    FreeAtomType restricted =
        new TypeBuilder("restricted")
            .middleAtom("value", "restricted_group")
            .back(new BackRecordBuilder().add("value", Helper.buildBackDataAtom("value")).build())
            .frontDataNode("value")
            .build();
    FreeAtomType restrictedArray =
        new TypeBuilder("restricted_array")
            .middleArray("value", "restricted_array_group")
            .back(Helper.buildBackDataArray("value"))
            .frontMark("_")
            .front(new FrontDataArrayBuilder("value").build())
            .autoComplete(1)
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(infinity)
            .type(one)
            .type(two)
            .type(three)
            .type(four)
            .type(five)
            .type(seven)
            .type(multiback)
            .type(quoted)
            .type(digits)
            .type(doubleQuoted)
            .type(plus)
            .type(plusEqual)
            .type(binaryBang)
            .type(waddle)
            .type(snooze)
            .type(multiplier)
            .type(array)
            .type(doubleArray)
            .type(record)
            .type(recordElement)
            .type(pair)
            .type(ratio)
            .type(restricted)
            .type(restrictedArray)
            .group(
                "test_group_1",
                new GroupBuilder()
                    .type(infinity)
                    .type(one)
                    .type(multiback)
                    .group("test_group_2")
                    .build())
            .group("test_group_2", new GroupBuilder().type(quoted).build())
            .group("restricted_group", new GroupBuilder().type(quoted).build())
            .group("restricted_array_group", new GroupBuilder().type(quoted).build())
            .group(
                "any",
                new GroupBuilder()
                    .type(infinity)
                    .type(one)
                    .type(two)
                    .type(three)
                    .type(four)
                    .type(five)
                    .type(quoted)
                    .type(digits)
                    .type(seven)
                    .type(plus)
                    .type(plusEqual)
                    .type(binaryBang)
                    .type(waddle)
                    .type(snooze)
                    .type(multiplier)
                    .type(array)
                    .type(restrictedArray)
                    .type(record)
                    .type(pair)
                    .type(ratio)
                    .build())
            .group("arrayChildren", new GroupBuilder().type(one).type(multiback).build())
            .build();

    final Atom atom = syntax.gap.create();
    new GeneralTestWizard(syntax, atom)
        .run(context -> atom.data.get("gap").selectDown(context))
        .sendText("9")
        .checkArrayTree(new TreeBuilder(digits).add("value", "9").build())
        .run(
            context -> {
              final VisualPrimitive.RangeAttachment range =
                  ((VisualPrimitive.PrimitiveSelection) context.selection).range;
              assertThat(range.beginOffset, equalTo(1));
              assertThat(range.endOffset, equalTo(1));
            });
  }

  @Test
  public void decisionMaking_textMark1Immediate() {
    final FreeAtomType one;
    final FreeAtomType two;
    final FreeAtomType three;
    final FreeAtomType textMark1;
    final FreeAtomType textMark2;
    final FreeAtomType textMark3;
    final FreeAtomType ambiguateTextMark3;
    final Syntax syntax;
    one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .build();
    two =
        new TypeBuilder("two")
            .back(Helper.buildBackPrimitive("two"))
            .front(new FrontMarkBuilder("two").build())
            .build();
    three =
        new TypeBuilder("three")
            .back(Helper.buildBackPrimitive("three"))
            .front(new FrontMarkBuilder("three").build())
            .build();
    textMark1 =
        new TypeBuilder("textmark1")
            .middlePrimitiveLetters("text")
            .middleAtom("atom", "any")
            .back(
                new BackRecordBuilder()
                    .add("text", Helper.buildBackDataPrimitive("text"))
                    .add("atom", Helper.buildBackDataAtom("atom"))
                    .build())
            .frontDataPrimitive("text")
            .frontMark("$1")
            .frontDataNode("atom")
            .autoComplete(1)
            .build();
    textMark2 =
        new TypeBuilder("textmark2")
            .middlePrimitiveLetters("text")
            .middleAtom("atom", "any")
            .middleAtom("atom2", "any")
            .back(
                new BackRecordBuilder()
                    .add("text", Helper.buildBackDataPrimitive("text"))
                    .add("atom", Helper.buildBackDataAtom("atom"))
                    .add("atom2", Helper.buildBackDataAtom("atom2"))
                    .build())
            .frontDataPrimitive("text")
            .frontMark("$2")
            .frontDataNode("atom")
            .frontDataNode("atom2")
            .autoComplete(1)
            .build();
    textMark3 =
        new TypeBuilder("textmark3")
            .middlePrimitiveLetters("text")
            .middleAtom("atom", "any")
            .middleAtom("atom2", "any")
            .back(
                new BackRecordBuilder()
                    .add("text", Helper.buildBackDataPrimitive("text"))
                    .add("atom", Helper.buildBackDataAtom("atom"))
                    .add("atom2", Helper.buildBackDataAtom("atom2"))
                    .build())
            .frontDataNode("atom2")
            .frontDataPrimitive("text")
            .frontMark("$3")
            .frontDataNode("atom")
            .autoComplete(1)
            .build();
    ambiguateTextMark3 =
        new TypeBuilder("atextmark3")
            .middleAtom("atom", "any")
            .back(new BackRecordBuilder().add("atom", Helper.buildBackDataAtom("atom")).build())
            .frontDataNode("atom")
            .frontMark("t")
            .autoComplete(1)
            .build();
    syntax =
        new SyntaxBuilder("any")
            .type(one)
            .type(two)
            .type(three)
            .type(textMark1)
            .type(textMark2)
            .type(textMark3)
            .type(ambiguateTextMark3)
            .group(
                "any",
                new GroupBuilder()
                    .type(one)
                    .type(two)
                    .type(three)
                    .type(textMark1)
                    .type(textMark2)
                    .type(textMark3)
                    .type(ambiguateTextMark3)
                    .build())
            .build();

    final Atom atom = syntax.gap.create();
    new GeneralTestWizard(syntax, atom)
        .run(context -> atom.data.get("gap").selectDown(context))
        .sendText("t")
        .sendText("$")
        .sendText("1")
        .sendText("t")
        .checkArrayTree(
            new TreeBuilder(textMark1)
                .add("text", "t")
                .add("atom", new TreeBuilder(syntax.gap).add("gap", "t"))
                .build())
        .run(
            context -> {
              final VisualPrimitive.RangeAttachment range =
                  ((VisualPrimitive.PrimitiveSelection) context.selection).range;
              assertThat(range.beginOffset, equalTo(1));
              assertThat(range.endOffset, equalTo(1));
            });
  }

  @Test
  public void decisionMaking_binaryUndecided() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(1)
            .build();
    FreeAtomType one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .autoComplete(-1)
            .build();
    FreeAtomType two =
        new TypeBuilder("two")
            .back(Helper.buildBackPrimitive("two"))
            .front(new FrontMarkBuilder("two").build())
            .autoComplete(-1)
            .build();
    FreeAtomType three =
        new TypeBuilder("three")
            .back(Helper.buildBackPrimitive("three"))
            .front(new FrontMarkBuilder("three").build())
            .autoComplete(-1)
            .build();
    FreeAtomType four =
        new TypeBuilder("four")
            .back(Helper.buildBackPrimitive("four"))
            .front(new FrontMarkBuilder("four").build())
            .autoComplete(-1)
            .build();
    FreeAtomType five =
        new TypeBuilder("five")
            .back(Helper.buildBackPrimitive("five"))
            .front(new FrontMarkBuilder("five").build())
            .autoComplete(-1)
            .build();
    FreeAtomType seven =
        new TypeBuilder("seven")
            .back(Helper.buildBackPrimitive("7"))
            .front(new FrontMarkBuilder("7").build())
            .autoComplete(-1)
            .build();
    FreeAtomType multiback =
        new TypeBuilder("multiback")
            .back(Helper.buildBackDataPrimitive("a"))
            .back(Helper.buildBackDataPrimitive("b"))
            .middlePrimitive("a")
            .middlePrimitive("b")
            .frontDataPrimitive("a")
            .frontMark("^")
            .frontDataPrimitive("b")
            .autoComplete(-1)
            .build();
    FreeAtomType quoted =
        new TypeBuilder("quoted")
            .middlePrimitive("value")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("value")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(1)
            .build();
    FreeAtomType digits =
        new TypeBuilder("digits")
            .middlePrimitiveDigits("value")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .autoComplete(1)
            .build();
    FreeAtomType doubleQuoted =
        new TypeBuilder("doubleuoted")
            .middlePrimitive("first")
            .middlePrimitive("second")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataPrimitive("first"))
                    .add("second", Helper.buildBackDataPrimitive("second"))
                    .build())
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("first")
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("second")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(-1)
            .build();
    FreeAtomType plus =
        new TypeBuilder("plus")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("+")
            .frontDataNode("second")
            .autoComplete(-1)
            .build();
    FreeAtomType plusEqual =
        new TypeBuilder("plusequal")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("+=")
            .frontDataNode("second")
            .autoComplete(-1)
            .build();
    FreeAtomType binaryBang =
        new TypeBuilder("bang")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("!")
            .frontDataNode("second")
            .autoComplete(1)
            .build();
    FreeAtomType waddle =
        new TypeBuilder("waddle")
            .middleAtom("first", "any")
            .back(new BackRecordBuilder().add("first", Helper.buildBackDataAtom("first")).build())
            .frontDataNode("first")
            .frontMark("?")
            .autoComplete(1)
            .build();
    FreeAtomType snooze =
        new TypeBuilder("snooze")
            .middleAtom("value", "any")
            .back(new BackRecordBuilder().add("value", Helper.buildBackDataAtom("value")).build())
            .frontMark("#")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType multiplier =
        new TypeBuilder("multiplier")
            .middlePrimitive("text")
            .middleAtom("value", "any")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value"))
                    .add("text", Helper.buildBackDataPrimitive("text"))
                    .build())
            .frontMark("x")
            .frontDataPrimitive("text")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType array =
        new TypeBuilder("array")
            .middleArray("value", "any")
            .back(Helper.buildBackDataArray("value"))
            .frontMark("[")
            .front(
                new FrontDataArrayBuilder("value")
                    .addSeparator(new FrontMarkBuilder(", ").build())
                    .build())
            .frontMark("]")
            .autoComplete(1)
            .build();
    FreeAtomType doubleArray =
        new TypeBuilder("doublearray")
            .middleArray("first", "any")
            .middleArray("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataArray("first"))
                    .add("second", Helper.buildBackDataArray("second"))
                    .build())
            .frontMark("[")
            .frontDataArray("first")
            .frontMark("?")
            .frontDataArray("second")
            .frontMark("]")
            .build();
    FreeAtomType record =
        new TypeBuilder("record")
            .middleRecord("value", "record_element")
            .back(Helper.buildBackDataRecord("value"))
            .frontMark("{")
            .frontDataArray("value")
            .frontMark("}")
            .autoComplete(1)
            .build();
    FreeAtomType recordElement =
        new TypeBuilder("record_element")
            .middlePrimitive("key")
            .middleAtom("value", "any")
            .back(Helper.buildBackDataKey("key"))
            .back(Helper.buildBackDataAtom("value"))
            .frontDataPrimitive("key")
            .frontMark(": ")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType pair =
        new TypeBuilder("pair")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackArrayBuilder()
                    .add(Helper.buildBackDataAtom("first"))
                    .add(Helper.buildBackDataAtom("second"))
                    .build())
            .frontMark("<")
            .frontDataNode("first")
            .frontMark(", ")
            .frontDataNode("second")
            .frontMark(">")
            .autoComplete(1)
            .build();
    FreeAtomType ratio =
        new TypeBuilder("ratio")
            .middlePrimitive("first")
            .middlePrimitive("second")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataPrimitive("first"))
                    .add("second", Helper.buildBackDataPrimitive("second"))
                    .build())
            .frontMark("<")
            .frontDataPrimitive("first")
            .frontMark(":")
            .frontDataPrimitive("second")
            .frontMark(">")
            .build();
    FreeAtomType restricted =
        new TypeBuilder("restricted")
            .middleAtom("value", "restricted_group")
            .back(new BackRecordBuilder().add("value", Helper.buildBackDataAtom("value")).build())
            .frontDataNode("value")
            .build();
    FreeAtomType restrictedArray =
        new TypeBuilder("restricted_array")
            .middleArray("value", "restricted_array_group")
            .back(Helper.buildBackDataArray("value"))
            .frontMark("_")
            .front(new FrontDataArrayBuilder("value").build())
            .autoComplete(1)
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(infinity)
            .type(one)
            .type(two)
            .type(three)
            .type(four)
            .type(five)
            .type(seven)
            .type(multiback)
            .type(quoted)
            .type(digits)
            .type(doubleQuoted)
            .type(plus)
            .type(plusEqual)
            .type(binaryBang)
            .type(waddle)
            .type(snooze)
            .type(multiplier)
            .type(array)
            .type(doubleArray)
            .type(record)
            .type(recordElement)
            .type(pair)
            .type(ratio)
            .type(restricted)
            .type(restrictedArray)
            .group(
                "test_group_1",
                new GroupBuilder()
                    .type(infinity)
                    .type(one)
                    .type(multiback)
                    .group("test_group_2")
                    .build())
            .group("test_group_2", new GroupBuilder().type(quoted).build())
            .group("restricted_group", new GroupBuilder().type(quoted).build())
            .group("restricted_array_group", new GroupBuilder().type(quoted).build())
            .group(
                "any",
                new GroupBuilder()
                    .type(infinity)
                    .type(one)
                    .type(two)
                    .type(three)
                    .type(four)
                    .type(five)
                    .type(quoted)
                    .type(digits)
                    .type(seven)
                    .type(plus)
                    .type(plusEqual)
                    .type(binaryBang)
                    .type(waddle)
                    .type(snooze)
                    .type(multiplier)
                    .type(array)
                    .type(restrictedArray)
                    .type(record)
                    .type(pair)
                    .type(ratio)
                    .build())
            .group("arrayChildren", new GroupBuilder().type(one).type(multiback).build())
            .build();

    final Context context = blank(syntax);
    context.selection.receiveText(context, "one");
    context.selection.receiveText(context, "+");
    assertTreeEqual(
        context,
        new TreeBuilder(syntax.suffixGap)
            .addArray("value", ImmutableList.of(new TreeBuilder(one).build()))
            .add("gap", "+")
            .build(),
        Helper.rootArray(context.document));
  }

  @Test
  public void decisionMaking_immediateReplaceOnSelection() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(1)
            .build();
    FreeAtomType one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .autoComplete(-1)
            .build();
    FreeAtomType two =
        new TypeBuilder("two")
            .back(Helper.buildBackPrimitive("two"))
            .front(new FrontMarkBuilder("two").build())
            .autoComplete(-1)
            .build();
    FreeAtomType three =
        new TypeBuilder("three")
            .back(Helper.buildBackPrimitive("three"))
            .front(new FrontMarkBuilder("three").build())
            .autoComplete(-1)
            .build();
    FreeAtomType four =
        new TypeBuilder("four")
            .back(Helper.buildBackPrimitive("four"))
            .front(new FrontMarkBuilder("four").build())
            .autoComplete(-1)
            .build();
    FreeAtomType five =
        new TypeBuilder("five")
            .back(Helper.buildBackPrimitive("five"))
            .front(new FrontMarkBuilder("five").build())
            .autoComplete(-1)
            .build();
    FreeAtomType seven =
        new TypeBuilder("seven")
            .back(Helper.buildBackPrimitive("7"))
            .front(new FrontMarkBuilder("7").build())
            .autoComplete(-1)
            .build();
    FreeAtomType multiback =
        new TypeBuilder("multiback")
            .back(Helper.buildBackDataPrimitive("a"))
            .back(Helper.buildBackDataPrimitive("b"))
            .middlePrimitive("a")
            .middlePrimitive("b")
            .frontDataPrimitive("a")
            .frontMark("^")
            .frontDataPrimitive("b")
            .autoComplete(-1)
            .build();
    FreeAtomType quoted =
        new TypeBuilder("quoted")
            .middlePrimitive("value")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("value")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(1)
            .build();
    FreeAtomType digits =
        new TypeBuilder("digits")
            .middlePrimitiveDigits("value")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .autoComplete(1)
            .build();
    FreeAtomType doubleQuoted =
        new TypeBuilder("doubleuoted")
            .middlePrimitive("first")
            .middlePrimitive("second")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataPrimitive("first"))
                    .add("second", Helper.buildBackDataPrimitive("second"))
                    .build())
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("first")
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("second")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(-1)
            .build();
    FreeAtomType plus =
        new TypeBuilder("plus")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("+")
            .frontDataNode("second")
            .autoComplete(-1)
            .build();
    FreeAtomType plusEqual =
        new TypeBuilder("plusequal")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("+=")
            .frontDataNode("second")
            .autoComplete(-1)
            .build();
    FreeAtomType binaryBang =
        new TypeBuilder("bang")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("!")
            .frontDataNode("second")
            .autoComplete(1)
            .build();
    FreeAtomType waddle =
        new TypeBuilder("waddle")
            .middleAtom("first", "any")
            .back(new BackRecordBuilder().add("first", Helper.buildBackDataAtom("first")).build())
            .frontDataNode("first")
            .frontMark("?")
            .autoComplete(1)
            .build();
    FreeAtomType snooze =
        new TypeBuilder("snooze")
            .middleAtom("value", "any")
            .back(new BackRecordBuilder().add("value", Helper.buildBackDataAtom("value")).build())
            .frontMark("#")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType multiplier =
        new TypeBuilder("multiplier")
            .middlePrimitive("text")
            .middleAtom("value", "any")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value"))
                    .add("text", Helper.buildBackDataPrimitive("text"))
                    .build())
            .frontMark("x")
            .frontDataPrimitive("text")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType array =
        new TypeBuilder("array")
            .middleArray("value", "any")
            .back(Helper.buildBackDataArray("value"))
            .frontMark("[")
            .front(
                new FrontDataArrayBuilder("value")
                    .addSeparator(new FrontMarkBuilder(", ").build())
                    .build())
            .frontMark("]")
            .autoComplete(1)
            .build();
    FreeAtomType doubleArray =
        new TypeBuilder("doublearray")
            .middleArray("first", "any")
            .middleArray("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataArray("first"))
                    .add("second", Helper.buildBackDataArray("second"))
                    .build())
            .frontMark("[")
            .frontDataArray("first")
            .frontMark("?")
            .frontDataArray("second")
            .frontMark("]")
            .build();
    FreeAtomType record =
        new TypeBuilder("record")
            .middleRecord("value", "record_element")
            .back(Helper.buildBackDataRecord("value"))
            .frontMark("{")
            .frontDataArray("value")
            .frontMark("}")
            .autoComplete(1)
            .build();
    FreeAtomType recordElement =
        new TypeBuilder("record_element")
            .middlePrimitive("key")
            .middleAtom("value", "any")
            .back(Helper.buildBackDataKey("key"))
            .back(Helper.buildBackDataAtom("value"))
            .frontDataPrimitive("key")
            .frontMark(": ")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType pair =
        new TypeBuilder("pair")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackArrayBuilder()
                    .add(Helper.buildBackDataAtom("first"))
                    .add(Helper.buildBackDataAtom("second"))
                    .build())
            .frontMark("<")
            .frontDataNode("first")
            .frontMark(", ")
            .frontDataNode("second")
            .frontMark(">")
            .autoComplete(1)
            .build();
    FreeAtomType ratio =
        new TypeBuilder("ratio")
            .middlePrimitive("first")
            .middlePrimitive("second")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataPrimitive("first"))
                    .add("second", Helper.buildBackDataPrimitive("second"))
                    .build())
            .frontMark("<")
            .frontDataPrimitive("first")
            .frontMark(":")
            .frontDataPrimitive("second")
            .frontMark(">")
            .build();
    FreeAtomType restricted =
        new TypeBuilder("restricted")
            .middleAtom("value", "restricted_group")
            .back(new BackRecordBuilder().add("value", Helper.buildBackDataAtom("value")).build())
            .frontDataNode("value")
            .build();
    FreeAtomType restrictedArray =
        new TypeBuilder("restricted_array")
            .middleArray("value", "restricted_array_group")
            .back(Helper.buildBackDataArray("value"))
            .frontMark("_")
            .front(new FrontDataArrayBuilder("value").build())
            .autoComplete(1)
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(infinity)
            .type(one)
            .type(two)
            .type(three)
            .type(four)
            .type(five)
            .type(seven)
            .type(multiback)
            .type(quoted)
            .type(digits)
            .type(doubleQuoted)
            .type(plus)
            .type(plusEqual)
            .type(binaryBang)
            .type(waddle)
            .type(snooze)
            .type(multiplier)
            .type(array)
            .type(doubleArray)
            .type(record)
            .type(recordElement)
            .type(pair)
            .type(ratio)
            .type(restricted)
            .type(restrictedArray)
            .group(
                "test_group_1",
                new GroupBuilder()
                    .type(infinity)
                    .type(one)
                    .type(multiback)
                    .group("test_group_2")
                    .build())
            .group("test_group_2", new GroupBuilder().type(quoted).build())
            .group("restricted_group", new GroupBuilder().type(quoted).build())
            .group("restricted_array_group", new GroupBuilder().type(quoted).build())
            .group(
                "any",
                new GroupBuilder()
                    .type(infinity)
                    .type(one)
                    .type(two)
                    .type(three)
                    .type(four)
                    .type(five)
                    .type(quoted)
                    .type(digits)
                    .type(seven)
                    .type(plus)
                    .type(plusEqual)
                    .type(binaryBang)
                    .type(waddle)
                    .type(snooze)
                    .type(multiplier)
                    .type(array)
                    .type(restrictedArray)
                    .type(record)
                    .type(pair)
                    .type(ratio)
                    .build())
            .group("arrayChildren", new GroupBuilder().type(one).type(multiback).build())
            .build();

    /*
    If the order isn't correct, context.setSyntax may be called after the selection is replaced by the converted
    value's selection.
     */
    final Atom gap = syntax.gap.create();
    final ValuePrimitive primitive = (ValuePrimitive) gap.data.get("gap");
    new GeneralTestWizard(syntax, gap)
        .run(context -> context.history.apply(context, new ChangePrimitiveSet(primitive, "\"")))
        .checkArrayTree(gap)
        .run(context -> primitive.selectDown(context))
        .checkArrayTree(new TreeBuilder(quoted).add("value", "").build())
        .run(
            context ->
                assertThat(
                    context.selection,
                    not(instanceOf(FrontGapBase.GapVisualPrimitive.GapSelection.class))));
  }

  // ========================================================================
  // ========================================================================
  // Unit gap
  @Test
  public void unitGap_unitContinueInside() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(1)
            .build();
    FreeAtomType one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .autoComplete(-1)
            .build();
    FreeAtomType two =
        new TypeBuilder("two")
            .back(Helper.buildBackPrimitive("two"))
            .front(new FrontMarkBuilder("two").build())
            .autoComplete(-1)
            .build();
    FreeAtomType three =
        new TypeBuilder("three")
            .back(Helper.buildBackPrimitive("three"))
            .front(new FrontMarkBuilder("three").build())
            .autoComplete(-1)
            .build();
    FreeAtomType four =
        new TypeBuilder("four")
            .back(Helper.buildBackPrimitive("four"))
            .front(new FrontMarkBuilder("four").build())
            .autoComplete(-1)
            .build();
    FreeAtomType five =
        new TypeBuilder("five")
            .back(Helper.buildBackPrimitive("five"))
            .front(new FrontMarkBuilder("five").build())
            .autoComplete(-1)
            .build();
    FreeAtomType seven =
        new TypeBuilder("seven")
            .back(Helper.buildBackPrimitive("7"))
            .front(new FrontMarkBuilder("7").build())
            .autoComplete(-1)
            .build();
    FreeAtomType multiback =
        new TypeBuilder("multiback")
            .back(Helper.buildBackDataPrimitive("a"))
            .back(Helper.buildBackDataPrimitive("b"))
            .middlePrimitive("a")
            .middlePrimitive("b")
            .frontDataPrimitive("a")
            .frontMark("^")
            .frontDataPrimitive("b")
            .autoComplete(-1)
            .build();
    FreeAtomType quoted =
        new TypeBuilder("quoted")
            .middlePrimitive("value")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("value")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(1)
            .build();
    FreeAtomType digits =
        new TypeBuilder("digits")
            .middlePrimitiveDigits("value")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .autoComplete(1)
            .build();
    FreeAtomType doubleQuoted =
        new TypeBuilder("doubleuoted")
            .middlePrimitive("first")
            .middlePrimitive("second")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataPrimitive("first"))
                    .add("second", Helper.buildBackDataPrimitive("second"))
                    .build())
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("first")
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("second")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(-1)
            .build();
    FreeAtomType plus =
        new TypeBuilder("plus")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("+")
            .frontDataNode("second")
            .autoComplete(-1)
            .build();
    FreeAtomType plusEqual =
        new TypeBuilder("plusequal")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("+=")
            .frontDataNode("second")
            .autoComplete(-1)
            .build();
    FreeAtomType binaryBang =
        new TypeBuilder("bang")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("!")
            .frontDataNode("second")
            .autoComplete(1)
            .build();
    FreeAtomType waddle =
        new TypeBuilder("waddle")
            .middleAtom("first", "any")
            .back(new BackRecordBuilder().add("first", Helper.buildBackDataAtom("first")).build())
            .frontDataNode("first")
            .frontMark("?")
            .autoComplete(1)
            .build();
    FreeAtomType snooze =
        new TypeBuilder("snooze")
            .middleAtom("value", "any")
            .back(new BackRecordBuilder().add("value", Helper.buildBackDataAtom("value")).build())
            .frontMark("#")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType multiplier =
        new TypeBuilder("multiplier")
            .middlePrimitive("text")
            .middleAtom("value", "any")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value"))
                    .add("text", Helper.buildBackDataPrimitive("text"))
                    .build())
            .frontMark("x")
            .frontDataPrimitive("text")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType array =
        new TypeBuilder("array")
            .middleArray("value", "any")
            .back(Helper.buildBackDataArray("value"))
            .frontMark("[")
            .front(
                new FrontDataArrayBuilder("value")
                    .addSeparator(new FrontMarkBuilder(", ").build())
                    .build())
            .frontMark("]")
            .autoComplete(1)
            .build();
    FreeAtomType doubleArray =
        new TypeBuilder("doublearray")
            .middleArray("first", "any")
            .middleArray("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataArray("first"))
                    .add("second", Helper.buildBackDataArray("second"))
                    .build())
            .frontMark("[")
            .frontDataArray("first")
            .frontMark("?")
            .frontDataArray("second")
            .frontMark("]")
            .build();
    FreeAtomType record =
        new TypeBuilder("record")
            .middleRecord("value", "record_element")
            .back(Helper.buildBackDataRecord("value"))
            .frontMark("{")
            .frontDataArray("value")
            .frontMark("}")
            .autoComplete(1)
            .build();
    FreeAtomType recordElement =
        new TypeBuilder("record_element")
            .middlePrimitive("key")
            .middleAtom("value", "any")
            .back(Helper.buildBackDataKey("key"))
            .back(Helper.buildBackDataAtom("value"))
            .frontDataPrimitive("key")
            .frontMark(": ")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType pair =
        new TypeBuilder("pair")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackArrayBuilder()
                    .add(Helper.buildBackDataAtom("first"))
                    .add(Helper.buildBackDataAtom("second"))
                    .build())
            .frontMark("<")
            .frontDataNode("first")
            .frontMark(", ")
            .frontDataNode("second")
            .frontMark(">")
            .autoComplete(1)
            .build();
    FreeAtomType ratio =
        new TypeBuilder("ratio")
            .middlePrimitive("first")
            .middlePrimitive("second")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataPrimitive("first"))
                    .add("second", Helper.buildBackDataPrimitive("second"))
                    .build())
            .frontMark("<")
            .frontDataPrimitive("first")
            .frontMark(":")
            .frontDataPrimitive("second")
            .frontMark(">")
            .build();
    FreeAtomType restricted =
        new TypeBuilder("restricted")
            .middleAtom("value", "restricted_group")
            .back(new BackRecordBuilder().add("value", Helper.buildBackDataAtom("value")).build())
            .frontDataNode("value")
            .build();
    FreeAtomType restrictedArray =
        new TypeBuilder("restricted_array")
            .middleArray("value", "restricted_array_group")
            .back(Helper.buildBackDataArray("value"))
            .frontMark("_")
            .front(new FrontDataArrayBuilder("value").build())
            .autoComplete(1)
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(infinity)
            .type(one)
            .type(two)
            .type(three)
            .type(four)
            .type(five)
            .type(seven)
            .type(multiback)
            .type(quoted)
            .type(digits)
            .type(doubleQuoted)
            .type(plus)
            .type(plusEqual)
            .type(binaryBang)
            .type(waddle)
            .type(snooze)
            .type(multiplier)
            .type(array)
            .type(doubleArray)
            .type(record)
            .type(recordElement)
            .type(pair)
            .type(ratio)
            .type(restricted)
            .type(restrictedArray)
            .group(
                "test_group_1",
                new GroupBuilder()
                    .type(infinity)
                    .type(one)
                    .type(multiback)
                    .group("test_group_2")
                    .build())
            .group("test_group_2", new GroupBuilder().type(quoted).build())
            .group("restricted_group", new GroupBuilder().type(quoted).build())
            .group("restricted_array_group", new GroupBuilder().type(quoted).build())
            .group(
                "any",
                new GroupBuilder()
                    .type(infinity)
                    .type(one)
                    .type(two)
                    .type(three)
                    .type(four)
                    .type(five)
                    .type(quoted)
                    .type(digits)
                    .type(seven)
                    .type(plus)
                    .type(plusEqual)
                    .type(binaryBang)
                    .type(waddle)
                    .type(snooze)
                    .type(multiplier)
                    .type(array)
                    .type(restrictedArray)
                    .type(record)
                    .type(pair)
                    .type(ratio)
                    .build())
            .group("arrayChildren", new GroupBuilder().type(one).type(multiback).build())
            .build();

    final Context context = blank(syntax);
    context.selection.receiveText(context, "\"");
    context.selection.receiveText(context, "e");
    assertTreeEqual(
        context,
        new TreeBuilder(quoted).add("value", "e").build(),
        Helper.rootArray(context.document));
  }

  @Test
  public void unitGap_unitContinueInsideArray() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(1)
            .build();
    FreeAtomType one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .autoComplete(-1)
            .build();
    FreeAtomType two =
        new TypeBuilder("two")
            .back(Helper.buildBackPrimitive("two"))
            .front(new FrontMarkBuilder("two").build())
            .autoComplete(-1)
            .build();
    FreeAtomType three =
        new TypeBuilder("three")
            .back(Helper.buildBackPrimitive("three"))
            .front(new FrontMarkBuilder("three").build())
            .autoComplete(-1)
            .build();
    FreeAtomType four =
        new TypeBuilder("four")
            .back(Helper.buildBackPrimitive("four"))
            .front(new FrontMarkBuilder("four").build())
            .autoComplete(-1)
            .build();
    FreeAtomType five =
        new TypeBuilder("five")
            .back(Helper.buildBackPrimitive("five"))
            .front(new FrontMarkBuilder("five").build())
            .autoComplete(-1)
            .build();
    FreeAtomType seven =
        new TypeBuilder("seven")
            .back(Helper.buildBackPrimitive("7"))
            .front(new FrontMarkBuilder("7").build())
            .autoComplete(-1)
            .build();
    FreeAtomType multiback =
        new TypeBuilder("multiback")
            .back(Helper.buildBackDataPrimitive("a"))
            .back(Helper.buildBackDataPrimitive("b"))
            .middlePrimitive("a")
            .middlePrimitive("b")
            .frontDataPrimitive("a")
            .frontMark("^")
            .frontDataPrimitive("b")
            .autoComplete(-1)
            .build();
    FreeAtomType quoted =
        new TypeBuilder("quoted")
            .middlePrimitive("value")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("value")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(1)
            .build();
    FreeAtomType digits =
        new TypeBuilder("digits")
            .middlePrimitiveDigits("value")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .autoComplete(1)
            .build();
    FreeAtomType doubleQuoted =
        new TypeBuilder("doubleuoted")
            .middlePrimitive("first")
            .middlePrimitive("second")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataPrimitive("first"))
                    .add("second", Helper.buildBackDataPrimitive("second"))
                    .build())
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("first")
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("second")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(-1)
            .build();
    FreeAtomType plus =
        new TypeBuilder("plus")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("+")
            .frontDataNode("second")
            .autoComplete(-1)
            .build();
    FreeAtomType plusEqual =
        new TypeBuilder("plusequal")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("+=")
            .frontDataNode("second")
            .autoComplete(-1)
            .build();
    FreeAtomType binaryBang =
        new TypeBuilder("bang")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("!")
            .frontDataNode("second")
            .autoComplete(1)
            .build();
    FreeAtomType waddle =
        new TypeBuilder("waddle")
            .middleAtom("first", "any")
            .back(new BackRecordBuilder().add("first", Helper.buildBackDataAtom("first")).build())
            .frontDataNode("first")
            .frontMark("?")
            .autoComplete(1)
            .build();
    FreeAtomType snooze =
        new TypeBuilder("snooze")
            .middleAtom("value", "any")
            .back(new BackRecordBuilder().add("value", Helper.buildBackDataAtom("value")).build())
            .frontMark("#")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType multiplier =
        new TypeBuilder("multiplier")
            .middlePrimitive("text")
            .middleAtom("value", "any")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value"))
                    .add("text", Helper.buildBackDataPrimitive("text"))
                    .build())
            .frontMark("x")
            .frontDataPrimitive("text")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType array =
        new TypeBuilder("array")
            .middleArray("value", "any")
            .back(Helper.buildBackDataArray("value"))
            .frontMark("[")
            .front(
                new FrontDataArrayBuilder("value")
                    .addSeparator(new FrontMarkBuilder(", ").build())
                    .build())
            .frontMark("]")
            .autoComplete(1)
            .build();
    FreeAtomType doubleArray =
        new TypeBuilder("doublearray")
            .middleArray("first", "any")
            .middleArray("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataArray("first"))
                    .add("second", Helper.buildBackDataArray("second"))
                    .build())
            .frontMark("[")
            .frontDataArray("first")
            .frontMark("?")
            .frontDataArray("second")
            .frontMark("]")
            .build();
    FreeAtomType record =
        new TypeBuilder("record")
            .middleRecord("value", "record_element")
            .back(Helper.buildBackDataRecord("value"))
            .frontMark("{")
            .frontDataArray("value")
            .frontMark("}")
            .autoComplete(1)
            .build();
    FreeAtomType recordElement =
        new TypeBuilder("record_element")
            .middlePrimitive("key")
            .middleAtom("value", "any")
            .back(Helper.buildBackDataKey("key"))
            .back(Helper.buildBackDataAtom("value"))
            .frontDataPrimitive("key")
            .frontMark(": ")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType pair =
        new TypeBuilder("pair")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackArrayBuilder()
                    .add(Helper.buildBackDataAtom("first"))
                    .add(Helper.buildBackDataAtom("second"))
                    .build())
            .frontMark("<")
            .frontDataNode("first")
            .frontMark(", ")
            .frontDataNode("second")
            .frontMark(">")
            .autoComplete(1)
            .build();
    FreeAtomType ratio =
        new TypeBuilder("ratio")
            .middlePrimitive("first")
            .middlePrimitive("second")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataPrimitive("first"))
                    .add("second", Helper.buildBackDataPrimitive("second"))
                    .build())
            .frontMark("<")
            .frontDataPrimitive("first")
            .frontMark(":")
            .frontDataPrimitive("second")
            .frontMark(">")
            .build();
    FreeAtomType restricted =
        new TypeBuilder("restricted")
            .middleAtom("value", "restricted_group")
            .back(new BackRecordBuilder().add("value", Helper.buildBackDataAtom("value")).build())
            .frontDataNode("value")
            .build();
    FreeAtomType restrictedArray =
        new TypeBuilder("restricted_array")
            .middleArray("value", "restricted_array_group")
            .back(Helper.buildBackDataArray("value"))
            .frontMark("_")
            .front(new FrontDataArrayBuilder("value").build())
            .autoComplete(1)
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(infinity)
            .type(one)
            .type(two)
            .type(three)
            .type(four)
            .type(five)
            .type(seven)
            .type(multiback)
            .type(quoted)
            .type(digits)
            .type(doubleQuoted)
            .type(plus)
            .type(plusEqual)
            .type(binaryBang)
            .type(waddle)
            .type(snooze)
            .type(multiplier)
            .type(array)
            .type(doubleArray)
            .type(record)
            .type(recordElement)
            .type(pair)
            .type(ratio)
            .type(restricted)
            .type(restrictedArray)
            .group(
                "test_group_1",
                new GroupBuilder()
                    .type(infinity)
                    .type(one)
                    .type(multiback)
                    .group("test_group_2")
                    .build())
            .group("test_group_2", new GroupBuilder().type(quoted).build())
            .group("restricted_group", new GroupBuilder().type(quoted).build())
            .group("restricted_array_group", new GroupBuilder().type(quoted).build())
            .group(
                "any",
                new GroupBuilder()
                    .type(infinity)
                    .type(one)
                    .type(two)
                    .type(three)
                    .type(four)
                    .type(five)
                    .type(quoted)
                    .type(digits)
                    .type(seven)
                    .type(plus)
                    .type(plusEqual)
                    .type(binaryBang)
                    .type(waddle)
                    .type(snooze)
                    .type(multiplier)
                    .type(array)
                    .type(restrictedArray)
                    .type(record)
                    .type(pair)
                    .type(ratio)
                    .build())
            .group("arrayChildren", new GroupBuilder().type(one).type(multiback).build())
            .build();

    final Context context = blank(syntax);
    context.selection.receiveText(context, "[");
    context.selection.receiveText(context, "e");
    assertTreeEqual(
        context,
        new TreeBuilder(array)
            .addArray("value", new TreeBuilder(syntax.gap).add("gap", "e").build())
            .build(),
        Helper.rootArray(context.document));
  }

  @Test
  public void suffix_suffixContinueInside() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(1)
            .build();
    FreeAtomType one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .autoComplete(-1)
            .build();
    FreeAtomType two =
        new TypeBuilder("two")
            .back(Helper.buildBackPrimitive("two"))
            .front(new FrontMarkBuilder("two").build())
            .autoComplete(-1)
            .build();
    FreeAtomType three =
        new TypeBuilder("three")
            .back(Helper.buildBackPrimitive("three"))
            .front(new FrontMarkBuilder("three").build())
            .autoComplete(-1)
            .build();
    FreeAtomType four =
        new TypeBuilder("four")
            .back(Helper.buildBackPrimitive("four"))
            .front(new FrontMarkBuilder("four").build())
            .autoComplete(-1)
            .build();
    FreeAtomType five =
        new TypeBuilder("five")
            .back(Helper.buildBackPrimitive("five"))
            .front(new FrontMarkBuilder("five").build())
            .autoComplete(-1)
            .build();
    FreeAtomType seven =
        new TypeBuilder("seven")
            .back(Helper.buildBackPrimitive("7"))
            .front(new FrontMarkBuilder("7").build())
            .autoComplete(-1)
            .build();
    FreeAtomType multiback =
        new TypeBuilder("multiback")
            .back(Helper.buildBackDataPrimitive("a"))
            .back(Helper.buildBackDataPrimitive("b"))
            .middlePrimitive("a")
            .middlePrimitive("b")
            .frontDataPrimitive("a")
            .frontMark("^")
            .frontDataPrimitive("b")
            .autoComplete(-1)
            .build();
    FreeAtomType quoted =
        new TypeBuilder("quoted")
            .middlePrimitive("value")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("value")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(1)
            .build();
    FreeAtomType digits =
        new TypeBuilder("digits")
            .middlePrimitiveDigits("value")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .autoComplete(1)
            .build();
    FreeAtomType doubleQuoted =
        new TypeBuilder("doubleuoted")
            .middlePrimitive("first")
            .middlePrimitive("second")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataPrimitive("first"))
                    .add("second", Helper.buildBackDataPrimitive("second"))
                    .build())
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("first")
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("second")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(-1)
            .build();
    FreeAtomType plus =
        new TypeBuilder("plus")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("+")
            .frontDataNode("second")
            .autoComplete(-1)
            .build();
    FreeAtomType plusEqual =
        new TypeBuilder("plusequal")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("+=")
            .frontDataNode("second")
            .autoComplete(-1)
            .build();
    FreeAtomType binaryBang =
        new TypeBuilder("bang")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("!")
            .frontDataNode("second")
            .autoComplete(1)
            .build();
    FreeAtomType waddle =
        new TypeBuilder("waddle")
            .middleAtom("first", "any")
            .back(new BackRecordBuilder().add("first", Helper.buildBackDataAtom("first")).build())
            .frontDataNode("first")
            .frontMark("?")
            .autoComplete(1)
            .build();
    FreeAtomType snooze =
        new TypeBuilder("snooze")
            .middleAtom("value", "any")
            .back(new BackRecordBuilder().add("value", Helper.buildBackDataAtom("value")).build())
            .frontMark("#")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType multiplier =
        new TypeBuilder("multiplier")
            .middlePrimitive("text")
            .middleAtom("value", "any")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value"))
                    .add("text", Helper.buildBackDataPrimitive("text"))
                    .build())
            .frontMark("x")
            .frontDataPrimitive("text")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType array =
        new TypeBuilder("array")
            .middleArray("value", "any")
            .back(Helper.buildBackDataArray("value"))
            .frontMark("[")
            .front(
                new FrontDataArrayBuilder("value")
                    .addSeparator(new FrontMarkBuilder(", ").build())
                    .build())
            .frontMark("]")
            .autoComplete(1)
            .build();
    FreeAtomType doubleArray =
        new TypeBuilder("doublearray")
            .middleArray("first", "any")
            .middleArray("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataArray("first"))
                    .add("second", Helper.buildBackDataArray("second"))
                    .build())
            .frontMark("[")
            .frontDataArray("first")
            .frontMark("?")
            .frontDataArray("second")
            .frontMark("]")
            .build();
    FreeAtomType record =
        new TypeBuilder("record")
            .middleRecord("value", "record_element")
            .back(Helper.buildBackDataRecord("value"))
            .frontMark("{")
            .frontDataArray("value")
            .frontMark("}")
            .autoComplete(1)
            .build();
    FreeAtomType recordElement =
        new TypeBuilder("record_element")
            .middlePrimitive("key")
            .middleAtom("value", "any")
            .back(Helper.buildBackDataKey("key"))
            .back(Helper.buildBackDataAtom("value"))
            .frontDataPrimitive("key")
            .frontMark(": ")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType pair =
        new TypeBuilder("pair")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackArrayBuilder()
                    .add(Helper.buildBackDataAtom("first"))
                    .add(Helper.buildBackDataAtom("second"))
                    .build())
            .frontMark("<")
            .frontDataNode("first")
            .frontMark(", ")
            .frontDataNode("second")
            .frontMark(">")
            .autoComplete(1)
            .build();
    FreeAtomType ratio =
        new TypeBuilder("ratio")
            .middlePrimitive("first")
            .middlePrimitive("second")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataPrimitive("first"))
                    .add("second", Helper.buildBackDataPrimitive("second"))
                    .build())
            .frontMark("<")
            .frontDataPrimitive("first")
            .frontMark(":")
            .frontDataPrimitive("second")
            .frontMark(">")
            .build();
    FreeAtomType restricted =
        new TypeBuilder("restricted")
            .middleAtom("value", "restricted_group")
            .back(new BackRecordBuilder().add("value", Helper.buildBackDataAtom("value")).build())
            .frontDataNode("value")
            .build();
    FreeAtomType restrictedArray =
        new TypeBuilder("restricted_array")
            .middleArray("value", "restricted_array_group")
            .back(Helper.buildBackDataArray("value"))
            .frontMark("_")
            .front(new FrontDataArrayBuilder("value").build())
            .autoComplete(1)
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(infinity)
            .type(one)
            .type(two)
            .type(three)
            .type(four)
            .type(five)
            .type(seven)
            .type(multiback)
            .type(quoted)
            .type(digits)
            .type(doubleQuoted)
            .type(plus)
            .type(plusEqual)
            .type(binaryBang)
            .type(waddle)
            .type(snooze)
            .type(multiplier)
            .type(array)
            .type(doubleArray)
            .type(record)
            .type(recordElement)
            .type(pair)
            .type(ratio)
            .type(restricted)
            .type(restrictedArray)
            .group(
                "test_group_1",
                new GroupBuilder()
                    .type(infinity)
                    .type(one)
                    .type(multiback)
                    .group("test_group_2")
                    .build())
            .group("test_group_2", new GroupBuilder().type(quoted).build())
            .group("restricted_group", new GroupBuilder().type(quoted).build())
            .group("restricted_array_group", new GroupBuilder().type(quoted).build())
            .group(
                "any",
                new GroupBuilder()
                    .type(infinity)
                    .type(one)
                    .type(two)
                    .type(three)
                    .type(four)
                    .type(five)
                    .type(quoted)
                    .type(digits)
                    .type(seven)
                    .type(plus)
                    .type(plusEqual)
                    .type(binaryBang)
                    .type(waddle)
                    .type(snooze)
                    .type(multiplier)
                    .type(array)
                    .type(restrictedArray)
                    .type(record)
                    .type(pair)
                    .type(ratio)
                    .build())
            .group("arrayChildren", new GroupBuilder().type(one).type(multiback).build())
            .build();

    innerTestTransform(
        syntax,
        () -> syntax.suffixGap.create(true, new TreeBuilder(one).build()),
        context -> {
          Helper.rootArray(context.document).data.get(0).data.get("gap").selectDown(context);
          context.selection.receiveText(context, "?");
          context.selection.receiveText(context, "e");
        },
        new TreeBuilder(syntax.suffixGap)
            .addArray(
                "value",
                ImmutableList.of(
                    new TreeBuilder(waddle).add("first", new TreeBuilder(one)).build()))
            .add("gap", "e")
            .build());
  }

  // ========================================================================
  // Suffix

  private void innerTestTransform(
      final Syntax syntax,
      final Supplier<Atom> start,
      final Consumer<Context> transform,
      final Atom end) {
    final Context context = buildDoc(syntax, start.get());
    transform.accept(context);
    assertTreeEqual(context, end, Helper.rootArray(context.document));
    context.history.undo(context);
    assertTreeEqual(context, start.get(), Helper.rootArray(context.document));
    context.history.redo(context);
    assertTreeEqual(context, end, Helper.rootArray(context.document));
  }

  @Test
  public void suffix_textMark3Immediate() {
    FreeAtomType one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .build();
    FreeAtomType two =
        new TypeBuilder("two")
            .back(Helper.buildBackPrimitive("two"))
            .front(new FrontMarkBuilder("two").build())
            .build();
    FreeAtomType three =
        new TypeBuilder("three")
            .back(Helper.buildBackPrimitive("three"))
            .front(new FrontMarkBuilder("three").build())
            .build();
    FreeAtomType textMark1 =
        new TypeBuilder("textmark1")
            .middlePrimitiveLetters("text")
            .middleAtom("atom", "any")
            .back(
                new BackRecordBuilder()
                    .add("text", Helper.buildBackDataPrimitive("text"))
                    .add("atom", Helper.buildBackDataAtom("atom"))
                    .build())
            .frontDataPrimitive("text")
            .frontMark("$1")
            .frontDataNode("atom")
            .autoComplete(1)
            .build();
    FreeAtomType textMark2 =
        new TypeBuilder("textmark2")
            .middlePrimitiveLetters("text")
            .middleAtom("atom", "any")
            .middleAtom("atom2", "any")
            .back(
                new BackRecordBuilder()
                    .add("text", Helper.buildBackDataPrimitive("text"))
                    .add("atom", Helper.buildBackDataAtom("atom"))
                    .add("atom2", Helper.buildBackDataAtom("atom2"))
                    .build())
            .frontDataPrimitive("text")
            .frontMark("$2")
            .frontDataNode("atom")
            .frontDataNode("atom2")
            .autoComplete(1)
            .build();
    FreeAtomType textMark3 =
        new TypeBuilder("textmark3")
            .middlePrimitiveLetters("text")
            .middleAtom("atom", "any")
            .middleAtom("atom2", "any")
            .back(
                new BackRecordBuilder()
                    .add("text", Helper.buildBackDataPrimitive("text"))
                    .add("atom", Helper.buildBackDataAtom("atom"))
                    .add("atom2", Helper.buildBackDataAtom("atom2"))
                    .build())
            .frontDataNode("atom2")
            .frontDataPrimitive("text")
            .frontMark("$3")
            .frontDataNode("atom")
            .autoComplete(1)
            .build();
    FreeAtomType ambiguateTextMark3 =
        new TypeBuilder("atextmark3")
            .middleAtom("atom", "any")
            .back(new BackRecordBuilder().add("atom", Helper.buildBackDataAtom("atom")).build())
            .frontDataNode("atom")
            .frontMark("t")
            .autoComplete(1)
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(one)
            .type(two)
            .type(three)
            .type(textMark1)
            .type(textMark2)
            .type(textMark3)
            .type(ambiguateTextMark3)
            .group(
                "any",
                new GroupBuilder()
                    .type(one)
                    .type(two)
                    .type(three)
                    .type(textMark1)
                    .type(textMark2)
                    .type(textMark3)
                    .type(ambiguateTextMark3)
                    .build())
            .build();

    final Atom atom = syntax.suffixGap.create(true, new TreeBuilder(one).build());
    new GeneralTestWizard(syntax, atom)
        .run(context -> atom.data.get("gap").selectDown(context))
        .sendText("t")
        .sendText("$")
        .sendText("t")
        .checkArrayTree(
            new TreeBuilder(textMark3)
                .add("text", "t")
                .add("atom", new TreeBuilder(syntax.gap).add("gap", "t"))
                .add("atom2", new TreeBuilder(one))
                .build());
  }

  @Test
  public void suffix_suffixOnlyPlaceAllowedTypes() {
    final Atom gap =
        SyntaxRestrictedRoot.syntax.suffixGap.create(
            true, new TreeBuilder(SyntaxRestrictedRoot.quoted).add("value", "hi").build());
    new GeneralTestWizard(SyntaxRestrictedRoot.syntax, gap)
        .run(context -> gap.data.get("gap").selectDown(context))
        .sendText("!")
        .checkArrayTree(
            new TreeBuilder(SyntaxRestrictedRoot.syntax.suffixGap)
                .addArray(
                    "value",
                    new TreeBuilder(SyntaxRestrictedRoot.quoted).add("value", "hi").build())
                .add("gap", "!")
                .build());
  }

  // ========================================================================
  // ========================================================================
  // Prefix
  @Test
  public void prefix_prefixContinue() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(1)
            .build();
    FreeAtomType one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .autoComplete(-1)
            .build();
    FreeAtomType two =
        new TypeBuilder("two")
            .back(Helper.buildBackPrimitive("two"))
            .front(new FrontMarkBuilder("two").build())
            .autoComplete(-1)
            .build();
    FreeAtomType three =
        new TypeBuilder("three")
            .back(Helper.buildBackPrimitive("three"))
            .front(new FrontMarkBuilder("three").build())
            .autoComplete(-1)
            .build();
    FreeAtomType four =
        new TypeBuilder("four")
            .back(Helper.buildBackPrimitive("four"))
            .front(new FrontMarkBuilder("four").build())
            .autoComplete(-1)
            .build();
    FreeAtomType five =
        new TypeBuilder("five")
            .back(Helper.buildBackPrimitive("five"))
            .front(new FrontMarkBuilder("five").build())
            .autoComplete(-1)
            .build();
    FreeAtomType seven =
        new TypeBuilder("seven")
            .back(Helper.buildBackPrimitive("7"))
            .front(new FrontMarkBuilder("7").build())
            .autoComplete(-1)
            .build();
    FreeAtomType multiback =
        new TypeBuilder("multiback")
            .back(Helper.buildBackDataPrimitive("a"))
            .back(Helper.buildBackDataPrimitive("b"))
            .middlePrimitive("a")
            .middlePrimitive("b")
            .frontDataPrimitive("a")
            .frontMark("^")
            .frontDataPrimitive("b")
            .autoComplete(-1)
            .build();
    FreeAtomType quoted =
        new TypeBuilder("quoted")
            .middlePrimitive("value")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("value")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(1)
            .build();
    FreeAtomType digits =
        new TypeBuilder("digits")
            .middlePrimitiveDigits("value")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .autoComplete(1)
            .build();
    FreeAtomType doubleQuoted =
        new TypeBuilder("doubleuoted")
            .middlePrimitive("first")
            .middlePrimitive("second")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataPrimitive("first"))
                    .add("second", Helper.buildBackDataPrimitive("second"))
                    .build())
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("first")
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("second")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(-1)
            .build();
    FreeAtomType plus =
        new TypeBuilder("plus")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("+")
            .frontDataNode("second")
            .autoComplete(-1)
            .build();
    FreeAtomType plusEqual =
        new TypeBuilder("plusequal")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("+=")
            .frontDataNode("second")
            .autoComplete(-1)
            .build();
    FreeAtomType binaryBang =
        new TypeBuilder("bang")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("!")
            .frontDataNode("second")
            .autoComplete(1)
            .build();
    FreeAtomType waddle =
        new TypeBuilder("waddle")
            .middleAtom("first", "any")
            .back(new BackRecordBuilder().add("first", Helper.buildBackDataAtom("first")).build())
            .frontDataNode("first")
            .frontMark("?")
            .autoComplete(1)
            .build();
    FreeAtomType snooze =
        new TypeBuilder("snooze")
            .middleAtom("value", "any")
            .back(new BackRecordBuilder().add("value", Helper.buildBackDataAtom("value")).build())
            .frontMark("#")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType multiplier =
        new TypeBuilder("multiplier")
            .middlePrimitive("text")
            .middleAtom("value", "any")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value"))
                    .add("text", Helper.buildBackDataPrimitive("text"))
                    .build())
            .frontMark("x")
            .frontDataPrimitive("text")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType array =
        new TypeBuilder("array")
            .middleArray("value", "any")
            .back(Helper.buildBackDataArray("value"))
            .frontMark("[")
            .front(
                new FrontDataArrayBuilder("value")
                    .addSeparator(new FrontMarkBuilder(", ").build())
                    .build())
            .frontMark("]")
            .autoComplete(1)
            .build();
    FreeAtomType doubleArray =
        new TypeBuilder("doublearray")
            .middleArray("first", "any")
            .middleArray("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataArray("first"))
                    .add("second", Helper.buildBackDataArray("second"))
                    .build())
            .frontMark("[")
            .frontDataArray("first")
            .frontMark("?")
            .frontDataArray("second")
            .frontMark("]")
            .build();
    FreeAtomType record =
        new TypeBuilder("record")
            .middleRecord("value", "record_element")
            .back(Helper.buildBackDataRecord("value"))
            .frontMark("{")
            .frontDataArray("value")
            .frontMark("}")
            .autoComplete(1)
            .build();
    FreeAtomType recordElement =
        new TypeBuilder("record_element")
            .middlePrimitive("key")
            .middleAtom("value", "any")
            .back(Helper.buildBackDataKey("key"))
            .back(Helper.buildBackDataAtom("value"))
            .frontDataPrimitive("key")
            .frontMark(": ")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType pair =
        new TypeBuilder("pair")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackArrayBuilder()
                    .add(Helper.buildBackDataAtom("first"))
                    .add(Helper.buildBackDataAtom("second"))
                    .build())
            .frontMark("<")
            .frontDataNode("first")
            .frontMark(", ")
            .frontDataNode("second")
            .frontMark(">")
            .autoComplete(1)
            .build();
    FreeAtomType ratio =
        new TypeBuilder("ratio")
            .middlePrimitive("first")
            .middlePrimitive("second")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataPrimitive("first"))
                    .add("second", Helper.buildBackDataPrimitive("second"))
                    .build())
            .frontMark("<")
            .frontDataPrimitive("first")
            .frontMark(":")
            .frontDataPrimitive("second")
            .frontMark(">")
            .build();
    FreeAtomType restricted =
        new TypeBuilder("restricted")
            .middleAtom("value", "restricted_group")
            .back(new BackRecordBuilder().add("value", Helper.buildBackDataAtom("value")).build())
            .frontDataNode("value")
            .build();
    FreeAtomType restrictedArray =
        new TypeBuilder("restricted_array")
            .middleArray("value", "restricted_array_group")
            .back(Helper.buildBackDataArray("value"))
            .frontMark("_")
            .front(new FrontDataArrayBuilder("value").build())
            .autoComplete(1)
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(infinity)
            .type(one)
            .type(two)
            .type(three)
            .type(four)
            .type(five)
            .type(seven)
            .type(multiback)
            .type(quoted)
            .type(digits)
            .type(doubleQuoted)
            .type(plus)
            .type(plusEqual)
            .type(binaryBang)
            .type(waddle)
            .type(snooze)
            .type(multiplier)
            .type(array)
            .type(doubleArray)
            .type(record)
            .type(recordElement)
            .type(pair)
            .type(ratio)
            .type(restricted)
            .type(restrictedArray)
            .group(
                "test_group_1",
                new GroupBuilder()
                    .type(infinity)
                    .type(one)
                    .type(multiback)
                    .group("test_group_2")
                    .build())
            .group("test_group_2", new GroupBuilder().type(quoted).build())
            .group("restricted_group", new GroupBuilder().type(quoted).build())
            .group("restricted_array_group", new GroupBuilder().type(quoted).build())
            .group(
                "any",
                new GroupBuilder()
                    .type(infinity)
                    .type(one)
                    .type(two)
                    .type(three)
                    .type(four)
                    .type(five)
                    .type(quoted)
                    .type(digits)
                    .type(seven)
                    .type(plus)
                    .type(plusEqual)
                    .type(binaryBang)
                    .type(waddle)
                    .type(snooze)
                    .type(multiplier)
                    .type(array)
                    .type(restrictedArray)
                    .type(record)
                    .type(pair)
                    .type(ratio)
                    .build())
            .group("arrayChildren", new GroupBuilder().type(one).type(multiback).build())
            .build();

    innerTestTransform(
        syntax,
        () ->
            new TreeBuilder(syntax.prefixGap)
                .add("gap", "")
                .addArray("value", new TreeBuilder(one).build())
                .build(),
        context -> {
          ((ValuePrimitive) context.locateLong(new Path("0", "gap"))).selectDown(context);
          context.selection.receiveText(context, "x");
          context.selection.receiveText(context, "13");
        },
        new TreeBuilder(multiplier).add("value", new TreeBuilder(one)).add("text", "13").build());
  }

  @Test
  public void prefix_prefixContinueWrap() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(1)
            .build();
    FreeAtomType one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .autoComplete(-1)
            .build();
    FreeAtomType two =
        new TypeBuilder("two")
            .back(Helper.buildBackPrimitive("two"))
            .front(new FrontMarkBuilder("two").build())
            .autoComplete(-1)
            .build();
    FreeAtomType three =
        new TypeBuilder("three")
            .back(Helper.buildBackPrimitive("three"))
            .front(new FrontMarkBuilder("three").build())
            .autoComplete(-1)
            .build();
    FreeAtomType four =
        new TypeBuilder("four")
            .back(Helper.buildBackPrimitive("four"))
            .front(new FrontMarkBuilder("four").build())
            .autoComplete(-1)
            .build();
    FreeAtomType five =
        new TypeBuilder("five")
            .back(Helper.buildBackPrimitive("five"))
            .front(new FrontMarkBuilder("five").build())
            .autoComplete(-1)
            .build();
    FreeAtomType seven =
        new TypeBuilder("seven")
            .back(Helper.buildBackPrimitive("7"))
            .front(new FrontMarkBuilder("7").build())
            .autoComplete(-1)
            .build();
    FreeAtomType multiback =
        new TypeBuilder("multiback")
            .back(Helper.buildBackDataPrimitive("a"))
            .back(Helper.buildBackDataPrimitive("b"))
            .middlePrimitive("a")
            .middlePrimitive("b")
            .frontDataPrimitive("a")
            .frontMark("^")
            .frontDataPrimitive("b")
            .autoComplete(-1)
            .build();
    FreeAtomType quoted =
        new TypeBuilder("quoted")
            .middlePrimitive("value")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("value")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(1)
            .build();
    FreeAtomType digits =
        new TypeBuilder("digits")
            .middlePrimitiveDigits("value")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .autoComplete(1)
            .build();
    FreeAtomType doubleQuoted =
        new TypeBuilder("doubleuoted")
            .middlePrimitive("first")
            .middlePrimitive("second")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataPrimitive("first"))
                    .add("second", Helper.buildBackDataPrimitive("second"))
                    .build())
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("first")
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("second")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(-1)
            .build();
    FreeAtomType plus =
        new TypeBuilder("plus")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("+")
            .frontDataNode("second")
            .autoComplete(-1)
            .build();
    FreeAtomType plusEqual =
        new TypeBuilder("plusequal")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("+=")
            .frontDataNode("second")
            .autoComplete(-1)
            .build();
    FreeAtomType binaryBang =
        new TypeBuilder("bang")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("!")
            .frontDataNode("second")
            .autoComplete(1)
            .build();
    FreeAtomType waddle =
        new TypeBuilder("waddle")
            .middleAtom("first", "any")
            .back(new BackRecordBuilder().add("first", Helper.buildBackDataAtom("first")).build())
            .frontDataNode("first")
            .frontMark("?")
            .autoComplete(1)
            .build();
    FreeAtomType snooze =
        new TypeBuilder("snooze")
            .middleAtom("value", "any")
            .back(new BackRecordBuilder().add("value", Helper.buildBackDataAtom("value")).build())
            .frontMark("#")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType multiplier =
        new TypeBuilder("multiplier")
            .middlePrimitive("text")
            .middleAtom("value", "any")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value"))
                    .add("text", Helper.buildBackDataPrimitive("text"))
                    .build())
            .frontMark("x")
            .frontDataPrimitive("text")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType array =
        new TypeBuilder("array")
            .middleArray("value", "any")
            .back(Helper.buildBackDataArray("value"))
            .frontMark("[")
            .front(
                new FrontDataArrayBuilder("value")
                    .addSeparator(new FrontMarkBuilder(", ").build())
                    .build())
            .frontMark("]")
            .autoComplete(1)
            .build();
    FreeAtomType doubleArray =
        new TypeBuilder("doublearray")
            .middleArray("first", "any")
            .middleArray("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataArray("first"))
                    .add("second", Helper.buildBackDataArray("second"))
                    .build())
            .frontMark("[")
            .frontDataArray("first")
            .frontMark("?")
            .frontDataArray("second")
            .frontMark("]")
            .build();
    FreeAtomType record =
        new TypeBuilder("record")
            .middleRecord("value", "record_element")
            .back(Helper.buildBackDataRecord("value"))
            .frontMark("{")
            .frontDataArray("value")
            .frontMark("}")
            .autoComplete(1)
            .build();
    FreeAtomType recordElement =
        new TypeBuilder("record_element")
            .middlePrimitive("key")
            .middleAtom("value", "any")
            .back(Helper.buildBackDataKey("key"))
            .back(Helper.buildBackDataAtom("value"))
            .frontDataPrimitive("key")
            .frontMark(": ")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType pair =
        new TypeBuilder("pair")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackArrayBuilder()
                    .add(Helper.buildBackDataAtom("first"))
                    .add(Helper.buildBackDataAtom("second"))
                    .build())
            .frontMark("<")
            .frontDataNode("first")
            .frontMark(", ")
            .frontDataNode("second")
            .frontMark(">")
            .autoComplete(1)
            .build();
    FreeAtomType ratio =
        new TypeBuilder("ratio")
            .middlePrimitive("first")
            .middlePrimitive("second")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataPrimitive("first"))
                    .add("second", Helper.buildBackDataPrimitive("second"))
                    .build())
            .frontMark("<")
            .frontDataPrimitive("first")
            .frontMark(":")
            .frontDataPrimitive("second")
            .frontMark(">")
            .build();
    FreeAtomType restricted =
        new TypeBuilder("restricted")
            .middleAtom("value", "restricted_group")
            .back(new BackRecordBuilder().add("value", Helper.buildBackDataAtom("value")).build())
            .frontDataNode("value")
            .build();
    FreeAtomType restrictedArray =
        new TypeBuilder("restricted_array")
            .middleArray("value", "restricted_array_group")
            .back(Helper.buildBackDataArray("value"))
            .frontMark("_")
            .front(new FrontDataArrayBuilder("value").build())
            .autoComplete(1)
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(infinity)
            .type(one)
            .type(two)
            .type(three)
            .type(four)
            .type(five)
            .type(seven)
            .type(multiback)
            .type(quoted)
            .type(digits)
            .type(doubleQuoted)
            .type(plus)
            .type(plusEqual)
            .type(binaryBang)
            .type(waddle)
            .type(snooze)
            .type(multiplier)
            .type(array)
            .type(doubleArray)
            .type(record)
            .type(recordElement)
            .type(pair)
            .type(ratio)
            .type(restricted)
            .type(restrictedArray)
            .group(
                "test_group_1",
                new GroupBuilder()
                    .type(infinity)
                    .type(one)
                    .type(multiback)
                    .group("test_group_2")
                    .build())
            .group("test_group_2", new GroupBuilder().type(quoted).build())
            .group("restricted_group", new GroupBuilder().type(quoted).build())
            .group("restricted_array_group", new GroupBuilder().type(quoted).build())
            .group(
                "any",
                new GroupBuilder()
                    .type(infinity)
                    .type(one)
                    .type(two)
                    .type(three)
                    .type(four)
                    .type(five)
                    .type(quoted)
                    .type(digits)
                    .type(seven)
                    .type(plus)
                    .type(plusEqual)
                    .type(binaryBang)
                    .type(waddle)
                    .type(snooze)
                    .type(multiplier)
                    .type(array)
                    .type(restrictedArray)
                    .type(record)
                    .type(pair)
                    .type(ratio)
                    .build())
            .group("arrayChildren", new GroupBuilder().type(one).type(multiback).build())
            .build();

    innerTestTransform(
        syntax,
        () ->
            new TreeBuilder(syntax.prefixGap)
                .add("gap", "")
                .addArray("value", new TreeBuilder(one).build())
                .build(),
        context -> {
          ((ValuePrimitive) context.locateLong(new Path("0", "gap"))).selectDown(context);
          context.selection.receiveText(context, "#");
          context.selection.receiveText(context, "e");
        },
        new TreeBuilder(snooze)
            .add(
                "value",
                new TreeBuilder(syntax.prefixGap)
                    .add("gap", "e")
                    .addArray("value", new TreeBuilder(one).build()))
            .build());
  }

  @Test
  public void prefix_prefixOnlyPlaceAllowedTypes() {
    final Atom gap =
        SyntaxRestrictedRoot.syntax.prefixGap.create(
            new TreeBuilder(SyntaxRestrictedRoot.quoted).add("value", "hi").build());
    new GeneralTestWizard(SyntaxRestrictedRoot.syntax, gap)
        .run(context -> gap.data.get("gap").selectDown(context))
        .sendText("!")
        .checkArrayTree(
            new TreeBuilder(SyntaxRestrictedRoot.syntax.prefixGap)
                .add("gap", "!")
                .addArray(
                    "value",
                    new TreeBuilder(SyntaxRestrictedRoot.quoted).add("value", "hi").build())
                .build());
  }

  // ========================================================================
  // ========================================================================
  // Suffix raising

  @Test
  public void suffixRaising_testRaisePrecedenceLower() {
    innerTestTransform(
        ExpressionSyntax.syntax,
        () ->
            new TreeBuilder(ExpressionSyntax.plus)
                .add("first", new TreeBuilder(ExpressionSyntax.infinity))
                .add(
                    "second",
                    ExpressionSyntax.syntax.suffixGap.create(
                        true, new TreeBuilder(ExpressionSyntax.infinity).build()))
                .build(),
        context -> {
          ((ValuePrimitive) context.locateLong(new Path("0", "second", "gap"))).selectDown(context);
          context.selection.receiveText(context, "*");
        },
        new TreeBuilder(ExpressionSyntax.plus)
            .add("first", new TreeBuilder(ExpressionSyntax.infinity))
            .add(
                "second",
                new TreeBuilder(ExpressionSyntax.multiply)
                    .add("first", new TreeBuilder(ExpressionSyntax.infinity))
                    .add("second", ExpressionSyntax.syntax.gap.create()))
            .build());
  }

  @Test
  public void suffixRaising_testRaisePrecedenceEqualAfter() {
    innerTestTransform(
        ExpressionSyntax.syntax,
        () ->
            new TreeBuilder(ExpressionSyntax.plus)
                .add("first", new TreeBuilder(ExpressionSyntax.infinity))
                .add(
                    "second",
                    ExpressionSyntax.syntax.suffixGap.create(
                        true, new TreeBuilder(ExpressionSyntax.infinity).build()))
                .build(),
        context -> {
          ((ValuePrimitive) context.locateLong(new Path("0", "second", "gap"))).selectDown(context);
          context.selection.receiveText(context, "+");
        },
        new TreeBuilder(ExpressionSyntax.plus)
            .add("first", new TreeBuilder(ExpressionSyntax.infinity))
            .add(
                "second",
                new TreeBuilder(ExpressionSyntax.plus)
                    .add("first", new TreeBuilder(ExpressionSyntax.infinity))
                    .add("second", ExpressionSyntax.syntax.gap.create()))
            .build());
  }

  @Test
  public void suffixRaising_testRaisePrecedenceEqualBefore() {
    innerTestTransform(
        ExpressionSyntax.syntax,
        () ->
            new TreeBuilder(ExpressionSyntax.minus)
                .add("first", new TreeBuilder(ExpressionSyntax.infinity))
                .add(
                    "second",
                    ExpressionSyntax.syntax.suffixGap.create(
                        true, new TreeBuilder(ExpressionSyntax.infinity).build()))
                .build(),
        context -> {
          ((ValuePrimitive) context.locateLong(new Path("0", "second", "gap"))).selectDown(context);
          context.selection.receiveText(context, "-");
        },
        new TreeBuilder(ExpressionSyntax.minus)
            .add(
                "first",
                new TreeBuilder(ExpressionSyntax.minus)
                    .add("first", new TreeBuilder(ExpressionSyntax.infinity))
                    .add("second", new TreeBuilder(ExpressionSyntax.infinity)))
            .add("second", ExpressionSyntax.syntax.gap.create())
            .build());
  }

  @Test
  public void suffixRaising_testRaisePrecedenceGreater() {
    innerTestTransform(
        ExpressionSyntax.syntax,
        () ->
            new TreeBuilder(ExpressionSyntax.multiply)
                .add("first", new TreeBuilder(ExpressionSyntax.infinity))
                .add(
                    "second",
                    ExpressionSyntax.syntax.suffixGap.create(
                        true, new TreeBuilder(ExpressionSyntax.infinity).build()))
                .build(),
        context -> {
          ((ValuePrimitive) context.locateLong(new Path("0", "second", "gap"))).selectDown(context);
          context.selection.receiveText(context, "+");
        },
        new TreeBuilder(ExpressionSyntax.plus)
            .add(
                "first",
                new TreeBuilder(ExpressionSyntax.multiply)
                    .add("first", new TreeBuilder(ExpressionSyntax.infinity))
                    .add("second", new TreeBuilder(ExpressionSyntax.infinity)))
            .add("second", ExpressionSyntax.syntax.gap.create())
            .build());
  }

  @Test
  public void suffixRaising_testRaiseSkipDissimilar() {
    innerTestTransform(
        ExpressionSyntax.syntax,
        () ->
            new TreeBuilder(ExpressionSyntax.subscript)
                .add("first", new TreeBuilder(ExpressionSyntax.infinity))
                .add(
                    "second",
                    ExpressionSyntax.syntax.suffixGap.create(
                        true, new TreeBuilder(ExpressionSyntax.infinity).build()))
                .build(),
        context -> {
          ((ValuePrimitive) context.locateLong(new Path("0", "second", "gap"))).selectDown(context);
          context.selection.receiveText(context, "+");
        },
        new TreeBuilder(ExpressionSyntax.plus)
            .add(
                "first",
                new TreeBuilder(ExpressionSyntax.subscript)
                    .add("first", new TreeBuilder(ExpressionSyntax.infinity))
                    .add("second", new TreeBuilder(ExpressionSyntax.infinity)))
            .add("second", ExpressionSyntax.syntax.gap.create())
            .build());
  }

  @Test
  public void suffixRaising_testRaiseBounded() {
    innerTestTransform(
        ExpressionSyntax.syntax,
        () ->
            new TreeBuilder(ExpressionSyntax.inclusiveRange)
                .add("first", new TreeBuilder(ExpressionSyntax.infinity))
                .add(
                    "second",
                    ExpressionSyntax.syntax.suffixGap.create(
                        true, new TreeBuilder(ExpressionSyntax.infinity).build()))
                .build(),
        context -> {
          ((ValuePrimitive) context.locateLong(new Path("0", "second", "gap"))).selectDown(context);
          context.selection.receiveText(context, "+");
        },
        new TreeBuilder(ExpressionSyntax.inclusiveRange)
            .add("first", new TreeBuilder(ExpressionSyntax.infinity))
            .add(
                "second",
                new TreeBuilder(ExpressionSyntax.plus)
                    .add("first", new TreeBuilder(ExpressionSyntax.infinity))
                    .add("second", ExpressionSyntax.syntax.gap.create()))
            .build());
  }

  // ========================================================================
  // ========================================================================
  // Deselection removal

  @Test
  public void deselectionRemoval_testDropArrayElement() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(1)
            .build();
    FreeAtomType one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .autoComplete(-1)
            .build();
    FreeAtomType two =
        new TypeBuilder("two")
            .back(Helper.buildBackPrimitive("two"))
            .front(new FrontMarkBuilder("two").build())
            .autoComplete(-1)
            .build();
    FreeAtomType three =
        new TypeBuilder("three")
            .back(Helper.buildBackPrimitive("three"))
            .front(new FrontMarkBuilder("three").build())
            .autoComplete(-1)
            .build();
    FreeAtomType four =
        new TypeBuilder("four")
            .back(Helper.buildBackPrimitive("four"))
            .front(new FrontMarkBuilder("four").build())
            .autoComplete(-1)
            .build();
    FreeAtomType five =
        new TypeBuilder("five")
            .back(Helper.buildBackPrimitive("five"))
            .front(new FrontMarkBuilder("five").build())
            .autoComplete(-1)
            .build();
    FreeAtomType seven =
        new TypeBuilder("seven")
            .back(Helper.buildBackPrimitive("7"))
            .front(new FrontMarkBuilder("7").build())
            .autoComplete(-1)
            .build();
    FreeAtomType multiback =
        new TypeBuilder("multiback")
            .back(Helper.buildBackDataPrimitive("a"))
            .back(Helper.buildBackDataPrimitive("b"))
            .middlePrimitive("a")
            .middlePrimitive("b")
            .frontDataPrimitive("a")
            .frontMark("^")
            .frontDataPrimitive("b")
            .autoComplete(-1)
            .build();
    FreeAtomType quoted =
        new TypeBuilder("quoted")
            .middlePrimitive("value")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("value")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(1)
            .build();
    FreeAtomType digits =
        new TypeBuilder("digits")
            .middlePrimitiveDigits("value")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .autoComplete(1)
            .build();
    FreeAtomType doubleQuoted =
        new TypeBuilder("doubleuoted")
            .middlePrimitive("first")
            .middlePrimitive("second")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataPrimitive("first"))
                    .add("second", Helper.buildBackDataPrimitive("second"))
                    .build())
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("first")
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("second")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(-1)
            .build();
    FreeAtomType plus =
        new TypeBuilder("plus")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("+")
            .frontDataNode("second")
            .autoComplete(-1)
            .build();
    FreeAtomType plusEqual =
        new TypeBuilder("plusequal")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("+=")
            .frontDataNode("second")
            .autoComplete(-1)
            .build();
    FreeAtomType binaryBang =
        new TypeBuilder("bang")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("!")
            .frontDataNode("second")
            .autoComplete(1)
            .build();
    FreeAtomType waddle =
        new TypeBuilder("waddle")
            .middleAtom("first", "any")
            .back(new BackRecordBuilder().add("first", Helper.buildBackDataAtom("first")).build())
            .frontDataNode("first")
            .frontMark("?")
            .autoComplete(1)
            .build();
    FreeAtomType snooze =
        new TypeBuilder("snooze")
            .middleAtom("value", "any")
            .back(new BackRecordBuilder().add("value", Helper.buildBackDataAtom("value")).build())
            .frontMark("#")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType multiplier =
        new TypeBuilder("multiplier")
            .middlePrimitive("text")
            .middleAtom("value", "any")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value"))
                    .add("text", Helper.buildBackDataPrimitive("text"))
                    .build())
            .frontMark("x")
            .frontDataPrimitive("text")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType array =
        new TypeBuilder("array")
            .middleArray("value", "any")
            .back(Helper.buildBackDataArray("value"))
            .frontMark("[")
            .front(
                new FrontDataArrayBuilder("value")
                    .addSeparator(new FrontMarkBuilder(", ").build())
                    .build())
            .frontMark("]")
            .autoComplete(1)
            .build();
    FreeAtomType doubleArray =
        new TypeBuilder("doublearray")
            .middleArray("first", "any")
            .middleArray("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataArray("first"))
                    .add("second", Helper.buildBackDataArray("second"))
                    .build())
            .frontMark("[")
            .frontDataArray("first")
            .frontMark("?")
            .frontDataArray("second")
            .frontMark("]")
            .build();
    FreeAtomType record =
        new TypeBuilder("record")
            .middleRecord("value", "record_element")
            .back(Helper.buildBackDataRecord("value"))
            .frontMark("{")
            .frontDataArray("value")
            .frontMark("}")
            .autoComplete(1)
            .build();
    FreeAtomType recordElement =
        new TypeBuilder("record_element")
            .middlePrimitive("key")
            .middleAtom("value", "any")
            .back(Helper.buildBackDataKey("key"))
            .back(Helper.buildBackDataAtom("value"))
            .frontDataPrimitive("key")
            .frontMark(": ")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType pair =
        new TypeBuilder("pair")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackArrayBuilder()
                    .add(Helper.buildBackDataAtom("first"))
                    .add(Helper.buildBackDataAtom("second"))
                    .build())
            .frontMark("<")
            .frontDataNode("first")
            .frontMark(", ")
            .frontDataNode("second")
            .frontMark(">")
            .autoComplete(1)
            .build();
    FreeAtomType ratio =
        new TypeBuilder("ratio")
            .middlePrimitive("first")
            .middlePrimitive("second")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataPrimitive("first"))
                    .add("second", Helper.buildBackDataPrimitive("second"))
                    .build())
            .frontMark("<")
            .frontDataPrimitive("first")
            .frontMark(":")
            .frontDataPrimitive("second")
            .frontMark(">")
            .build();
    FreeAtomType restricted =
        new TypeBuilder("restricted")
            .middleAtom("value", "restricted_group")
            .back(new BackRecordBuilder().add("value", Helper.buildBackDataAtom("value")).build())
            .frontDataNode("value")
            .build();
    FreeAtomType restrictedArray =
        new TypeBuilder("restricted_array")
            .middleArray("value", "restricted_array_group")
            .back(Helper.buildBackDataArray("value"))
            .frontMark("_")
            .front(new FrontDataArrayBuilder("value").build())
            .autoComplete(1)
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(infinity)
            .type(one)
            .type(two)
            .type(three)
            .type(four)
            .type(five)
            .type(seven)
            .type(multiback)
            .type(quoted)
            .type(digits)
            .type(doubleQuoted)
            .type(plus)
            .type(plusEqual)
            .type(binaryBang)
            .type(waddle)
            .type(snooze)
            .type(multiplier)
            .type(array)
            .type(doubleArray)
            .type(record)
            .type(recordElement)
            .type(pair)
            .type(ratio)
            .type(restricted)
            .type(restrictedArray)
            .group(
                "test_group_1",
                new GroupBuilder()
                    .type(infinity)
                    .type(one)
                    .type(multiback)
                    .group("test_group_2")
                    .build())
            .group("test_group_2", new GroupBuilder().type(quoted).build())
            .group("restricted_group", new GroupBuilder().type(quoted).build())
            .group("restricted_array_group", new GroupBuilder().type(quoted).build())
            .group(
                "any",
                new GroupBuilder()
                    .type(infinity)
                    .type(one)
                    .type(two)
                    .type(three)
                    .type(four)
                    .type(five)
                    .type(quoted)
                    .type(digits)
                    .type(seven)
                    .type(plus)
                    .type(plusEqual)
                    .type(binaryBang)
                    .type(waddle)
                    .type(snooze)
                    .type(multiplier)
                    .type(array)
                    .type(restrictedArray)
                    .type(record)
                    .type(pair)
                    .type(ratio)
                    .build())
            .group("arrayChildren", new GroupBuilder().type(one).type(multiback).build())
            .build();

    innerTestTransform(
        syntax,
        () -> new TreeBuilder(array).addArray("value", syntax.gap.create()).build(),
        context -> {
          ((ValuePrimitive) context.locateLong(new Path("0", "0"))).selectDown(context);
          ((Atom) context.locateShort(new Path("0", "0"))).parent.selectUp(context);
        },
        new TreeBuilder(array).addArray("value").build());
  }

  @Test
  public void deselectionRemoval_testDontDropNodeGap() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(1)
            .build();
    FreeAtomType one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .autoComplete(-1)
            .build();
    FreeAtomType two =
        new TypeBuilder("two")
            .back(Helper.buildBackPrimitive("two"))
            .front(new FrontMarkBuilder("two").build())
            .autoComplete(-1)
            .build();
    FreeAtomType three =
        new TypeBuilder("three")
            .back(Helper.buildBackPrimitive("three"))
            .front(new FrontMarkBuilder("three").build())
            .autoComplete(-1)
            .build();
    FreeAtomType four =
        new TypeBuilder("four")
            .back(Helper.buildBackPrimitive("four"))
            .front(new FrontMarkBuilder("four").build())
            .autoComplete(-1)
            .build();
    FreeAtomType five =
        new TypeBuilder("five")
            .back(Helper.buildBackPrimitive("five"))
            .front(new FrontMarkBuilder("five").build())
            .autoComplete(-1)
            .build();
    FreeAtomType seven =
        new TypeBuilder("seven")
            .back(Helper.buildBackPrimitive("7"))
            .front(new FrontMarkBuilder("7").build())
            .autoComplete(-1)
            .build();
    FreeAtomType multiback =
        new TypeBuilder("multiback")
            .back(Helper.buildBackDataPrimitive("a"))
            .back(Helper.buildBackDataPrimitive("b"))
            .middlePrimitive("a")
            .middlePrimitive("b")
            .frontDataPrimitive("a")
            .frontMark("^")
            .frontDataPrimitive("b")
            .autoComplete(-1)
            .build();
    FreeAtomType quoted =
        new TypeBuilder("quoted")
            .middlePrimitive("value")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("value")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(1)
            .build();
    FreeAtomType digits =
        new TypeBuilder("digits")
            .middlePrimitiveDigits("value")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .autoComplete(1)
            .build();
    FreeAtomType doubleQuoted =
        new TypeBuilder("doubleuoted")
            .middlePrimitive("first")
            .middlePrimitive("second")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataPrimitive("first"))
                    .add("second", Helper.buildBackDataPrimitive("second"))
                    .build())
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("first")
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("second")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(-1)
            .build();
    FreeAtomType plus =
        new TypeBuilder("plus")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("+")
            .frontDataNode("second")
            .autoComplete(-1)
            .build();
    FreeAtomType plusEqual =
        new TypeBuilder("plusequal")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("+=")
            .frontDataNode("second")
            .autoComplete(-1)
            .build();
    FreeAtomType binaryBang =
        new TypeBuilder("bang")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("!")
            .frontDataNode("second")
            .autoComplete(1)
            .build();
    FreeAtomType waddle =
        new TypeBuilder("waddle")
            .middleAtom("first", "any")
            .back(new BackRecordBuilder().add("first", Helper.buildBackDataAtom("first")).build())
            .frontDataNode("first")
            .frontMark("?")
            .autoComplete(1)
            .build();
    FreeAtomType snooze =
        new TypeBuilder("snooze")
            .middleAtom("value", "any")
            .back(new BackRecordBuilder().add("value", Helper.buildBackDataAtom("value")).build())
            .frontMark("#")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType multiplier =
        new TypeBuilder("multiplier")
            .middlePrimitive("text")
            .middleAtom("value", "any")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value"))
                    .add("text", Helper.buildBackDataPrimitive("text"))
                    .build())
            .frontMark("x")
            .frontDataPrimitive("text")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType array =
        new TypeBuilder("array")
            .middleArray("value", "any")
            .back(Helper.buildBackDataArray("value"))
            .frontMark("[")
            .front(
                new FrontDataArrayBuilder("value")
                    .addSeparator(new FrontMarkBuilder(", ").build())
                    .build())
            .frontMark("]")
            .autoComplete(1)
            .build();
    FreeAtomType doubleArray =
        new TypeBuilder("doublearray")
            .middleArray("first", "any")
            .middleArray("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataArray("first"))
                    .add("second", Helper.buildBackDataArray("second"))
                    .build())
            .frontMark("[")
            .frontDataArray("first")
            .frontMark("?")
            .frontDataArray("second")
            .frontMark("]")
            .build();
    FreeAtomType record =
        new TypeBuilder("record")
            .middleRecord("value", "record_element")
            .back(Helper.buildBackDataRecord("value"))
            .frontMark("{")
            .frontDataArray("value")
            .frontMark("}")
            .autoComplete(1)
            .build();
    FreeAtomType recordElement =
        new TypeBuilder("record_element")
            .middlePrimitive("key")
            .middleAtom("value", "any")
            .back(Helper.buildBackDataKey("key"))
            .back(Helper.buildBackDataAtom("value"))
            .frontDataPrimitive("key")
            .frontMark(": ")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType pair =
        new TypeBuilder("pair")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackArrayBuilder()
                    .add(Helper.buildBackDataAtom("first"))
                    .add(Helper.buildBackDataAtom("second"))
                    .build())
            .frontMark("<")
            .frontDataNode("first")
            .frontMark(", ")
            .frontDataNode("second")
            .frontMark(">")
            .autoComplete(1)
            .build();
    FreeAtomType ratio =
        new TypeBuilder("ratio")
            .middlePrimitive("first")
            .middlePrimitive("second")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataPrimitive("first"))
                    .add("second", Helper.buildBackDataPrimitive("second"))
                    .build())
            .frontMark("<")
            .frontDataPrimitive("first")
            .frontMark(":")
            .frontDataPrimitive("second")
            .frontMark(">")
            .build();
    FreeAtomType restricted =
        new TypeBuilder("restricted")
            .middleAtom("value", "restricted_group")
            .back(new BackRecordBuilder().add("value", Helper.buildBackDataAtom("value")).build())
            .frontDataNode("value")
            .build();
    FreeAtomType restrictedArray =
        new TypeBuilder("restricted_array")
            .middleArray("value", "restricted_array_group")
            .back(Helper.buildBackDataArray("value"))
            .frontMark("_")
            .front(new FrontDataArrayBuilder("value").build())
            .autoComplete(1)
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(infinity)
            .type(one)
            .type(two)
            .type(three)
            .type(four)
            .type(five)
            .type(seven)
            .type(multiback)
            .type(quoted)
            .type(digits)
            .type(doubleQuoted)
            .type(plus)
            .type(plusEqual)
            .type(binaryBang)
            .type(waddle)
            .type(snooze)
            .type(multiplier)
            .type(array)
            .type(doubleArray)
            .type(record)
            .type(recordElement)
            .type(pair)
            .type(ratio)
            .type(restricted)
            .type(restrictedArray)
            .group(
                "test_group_1",
                new GroupBuilder()
                    .type(infinity)
                    .type(one)
                    .type(multiback)
                    .group("test_group_2")
                    .build())
            .group("test_group_2", new GroupBuilder().type(quoted).build())
            .group("restricted_group", new GroupBuilder().type(quoted).build())
            .group("restricted_array_group", new GroupBuilder().type(quoted).build())
            .group(
                "any",
                new GroupBuilder()
                    .type(infinity)
                    .type(one)
                    .type(two)
                    .type(three)
                    .type(four)
                    .type(five)
                    .type(quoted)
                    .type(digits)
                    .type(seven)
                    .type(plus)
                    .type(plusEqual)
                    .type(binaryBang)
                    .type(waddle)
                    .type(snooze)
                    .type(multiplier)
                    .type(array)
                    .type(restrictedArray)
                    .type(record)
                    .type(pair)
                    .type(ratio)
                    .build())
            .group("arrayChildren", new GroupBuilder().type(one).type(multiback).build())
            .build();

    innerTestTransform(
        syntax,
        () -> new TreeBuilder(array).addArray("value", syntax.gap.create()).build(),
        context -> {
          ((ValuePrimitive) context.locateLong(new Path("0", "0"))).selectDown(context);
          context.selection.receiveText(context, "urt");
          ((ValueArray) context.locateLong(new Path("0"))).visual.selectDown(context);
        },
        new TreeBuilder(array)
            .addArray("value", new TreeBuilder(syntax.gap).add("gap", "urt").build())
            .build());
  }

  @Test
  public void deselectionRemoval_testDontDropOutOfTree() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(1)
            .build();
    FreeAtomType one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .autoComplete(-1)
            .build();
    FreeAtomType two =
        new TypeBuilder("two")
            .back(Helper.buildBackPrimitive("two"))
            .front(new FrontMarkBuilder("two").build())
            .autoComplete(-1)
            .build();
    FreeAtomType three =
        new TypeBuilder("three")
            .back(Helper.buildBackPrimitive("three"))
            .front(new FrontMarkBuilder("three").build())
            .autoComplete(-1)
            .build();
    FreeAtomType four =
        new TypeBuilder("four")
            .back(Helper.buildBackPrimitive("four"))
            .front(new FrontMarkBuilder("four").build())
            .autoComplete(-1)
            .build();
    FreeAtomType five =
        new TypeBuilder("five")
            .back(Helper.buildBackPrimitive("five"))
            .front(new FrontMarkBuilder("five").build())
            .autoComplete(-1)
            .build();
    FreeAtomType seven =
        new TypeBuilder("seven")
            .back(Helper.buildBackPrimitive("7"))
            .front(new FrontMarkBuilder("7").build())
            .autoComplete(-1)
            .build();
    FreeAtomType multiback =
        new TypeBuilder("multiback")
            .back(Helper.buildBackDataPrimitive("a"))
            .back(Helper.buildBackDataPrimitive("b"))
            .middlePrimitive("a")
            .middlePrimitive("b")
            .frontDataPrimitive("a")
            .frontMark("^")
            .frontDataPrimitive("b")
            .autoComplete(-1)
            .build();
    FreeAtomType quoted =
        new TypeBuilder("quoted")
            .middlePrimitive("value")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("value")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(1)
            .build();
    FreeAtomType digits =
        new TypeBuilder("digits")
            .middlePrimitiveDigits("value")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .autoComplete(1)
            .build();
    FreeAtomType doubleQuoted =
        new TypeBuilder("doubleuoted")
            .middlePrimitive("first")
            .middlePrimitive("second")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataPrimitive("first"))
                    .add("second", Helper.buildBackDataPrimitive("second"))
                    .build())
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("first")
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("second")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(-1)
            .build();
    FreeAtomType plus =
        new TypeBuilder("plus")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("+")
            .frontDataNode("second")
            .autoComplete(-1)
            .build();
    FreeAtomType plusEqual =
        new TypeBuilder("plusequal")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("+=")
            .frontDataNode("second")
            .autoComplete(-1)
            .build();
    FreeAtomType binaryBang =
        new TypeBuilder("bang")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("!")
            .frontDataNode("second")
            .autoComplete(1)
            .build();
    FreeAtomType waddle =
        new TypeBuilder("waddle")
            .middleAtom("first", "any")
            .back(new BackRecordBuilder().add("first", Helper.buildBackDataAtom("first")).build())
            .frontDataNode("first")
            .frontMark("?")
            .autoComplete(1)
            .build();
    FreeAtomType snooze =
        new TypeBuilder("snooze")
            .middleAtom("value", "any")
            .back(new BackRecordBuilder().add("value", Helper.buildBackDataAtom("value")).build())
            .frontMark("#")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType multiplier =
        new TypeBuilder("multiplier")
            .middlePrimitive("text")
            .middleAtom("value", "any")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value"))
                    .add("text", Helper.buildBackDataPrimitive("text"))
                    .build())
            .frontMark("x")
            .frontDataPrimitive("text")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType array =
        new TypeBuilder("array")
            .middleArray("value", "any")
            .back(Helper.buildBackDataArray("value"))
            .frontMark("[")
            .front(
                new FrontDataArrayBuilder("value")
                    .addSeparator(new FrontMarkBuilder(", ").build())
                    .build())
            .frontMark("]")
            .autoComplete(1)
            .build();
    FreeAtomType doubleArray =
        new TypeBuilder("doublearray")
            .middleArray("first", "any")
            .middleArray("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataArray("first"))
                    .add("second", Helper.buildBackDataArray("second"))
                    .build())
            .frontMark("[")
            .frontDataArray("first")
            .frontMark("?")
            .frontDataArray("second")
            .frontMark("]")
            .build();
    FreeAtomType record =
        new TypeBuilder("record")
            .middleRecord("value", "record_element")
            .back(Helper.buildBackDataRecord("value"))
            .frontMark("{")
            .frontDataArray("value")
            .frontMark("}")
            .autoComplete(1)
            .build();
    FreeAtomType recordElement =
        new TypeBuilder("record_element")
            .middlePrimitive("key")
            .middleAtom("value", "any")
            .back(Helper.buildBackDataKey("key"))
            .back(Helper.buildBackDataAtom("value"))
            .frontDataPrimitive("key")
            .frontMark(": ")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType pair =
        new TypeBuilder("pair")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackArrayBuilder()
                    .add(Helper.buildBackDataAtom("first"))
                    .add(Helper.buildBackDataAtom("second"))
                    .build())
            .frontMark("<")
            .frontDataNode("first")
            .frontMark(", ")
            .frontDataNode("second")
            .frontMark(">")
            .autoComplete(1)
            .build();
    FreeAtomType ratio =
        new TypeBuilder("ratio")
            .middlePrimitive("first")
            .middlePrimitive("second")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataPrimitive("first"))
                    .add("second", Helper.buildBackDataPrimitive("second"))
                    .build())
            .frontMark("<")
            .frontDataPrimitive("first")
            .frontMark(":")
            .frontDataPrimitive("second")
            .frontMark(">")
            .build();
    FreeAtomType restricted =
        new TypeBuilder("restricted")
            .middleAtom("value", "restricted_group")
            .back(new BackRecordBuilder().add("value", Helper.buildBackDataAtom("value")).build())
            .frontDataNode("value")
            .build();
    FreeAtomType restrictedArray =
        new TypeBuilder("restricted_array")
            .middleArray("value", "restricted_array_group")
            .back(Helper.buildBackDataArray("value"))
            .frontMark("_")
            .front(new FrontDataArrayBuilder("value").build())
            .autoComplete(1)
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(infinity)
            .type(one)
            .type(two)
            .type(three)
            .type(four)
            .type(five)
            .type(seven)
            .type(multiback)
            .type(quoted)
            .type(digits)
            .type(doubleQuoted)
            .type(plus)
            .type(plusEqual)
            .type(binaryBang)
            .type(waddle)
            .type(snooze)
            .type(multiplier)
            .type(array)
            .type(doubleArray)
            .type(record)
            .type(recordElement)
            .type(pair)
            .type(ratio)
            .type(restricted)
            .type(restrictedArray)
            .group(
                "test_group_1",
                new GroupBuilder()
                    .type(infinity)
                    .type(one)
                    .type(multiback)
                    .group("test_group_2")
                    .build())
            .group("test_group_2", new GroupBuilder().type(quoted).build())
            .group("restricted_group", new GroupBuilder().type(quoted).build())
            .group("restricted_array_group", new GroupBuilder().type(quoted).build())
            .group(
                "any",
                new GroupBuilder()
                    .type(infinity)
                    .type(one)
                    .type(two)
                    .type(three)
                    .type(four)
                    .type(five)
                    .type(quoted)
                    .type(digits)
                    .type(seven)
                    .type(plus)
                    .type(plusEqual)
                    .type(binaryBang)
                    .type(waddle)
                    .type(snooze)
                    .type(multiplier)
                    .type(array)
                    .type(restrictedArray)
                    .type(record)
                    .type(pair)
                    .type(ratio)
                    .build())
            .group("arrayChildren", new GroupBuilder().type(one).type(multiback).build())
            .build();

    final Context context = buildDoc(syntax, syntax.gap.create(), syntax.gap.create());
    ((Atom) context.locateShort(new Path("0"))).visual.selectDown(context);
    ((Atom) context.locateShort(new Path("1"))).visual.selectDown(context);
    assertThat(Helper.rootArray(context.document).data.size(), equalTo(1));
    assertTreeEqual(context, syntax.gap.create(), Helper.rootArray(context.document));
    assertThat(context.selection.getPath().toList(), equalTo(ImmutableList.of("0", "0")));
    context.history.undo(context);
    assertThat(Helper.rootArray(context.document).data.size(), equalTo(2));
    assertTreeEqual(syntax.gap.create(), Helper.rootArray(context.document).data.get(0));
    assertThat(context.selection.getPath().toList(), equalTo(ImmutableList.of("1", "0")));
    context.history.redo(context);
    assertThat(Helper.rootArray(context.document).data.size(), equalTo(1));
    assertTreeEqual(syntax.gap.create(), Helper.rootArray(context.document).data.get(0));
    assertThat(context.selection.getPath().toList(), equalTo(ImmutableList.of("0", "0")));
  }

  @Test
  public void deselectionRemoval_testDropSuffixValue() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(1)
            .build();
    FreeAtomType one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .autoComplete(-1)
            .build();
    FreeAtomType two =
        new TypeBuilder("two")
            .back(Helper.buildBackPrimitive("two"))
            .front(new FrontMarkBuilder("two").build())
            .autoComplete(-1)
            .build();
    FreeAtomType three =
        new TypeBuilder("three")
            .back(Helper.buildBackPrimitive("three"))
            .front(new FrontMarkBuilder("three").build())
            .autoComplete(-1)
            .build();
    FreeAtomType four =
        new TypeBuilder("four")
            .back(Helper.buildBackPrimitive("four"))
            .front(new FrontMarkBuilder("four").build())
            .autoComplete(-1)
            .build();
    FreeAtomType five =
        new TypeBuilder("five")
            .back(Helper.buildBackPrimitive("five"))
            .front(new FrontMarkBuilder("five").build())
            .autoComplete(-1)
            .build();
    FreeAtomType seven =
        new TypeBuilder("seven")
            .back(Helper.buildBackPrimitive("7"))
            .front(new FrontMarkBuilder("7").build())
            .autoComplete(-1)
            .build();
    FreeAtomType multiback =
        new TypeBuilder("multiback")
            .back(Helper.buildBackDataPrimitive("a"))
            .back(Helper.buildBackDataPrimitive("b"))
            .middlePrimitive("a")
            .middlePrimitive("b")
            .frontDataPrimitive("a")
            .frontMark("^")
            .frontDataPrimitive("b")
            .autoComplete(-1)
            .build();
    FreeAtomType quoted =
        new TypeBuilder("quoted")
            .middlePrimitive("value")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("value")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(1)
            .build();
    FreeAtomType digits =
        new TypeBuilder("digits")
            .middlePrimitiveDigits("value")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .autoComplete(1)
            .build();
    FreeAtomType doubleQuoted =
        new TypeBuilder("doubleuoted")
            .middlePrimitive("first")
            .middlePrimitive("second")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataPrimitive("first"))
                    .add("second", Helper.buildBackDataPrimitive("second"))
                    .build())
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("first")
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("second")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(-1)
            .build();
    FreeAtomType plus =
        new TypeBuilder("plus")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("+")
            .frontDataNode("second")
            .autoComplete(-1)
            .build();
    FreeAtomType plusEqual =
        new TypeBuilder("plusequal")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("+=")
            .frontDataNode("second")
            .autoComplete(-1)
            .build();
    FreeAtomType binaryBang =
        new TypeBuilder("bang")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("!")
            .frontDataNode("second")
            .autoComplete(1)
            .build();
    FreeAtomType waddle =
        new TypeBuilder("waddle")
            .middleAtom("first", "any")
            .back(new BackRecordBuilder().add("first", Helper.buildBackDataAtom("first")).build())
            .frontDataNode("first")
            .frontMark("?")
            .autoComplete(1)
            .build();
    FreeAtomType snooze =
        new TypeBuilder("snooze")
            .middleAtom("value", "any")
            .back(new BackRecordBuilder().add("value", Helper.buildBackDataAtom("value")).build())
            .frontMark("#")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType multiplier =
        new TypeBuilder("multiplier")
            .middlePrimitive("text")
            .middleAtom("value", "any")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value"))
                    .add("text", Helper.buildBackDataPrimitive("text"))
                    .build())
            .frontMark("x")
            .frontDataPrimitive("text")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType array =
        new TypeBuilder("array")
            .middleArray("value", "any")
            .back(Helper.buildBackDataArray("value"))
            .frontMark("[")
            .front(
                new FrontDataArrayBuilder("value")
                    .addSeparator(new FrontMarkBuilder(", ").build())
                    .build())
            .frontMark("]")
            .autoComplete(1)
            .build();
    FreeAtomType doubleArray =
        new TypeBuilder("doublearray")
            .middleArray("first", "any")
            .middleArray("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataArray("first"))
                    .add("second", Helper.buildBackDataArray("second"))
                    .build())
            .frontMark("[")
            .frontDataArray("first")
            .frontMark("?")
            .frontDataArray("second")
            .frontMark("]")
            .build();
    FreeAtomType record =
        new TypeBuilder("record")
            .middleRecord("value", "record_element")
            .back(Helper.buildBackDataRecord("value"))
            .frontMark("{")
            .frontDataArray("value")
            .frontMark("}")
            .autoComplete(1)
            .build();
    FreeAtomType recordElement =
        new TypeBuilder("record_element")
            .middlePrimitive("key")
            .middleAtom("value", "any")
            .back(Helper.buildBackDataKey("key"))
            .back(Helper.buildBackDataAtom("value"))
            .frontDataPrimitive("key")
            .frontMark(": ")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType pair =
        new TypeBuilder("pair")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackArrayBuilder()
                    .add(Helper.buildBackDataAtom("first"))
                    .add(Helper.buildBackDataAtom("second"))
                    .build())
            .frontMark("<")
            .frontDataNode("first")
            .frontMark(", ")
            .frontDataNode("second")
            .frontMark(">")
            .autoComplete(1)
            .build();
    FreeAtomType ratio =
        new TypeBuilder("ratio")
            .middlePrimitive("first")
            .middlePrimitive("second")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataPrimitive("first"))
                    .add("second", Helper.buildBackDataPrimitive("second"))
                    .build())
            .frontMark("<")
            .frontDataPrimitive("first")
            .frontMark(":")
            .frontDataPrimitive("second")
            .frontMark(">")
            .build();
    FreeAtomType restricted =
        new TypeBuilder("restricted")
            .middleAtom("value", "restricted_group")
            .back(new BackRecordBuilder().add("value", Helper.buildBackDataAtom("value")).build())
            .frontDataNode("value")
            .build();
    FreeAtomType restrictedArray =
        new TypeBuilder("restricted_array")
            .middleArray("value", "restricted_array_group")
            .back(Helper.buildBackDataArray("value"))
            .frontMark("_")
            .front(new FrontDataArrayBuilder("value").build())
            .autoComplete(1)
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(infinity)
            .type(one)
            .type(two)
            .type(three)
            .type(four)
            .type(five)
            .type(seven)
            .type(multiback)
            .type(quoted)
            .type(digits)
            .type(doubleQuoted)
            .type(plus)
            .type(plusEqual)
            .type(binaryBang)
            .type(waddle)
            .type(snooze)
            .type(multiplier)
            .type(array)
            .type(doubleArray)
            .type(record)
            .type(recordElement)
            .type(pair)
            .type(ratio)
            .type(restricted)
            .type(restrictedArray)
            .group(
                "test_group_1",
                new GroupBuilder()
                    .type(infinity)
                    .type(one)
                    .type(multiback)
                    .group("test_group_2")
                    .build())
            .group("test_group_2", new GroupBuilder().type(quoted).build())
            .group("restricted_group", new GroupBuilder().type(quoted).build())
            .group("restricted_array_group", new GroupBuilder().type(quoted).build())
            .group(
                "any",
                new GroupBuilder()
                    .type(infinity)
                    .type(one)
                    .type(two)
                    .type(three)
                    .type(four)
                    .type(five)
                    .type(quoted)
                    .type(digits)
                    .type(seven)
                    .type(plus)
                    .type(plusEqual)
                    .type(binaryBang)
                    .type(waddle)
                    .type(snooze)
                    .type(multiplier)
                    .type(array)
                    .type(restrictedArray)
                    .type(record)
                    .type(pair)
                    .type(ratio)
                    .build())
            .group("arrayChildren", new GroupBuilder().type(one).type(multiback).build())
            .build();

    innerTestTransform(
        syntax,
        () -> syntax.suffixGap.create(true, new TreeBuilder(infinity).build()),
        context -> {
          ((ValuePrimitive) context.locateLong(new Path("0", "gap"))).selectDown(context);
          ((Atom) context.locateShort(new Path("0"))).parent.selectUp(context);
        },
        new TreeBuilder(infinity).build());
  }

  @Test
  public void deselectionRemoval_testDropPrefixValue() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(1)
            .build();
    FreeAtomType one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .autoComplete(-1)
            .build();
    FreeAtomType two =
        new TypeBuilder("two")
            .back(Helper.buildBackPrimitive("two"))
            .front(new FrontMarkBuilder("two").build())
            .autoComplete(-1)
            .build();
    FreeAtomType three =
        new TypeBuilder("three")
            .back(Helper.buildBackPrimitive("three"))
            .front(new FrontMarkBuilder("three").build())
            .autoComplete(-1)
            .build();
    FreeAtomType four =
        new TypeBuilder("four")
            .back(Helper.buildBackPrimitive("four"))
            .front(new FrontMarkBuilder("four").build())
            .autoComplete(-1)
            .build();
    FreeAtomType five =
        new TypeBuilder("five")
            .back(Helper.buildBackPrimitive("five"))
            .front(new FrontMarkBuilder("five").build())
            .autoComplete(-1)
            .build();
    FreeAtomType seven =
        new TypeBuilder("seven")
            .back(Helper.buildBackPrimitive("7"))
            .front(new FrontMarkBuilder("7").build())
            .autoComplete(-1)
            .build();
    FreeAtomType multiback =
        new TypeBuilder("multiback")
            .back(Helper.buildBackDataPrimitive("a"))
            .back(Helper.buildBackDataPrimitive("b"))
            .middlePrimitive("a")
            .middlePrimitive("b")
            .frontDataPrimitive("a")
            .frontMark("^")
            .frontDataPrimitive("b")
            .autoComplete(-1)
            .build();
    FreeAtomType quoted =
        new TypeBuilder("quoted")
            .middlePrimitive("value")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("value")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(1)
            .build();
    FreeAtomType digits =
        new TypeBuilder("digits")
            .middlePrimitiveDigits("value")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .autoComplete(1)
            .build();
    FreeAtomType doubleQuoted =
        new TypeBuilder("doubleuoted")
            .middlePrimitive("first")
            .middlePrimitive("second")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataPrimitive("first"))
                    .add("second", Helper.buildBackDataPrimitive("second"))
                    .build())
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("first")
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("second")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(-1)
            .build();
    FreeAtomType plus =
        new TypeBuilder("plus")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("+")
            .frontDataNode("second")
            .autoComplete(-1)
            .build();
    FreeAtomType plusEqual =
        new TypeBuilder("plusequal")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("+=")
            .frontDataNode("second")
            .autoComplete(-1)
            .build();
    FreeAtomType binaryBang =
        new TypeBuilder("bang")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("!")
            .frontDataNode("second")
            .autoComplete(1)
            .build();
    FreeAtomType waddle =
        new TypeBuilder("waddle")
            .middleAtom("first", "any")
            .back(new BackRecordBuilder().add("first", Helper.buildBackDataAtom("first")).build())
            .frontDataNode("first")
            .frontMark("?")
            .autoComplete(1)
            .build();
    FreeAtomType snooze =
        new TypeBuilder("snooze")
            .middleAtom("value", "any")
            .back(new BackRecordBuilder().add("value", Helper.buildBackDataAtom("value")).build())
            .frontMark("#")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType multiplier =
        new TypeBuilder("multiplier")
            .middlePrimitive("text")
            .middleAtom("value", "any")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value"))
                    .add("text", Helper.buildBackDataPrimitive("text"))
                    .build())
            .frontMark("x")
            .frontDataPrimitive("text")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType array =
        new TypeBuilder("array")
            .middleArray("value", "any")
            .back(Helper.buildBackDataArray("value"))
            .frontMark("[")
            .front(
                new FrontDataArrayBuilder("value")
                    .addSeparator(new FrontMarkBuilder(", ").build())
                    .build())
            .frontMark("]")
            .autoComplete(1)
            .build();
    FreeAtomType doubleArray =
        new TypeBuilder("doublearray")
            .middleArray("first", "any")
            .middleArray("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataArray("first"))
                    .add("second", Helper.buildBackDataArray("second"))
                    .build())
            .frontMark("[")
            .frontDataArray("first")
            .frontMark("?")
            .frontDataArray("second")
            .frontMark("]")
            .build();
    FreeAtomType record =
        new TypeBuilder("record")
            .middleRecord("value", "record_element")
            .back(Helper.buildBackDataRecord("value"))
            .frontMark("{")
            .frontDataArray("value")
            .frontMark("}")
            .autoComplete(1)
            .build();
    FreeAtomType recordElement =
        new TypeBuilder("record_element")
            .middlePrimitive("key")
            .middleAtom("value", "any")
            .back(Helper.buildBackDataKey("key"))
            .back(Helper.buildBackDataAtom("value"))
            .frontDataPrimitive("key")
            .frontMark(": ")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType pair =
        new TypeBuilder("pair")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackArrayBuilder()
                    .add(Helper.buildBackDataAtom("first"))
                    .add(Helper.buildBackDataAtom("second"))
                    .build())
            .frontMark("<")
            .frontDataNode("first")
            .frontMark(", ")
            .frontDataNode("second")
            .frontMark(">")
            .autoComplete(1)
            .build();
    FreeAtomType ratio =
        new TypeBuilder("ratio")
            .middlePrimitive("first")
            .middlePrimitive("second")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataPrimitive("first"))
                    .add("second", Helper.buildBackDataPrimitive("second"))
                    .build())
            .frontMark("<")
            .frontDataPrimitive("first")
            .frontMark(":")
            .frontDataPrimitive("second")
            .frontMark(">")
            .build();
    FreeAtomType restricted =
        new TypeBuilder("restricted")
            .middleAtom("value", "restricted_group")
            .back(new BackRecordBuilder().add("value", Helper.buildBackDataAtom("value")).build())
            .frontDataNode("value")
            .build();
    FreeAtomType restrictedArray =
        new TypeBuilder("restricted_array")
            .middleArray("value", "restricted_array_group")
            .back(Helper.buildBackDataArray("value"))
            .frontMark("_")
            .front(new FrontDataArrayBuilder("value").build())
            .autoComplete(1)
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(infinity)
            .type(one)
            .type(two)
            .type(three)
            .type(four)
            .type(five)
            .type(seven)
            .type(multiback)
            .type(quoted)
            .type(digits)
            .type(doubleQuoted)
            .type(plus)
            .type(plusEqual)
            .type(binaryBang)
            .type(waddle)
            .type(snooze)
            .type(multiplier)
            .type(array)
            .type(doubleArray)
            .type(record)
            .type(recordElement)
            .type(pair)
            .type(ratio)
            .type(restricted)
            .type(restrictedArray)
            .group(
                "test_group_1",
                new GroupBuilder()
                    .type(infinity)
                    .type(one)
                    .type(multiback)
                    .group("test_group_2")
                    .build())
            .group("test_group_2", new GroupBuilder().type(quoted).build())
            .group("restricted_group", new GroupBuilder().type(quoted).build())
            .group("restricted_array_group", new GroupBuilder().type(quoted).build())
            .group(
                "any",
                new GroupBuilder()
                    .type(infinity)
                    .type(one)
                    .type(two)
                    .type(three)
                    .type(four)
                    .type(five)
                    .type(quoted)
                    .type(digits)
                    .type(seven)
                    .type(plus)
                    .type(plusEqual)
                    .type(binaryBang)
                    .type(waddle)
                    .type(snooze)
                    .type(multiplier)
                    .type(array)
                    .type(restrictedArray)
                    .type(record)
                    .type(pair)
                    .type(ratio)
                    .build())
            .group("arrayChildren", new GroupBuilder().type(one).type(multiback).build())
            .build();

    innerTestTransform(
        syntax,
        () -> syntax.prefixGap.create(new TreeBuilder(infinity).build()),
        context -> {
          ((ValuePrimitive) context.locateLong(new Path("0", "gap"))).selectDown(context);
          ((Atom) context.locateShort(new Path("0"))).parent.selectUp(context);
        },
        new TreeBuilder(infinity).build());
  }

  // ========================================================================
  // Array gap creation

  @Test
  public void arrayGapCreation_testCreateArrayGap() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(1)
            .build();
    FreeAtomType one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .autoComplete(-1)
            .build();
    FreeAtomType two =
        new TypeBuilder("two")
            .back(Helper.buildBackPrimitive("two"))
            .front(new FrontMarkBuilder("two").build())
            .autoComplete(-1)
            .build();
    FreeAtomType three =
        new TypeBuilder("three")
            .back(Helper.buildBackPrimitive("three"))
            .front(new FrontMarkBuilder("three").build())
            .autoComplete(-1)
            .build();
    FreeAtomType four =
        new TypeBuilder("four")
            .back(Helper.buildBackPrimitive("four"))
            .front(new FrontMarkBuilder("four").build())
            .autoComplete(-1)
            .build();
    FreeAtomType five =
        new TypeBuilder("five")
            .back(Helper.buildBackPrimitive("five"))
            .front(new FrontMarkBuilder("five").build())
            .autoComplete(-1)
            .build();
    FreeAtomType seven =
        new TypeBuilder("seven")
            .back(Helper.buildBackPrimitive("7"))
            .front(new FrontMarkBuilder("7").build())
            .autoComplete(-1)
            .build();
    FreeAtomType multiback =
        new TypeBuilder("multiback")
            .back(Helper.buildBackDataPrimitive("a"))
            .back(Helper.buildBackDataPrimitive("b"))
            .middlePrimitive("a")
            .middlePrimitive("b")
            .frontDataPrimitive("a")
            .frontMark("^")
            .frontDataPrimitive("b")
            .autoComplete(-1)
            .build();
    FreeAtomType quoted =
        new TypeBuilder("quoted")
            .middlePrimitive("value")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("value")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(1)
            .build();
    FreeAtomType digits =
        new TypeBuilder("digits")
            .middlePrimitiveDigits("value")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .autoComplete(1)
            .build();
    FreeAtomType doubleQuoted =
        new TypeBuilder("doubleuoted")
            .middlePrimitive("first")
            .middlePrimitive("second")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataPrimitive("first"))
                    .add("second", Helper.buildBackDataPrimitive("second"))
                    .build())
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("first")
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("second")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(-1)
            .build();
    FreeAtomType plus =
        new TypeBuilder("plus")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("+")
            .frontDataNode("second")
            .autoComplete(-1)
            .build();
    FreeAtomType plusEqual =
        new TypeBuilder("plusequal")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("+=")
            .frontDataNode("second")
            .autoComplete(-1)
            .build();
    FreeAtomType binaryBang =
        new TypeBuilder("bang")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("!")
            .frontDataNode("second")
            .autoComplete(1)
            .build();
    FreeAtomType waddle =
        new TypeBuilder("waddle")
            .middleAtom("first", "any")
            .back(new BackRecordBuilder().add("first", Helper.buildBackDataAtom("first")).build())
            .frontDataNode("first")
            .frontMark("?")
            .autoComplete(1)
            .build();
    FreeAtomType snooze =
        new TypeBuilder("snooze")
            .middleAtom("value", "any")
            .back(new BackRecordBuilder().add("value", Helper.buildBackDataAtom("value")).build())
            .frontMark("#")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType multiplier =
        new TypeBuilder("multiplier")
            .middlePrimitive("text")
            .middleAtom("value", "any")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value"))
                    .add("text", Helper.buildBackDataPrimitive("text"))
                    .build())
            .frontMark("x")
            .frontDataPrimitive("text")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType array =
        new TypeBuilder("array")
            .middleArray("value", "any")
            .back(Helper.buildBackDataArray("value"))
            .frontMark("[")
            .front(
                new FrontDataArrayBuilder("value")
                    .addSeparator(new FrontMarkBuilder(", ").build())
                    .build())
            .frontMark("]")
            .autoComplete(1)
            .build();
    FreeAtomType doubleArray =
        new TypeBuilder("doublearray")
            .middleArray("first", "any")
            .middleArray("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataArray("first"))
                    .add("second", Helper.buildBackDataArray("second"))
                    .build())
            .frontMark("[")
            .frontDataArray("first")
            .frontMark("?")
            .frontDataArray("second")
            .frontMark("]")
            .build();
    FreeAtomType record =
        new TypeBuilder("record")
            .middleRecord("value", "record_element")
            .back(Helper.buildBackDataRecord("value"))
            .frontMark("{")
            .frontDataArray("value")
            .frontMark("}")
            .autoComplete(1)
            .build();
    FreeAtomType recordElement =
        new TypeBuilder("record_element")
            .middlePrimitive("key")
            .middleAtom("value", "any")
            .back(Helper.buildBackDataKey("key"))
            .back(Helper.buildBackDataAtom("value"))
            .frontDataPrimitive("key")
            .frontMark(": ")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType pair =
        new TypeBuilder("pair")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackArrayBuilder()
                    .add(Helper.buildBackDataAtom("first"))
                    .add(Helper.buildBackDataAtom("second"))
                    .build())
            .frontMark("<")
            .frontDataNode("first")
            .frontMark(", ")
            .frontDataNode("second")
            .frontMark(">")
            .autoComplete(1)
            .build();
    FreeAtomType ratio =
        new TypeBuilder("ratio")
            .middlePrimitive("first")
            .middlePrimitive("second")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataPrimitive("first"))
                    .add("second", Helper.buildBackDataPrimitive("second"))
                    .build())
            .frontMark("<")
            .frontDataPrimitive("first")
            .frontMark(":")
            .frontDataPrimitive("second")
            .frontMark(">")
            .build();
    FreeAtomType restricted =
        new TypeBuilder("restricted")
            .middleAtom("value", "restricted_group")
            .back(new BackRecordBuilder().add("value", Helper.buildBackDataAtom("value")).build())
            .frontDataNode("value")
            .build();
    FreeAtomType restrictedArray =
        new TypeBuilder("restricted_array")
            .middleArray("value", "restricted_array_group")
            .back(Helper.buildBackDataArray("value"))
            .frontMark("_")
            .front(new FrontDataArrayBuilder("value").build())
            .autoComplete(1)
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(infinity)
            .type(one)
            .type(two)
            .type(three)
            .type(four)
            .type(five)
            .type(seven)
            .type(multiback)
            .type(quoted)
            .type(digits)
            .type(doubleQuoted)
            .type(plus)
            .type(plusEqual)
            .type(binaryBang)
            .type(waddle)
            .type(snooze)
            .type(multiplier)
            .type(array)
            .type(doubleArray)
            .type(record)
            .type(recordElement)
            .type(pair)
            .type(ratio)
            .type(restricted)
            .type(restrictedArray)
            .group(
                "test_group_1",
                new GroupBuilder()
                    .type(infinity)
                    .type(one)
                    .type(multiback)
                    .group("test_group_2")
                    .build())
            .group("test_group_2", new GroupBuilder().type(quoted).build())
            .group("restricted_group", new GroupBuilder().type(quoted).build())
            .group("restricted_array_group", new GroupBuilder().type(quoted).build())
            .group(
                "any",
                new GroupBuilder()
                    .type(infinity)
                    .type(one)
                    .type(two)
                    .type(three)
                    .type(four)
                    .type(five)
                    .type(quoted)
                    .type(digits)
                    .type(seven)
                    .type(plus)
                    .type(plusEqual)
                    .type(binaryBang)
                    .type(waddle)
                    .type(snooze)
                    .type(multiplier)
                    .type(array)
                    .type(restrictedArray)
                    .type(record)
                    .type(pair)
                    .type(ratio)
                    .build())
            .group("arrayChildren", new GroupBuilder().type(one).type(multiback).build())
            .build();

    innerTestTransform(
        syntax,
        () -> new TreeBuilder(array).addArray("value").build(),
        context -> {
          ((ValueArray) context.locateLong(new Path("0"))).visual.selectDown(context);
        },
        new TreeBuilder(array).addArray("value", syntax.gap.create()).build());
  }

  @Test
  public void arrayGapCreation_testCreateArrayDefault() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(1)
            .build();
    FreeAtomType one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .autoComplete(-1)
            .build();
    FreeAtomType two =
        new TypeBuilder("two")
            .back(Helper.buildBackPrimitive("two"))
            .front(new FrontMarkBuilder("two").build())
            .autoComplete(-1)
            .build();
    FreeAtomType three =
        new TypeBuilder("three")
            .back(Helper.buildBackPrimitive("three"))
            .front(new FrontMarkBuilder("three").build())
            .autoComplete(-1)
            .build();
    FreeAtomType four =
        new TypeBuilder("four")
            .back(Helper.buildBackPrimitive("four"))
            .front(new FrontMarkBuilder("four").build())
            .autoComplete(-1)
            .build();
    FreeAtomType five =
        new TypeBuilder("five")
            .back(Helper.buildBackPrimitive("five"))
            .front(new FrontMarkBuilder("five").build())
            .autoComplete(-1)
            .build();
    FreeAtomType seven =
        new TypeBuilder("seven")
            .back(Helper.buildBackPrimitive("7"))
            .front(new FrontMarkBuilder("7").build())
            .autoComplete(-1)
            .build();
    FreeAtomType multiback =
        new TypeBuilder("multiback")
            .back(Helper.buildBackDataPrimitive("a"))
            .back(Helper.buildBackDataPrimitive("b"))
            .middlePrimitive("a")
            .middlePrimitive("b")
            .frontDataPrimitive("a")
            .frontMark("^")
            .frontDataPrimitive("b")
            .autoComplete(-1)
            .build();
    FreeAtomType quoted =
        new TypeBuilder("quoted")
            .middlePrimitive("value")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("value")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(1)
            .build();
    FreeAtomType digits =
        new TypeBuilder("digits")
            .middlePrimitiveDigits("value")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .autoComplete(1)
            .build();
    FreeAtomType doubleQuoted =
        new TypeBuilder("doubleuoted")
            .middlePrimitive("first")
            .middlePrimitive("second")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataPrimitive("first"))
                    .add("second", Helper.buildBackDataPrimitive("second"))
                    .build())
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("first")
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("second")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(-1)
            .build();
    FreeAtomType plus =
        new TypeBuilder("plus")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("+")
            .frontDataNode("second")
            .autoComplete(-1)
            .build();
    FreeAtomType plusEqual =
        new TypeBuilder("plusequal")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("+=")
            .frontDataNode("second")
            .autoComplete(-1)
            .build();
    FreeAtomType binaryBang =
        new TypeBuilder("bang")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("!")
            .frontDataNode("second")
            .autoComplete(1)
            .build();
    FreeAtomType waddle =
        new TypeBuilder("waddle")
            .middleAtom("first", "any")
            .back(new BackRecordBuilder().add("first", Helper.buildBackDataAtom("first")).build())
            .frontDataNode("first")
            .frontMark("?")
            .autoComplete(1)
            .build();
    FreeAtomType snooze =
        new TypeBuilder("snooze")
            .middleAtom("value", "any")
            .back(new BackRecordBuilder().add("value", Helper.buildBackDataAtom("value")).build())
            .frontMark("#")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType multiplier =
        new TypeBuilder("multiplier")
            .middlePrimitive("text")
            .middleAtom("value", "any")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value"))
                    .add("text", Helper.buildBackDataPrimitive("text"))
                    .build())
            .frontMark("x")
            .frontDataPrimitive("text")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType array =
        new TypeBuilder("array")
            .middleArray("value", "any")
            .back(Helper.buildBackDataArray("value"))
            .frontMark("[")
            .front(
                new FrontDataArrayBuilder("value")
                    .addSeparator(new FrontMarkBuilder(", ").build())
                    .build())
            .frontMark("]")
            .autoComplete(1)
            .build();
    FreeAtomType doubleArray =
        new TypeBuilder("doublearray")
            .middleArray("first", "any")
            .middleArray("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataArray("first"))
                    .add("second", Helper.buildBackDataArray("second"))
                    .build())
            .frontMark("[")
            .frontDataArray("first")
            .frontMark("?")
            .frontDataArray("second")
            .frontMark("]")
            .build();
    FreeAtomType record =
        new TypeBuilder("record")
            .middleRecord("value", "record_element")
            .back(Helper.buildBackDataRecord("value"))
            .frontMark("{")
            .frontDataArray("value")
            .frontMark("}")
            .autoComplete(1)
            .build();
    FreeAtomType recordElement =
        new TypeBuilder("record_element")
            .middlePrimitive("key")
            .middleAtom("value", "any")
            .back(Helper.buildBackDataKey("key"))
            .back(Helper.buildBackDataAtom("value"))
            .frontDataPrimitive("key")
            .frontMark(": ")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType pair =
        new TypeBuilder("pair")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackArrayBuilder()
                    .add(Helper.buildBackDataAtom("first"))
                    .add(Helper.buildBackDataAtom("second"))
                    .build())
            .frontMark("<")
            .frontDataNode("first")
            .frontMark(", ")
            .frontDataNode("second")
            .frontMark(">")
            .autoComplete(1)
            .build();
    FreeAtomType ratio =
        new TypeBuilder("ratio")
            .middlePrimitive("first")
            .middlePrimitive("second")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataPrimitive("first"))
                    .add("second", Helper.buildBackDataPrimitive("second"))
                    .build())
            .frontMark("<")
            .frontDataPrimitive("first")
            .frontMark(":")
            .frontDataPrimitive("second")
            .frontMark(">")
            .build();
    FreeAtomType restricted =
        new TypeBuilder("restricted")
            .middleAtom("value", "restricted_group")
            .back(new BackRecordBuilder().add("value", Helper.buildBackDataAtom("value")).build())
            .frontDataNode("value")
            .build();
    FreeAtomType restrictedArray =
        new TypeBuilder("restricted_array")
            .middleArray("value", "restricted_array_group")
            .back(Helper.buildBackDataArray("value"))
            .frontMark("_")
            .front(new FrontDataArrayBuilder("value").build())
            .autoComplete(1)
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(infinity)
            .type(one)
            .type(two)
            .type(three)
            .type(four)
            .type(five)
            .type(seven)
            .type(multiback)
            .type(quoted)
            .type(digits)
            .type(doubleQuoted)
            .type(plus)
            .type(plusEqual)
            .type(binaryBang)
            .type(waddle)
            .type(snooze)
            .type(multiplier)
            .type(array)
            .type(doubleArray)
            .type(record)
            .type(recordElement)
            .type(pair)
            .type(ratio)
            .type(restricted)
            .type(restrictedArray)
            .group(
                "test_group_1",
                new GroupBuilder()
                    .type(infinity)
                    .type(one)
                    .type(multiback)
                    .group("test_group_2")
                    .build())
            .group("test_group_2", new GroupBuilder().type(quoted).build())
            .group("restricted_group", new GroupBuilder().type(quoted).build())
            .group("restricted_array_group", new GroupBuilder().type(quoted).build())
            .group(
                "any",
                new GroupBuilder()
                    .type(infinity)
                    .type(one)
                    .type(two)
                    .type(three)
                    .type(four)
                    .type(five)
                    .type(quoted)
                    .type(digits)
                    .type(seven)
                    .type(plus)
                    .type(plusEqual)
                    .type(binaryBang)
                    .type(waddle)
                    .type(snooze)
                    .type(multiplier)
                    .type(array)
                    .type(restrictedArray)
                    .type(record)
                    .type(pair)
                    .type(ratio)
                    .build())
            .group("arrayChildren", new GroupBuilder().type(one).type(multiback).build())
            .build();

    innerTestTransform(
        syntax,
        () -> new TreeBuilder(restrictedArray).addArray("value").build(),
        context -> {
          ((ValueArray) context.locateLong(new Path("0"))).visual.selectDown(context);
        },
        new TreeBuilder(restrictedArray)
            .addArray("value", new TreeBuilder(quoted).add("value", "").build())
            .build());
  }

  @Test
  public void arrayGapCreation_testFillArrayFromGapDefault() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(1)
            .build();
    FreeAtomType one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .autoComplete(-1)
            .build();
    FreeAtomType two =
        new TypeBuilder("two")
            .back(Helper.buildBackPrimitive("two"))
            .front(new FrontMarkBuilder("two").build())
            .autoComplete(-1)
            .build();
    FreeAtomType three =
        new TypeBuilder("three")
            .back(Helper.buildBackPrimitive("three"))
            .front(new FrontMarkBuilder("three").build())
            .autoComplete(-1)
            .build();
    FreeAtomType four =
        new TypeBuilder("four")
            .back(Helper.buildBackPrimitive("four"))
            .front(new FrontMarkBuilder("four").build())
            .autoComplete(-1)
            .build();
    FreeAtomType five =
        new TypeBuilder("five")
            .back(Helper.buildBackPrimitive("five"))
            .front(new FrontMarkBuilder("five").build())
            .autoComplete(-1)
            .build();
    FreeAtomType seven =
        new TypeBuilder("seven")
            .back(Helper.buildBackPrimitive("7"))
            .front(new FrontMarkBuilder("7").build())
            .autoComplete(-1)
            .build();
    FreeAtomType multiback =
        new TypeBuilder("multiback")
            .back(Helper.buildBackDataPrimitive("a"))
            .back(Helper.buildBackDataPrimitive("b"))
            .middlePrimitive("a")
            .middlePrimitive("b")
            .frontDataPrimitive("a")
            .frontMark("^")
            .frontDataPrimitive("b")
            .autoComplete(-1)
            .build();
    FreeAtomType quoted =
        new TypeBuilder("quoted")
            .middlePrimitive("value")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("value")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(1)
            .build();
    FreeAtomType digits =
        new TypeBuilder("digits")
            .middlePrimitiveDigits("value")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .autoComplete(1)
            .build();
    FreeAtomType doubleQuoted =
        new TypeBuilder("doubleuoted")
            .middlePrimitive("first")
            .middlePrimitive("second")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataPrimitive("first"))
                    .add("second", Helper.buildBackDataPrimitive("second"))
                    .build())
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("first")
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("second")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(-1)
            .build();
    FreeAtomType plus =
        new TypeBuilder("plus")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("+")
            .frontDataNode("second")
            .autoComplete(-1)
            .build();
    FreeAtomType plusEqual =
        new TypeBuilder("plusequal")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("+=")
            .frontDataNode("second")
            .autoComplete(-1)
            .build();
    FreeAtomType binaryBang =
        new TypeBuilder("bang")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first"))
                    .add("second", Helper.buildBackDataAtom("second"))
                    .build())
            .frontDataNode("first")
            .frontMark("!")
            .frontDataNode("second")
            .autoComplete(1)
            .build();
    FreeAtomType waddle =
        new TypeBuilder("waddle")
            .middleAtom("first", "any")
            .back(new BackRecordBuilder().add("first", Helper.buildBackDataAtom("first")).build())
            .frontDataNode("first")
            .frontMark("?")
            .autoComplete(1)
            .build();
    FreeAtomType snooze =
        new TypeBuilder("snooze")
            .middleAtom("value", "any")
            .back(new BackRecordBuilder().add("value", Helper.buildBackDataAtom("value")).build())
            .frontMark("#")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType multiplier =
        new TypeBuilder("multiplier")
            .middlePrimitive("text")
            .middleAtom("value", "any")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value"))
                    .add("text", Helper.buildBackDataPrimitive("text"))
                    .build())
            .frontMark("x")
            .frontDataPrimitive("text")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType array =
        new TypeBuilder("array")
            .middleArray("value", "any")
            .back(Helper.buildBackDataArray("value"))
            .frontMark("[")
            .front(
                new FrontDataArrayBuilder("value")
                    .addSeparator(new FrontMarkBuilder(", ").build())
                    .build())
            .frontMark("]")
            .autoComplete(1)
            .build();
    FreeAtomType doubleArray =
        new TypeBuilder("doublearray")
            .middleArray("first", "any")
            .middleArray("second", "any")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataArray("first"))
                    .add("second", Helper.buildBackDataArray("second"))
                    .build())
            .frontMark("[")
            .frontDataArray("first")
            .frontMark("?")
            .frontDataArray("second")
            .frontMark("]")
            .build();
    FreeAtomType record =
        new TypeBuilder("record")
            .middleRecord("value", "record_element")
            .back(Helper.buildBackDataRecord("value"))
            .frontMark("{")
            .frontDataArray("value")
            .frontMark("}")
            .autoComplete(1)
            .build();
    FreeAtomType recordElement =
        new TypeBuilder("record_element")
            .middlePrimitive("key")
            .middleAtom("value", "any")
            .back(Helper.buildBackDataKey("key"))
            .back(Helper.buildBackDataAtom("value"))
            .frontDataPrimitive("key")
            .frontMark(": ")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    FreeAtomType pair =
        new TypeBuilder("pair")
            .middleAtom("first", "any")
            .middleAtom("second", "any")
            .back(
                new BackArrayBuilder()
                    .add(Helper.buildBackDataAtom("first"))
                    .add(Helper.buildBackDataAtom("second"))
                    .build())
            .frontMark("<")
            .frontDataNode("first")
            .frontMark(", ")
            .frontDataNode("second")
            .frontMark(">")
            .autoComplete(1)
            .build();
    FreeAtomType ratio =
        new TypeBuilder("ratio")
            .middlePrimitive("first")
            .middlePrimitive("second")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataPrimitive("first"))
                    .add("second", Helper.buildBackDataPrimitive("second"))
                    .build())
            .frontMark("<")
            .frontDataPrimitive("first")
            .frontMark(":")
            .frontDataPrimitive("second")
            .frontMark(">")
            .build();
    FreeAtomType restricted =
        new TypeBuilder("restricted")
            .middleAtom("value", "restricted_group")
            .back(new BackRecordBuilder().add("value", Helper.buildBackDataAtom("value")).build())
            .frontDataNode("value")
            .build();
    FreeAtomType restrictedArray =
        new TypeBuilder("restricted_array")
            .middleArray("value", "restricted_array_group")
            .back(Helper.buildBackDataArray("value"))
            .frontMark("_")
            .front(new FrontDataArrayBuilder("value").build())
            .autoComplete(1)
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(infinity)
            .type(one)
            .type(two)
            .type(three)
            .type(four)
            .type(five)
            .type(seven)
            .type(multiback)
            .type(quoted)
            .type(digits)
            .type(doubleQuoted)
            .type(plus)
            .type(plusEqual)
            .type(binaryBang)
            .type(waddle)
            .type(snooze)
            .type(multiplier)
            .type(array)
            .type(doubleArray)
            .type(record)
            .type(recordElement)
            .type(pair)
            .type(ratio)
            .type(restricted)
            .type(restrictedArray)
            .group(
                "test_group_1",
                new GroupBuilder()
                    .type(infinity)
                    .type(one)
                    .type(multiback)
                    .group("test_group_2")
                    .build())
            .group("test_group_2", new GroupBuilder().type(quoted).build())
            .group("restricted_group", new GroupBuilder().type(quoted).build())
            .group("restricted_array_group", new GroupBuilder().type(quoted).build())
            .group(
                "any",
                new GroupBuilder()
                    .type(infinity)
                    .type(one)
                    .type(two)
                    .type(three)
                    .type(four)
                    .type(five)
                    .type(quoted)
                    .type(digits)
                    .type(seven)
                    .type(plus)
                    .type(plusEqual)
                    .type(binaryBang)
                    .type(waddle)
                    .type(snooze)
                    .type(multiplier)
                    .type(array)
                    .type(restrictedArray)
                    .type(record)
                    .type(pair)
                    .type(ratio)
                    .build())
            .group("arrayChildren", new GroupBuilder().type(one).type(multiback).build())
            .build();

    new GeneralTestWizard(syntax, syntax.gap.create())
        .act("enter")
        .sendText("_")
        .checkArrayTree(
            new TreeBuilder(restrictedArray)
                .addArray("value", new TreeBuilder(quoted).add("value", "").build())
                .build());
  }
}
