package com.zarbosoft.merman.editorcore;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.document.fields.FieldArray;
import com.zarbosoft.merman.core.document.fields.FieldPrimitive;
import com.zarbosoft.merman.core.syntax.FreeAtomType;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.core.visual.visuals.VisualFrontPrimitive;
import com.zarbosoft.merman.editorcore.gap.EditGapCursor;
import com.zarbosoft.merman.editorcore.helper.BackArrayBuilder;
import com.zarbosoft.merman.editorcore.helper.BackRecordBuilder;
import com.zarbosoft.merman.editorcore.helper.FrontDataArrayBuilder;
import com.zarbosoft.merman.editorcore.helper.FrontMarkBuilder;
import com.zarbosoft.merman.editorcore.helper.GeneralTestWizard;
import com.zarbosoft.merman.editorcore.helper.GroupBuilder;
import com.zarbosoft.merman.editorcore.helper.Helper;
import com.zarbosoft.merman.editorcore.helper.SyntaxBuilder;
import com.zarbosoft.merman.editorcore.helper.TreeBuilder;
import com.zarbosoft.merman.editorcore.helper.TypeBuilder;
import com.zarbosoft.merman.editorcore.history.changes.ChangePrimitive;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSSet;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.zarbosoft.merman.editorcore.helper.Helper.assertTreeEqual;
import static com.zarbosoft.merman.editorcore.helper.Helper.buildDoc;
import static org.hamcrest.core.Is.is;
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
                new GroupBuilder().type(infinity).type(one).type(two).group("test_group_2").build())
            .group("test_group_2", new GroupBuilder().type(three).type(one).build())
            .group("unused", new GroupBuilder().type(four).type(one).build())
            .build();

    final Editor editor = blank(syntax);
    assertThat(
        editor.context.syntax.splayedTypes.get("test_group_1"),
        equalTo(TSSet.of(infinity, one, two, three)));
  }

  private Editor blank(Syntax syntax) {
    final Atom gap = Helper.createGap(syntax);
    final Editor editor = buildDoc(syntax, gap);
    gap.visual.selectAnyChild(editor.context);
    return editor;
  }

  // ========================================================================
  // ========================================================================
  // Decision making and replacement

  /**
   * Check that typing a letter that matches a type produces that type as a choice
   */
  @Test
  public void decisionMaking_choiceCount() {
    FreeAtomType quoted =
        new TypeBuilder("quoted")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("value")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(false)
            .build();
    FreeAtomType restricted =
        new TypeBuilder("restricted")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "restricted_group"))
                    .build())
            .frontDataNode("value")
            .build();
    Syntax syntax =
        new SyntaxBuilder("restricted_group")
            .type(quoted)
            .type(restricted)
            .group("restricted_group", new GroupBuilder().type(quoted).build())
            .build();

    final Atom gap = Helper.createGap(syntax);
    new GeneralTestWizard(syntax, new TreeBuilder(restricted).add("value", gap).build())
        .run(editor -> gap.fields.getOpt("gap").selectInto(editor.context))
        .sendText("q")
        .run(
            editor ->
                assertThat(
                    ((EditGapCursor) editor.context.cursor).choicePage.choices.size(), is(1)));
  }

  /**
   * With an ambiguous character (potentially matching multiple autocompletable types) a gap autocompletes neither
   */
  @Test
  public void decisionMaking_undecided() {
    FreeAtomType one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .autoComplete(true)
            .build();
    FreeAtomType onyx =
            new TypeBuilder("onyx")
                    .back(Helper.buildBackPrimitive("onyx"))
                    .front(new FrontMarkBuilder("onyx").build())
                    .autoComplete(true)
                    .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(one)
            .type(onyx)
            .group(
                "any",
                new GroupBuilder()
                    .type(one)
                        .type(onyx)
                    .build())
            .build();

    final Editor editor = blank(syntax);
    editor.context.cursor.handleTyping(editor.context, "o");
    assertTreeEqual(
        editor.context,
        new TreeBuilder(syntax.gap).add("gap", "o").build(),
        Helper.rootArray(editor.context.document));
  }

  /**
   * If ambiguous input is entered into a gap, even if the input exactly matches a choice, that choice isn't autocompleted.
   */
  @Test
  public void decisionMaking_undecidedFull() {
    FreeAtomType one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .autoComplete(true)
            .build();
    FreeAtomType onep =
        new TypeBuilder("onep")
            .back(Helper.buildBackPrimitive("onep"))
            .front(new FrontMarkBuilder("onep").build())
            .autoComplete(true)
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(one)
            .type(onep)
            .group(
                "any",
                new GroupBuilder()
                    .type(one)
                    .type(onep)
                    .build())
            .build();

    final Editor editor = blank(syntax);
    editor.context.cursor.handleTyping(editor.context, "o");
    editor.context.cursor.handleTyping(editor.context, "n");
    editor.context.cursor.handleTyping(editor.context, "e");
    assertTreeEqual(
        editor.context,
        new TreeBuilder(syntax.gap).add("gap", "one").build(),
        Helper.rootArray(editor.context.document));
  }

  /**
   * If a partial input is entered that matches only one autocomplete type, autocomplete.
   */
  @Test
  public void decisionMaking_immediate() {
    FreeAtomType one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .autoComplete(true)
            .build();
    FreeAtomType onut =
        new TypeBuilder("orgol")
            .back(Helper.buildBackPrimitive("orgol"))
            .front(new FrontMarkBuilder("orgol").build())
            .autoComplete(true)
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(one)
            .type(onut)
            .group(
                "any",
                new GroupBuilder()
                    .type(one)
                    .type(onut)
                    .build())
            .build();

    final Editor editor = blank(syntax);
    editor.context.cursor.handleTyping(editor.context, "o");
    editor.context.cursor.handleTyping(editor.context, "n");
    assertTreeEqual(
        editor.context,
        new TreeBuilder(syntax.suffixGap)
            .addArray("value", TSList.of(new TreeBuilder(one).build()))
            .add("gap", "")
            .build(),
        Helper.rootArray(editor.context.document));
  }

  @Test
  public void decisionMaking_binaryImmediate() {
    // Input that doesn't match fixed primitive string that matched previously causes it to be
    // converted (TODO split?)
    // Carryover text that matches unambiguously an autoconvert binary operator causes it to
    // autoconvert
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(true)
            .build();
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
    FreeAtomType seven =
        new TypeBuilder("seven")
            .back(Helper.buildBackPrimitive("7"))
            .front(new FrontMarkBuilder("7").build())
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
    FreeAtomType quoted =
        new TypeBuilder("quoted")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("value")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(true)
            .build();
    FreeAtomType digits =
        new TypeBuilder("digits")
            .back(Helper.buildBackDataPrimitiveDigits("value"))
            .frontDataPrimitive("value")
            .autoComplete(true)
            .build();
    FreeAtomType doubleQuoted =
        new TypeBuilder("doubleuoted")
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
            .autoComplete(false)
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
            .autoComplete(false)
            .build();
    FreeAtomType plusEqual =
        new TypeBuilder("plusequal")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("+=")
            .frontDataNode("second")
            .autoComplete(false)
            .build();
    FreeAtomType binaryBang =
        new TypeBuilder("bang")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("!")
            .frontDataNode("second")
            .autoComplete(true)
            .build();
    FreeAtomType waddle =
        new TypeBuilder("waddle")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("?")
            .autoComplete(true)
            .build();
    FreeAtomType snooze =
        new TypeBuilder("snooze")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "any"))
                    .build())
            .frontMark("#")
            .frontDataNode("value")
            .autoComplete(true)
            .build();
    FreeAtomType multiplier =
        new TypeBuilder("multiplier")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "any"))
                    .add("text", Helper.buildBackDataPrimitive("text"))
                    .build())
            .frontMark("x")
            .frontDataPrimitive("text")
            .frontDataNode("value")
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
    FreeAtomType doubleArray =
        new TypeBuilder("doublearray")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataArray("first", "any"))
                    .add("second", Helper.buildBackDataArray("second", "any"))
                    .build())
            .frontMark("[")
            .frontDataArray("first")
            .frontMark("?")
            .frontDataArray("second")
            .frontMark("]")
            .build();
    FreeAtomType record =
        new TypeBuilder("record")
            .back(Helper.buildBackDataRecord("value", "record_element"))
            .frontMark("{")
            .frontDataArray("value")
            .frontMark("}")
            .autoComplete(true)
            .build();
    FreeAtomType recordElement =
        new TypeBuilder("record_element")
            .back(Helper.buildBackDataKey("key"))
            .back(Helper.buildBackDataAtom("value", "any"))
            .frontDataPrimitive("key")
            .frontMark(": ")
            .frontDataNode("value")
            .autoComplete(true)
            .build();
    FreeAtomType pair =
        new TypeBuilder("pair")
            .back(
                new BackArrayBuilder()
                    .add(Helper.buildBackDataAtom("first", "any"))
                    .add(Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontMark("<")
            .frontDataNode("first")
            .frontMark(", ")
            .frontDataNode("second")
            .frontMark(">")
            .autoComplete(true)
            .build();
    FreeAtomType ratio =
        new TypeBuilder("ratio")
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
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "restricted_group"))
                    .build())
            .frontDataNode("value")
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

    final Editor editor = blank(syntax);
    editor.context.cursor.handleTyping(editor.context, "one");
    editor.context.cursor.handleTyping(editor.context, "!");
    editor.context.cursor.handleTyping(editor.context, "t");
    assertTreeEqual(
        editor.context,
        new TreeBuilder(binaryBang)
            .add("first", new TreeBuilder(one))
            .add("second", new TreeBuilder(syntax.gap).add("gap", "t"))
            .build(),
        Helper.rootArray(editor.context.document));
  }

  @Test
  public void decisionMaking_binaryTextImmediate() {
    // Input that doesn't match data primitive string by pattern that matched previously causes it
    // to be converted (TODO separate?)
    // Carryover text that matches unambiguously an autoconvert binary operator causes it to
    // autoconvert
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(true)
            .build();
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
    FreeAtomType seven =
        new TypeBuilder("seven")
            .back(Helper.buildBackPrimitive("7"))
            .front(new FrontMarkBuilder("7").build())
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
    FreeAtomType quoted =
        new TypeBuilder("quoted")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("value")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(true)
            .build();
    FreeAtomType digits =
        new TypeBuilder("digits")
            .back(Helper.buildBackDataPrimitiveDigits("value"))
            .frontDataPrimitive("value")
            .autoComplete(true)
            .build();
    FreeAtomType doubleQuoted =
        new TypeBuilder("doubleuoted")
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
            .autoComplete(false)
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
            .autoComplete(false)
            .build();
    FreeAtomType plusEqual =
        new TypeBuilder("plusequal")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("+=")
            .frontDataNode("second")
            .autoComplete(false)
            .build();
    FreeAtomType binaryBang =
        new TypeBuilder("bang")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("!")
            .frontDataNode("second")
            .autoComplete(true)
            .build();
    FreeAtomType waddle =
        new TypeBuilder("waddle")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("?")
            .autoComplete(true)
            .build();
    FreeAtomType snooze =
        new TypeBuilder("snooze")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "any"))
                    .build())
            .frontMark("#")
            .frontDataNode("value")
            .autoComplete(true)
            .build();
    FreeAtomType multiplier =
        new TypeBuilder("multiplier")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "any"))
                    .add("text", Helper.buildBackDataPrimitive("text"))
                    .build())
            .frontMark("x")
            .frontDataPrimitive("text")
            .frontDataNode("value")
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
    FreeAtomType doubleArray =
        new TypeBuilder("doublearray")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataArray("first", "any"))
                    .add("second", Helper.buildBackDataArray("second", "any"))
                    .build())
            .frontMark("[")
            .frontDataArray("first")
            .frontMark("?")
            .frontDataArray("second")
            .frontMark("]")
            .build();
    FreeAtomType record =
        new TypeBuilder("record")
            .back(Helper.buildBackDataRecord("value", "record_element"))
            .frontMark("{")
            .frontDataArray("value")
            .frontMark("}")
            .autoComplete(true)
            .build();
    FreeAtomType recordElement =
        new TypeBuilder("record_element")
            .back(Helper.buildBackDataKey("key"))
            .back(Helper.buildBackDataAtom("value", "any"))
            .frontDataPrimitive("key")
            .frontMark(": ")
            .frontDataNode("value")
            .autoComplete(true)
            .build();
    FreeAtomType pair =
        new TypeBuilder("pair")
            .back(
                new BackArrayBuilder()
                    .add(Helper.buildBackDataAtom("first", "any"))
                    .add(Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontMark("<")
            .frontDataNode("first")
            .frontMark(", ")
            .frontDataNode("second")
            .frontMark(">")
            .autoComplete(true)
            .build();
    FreeAtomType ratio =
        new TypeBuilder("ratio")
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
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "restricted_group"))
                    .build())
            .frontDataNode("value")
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

    final Editor editor = blank(syntax);
    editor.context.cursor.handleTyping(editor.context, "7");
    editor.context.cursor.handleTyping(editor.context, "!");
    editor.context.cursor.handleTyping(editor.context, "t");
    assertTreeEqual(
        editor.context,
        new TreeBuilder(binaryBang)
            .add("first", new TreeBuilder(digits).add("value", "7"))
            .add("second", new TreeBuilder(syntax.gap).add("gap", "t"))
            .build(),
        Helper.rootArray(editor.context.document));
  }

  @Test
  public void decisionMaking_textPrimitiveImmediate() {
    // Typing a character that only matches dynamic primitive pattern autocompletes to that type
    // ex: 9 can only appear in digit type, so pressing 9 autocompletes to digit type (1 choice)
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(true)
            .build();
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
    FreeAtomType seven =
        new TypeBuilder("seven")
            .back(Helper.buildBackPrimitive("7"))
            .front(new FrontMarkBuilder("7").build())
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
    FreeAtomType quoted =
        new TypeBuilder("quoted")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("value")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(true)
            .build();
    FreeAtomType digits =
        new TypeBuilder("digits")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .autoComplete(true)
            .build();
    FreeAtomType doubleQuoted =
        new TypeBuilder("doubleuoted")
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
            .autoComplete(false)
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
            .autoComplete(false)
            .build();
    FreeAtomType plusEqual =
        new TypeBuilder("plusequal")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("+=")
            .frontDataNode("second")
            .autoComplete(false)
            .build();
    FreeAtomType binaryBang =
        new TypeBuilder("bang")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("!")
            .frontDataNode("second")
            .autoComplete(true)
            .build();
    FreeAtomType waddle =
        new TypeBuilder("waddle")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("?")
            .autoComplete(true)
            .build();
    FreeAtomType snooze =
        new TypeBuilder("snooze")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "any"))
                    .build())
            .frontMark("#")
            .frontDataNode("value")
            .autoComplete(true)
            .build();
    FreeAtomType multiplier =
        new TypeBuilder("multiplier")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "any"))
                    .add("text", Helper.buildBackDataPrimitive("text"))
                    .build())
            .frontMark("x")
            .frontDataPrimitive("text")
            .frontDataNode("value")
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
    FreeAtomType doubleArray =
        new TypeBuilder("doublearray")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataArray("first", "any"))
                    .add("second", Helper.buildBackDataArray("second", "any"))
                    .build())
            .frontMark("[")
            .frontDataArray("first")
            .frontMark("?")
            .frontDataArray("second")
            .frontMark("]")
            .build();
    FreeAtomType record =
        new TypeBuilder("record")
            .back(Helper.buildBackDataRecord("value", "record_element"))
            .frontMark("{")
            .frontDataArray("value")
            .frontMark("}")
            .autoComplete(true)
            .build();
    FreeAtomType recordElement =
        new TypeBuilder("record_element")
            .back(Helper.buildBackDataKey("key"))
            .back(Helper.buildBackDataAtom("value", "any"))
            .frontDataPrimitive("key")
            .frontMark(": ")
            .frontDataNode("value")
            .autoComplete(true)
            .build();
    FreeAtomType pair =
        new TypeBuilder("pair")
            .back(
                new BackArrayBuilder()
                    .add(Helper.buildBackDataAtom("first", "any"))
                    .add(Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontMark("<")
            .frontDataNode("first")
            .frontMark(", ")
            .frontDataNode("second")
            .frontMark(">")
            .autoComplete(true)
            .build();
    FreeAtomType ratio =
        new TypeBuilder("ratio")
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
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "restricted_group"))
                    .build())
            .frontDataNode("value")
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

    final Atom atom = Helper.createGap(syntax);
    new GeneralTestWizard(syntax, atom)
        .run(editor -> atom.fields.getOpt("gap").selectInto(editor.context))
        .sendText("9")
        .checkArrayTree(new TreeBuilder(digits).add("value", "9").build())
        .run(
            editor -> {
              final VisualFrontPrimitive.RangeAttachment range =
                  ((VisualFrontPrimitive.Cursor) editor.context.cursor).range;
              assertThat(range.beginOffset, equalTo(1));
              assertThat(range.endOffset, equalTo(1));
            });
  }

  @Test
  public void decisionMaking_textMark1Immediate() {
    // input is ambiguous if type A or type B followed by type C
    // when input becomes clear that it's type B followed by C, both B and C are resolved
    // immediately
    // (part of the input doesn't match b, so must start with resolving b)
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
            .back(
                new BackRecordBuilder()
                    .add("text", Helper.buildBackDataPrimitiveLetters("text"))
                    .add("atom", Helper.buildBackDataAtom("atom", "any"))
                    .build())
            .frontDataPrimitive("text")
            .frontMark("$1")
            .frontDataNode("atom")
            .autoComplete(true)
            .build();
    textMark2 =
        new TypeBuilder("textmark2")
            .back(
                new BackRecordBuilder()
                    .add("text", Helper.buildBackDataPrimitiveLetters("text"))
                    .add("atom", Helper.buildBackDataAtom("atom", "any"))
                    .add("atom2", Helper.buildBackDataAtom("atom2", "any"))
                    .build())
            .frontDataPrimitive("text")
            .frontMark("$2")
            .frontDataNode("atom")
            .frontDataNode("atom2")
            .autoComplete(true)
            .build();
    textMark3 =
        new TypeBuilder("textmark3")
            .back(
                new BackRecordBuilder()
                    .add("text", Helper.buildBackDataPrimitiveLetters("text"))
                    .add("atom", Helper.buildBackDataAtom("atom", "any"))
                    .add("atom2", Helper.buildBackDataAtom("atom2", "any"))
                    .build())
            .frontDataNode("atom2")
            .frontDataPrimitive("text")
            .frontMark("$3")
            .frontDataNode("atom")
            .autoComplete(true)
            .build();
    ambiguateTextMark3 =
        new TypeBuilder("atextmark3")
            .back(
                new BackRecordBuilder()
                    .add("atom", Helper.buildBackDataAtom("atom", "any"))
                    .build())
            .frontDataNode("atom")
            .frontMark("t")
            .autoComplete(true)
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

    final Atom atom = Helper.createGap(syntax);
    new GeneralTestWizard(syntax, atom)
        .run(editor -> atom.fields.getOpt("gap").selectInto(editor.context))
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
            editor -> {
              final VisualFrontPrimitive.RangeAttachment range =
                  ((VisualFrontPrimitive.Cursor) editor.context.cursor).range;
              assertThat(range.beginOffset, equalTo(1));
              assertThat(range.endOffset, equalTo(1));
            });
  }

  @Test
  public void decisionMaking_binaryUndecided() {
    // ambiguity in suffix gap, doesn't auto resolve
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(true)
            .build();
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
    FreeAtomType seven =
        new TypeBuilder("seven")
            .back(Helper.buildBackPrimitive("7"))
            .front(new FrontMarkBuilder("7").build())
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
    FreeAtomType quoted =
        new TypeBuilder("quoted")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("value")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(true)
            .build();
    FreeAtomType digits =
        new TypeBuilder("digits")
            .back(Helper.buildBackDataPrimitiveDigits("value"))
            .frontDataPrimitive("value")
            .autoComplete(true)
            .build();
    FreeAtomType doubleQuoted =
        new TypeBuilder("doubleuoted")
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
            .autoComplete(false)
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
            .autoComplete(false)
            .build();
    FreeAtomType plusEqual =
        new TypeBuilder("plusequal")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("+=")
            .frontDataNode("second")
            .autoComplete(false)
            .build();
    FreeAtomType binaryBang =
        new TypeBuilder("bang")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("!")
            .frontDataNode("second")
            .autoComplete(true)
            .build();
    FreeAtomType waddle =
        new TypeBuilder("waddle")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("?")
            .autoComplete(true)
            .build();
    FreeAtomType snooze =
        new TypeBuilder("snooze")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "any"))
                    .build())
            .frontMark("#")
            .frontDataNode("value")
            .autoComplete(true)
            .build();
    FreeAtomType multiplier =
        new TypeBuilder("multiplier")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "any"))
                    .add("text", Helper.buildBackDataPrimitive("text"))
                    .build())
            .frontMark("x")
            .frontDataPrimitive("text")
            .frontDataNode("value")
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
    FreeAtomType doubleArray =
        new TypeBuilder("doublearray")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataArray("first", "any"))
                    .add("second", Helper.buildBackDataArray("second", "any"))
                    .build())
            .frontMark("[")
            .frontDataArray("first")
            .frontMark("?")
            .frontDataArray("second")
            .frontMark("]")
            .build();
    FreeAtomType record =
        new TypeBuilder("record")
            .back(Helper.buildBackDataRecord("value", "record_element"))
            .frontMark("{")
            .frontDataArray("value")
            .frontMark("}")
            .autoComplete(true)
            .build();
    FreeAtomType recordElement =
        new TypeBuilder("record_element")
            .back(Helper.buildBackDataKey("key"))
            .back(Helper.buildBackDataAtom("value", "any"))
            .frontDataPrimitive("key")
            .frontMark(": ")
            .frontDataNode("value")
            .autoComplete(true)
            .build();
    FreeAtomType pair =
        new TypeBuilder("pair")
            .back(
                new BackArrayBuilder()
                    .add(Helper.buildBackDataAtom("first", "any"))
                    .add(Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontMark("<")
            .frontDataNode("first")
            .frontMark(", ")
            .frontDataNode("second")
            .frontMark(">")
            .autoComplete(true)
            .build();
    FreeAtomType ratio =
        new TypeBuilder("ratio")
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
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "restricted_group"))
                    .build())
            .frontDataNode("value")
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

    final Editor editor = blank(syntax);
    editor.context.cursor.handleTyping(editor.context, "one");
    editor.context.cursor.handleTyping(editor.context, "+");
    assertTreeEqual(
        editor.context,
        new TreeBuilder(syntax.suffixGap)
            .addArray("value", TSList.of(new TreeBuilder(one).build()))
            .add("gap", "+")
            .build(),
        Helper.rootArray(editor.context.document));
  }

  @Test
  public void decisionMaking_immediateReplaceOnSelection() {
    // a partially completed gap will be completed automatically when deselected
    // TODO don't do this maybe
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(true)
            .build();
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
    FreeAtomType seven =
        new TypeBuilder("seven")
            .back(Helper.buildBackPrimitive("7"))
            .front(new FrontMarkBuilder("7").build())
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
    FreeAtomType quoted =
        new TypeBuilder("quoted")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("value")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(true)
            .build();
    FreeAtomType digits =
        new TypeBuilder("digits")
            .back(Helper.buildBackDataPrimitiveDigits("value"))
            .frontDataPrimitive("value")
            .autoComplete(true)
            .build();
    FreeAtomType doubleQuoted =
        new TypeBuilder("doubleuoted")
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
            .autoComplete(false)
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
            .autoComplete(false)
            .build();
    FreeAtomType plusEqual =
        new TypeBuilder("plusequal")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("+=")
            .frontDataNode("second")
            .autoComplete(false)
            .build();
    FreeAtomType binaryBang =
        new TypeBuilder("bang")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("!")
            .frontDataNode("second")
            .autoComplete(true)
            .build();
    FreeAtomType waddle =
        new TypeBuilder("waddle")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("?")
            .autoComplete(true)
            .build();
    FreeAtomType snooze =
        new TypeBuilder("snooze")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "any"))
                    .build())
            .frontMark("#")
            .frontDataNode("value")
            .autoComplete(true)
            .build();
    FreeAtomType multiplier =
        new TypeBuilder("multiplier")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "any"))
                    .add("text", Helper.buildBackDataPrimitive("text"))
                    .build())
            .frontMark("x")
            .frontDataPrimitive("text")
            .frontDataNode("value")
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
    FreeAtomType doubleArray =
        new TypeBuilder("doublearray")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataArray("first", "any"))
                    .add("second", Helper.buildBackDataArray("second", "any"))
                    .build())
            .frontMark("[")
            .frontDataArray("first")
            .frontMark("?")
            .frontDataArray("second")
            .frontMark("]")
            .build();
    FreeAtomType record =
        new TypeBuilder("record")
            .back(Helper.buildBackDataRecord("value", "record_element"))
            .frontMark("{")
            .frontDataArray("value")
            .frontMark("}")
            .autoComplete(true)
            .build();
    FreeAtomType recordElement =
        new TypeBuilder("record_element")
            .back(Helper.buildBackDataKey("key"))
            .back(Helper.buildBackDataAtom("value", "any"))
            .frontDataPrimitive("key")
            .frontMark(": ")
            .frontDataNode("value")
            .autoComplete(true)
            .build();
    FreeAtomType pair =
        new TypeBuilder("pair")
            .back(
                new BackArrayBuilder()
                    .add(Helper.buildBackDataAtom("first", "any"))
                    .add(Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontMark("<")
            .frontDataNode("first")
            .frontMark(", ")
            .frontDataNode("second")
            .frontMark(">")
            .autoComplete(true)
            .build();
    FreeAtomType ratio =
        new TypeBuilder("ratio")
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
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "restricted_group"))
                    .build())
            .frontDataNode("value")
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
    final Atom gap = Helper.createGap(syntax);
    final FieldPrimitive primitive = (FieldPrimitive) gap.fields.getOpt("gap");
    new GeneralTestWizard(syntax, gap)
        .change(new ChangePrimitive(primitive, 0, primitive.length(), "\""))
        .checkArrayTree(gap)
        .run(editor -> primitive.selectInto(editor.context))
        .checkArrayTree(new TreeBuilder(quoted).add("value", "").build())
        .run(
            editor ->
                assertThat(
                    editor.context.cursor, Matchers.not(Matchers.instanceOf(EditGapCursor.class))));
  }

  // ========================================================================
  // ========================================================================
  // Unit gap
  @Test
  public void unitGap_unitContinueInside() {
    // After completing gap, next primitive is selected automatically if primitive is first editable
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(true)
            .build();
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
    FreeAtomType seven =
        new TypeBuilder("seven")
            .back(Helper.buildBackPrimitive("7"))
            .front(new FrontMarkBuilder("7").build())
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
    FreeAtomType quoted =
        new TypeBuilder("quoted")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("value")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(true)
            .build();
    FreeAtomType digits =
        new TypeBuilder("digits")
            .back(Helper.buildBackDataPrimitiveDigits("value"))
            .frontDataPrimitive("value")
            .autoComplete(true)
            .build();
    FreeAtomType doubleQuoted =
        new TypeBuilder("doubleuoted")
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
            .autoComplete(false)
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
            .autoComplete(false)
            .build();
    FreeAtomType plusEqual =
        new TypeBuilder("plusequal")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("+=")
            .frontDataNode("second")
            .autoComplete(false)
            .build();
    FreeAtomType binaryBang =
        new TypeBuilder("bang")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("!")
            .frontDataNode("second")
            .autoComplete(true)
            .build();
    FreeAtomType waddle =
        new TypeBuilder("waddle")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("?")
            .autoComplete(true)
            .build();
    FreeAtomType snooze =
        new TypeBuilder("snooze")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "any"))
                    .build())
            .frontMark("#")
            .frontDataNode("value")
            .autoComplete(true)
            .build();
    FreeAtomType multiplier =
        new TypeBuilder("multiplier")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "any"))
                    .add("text", Helper.buildBackDataPrimitive("text"))
                    .build())
            .frontMark("x")
            .frontDataPrimitive("text")
            .frontDataNode("value")
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
    FreeAtomType doubleArray =
        new TypeBuilder("doublearray")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataArray("first", "any"))
                    .add("second", Helper.buildBackDataArray("second", "any"))
                    .build())
            .frontMark("[")
            .frontDataArray("first")
            .frontMark("?")
            .frontDataArray("second")
            .frontMark("]")
            .build();
    FreeAtomType record =
        new TypeBuilder("record")
            .back(Helper.buildBackDataRecord("value", "record_element"))
            .frontMark("{")
            .frontDataArray("value")
            .frontMark("}")
            .autoComplete(true)
            .build();
    FreeAtomType recordElement =
        new TypeBuilder("record_element")
            .back(Helper.buildBackDataKey("key"))
            .back(Helper.buildBackDataAtom("value", "any"))
            .frontDataPrimitive("key")
            .frontMark(": ")
            .frontDataNode("value")
            .autoComplete(true)
            .build();
    FreeAtomType pair =
        new TypeBuilder("pair")
            .back(
                new BackArrayBuilder()
                    .add(Helper.buildBackDataAtom("first", "any"))
                    .add(Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontMark("<")
            .frontDataNode("first")
            .frontMark(", ")
            .frontDataNode("second")
            .frontMark(">")
            .autoComplete(true)
            .build();
    FreeAtomType ratio =
        new TypeBuilder("ratio")
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
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "restricted_group"))
                    .build())
            .frontDataNode("value")
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

    final Editor editor = blank(syntax);
    editor.context.cursor.handleTyping(editor.context, "\"");
    editor.context.cursor.handleTyping(editor.context, "e");
    assertTreeEqual(
        editor.context,
        new TreeBuilder(quoted).add("value", "e").build(),
        Helper.rootArray(editor.context.document));
  }

  @Test
  public void unitGap_unitContinueInsideArray() {
    // After completing gap, array is selected (creating element)
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(true)
            .build();
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
    FreeAtomType seven =
        new TypeBuilder("seven")
            .back(Helper.buildBackPrimitive("7"))
            .front(new FrontMarkBuilder("7").build())
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
    FreeAtomType quoted =
        new TypeBuilder("quoted")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("value")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(true)
            .build();
    FreeAtomType digits =
        new TypeBuilder("digits")
            .back(Helper.buildBackDataPrimitiveDigits("value"))
            .frontDataPrimitive("value")
            .autoComplete(true)
            .build();
    FreeAtomType doubleQuoted =
        new TypeBuilder("doubleuoted")
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
            .autoComplete(false)
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
            .autoComplete(false)
            .build();
    FreeAtomType plusEqual =
        new TypeBuilder("plusequal")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("+=")
            .frontDataNode("second")
            .autoComplete(false)
            .build();
    FreeAtomType binaryBang =
        new TypeBuilder("bang")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("!")
            .frontDataNode("second")
            .autoComplete(true)
            .build();
    FreeAtomType waddle =
        new TypeBuilder("waddle")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("?")
            .autoComplete(true)
            .build();
    FreeAtomType snooze =
        new TypeBuilder("snooze")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "any"))
                    .build())
            .frontMark("#")
            .frontDataNode("value")
            .autoComplete(true)
            .build();
    FreeAtomType multiplier =
        new TypeBuilder("multiplier")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "any"))
                    .add("text", Helper.buildBackDataPrimitive("text"))
                    .build())
            .frontMark("x")
            .frontDataPrimitive("text")
            .frontDataNode("value")
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
    FreeAtomType doubleArray =
        new TypeBuilder("doublearray")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataArray("first", "any"))
                    .add("second", Helper.buildBackDataArray("second", "any"))
                    .build())
            .frontMark("[")
            .frontDataArray("first")
            .frontMark("?")
            .frontDataArray("second")
            .frontMark("]")
            .build();
    FreeAtomType record =
        new TypeBuilder("record")
            .back(Helper.buildBackDataRecord("value", "record_element"))
            .frontMark("{")
            .frontDataArray("value")
            .frontMark("}")
            .autoComplete(true)
            .build();
    FreeAtomType recordElement =
        new TypeBuilder("record_element")
            .back(Helper.buildBackDataKey("key"))
            .back(Helper.buildBackDataAtom("value", "any"))
            .frontDataPrimitive("key")
            .frontMark(": ")
            .frontDataNode("value")
            .autoComplete(true)
            .build();
    FreeAtomType pair =
        new TypeBuilder("pair")
            .back(
                new BackArrayBuilder()
                    .add(Helper.buildBackDataAtom("first", "any"))
                    .add(Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontMark("<")
            .frontDataNode("first")
            .frontMark(", ")
            .frontDataNode("second")
            .frontMark(">")
            .autoComplete(true)
            .build();
    FreeAtomType ratio =
        new TypeBuilder("ratio")
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
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "restricted_group"))
                    .build())
            .frontDataNode("value")
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

    final Editor editor = blank(syntax);
    editor.context.cursor.handleTyping(editor.context, "[");
    editor.context.cursor.handleTyping(editor.context, "e");
    assertTreeEqual(
        editor.context,
        new TreeBuilder(array)
            .addArray("value", new TreeBuilder(syntax.gap).add("gap", "e").build())
            .build(),
        Helper.rootArray(editor.context.document));
  }

  @Test
  public void suffix_suffixContinueInside() {
    // In suffix gap if completed atom has editable after the matched key text,
    // after completeion selection automatically goes into that editable (gap in this case)
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(true)
            .build();
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
    FreeAtomType seven =
        new TypeBuilder("seven")
            .back(Helper.buildBackPrimitive("7"))
            .front(new FrontMarkBuilder("7").build())
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
    FreeAtomType quoted =
        new TypeBuilder("quoted")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("value")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(true)
            .build();
    FreeAtomType digits =
        new TypeBuilder("digits")
            .back(Helper.buildBackDataPrimitiveDigits("value"))
            .frontDataPrimitive("value")
            .autoComplete(true)
            .build();
    FreeAtomType doubleQuoted =
        new TypeBuilder("doubleuoted")
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
            .autoComplete(false)
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
            .autoComplete(false)
            .build();
    FreeAtomType plusEqual =
        new TypeBuilder("plusequal")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("+=")
            .frontDataNode("second")
            .autoComplete(false)
            .build();
    FreeAtomType binaryBang =
        new TypeBuilder("bang")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("!")
            .frontDataNode("second")
            .autoComplete(true)
            .build();
    FreeAtomType waddle =
        new TypeBuilder("waddle")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("?")
            .autoComplete(true)
            .build();
    FreeAtomType snooze =
        new TypeBuilder("snooze")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "any"))
                    .build())
            .frontMark("#")
            .frontDataNode("value")
            .autoComplete(true)
            .build();
    FreeAtomType multiplier =
        new TypeBuilder("multiplier")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "any"))
                    .add("text", Helper.buildBackDataPrimitive("text"))
                    .build())
            .frontMark("x")
            .frontDataPrimitive("text")
            .frontDataNode("value")
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
    FreeAtomType doubleArray =
        new TypeBuilder("doublearray")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataArray("first", "any"))
                    .add("second", Helper.buildBackDataArray("second", "any"))
                    .build())
            .frontMark("[")
            .frontDataArray("first")
            .frontMark("?")
            .frontDataArray("second")
            .frontMark("]")
            .build();
    FreeAtomType record =
        new TypeBuilder("record")
            .back(Helper.buildBackDataRecord("value", "record_element"))
            .frontMark("{")
            .frontDataArray("value")
            .frontMark("}")
            .autoComplete(true)
            .build();
    FreeAtomType recordElement =
        new TypeBuilder("record_element")
            .back(Helper.buildBackDataKey("key"))
            .back(Helper.buildBackDataAtom("value", "any"))
            .frontDataPrimitive("key")
            .frontMark(": ")
            .frontDataNode("value")
            .autoComplete(true)
            .build();
    FreeAtomType pair =
        new TypeBuilder("pair")
            .back(
                new BackArrayBuilder()
                    .add(Helper.buildBackDataAtom("first", "any"))
                    .add(Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontMark("<")
            .frontDataNode("first")
            .frontMark(", ")
            .frontDataNode("second")
            .frontMark(">")
            .autoComplete(true)
            .build();
    FreeAtomType ratio =
        new TypeBuilder("ratio")
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
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "restricted_group"))
                    .build())
            .frontDataNode("value")
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
        () -> Helper.createSuffixGap(syntax, new TreeBuilder(one).build()),
        context -> {
          Helper.rootArray(context.document).data.get(0).fields.getOpt("gap").selectInto(context);
          context.cursor.handleTyping(context, "?");
          context.cursor.handleTyping(context, "e");
        },
        new TreeBuilder(syntax.suffixGap)
            .addArray(
                "value",
                TSList.of(new TreeBuilder(waddle).add("first", new TreeBuilder(one)).build()))
            .add("gap", "e")
            .build());
  }

  private void innerTestTransform(
      final Syntax syntax,
      final Supplier<Atom> start,
      final Consumer<Context> transform,
      final Atom end) {
    final Editor editor = buildDoc(syntax, start.get());
    transform.accept(editor.context);
    assertTreeEqual(editor.context, end, Helper.rootArray(editor.context.document));
    editor.history.undo(editor.context);
    assertTreeEqual(editor.context, start.get(), Helper.rootArray(editor.context.document));
    editor.history.redo(editor.context);
    assertTreeEqual(editor.context, end, Helper.rootArray(editor.context.document));
  }

  @Test
  public void suffix_textMark3Immediate() {
    // match text then mark as soon as it's unambiguous
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
            .back(
                new BackRecordBuilder()
                    .add("text", Helper.buildBackDataPrimitiveLetters("text"))
                    .add("atom", Helper.buildBackDataAtom("atom", "any"))
                    .build())
            .frontDataPrimitive("text")
            .frontMark("$1")
            .frontDataNode("atom")
            .autoComplete(true)
            .build();
    FreeAtomType textMark2 =
        new TypeBuilder("textmark2")
            .back(
                new BackRecordBuilder()
                    .add("text", Helper.buildBackDataPrimitiveLetters("text"))
                    .add("atom", Helper.buildBackDataAtom("atom", "any"))
                    .add("atom2", Helper.buildBackDataAtom("atom2", "any"))
                    .build())
            .frontDataPrimitive("text")
            .frontMark("$2")
            .frontDataNode("atom")
            .frontDataNode("atom2")
            .autoComplete(true)
            .build();
    FreeAtomType textMark3 =
        new TypeBuilder("textmark3")
            .back(
                new BackRecordBuilder()
                    .add("text", Helper.buildBackDataPrimitiveLetters("text"))
                    .add("atom", Helper.buildBackDataAtom("atom", "any"))
                    .add("atom2", Helper.buildBackDataAtom("atom2", "any"))
                    .build())
            .frontDataNode("atom2")
            .frontDataPrimitive("text")
            .frontMark("$3")
            .frontDataNode("atom")
            .autoComplete(true)
            .build();
    FreeAtomType ambiguateTextMark3 =
        new TypeBuilder("atextmark3")
            .back(
                new BackRecordBuilder()
                    .add("atom", Helper.buildBackDataAtom("atom", "any"))
                    .build())
            .frontDataNode("atom")
            .frontMark("t")
            .autoComplete(true)
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

    final Atom atom = Helper.createSuffixGap(syntax, new TreeBuilder(one).build());
    new GeneralTestWizard(syntax, atom)
        .run(editor -> atom.fields.getOpt("gap").selectInto(editor.context))
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
    FreeAtomType one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .autoComplete(false)
            .build();
    FreeAtomType quoted =
        new TypeBuilder("quoted")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("value")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(true)
            .build();
    FreeAtomType binaryBang =
        new TypeBuilder("bang")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "one"))
                    .add("second", Helper.buildBackDataAtom("second", "one"))
                    .build())
            .frontDataNode("first")
            .frontMark("!")
            .frontDataNode("second")
            .autoComplete(true)
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(quoted)
            .type(one)
            .type(binaryBang)
            .group("restricted_group", new GroupBuilder().type(quoted).build())
            .group("any", new GroupBuilder().type(quoted).type(binaryBang).build())
            .build();

    final Atom gap =
        Helper.createSuffixGap(syntax, new TreeBuilder(quoted).add("value", "hi").build());
    new GeneralTestWizard(syntax, gap)
        .run(editor -> gap.fields.getOpt("gap").selectInto(editor.context))
        .sendText("!")
        .checkArrayTree(
            new TreeBuilder(syntax.suffixGap)
                .addArray("value", new TreeBuilder(quoted).add("value", "hi").build())
                .add("gap", "!")
                .build());
  }

  // ========================================================================
  // ========================================================================
  // Suffix raising

  @Test
  public void suffixRaising_testRaisePrecedenceLower() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(true)
            .build();
    FreeAtomType factorial =
        new TypeBuilder("factorial")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "any"))
                    .build())
            .frontDataNode("value")
            .frontMark("!")
            .autoComplete(true)
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
            .autoComplete(true)
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
            .autoComplete(true)
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
            .autoComplete(true)
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
            .autoComplete(true)
            .build();
    FreeAtomType subscript =
        new TypeBuilder("subscript")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("_")
            .frontDataNode("second")
            .precedence(0)
            .autoComplete(true)
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
            .autoComplete(true)
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

    innerTestTransform(
        syntax,
        () ->
            new TreeBuilder(plus)
                .add("first", new TreeBuilder(infinity))
                .add("second", Helper.createSuffixGap(syntax, new TreeBuilder(infinity).build()))
                .build(),
        context -> {
          ((FieldPrimitive)
                  context.syntaxLocate(new SyntaxPath("value", "0", "second", "atom", "gap")))
              .selectInto(context);
          context.cursor.handleTyping(context, "*");
        },
        new TreeBuilder(plus)
            .add("first", new TreeBuilder(infinity))
            .add(
                "second",
                new TreeBuilder(multiply)
                    .add("first", new TreeBuilder(infinity))
                    .add("second", Helper.createGap(syntax)))
            .build());
  }

  @Test
  public void suffixRaising_testRaisePrecedenceEqualAfter() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(true)
            .build();
    FreeAtomType factorial =
        new TypeBuilder("factorial")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "any"))
                    .build())
            .frontDataNode("value")
            .frontMark("!")
            .autoComplete(true)
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
            .autoComplete(true)
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
            .autoComplete(true)
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
            .autoComplete(true)
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
            .autoComplete(true)
            .build();
    FreeAtomType subscript =
        new TypeBuilder("subscript")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("_")
            .frontDataNode("second")
            .precedence(0)
            .autoComplete(true)
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
            .autoComplete(true)
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

    innerTestTransform(
        syntax,
        () ->
            new TreeBuilder(plus)
                .add("first", new TreeBuilder(infinity))
                .add("second", Helper.createSuffixGap(syntax, new TreeBuilder(infinity).build()))
                .build(),
        context -> {
          ((FieldPrimitive)
                  context.syntaxLocate(new SyntaxPath("value", "0", "second", "atom", "gap")))
              .selectInto(context);
          context.cursor.handleTyping(context, "+");
        },
        new TreeBuilder(plus)
            .add("first", new TreeBuilder(infinity))
            .add(
                "second",
                new TreeBuilder(plus)
                    .add("first", new TreeBuilder(infinity))
                    .add("second", Helper.createGap(syntax)))
            .build());
  }

  @Test
  public void suffixRaising_testRaisePrecedenceEqualBefore() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(true)
            .build();
    FreeAtomType factorial =
        new TypeBuilder("factorial")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "any"))
                    .build())
            .frontDataNode("value")
            .frontMark("!")
            .autoComplete(true)
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
            .autoComplete(true)
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
            .autoComplete(true)
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
            .autoComplete(true)
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
            .autoComplete(true)
            .build();
    FreeAtomType subscript =
        new TypeBuilder("subscript")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("_")
            .frontDataNode("second")
            .precedence(0)
            .autoComplete(true)
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
            .autoComplete(true)
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

    innerTestTransform(
        syntax,
        () ->
            new TreeBuilder(minus)
                .add("first", new TreeBuilder(infinity))
                .add("second", Helper.createSuffixGap(syntax, new TreeBuilder(infinity).build()))
                .build(),
        context -> {
          ((FieldPrimitive)
                  context.syntaxLocate(new SyntaxPath("value", "0", "second", "atom", "gap")))
              .selectInto(context);
          context.cursor.handleTyping(context, "-");
        },
        new TreeBuilder(minus)
            .add(
                "first",
                new TreeBuilder(minus)
                    .add("first", new TreeBuilder(infinity))
                    .add("second", new TreeBuilder(infinity)))
            .add("second", Helper.createGap(syntax))
            .build());
  }

  @Test
  public void suffixRaising_testRaisePrecedenceGreater() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(true)
            .build();
    FreeAtomType factorial =
        new TypeBuilder("factorial")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "any"))
                    .build())
            .frontDataNode("value")
            .frontMark("!")
            .autoComplete(true)
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
            .autoComplete(true)
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
            .autoComplete(true)
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
            .autoComplete(true)
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
            .autoComplete(true)
            .build();
    FreeAtomType subscript =
        new TypeBuilder("subscript")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("_")
            .frontDataNode("second")
            .precedence(0)
            .autoComplete(true)
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
            .autoComplete(true)
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

    innerTestTransform(
        syntax,
        () ->
            new TreeBuilder(multiply)
                .add("first", new TreeBuilder(infinity))
                .add("second", Helper.createSuffixGap(syntax, new TreeBuilder(infinity).build()))
                .build(),
        context -> {
          ((FieldPrimitive)
                  context.syntaxLocate(new SyntaxPath("value", "0", "second", "atom", "gap")))
              .selectInto(context);
          context.cursor.handleTyping(context, "+");
        },
        new TreeBuilder(plus)
            .add(
                "first",
                new TreeBuilder(multiply)
                    .add("first", new TreeBuilder(infinity))
                    .add("second", new TreeBuilder(infinity)))
            .add("second", Helper.createGap(syntax))
            .build());
  }

  @Test
  public void suffixRaising_testRaiseSkipDissimilar() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(true)
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
            .autoComplete(true)
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
            .autoComplete(true)
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(infinity)
            .type(plus)
            .type(subscript)
            .group("name", new GroupBuilder().type(infinity).type(subscript).build())
            .group("any", new GroupBuilder().type(plus).group("name").build())
            .build();

    innerTestTransform(
        syntax,
        () ->
            new TreeBuilder(subscript)
                .add("first", new TreeBuilder(infinity))
                .add("second", Helper.createSuffixGap(syntax, new TreeBuilder(infinity).build()))
                .build(),
        context -> {
          ((FieldPrimitive)
                  context.syntaxLocate(new SyntaxPath("value", "0", "second", "atom", "gap")))
              .selectInto(context);
          context.cursor.handleTyping(context, "+");
        },
        new TreeBuilder(plus)
            .add(
                "first",
                new TreeBuilder(subscript)
                    .add("first", new TreeBuilder(infinity))
                    .add("second", new TreeBuilder(infinity)))
            .add("second", Helper.createGap(syntax))
            .build());
  }

  @Test
  public void suffixRaising_testRaiseBounded() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(true)
            .build();
    FreeAtomType factorial =
        new TypeBuilder("factorial")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "any"))
                    .build())
            .frontDataNode("value")
            .frontMark("!")
            .autoComplete(true)
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
            .autoComplete(true)
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
            .autoComplete(true)
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
            .autoComplete(true)
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
            .autoComplete(true)
            .build();
    FreeAtomType subscript =
        new TypeBuilder("subscript")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("_")
            .frontDataNode("second")
            .precedence(0)
            .autoComplete(true)
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
            .autoComplete(true)
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

    innerTestTransform(
        syntax,
        () ->
            new TreeBuilder(inclusiveRange)
                .add("first", new TreeBuilder(infinity))
                .add("second", Helper.createSuffixGap(syntax, new TreeBuilder(infinity).build()))
                .build(),
        context -> {
          ((FieldPrimitive)
                  context.syntaxLocate(new SyntaxPath("value", "0", "second", "atom", "gap")))
              .selectInto(context);
          context.cursor.handleTyping(context, "+");
        },
        new TreeBuilder(inclusiveRange)
            .add("first", new TreeBuilder(infinity))
            .add(
                "second",
                new TreeBuilder(plus)
                    .add("first", new TreeBuilder(infinity))
                    .add("second", Helper.createGap(syntax)))
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
            .autoComplete(true)
            .build();
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
    FreeAtomType seven =
        new TypeBuilder("seven")
            .back(Helper.buildBackPrimitive("7"))
            .front(new FrontMarkBuilder("7").build())
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
    FreeAtomType quoted =
        new TypeBuilder("quoted")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("value")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(true)
            .build();
    FreeAtomType digits =
        new TypeBuilder("digits")
            .back(Helper.buildBackDataPrimitiveDigits("value"))
            .frontDataPrimitive("value")
            .autoComplete(true)
            .build();
    FreeAtomType doubleQuoted =
        new TypeBuilder("doubleuoted")
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
            .autoComplete(false)
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
            .autoComplete(false)
            .build();
    FreeAtomType plusEqual =
        new TypeBuilder("plusequal")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("+=")
            .frontDataNode("second")
            .autoComplete(false)
            .build();
    FreeAtomType binaryBang =
        new TypeBuilder("bang")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("!")
            .frontDataNode("second")
            .autoComplete(true)
            .build();
    FreeAtomType waddle =
        new TypeBuilder("waddle")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("?")
            .autoComplete(true)
            .build();
    FreeAtomType snooze =
        new TypeBuilder("snooze")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "any"))
                    .build())
            .frontMark("#")
            .frontDataNode("value")
            .autoComplete(true)
            .build();
    FreeAtomType multiplier =
        new TypeBuilder("multiplier")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "any"))
                    .add("text", Helper.buildBackDataPrimitive("text"))
                    .build())
            .frontMark("x")
            .frontDataPrimitive("text")
            .frontDataNode("value")
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
    FreeAtomType doubleArray =
        new TypeBuilder("doublearray")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataArray("first", "any"))
                    .add("second", Helper.buildBackDataArray("second", "any"))
                    .build())
            .frontMark("[")
            .frontDataArray("first")
            .frontMark("?")
            .frontDataArray("second")
            .frontMark("]")
            .build();
    FreeAtomType record =
        new TypeBuilder("record")
            .back(Helper.buildBackDataRecord("value", "record_element"))
            .frontMark("{")
            .frontDataArray("value")
            .frontMark("}")
            .autoComplete(true)
            .build();
    FreeAtomType recordElement =
        new TypeBuilder("record_element")
            .back(Helper.buildBackDataKey("key"))
            .back(Helper.buildBackDataAtom("value", "any"))
            .frontDataPrimitive("key")
            .frontMark(": ")
            .frontDataNode("value")
            .autoComplete(true)
            .build();
    FreeAtomType pair =
        new TypeBuilder("pair")
            .back(
                new BackArrayBuilder()
                    .add(Helper.buildBackDataAtom("first", "any"))
                    .add(Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontMark("<")
            .frontDataNode("first")
            .frontMark(", ")
            .frontDataNode("second")
            .frontMark(">")
            .autoComplete(true)
            .build();
    FreeAtomType ratio =
        new TypeBuilder("ratio")
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
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "restricted_group"))
                    .build())
            .frontDataNode("value")
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
        () -> new TreeBuilder(array).addArray("value", Helper.createGap(syntax)).build(),
        context -> {
          ((FieldPrimitive) context.syntaxLocate(new SyntaxPath("value", "0", "value", "0", "gap")))
              .selectInto(context);
          ((Atom) context.syntaxLocate(new SyntaxPath("value", "0", "value", "0")))
              .valueParentRef.selectValue(context);
        },
        new TreeBuilder(array).addArray("value").build());
  }

  @Test
  public void deselectionRemoval_testDontDropNodeGap() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(true)
            .build();
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
    FreeAtomType seven =
        new TypeBuilder("seven")
            .back(Helper.buildBackPrimitive("7"))
            .front(new FrontMarkBuilder("7").build())
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
    FreeAtomType quoted =
        new TypeBuilder("quoted")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("value")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(true)
            .build();
    FreeAtomType digits =
        new TypeBuilder("digits")
            .back(Helper.buildBackDataPrimitiveDigits("value"))
            .frontDataPrimitive("value")
            .autoComplete(true)
            .build();
    FreeAtomType doubleQuoted =
        new TypeBuilder("doubleuoted")
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
            .autoComplete(false)
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
            .autoComplete(false)
            .build();
    FreeAtomType plusEqual =
        new TypeBuilder("plusequal")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("+=")
            .frontDataNode("second")
            .autoComplete(false)
            .build();
    FreeAtomType binaryBang =
        new TypeBuilder("bang")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("!")
            .frontDataNode("second")
            .autoComplete(true)
            .build();
    FreeAtomType waddle =
        new TypeBuilder("waddle")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("?")
            .autoComplete(true)
            .build();
    FreeAtomType snooze =
        new TypeBuilder("snooze")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "any"))
                    .build())
            .frontMark("#")
            .frontDataNode("value")
            .autoComplete(true)
            .build();
    FreeAtomType multiplier =
        new TypeBuilder("multiplier")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "any"))
                    .add("text", Helper.buildBackDataPrimitive("text"))
                    .build())
            .frontMark("x")
            .frontDataPrimitive("text")
            .frontDataNode("value")
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
    FreeAtomType doubleArray =
        new TypeBuilder("doublearray")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataArray("first", "any"))
                    .add("second", Helper.buildBackDataArray("second", "any"))
                    .build())
            .frontMark("[")
            .frontDataArray("first")
            .frontMark("?")
            .frontDataArray("second")
            .frontMark("]")
            .build();
    FreeAtomType record =
        new TypeBuilder("record")
            .back(Helper.buildBackDataRecord("value", "record_element"))
            .frontMark("{")
            .frontDataArray("value")
            .frontMark("}")
            .autoComplete(true)
            .build();
    FreeAtomType recordElement =
        new TypeBuilder("record_element")
            .back(Helper.buildBackDataKey("key"))
            .back(Helper.buildBackDataAtom("value", "any"))
            .frontDataPrimitive("key")
            .frontMark(": ")
            .frontDataNode("value")
            .autoComplete(true)
            .build();
    FreeAtomType pair =
        new TypeBuilder("pair")
            .back(
                new BackArrayBuilder()
                    .add(Helper.buildBackDataAtom("first", "any"))
                    .add(Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontMark("<")
            .frontDataNode("first")
            .frontMark(", ")
            .frontDataNode("second")
            .frontMark(">")
            .autoComplete(true)
            .build();
    FreeAtomType ratio =
        new TypeBuilder("ratio")
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
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "restricted_group"))
                    .build())
            .frontDataNode("value")
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
        () -> new TreeBuilder(array).addArray("value", Helper.createGap(syntax)).build(),
        context -> {
          ((FieldPrimitive) context.syntaxLocate(new SyntaxPath("value", "0", "value", "0", "gap")))
              .selectInto(context);
          context.cursor.handleTyping(context, "urt");
          ((FieldArray) context.syntaxLocate(new SyntaxPath("value", "0", "value")))
              .visual.selectAnyChild(context);
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
            .autoComplete(true)
            .build();
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
    FreeAtomType seven =
        new TypeBuilder("seven")
            .back(Helper.buildBackPrimitive("7"))
            .front(new FrontMarkBuilder("7").build())
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
    FreeAtomType quoted =
        new TypeBuilder("quoted")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("value")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(true)
            .build();
    FreeAtomType digits =
        new TypeBuilder("digits")
            .back(Helper.buildBackDataPrimitiveDigits("value"))
            .frontDataPrimitive("value")
            .autoComplete(true)
            .build();
    FreeAtomType doubleQuoted =
        new TypeBuilder("doubleuoted")
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
            .autoComplete(false)
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
            .autoComplete(false)
            .build();
    FreeAtomType plusEqual =
        new TypeBuilder("plusequal")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("+=")
            .frontDataNode("second")
            .autoComplete(false)
            .build();
    FreeAtomType binaryBang =
        new TypeBuilder("bang")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("!")
            .frontDataNode("second")
            .autoComplete(true)
            .build();
    FreeAtomType waddle =
        new TypeBuilder("waddle")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("?")
            .autoComplete(true)
            .build();
    FreeAtomType snooze =
        new TypeBuilder("snooze")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "any"))
                    .build())
            .frontMark("#")
            .frontDataNode("value")
            .autoComplete(true)
            .build();
    FreeAtomType multiplier =
        new TypeBuilder("multiplier")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "any"))
                    .add("text", Helper.buildBackDataPrimitive("text"))
                    .build())
            .frontMark("x")
            .frontDataPrimitive("text")
            .frontDataNode("value")
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
    FreeAtomType doubleArray =
        new TypeBuilder("doublearray")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataArray("first", "any"))
                    .add("second", Helper.buildBackDataArray("second", "any"))
                    .build())
            .frontMark("[")
            .frontDataArray("first")
            .frontMark("?")
            .frontDataArray("second")
            .frontMark("]")
            .build();
    FreeAtomType record =
        new TypeBuilder("record")
            .back(Helper.buildBackDataRecord("value", "record_element"))
            .frontMark("{")
            .frontDataArray("value")
            .frontMark("}")
            .autoComplete(true)
            .build();
    FreeAtomType recordElement =
        new TypeBuilder("record_element")
            .back(Helper.buildBackDataKey("key"))
            .back(Helper.buildBackDataAtom("value", "any"))
            .frontDataPrimitive("key")
            .frontMark(": ")
            .frontDataNode("value")
            .autoComplete(true)
            .build();
    FreeAtomType pair =
        new TypeBuilder("pair")
            .back(
                new BackArrayBuilder()
                    .add(Helper.buildBackDataAtom("first", "any"))
                    .add(Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontMark("<")
            .frontDataNode("first")
            .frontMark(", ")
            .frontDataNode("second")
            .frontMark(">")
            .autoComplete(true)
            .build();
    FreeAtomType ratio =
        new TypeBuilder("ratio")
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
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "restricted_group"))
                    .build())
            .frontDataNode("value")
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

    final Editor editor = buildDoc(syntax, Helper.createGap(syntax), Helper.createGap(syntax));
    ((Atom) editor.context.syntaxLocate(new SyntaxPath("value", "0")))
        .visual.selectAnyChild(editor.context);
    ((Atom) editor.context.syntaxLocate(new SyntaxPath("value", "1")))
        .visual.selectAnyChild(editor.context);
    assertThat(Helper.rootArray(editor.context.document).data.size(), equalTo(1));
    assertTreeEqual(
        editor.context, Helper.createGap(syntax), Helper.rootArray(editor.context.document));
    assertThat(
        editor.context.cursor.getSyntaxPath(), equalTo(new SyntaxPath("value", "0", "gap", "0")));
    editor.history.undo(editor.context);
    assertThat(Helper.rootArray(editor.context.document).data.size(), equalTo(2));
    assertTreeEqual(
        Helper.createGap(syntax), Helper.rootArray(editor.context.document).data.get(0));
    assertThat(
        editor.context.cursor.getSyntaxPath(), equalTo(new SyntaxPath("value", "1", "gap", "0")));
    editor.history.redo(editor.context);
    assertThat(Helper.rootArray(editor.context.document).data.size(), equalTo(1));
    assertTreeEqual(
        Helper.createGap(syntax), Helper.rootArray(editor.context.document).data.get(0));
    assertThat(
        editor.context.cursor.getSyntaxPath(), equalTo(new SyntaxPath("value", "0", "gap", "0")));
  }

  @Test
  public void deselectionRemoval_testDropSuffixValue() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(true)
            .build();
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
    FreeAtomType seven =
        new TypeBuilder("seven")
            .back(Helper.buildBackPrimitive("7"))
            .front(new FrontMarkBuilder("7").build())
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
    FreeAtomType quoted =
        new TypeBuilder("quoted")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("value")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(true)
            .build();
    FreeAtomType digits =
        new TypeBuilder("digits")
            .back(Helper.buildBackDataPrimitiveDigits("value"))
            .frontDataPrimitive("value")
            .autoComplete(true)
            .build();
    FreeAtomType doubleQuoted =
        new TypeBuilder("doubleuoted")
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
            .autoComplete(false)
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
            .autoComplete(false)
            .build();
    FreeAtomType plusEqual =
        new TypeBuilder("plusequal")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("+=")
            .frontDataNode("second")
            .autoComplete(false)
            .build();
    FreeAtomType binaryBang =
        new TypeBuilder("bang")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("!")
            .frontDataNode("second")
            .autoComplete(true)
            .build();
    FreeAtomType waddle =
        new TypeBuilder("waddle")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("?")
            .autoComplete(true)
            .build();
    FreeAtomType snooze =
        new TypeBuilder("snooze")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "any"))
                    .build())
            .frontMark("#")
            .frontDataNode("value")
            .autoComplete(true)
            .build();
    FreeAtomType multiplier =
        new TypeBuilder("multiplier")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "any"))
                    .add("text", Helper.buildBackDataPrimitive("text"))
                    .build())
            .frontMark("x")
            .frontDataPrimitive("text")
            .frontDataNode("value")
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
    FreeAtomType doubleArray =
        new TypeBuilder("doublearray")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataArray("first", "any"))
                    .add("second", Helper.buildBackDataArray("second", "any"))
                    .build())
            .frontMark("[")
            .frontDataArray("first")
            .frontMark("?")
            .frontDataArray("second")
            .frontMark("]")
            .build();
    FreeAtomType record =
        new TypeBuilder("record")
            .back(Helper.buildBackDataRecord("value", "record_element"))
            .frontMark("{")
            .frontDataArray("value")
            .frontMark("}")
            .autoComplete(true)
            .build();
    FreeAtomType recordElement =
        new TypeBuilder("record_element")
            .back(Helper.buildBackDataKey("key"))
            .back(Helper.buildBackDataAtom("value", "any"))
            .frontDataPrimitive("key")
            .frontMark(": ")
            .frontDataNode("value")
            .autoComplete(true)
            .build();
    FreeAtomType pair =
        new TypeBuilder("pair")
            .back(
                new BackArrayBuilder()
                    .add(Helper.buildBackDataAtom("first", "any"))
                    .add(Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontMark("<")
            .frontDataNode("first")
            .frontMark(", ")
            .frontDataNode("second")
            .frontMark(">")
            .autoComplete(true)
            .build();
    FreeAtomType ratio =
        new TypeBuilder("ratio")
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
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "restricted_group"))
                    .build())
            .frontDataNode("value")
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
        () -> Helper.createSuffixGap(syntax, new TreeBuilder(infinity).build()),
        context -> {
          ((FieldPrimitive) context.syntaxLocate(new SyntaxPath("value", "0", "gap")))
              .selectInto(context);
          ((Atom) context.syntaxLocate(new SyntaxPath("value", "0")))
              .valueParentRef.selectValue(context);
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
            .autoComplete(true)
            .build();
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
    FreeAtomType seven =
        new TypeBuilder("seven")
            .back(Helper.buildBackPrimitive("7"))
            .front(new FrontMarkBuilder("7").build())
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
    FreeAtomType quoted =
        new TypeBuilder("quoted")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("value")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(true)
            .build();
    FreeAtomType digits =
        new TypeBuilder("digits")
            .back(Helper.buildBackDataPrimitiveDigits("value"))
            .frontDataPrimitive("value")
            .autoComplete(true)
            .build();
    FreeAtomType doubleQuoted =
        new TypeBuilder("doubleuoted")
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
            .autoComplete(false)
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
            .autoComplete(false)
            .build();
    FreeAtomType plusEqual =
        new TypeBuilder("plusequal")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("+=")
            .frontDataNode("second")
            .autoComplete(false)
            .build();
    FreeAtomType binaryBang =
        new TypeBuilder("bang")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("!")
            .frontDataNode("second")
            .autoComplete(true)
            .build();
    FreeAtomType waddle =
        new TypeBuilder("waddle")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("?")
            .autoComplete(true)
            .build();
    FreeAtomType snooze =
        new TypeBuilder("snooze")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "any"))
                    .build())
            .frontMark("#")
            .frontDataNode("value")
            .autoComplete(true)
            .build();
    FreeAtomType multiplier =
        new TypeBuilder("multiplier")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "any"))
                    .add("text", Helper.buildBackDataPrimitive("text"))
                    .build())
            .frontMark("x")
            .frontDataPrimitive("text")
            .frontDataNode("value")
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
    FreeAtomType doubleArray =
        new TypeBuilder("doublearray")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataArray("first", "any"))
                    .add("second", Helper.buildBackDataArray("second", "any"))
                    .build())
            .frontMark("[")
            .frontDataArray("first")
            .frontMark("?")
            .frontDataArray("second")
            .frontMark("]")
            .build();
    FreeAtomType record =
        new TypeBuilder("record")
            .back(Helper.buildBackDataRecord("value", "record_element"))
            .frontMark("{")
            .frontDataArray("value")
            .frontMark("}")
            .autoComplete(true)
            .build();
    FreeAtomType recordElement =
        new TypeBuilder("record_element")
            .back(Helper.buildBackDataKey("key"))
            .back(Helper.buildBackDataAtom("value", "any"))
            .frontDataPrimitive("key")
            .frontMark(": ")
            .frontDataNode("value")
            .autoComplete(true)
            .build();
    FreeAtomType pair =
        new TypeBuilder("pair")
            .back(
                new BackArrayBuilder()
                    .add(Helper.buildBackDataAtom("first", "any"))
                    .add(Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontMark("<")
            .frontDataNode("first")
            .frontMark(", ")
            .frontDataNode("second")
            .frontMark(">")
            .autoComplete(true)
            .build();
    FreeAtomType ratio =
        new TypeBuilder("ratio")
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
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "restricted_group"))
                    .build())
            .frontDataNode("value")
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
          ((FieldArray) context.syntaxLocate(new SyntaxPath("value", "0", "value")))
              .visual.selectAnyChild(context);
        },
        new TreeBuilder(array).addArray("value", Helper.createGap(syntax)).build());
  }

  @Test
  public void arrayGapCreation_testCreateArrayDefault() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(true)
            .build();
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
    FreeAtomType seven =
        new TypeBuilder("seven")
            .back(Helper.buildBackPrimitive("7"))
            .front(new FrontMarkBuilder("7").build())
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
    FreeAtomType quoted =
        new TypeBuilder("quoted")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("value")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(true)
            .build();
    FreeAtomType digits =
        new TypeBuilder("digits")
            .back(Helper.buildBackDataPrimitiveDigits("value"))
            .frontDataPrimitive("value")
            .autoComplete(true)
            .build();
    FreeAtomType doubleQuoted =
        new TypeBuilder("doubleuoted")
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
            .autoComplete(false)
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
            .autoComplete(false)
            .build();
    FreeAtomType plusEqual =
        new TypeBuilder("plusequal")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("+=")
            .frontDataNode("second")
            .autoComplete(false)
            .build();
    FreeAtomType binaryBang =
        new TypeBuilder("bang")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("!")
            .frontDataNode("second")
            .autoComplete(true)
            .build();
    FreeAtomType waddle =
        new TypeBuilder("waddle")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("?")
            .autoComplete(true)
            .build();
    FreeAtomType snooze =
        new TypeBuilder("snooze")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "any"))
                    .build())
            .frontMark("#")
            .frontDataNode("value")
            .autoComplete(true)
            .build();
    FreeAtomType multiplier =
        new TypeBuilder("multiplier")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "any"))
                    .add("text", Helper.buildBackDataPrimitive("text"))
                    .build())
            .frontMark("x")
            .frontDataPrimitive("text")
            .frontDataNode("value")
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
    FreeAtomType doubleArray =
        new TypeBuilder("doublearray")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataArray("first", "any"))
                    .add("second", Helper.buildBackDataArray("second", "any"))
                    .build())
            .frontMark("[")
            .frontDataArray("first")
            .frontMark("?")
            .frontDataArray("second")
            .frontMark("]")
            .build();
    FreeAtomType record =
        new TypeBuilder("record")
            .back(Helper.buildBackDataRecord("value", "record_element"))
            .frontMark("{")
            .frontDataArray("value")
            .frontMark("}")
            .autoComplete(true)
            .build();
    FreeAtomType recordElement =
        new TypeBuilder("record_element")
            .back(Helper.buildBackDataKey("key"))
            .back(Helper.buildBackDataAtom("value", "any"))
            .frontDataPrimitive("key")
            .frontMark(": ")
            .frontDataNode("value")
            .autoComplete(true)
            .build();
    FreeAtomType pair =
        new TypeBuilder("pair")
            .back(
                new BackArrayBuilder()
                    .add(Helper.buildBackDataAtom("first", "any"))
                    .add(Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontMark("<")
            .frontDataNode("first")
            .frontMark(", ")
            .frontDataNode("second")
            .frontMark(">")
            .autoComplete(true)
            .build();
    FreeAtomType ratio =
        new TypeBuilder("ratio")
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
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "restricted_group"))
                    .build())
            .frontDataNode("value")
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
          ((FieldArray) context.syntaxLocate(new SyntaxPath("value", "0", "value")))
              .visual.selectAnyChild(context);
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
            .autoComplete(true)
            .build();
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
    FreeAtomType seven =
        new TypeBuilder("seven")
            .back(Helper.buildBackPrimitive("7"))
            .front(new FrontMarkBuilder("7").build())
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
    FreeAtomType quoted =
        new TypeBuilder("quoted")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("value")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(true)
            .build();
    FreeAtomType digits =
        new TypeBuilder("digits")
            .back(Helper.buildBackDataPrimitiveDigits("value"))
            .frontDataPrimitive("value")
            .autoComplete(true)
            .build();
    FreeAtomType doubleQuoted =
        new TypeBuilder("doubleuoted")
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
            .autoComplete(false)
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
            .autoComplete(false)
            .build();
    FreeAtomType plusEqual =
        new TypeBuilder("plusequal")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("+=")
            .frontDataNode("second")
            .autoComplete(false)
            .build();
    FreeAtomType binaryBang =
        new TypeBuilder("bang")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("!")
            .frontDataNode("second")
            .autoComplete(true)
            .build();
    FreeAtomType waddle =
        new TypeBuilder("waddle")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("?")
            .autoComplete(true)
            .build();
    FreeAtomType snooze =
        new TypeBuilder("snooze")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "any"))
                    .build())
            .frontMark("#")
            .frontDataNode("value")
            .autoComplete(true)
            .build();
    FreeAtomType multiplier =
        new TypeBuilder("multiplier")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "any"))
                    .add("text", Helper.buildBackDataPrimitive("text"))
                    .build())
            .frontMark("x")
            .frontDataPrimitive("text")
            .frontDataNode("value")
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
    FreeAtomType doubleArray =
        new TypeBuilder("doublearray")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataArray("first", "any"))
                    .add("second", Helper.buildBackDataArray("second", "any"))
                    .build())
            .frontMark("[")
            .frontDataArray("first")
            .frontMark("?")
            .frontDataArray("second")
            .frontMark("]")
            .build();
    FreeAtomType record =
        new TypeBuilder("record")
            .back(Helper.buildBackDataRecord("value", "record_element"))
            .frontMark("{")
            .frontDataArray("value")
            .frontMark("}")
            .autoComplete(true)
            .build();
    FreeAtomType recordElement =
        new TypeBuilder("record_element")
            .back(Helper.buildBackDataKey("key"))
            .back(Helper.buildBackDataAtom("value", "any"))
            .frontDataPrimitive("key")
            .frontMark(": ")
            .frontDataNode("value")
            .autoComplete(true)
            .build();
    FreeAtomType pair =
        new TypeBuilder("pair")
            .back(
                new BackArrayBuilder()
                    .add(Helper.buildBackDataAtom("first", "any"))
                    .add(Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontMark("<")
            .frontDataNode("first")
            .frontMark(", ")
            .frontDataNode("second")
            .frontMark(">")
            .autoComplete(true)
            .build();
    FreeAtomType ratio =
        new TypeBuilder("ratio")
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
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "restricted_group"))
                    .build())
            .frontDataNode("value")
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

    new GeneralTestWizard(syntax, Helper.createGap(syntax))
        .actEnter()
        .sendText("_")
        .checkArrayTree(
            new TreeBuilder(restrictedArray)
                .addArray("value", new TreeBuilder(quoted).add("value", "").build())
                .build());
  }
}
