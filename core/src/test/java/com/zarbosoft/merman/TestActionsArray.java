package com.zarbosoft.merman;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.ValueArray;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.editor.visual.visuals.VisualFrontArray;
import com.zarbosoft.merman.helper.BackArrayBuilder;
import com.zarbosoft.merman.helper.BackRecordBuilder;
import com.zarbosoft.merman.helper.FrontDataArrayBuilder;
import com.zarbosoft.merman.helper.FrontMarkBuilder;
import com.zarbosoft.merman.helper.GeneralTestWizard;
import com.zarbosoft.merman.helper.GroupBuilder;
import com.zarbosoft.merman.helper.Helper;
import com.zarbosoft.merman.helper.MiscSyntax;
import com.zarbosoft.merman.helper.SyntaxBuilder;
import com.zarbosoft.merman.helper.TreeBuilder;
import com.zarbosoft.merman.helper.TypeBuilder;
import com.zarbosoft.merman.syntax.FreeAtomType;
import com.zarbosoft.merman.syntax.Syntax;
import org.junit.Test;

import static com.zarbosoft.merman.helper.Helper.assertTreeEqual;
import static com.zarbosoft.merman.helper.Helper.buildDoc;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class TestActionsArray {

  @Test
  public void testEnter() {
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
                    .frontDataPrimitive("a")
                    .frontMark("^")
                    .frontDataPrimitive("b")
                    .autoComplete(-1)
                    .build();
    FreeAtomType quoted =
            new TypeBuilder("quoted")
                    .back(Helper.buildBackDataPrimitive("value"))
                    .front(new FrontMarkBuilder("\"").build())
                    .frontDataPrimitive("value")
                    .front(new FrontMarkBuilder("\"").build())
                    .autoComplete(1)
                    .build();
    FreeAtomType digits =
            new TypeBuilder("digits")
                    .back(Helper.buildBackDataPrimitiveDigits("value"))
                    .frontDataPrimitive("value")
                    .autoComplete(1)
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
                    .autoComplete(-1)
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
                    .autoComplete(-1)
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
                    .autoComplete(-1)
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
                    .autoComplete(1)
                    .build();
    FreeAtomType waddle =
            new TypeBuilder("waddle")
                    .back(
                            new BackRecordBuilder()
                                    .add("first", Helper.buildBackDataAtom("first", "any"))
                                    .build())
                    .frontDataNode("first")
                    .frontMark("?")
                    .autoComplete(1)
                    .build();
    FreeAtomType snooze =
            new TypeBuilder("snooze")
                    .back(
                            new BackRecordBuilder()
                                    .add("value", Helper.buildBackDataAtom("value", "any"))
                                    .build())
                    .frontMark("#")
                    .frontDataNode("value")
                    .autoComplete(1)
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
                    .autoComplete(1)
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
                    .autoComplete(1)
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
                    .autoComplete(1)
                    .build();
    FreeAtomType recordElement =
            new TypeBuilder("record_element")
                    .back(Helper.buildBackDataKey("key"))
                    .back(Helper.buildBackDataAtom("value", "any"))
                    .frontDataPrimitive("key")
                    .frontMark(": ")
                    .frontDataNode("value")
                    .autoComplete(1)
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
                    .autoComplete(1)
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

    final Context context =
        build(syntax,
            new TreeBuilder(snooze)
                .add("value", new TreeBuilder(infinity).build())
                .build());
    visual(context).select(context, true, 0, 0);
    Helper.act(context, "enter");
    assertThat(
        context.cursor.getSyntaxPath(),
        equalTo(new Path("value", "0", "value", "0", "value")));
  }

  public Context build(Syntax syntax, final Atom... atoms) {
    final Context context =
        buildDoc(
            syntax, new TreeBuilder(MiscSyntax.array).addArray("value", atoms).build());
    ((ValueArray) Helper.rootArray(context.document).data.get(0).fields.getOpt("value"))
        .selectInto(context);
    return context;
  }

  public static VisualFrontArray visual(final Context context) {
    return (VisualFrontArray) context.cursor.getVisual().parent().visual();
  }

  @Test
  public void testExit() {

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
                    .frontDataPrimitive("a")
                    .frontMark("^")
                    .frontDataPrimitive("b")
                    .autoComplete(-1)
                    .build();
    FreeAtomType quoted =
            new TypeBuilder("quoted")
                    .back(Helper.buildBackDataPrimitive("value"))
                    .front(new FrontMarkBuilder("\"").build())
                    .frontDataPrimitive("value")
                    .front(new FrontMarkBuilder("\"").build())
                    .autoComplete(1)
                    .build();
    FreeAtomType digits =
            new TypeBuilder("digits")
                    .back(Helper.buildBackDataPrimitiveDigits("value"))
                    .frontDataPrimitive("value")
                    .autoComplete(1)
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
                    .autoComplete(-1)
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
                    .autoComplete(-1)
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
                    .autoComplete(-1)
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
                    .autoComplete(1)
                    .build();
    FreeAtomType waddle =
            new TypeBuilder("waddle")
                    .back(
                            new BackRecordBuilder()
                                    .add("first", Helper.buildBackDataAtom("first", "any"))
                                    .build())
                    .frontDataNode("first")
                    .frontMark("?")
                    .autoComplete(1)
                    .build();
    FreeAtomType snooze =
            new TypeBuilder("snooze")
                    .back(
                            new BackRecordBuilder()
                                    .add("value", Helper.buildBackDataAtom("value", "any"))
                                    .build())
                    .frontMark("#")
                    .frontDataNode("value")
                    .autoComplete(1)
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
                    .autoComplete(1)
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
                    .autoComplete(1)
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
                    .autoComplete(1)
                    .build();
    FreeAtomType recordElement =
            new TypeBuilder("record_element")
                    .back(Helper.buildBackDataKey("key"))
                    .back(Helper.buildBackDataAtom("value", "any"))
                    .frontDataPrimitive("key")
                    .frontMark(": ")
                    .frontDataNode("value")
                    .autoComplete(1)
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
                    .autoComplete(1)
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

    final Context context =
        build(
                syntax, new TreeBuilder(snooze)
                .add("value", new TreeBuilder(infinity).build())
                .build());
    visual(context).select(context, true, 0, 0);
    Helper.act(context, "exit");
    assertThat(context.cursor.getSyntaxPath(), equalTo(new Path("value", "0")));
  }

  @Test
  public void testNext() {
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
                    .frontDataPrimitive("a")
                    .frontMark("^")
                    .frontDataPrimitive("b")
                    .autoComplete(-1)
                    .build();
    FreeAtomType quoted =
            new TypeBuilder("quoted")
                    .back(Helper.buildBackDataPrimitive("value"))
                    .front(new FrontMarkBuilder("\"").build())
                    .frontDataPrimitive("value")
                    .front(new FrontMarkBuilder("\"").build())
                    .autoComplete(1)
                    .build();
    FreeAtomType digits =
            new TypeBuilder("digits")
                    .back(Helper.buildBackDataPrimitiveDigits("value"))
                    .frontDataPrimitive("value")
                    .autoComplete(1)
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
                    .autoComplete(-1)
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
                    .autoComplete(-1)
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
                    .autoComplete(-1)
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
                    .autoComplete(1)
                    .build();
    FreeAtomType waddle =
            new TypeBuilder("waddle")
                    .back(
                            new BackRecordBuilder()
                                    .add("first", Helper.buildBackDataAtom("first", "any"))
                                    .build())
                    .frontDataNode("first")
                    .frontMark("?")
                    .autoComplete(1)
                    .build();
    FreeAtomType snooze =
            new TypeBuilder("snooze")
                    .back(
                            new BackRecordBuilder()
                                    .add("value", Helper.buildBackDataAtom("value", "any"))
                                    .build())
                    .frontMark("#")
                    .frontDataNode("value")
                    .autoComplete(1)
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
                    .autoComplete(1)
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
                    .autoComplete(1)
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
                    .autoComplete(1)
                    .build();
    FreeAtomType recordElement =
            new TypeBuilder("record_element")
                    .back(Helper.buildBackDataKey("key"))
                    .back(Helper.buildBackDataAtom("value", "any"))
                    .frontDataPrimitive("key")
                    .frontMark(": ")
                    .frontDataNode("value")
                    .autoComplete(1)
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
                    .autoComplete(1)
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
    final Atom target = new TreeBuilder(one).build();
    new GeneralTestWizard(
            syntax,
             new TreeBuilder(doubleArray)
                .addArray("first", target)
                .addArray("second", new TreeBuilder(one).build())
                .build())
        .run(context -> target.parent.selectChild(context))
        .act("next")
        .run(
            context ->
                assertThat(
                    context.cursor.getSyntaxPath(),
                    equalTo(new Path("value", "0", "second", "0"))));
  }

  @Test
  public void testPrevious() {
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
                    .frontDataPrimitive("a")
                    .frontMark("^")
                    .frontDataPrimitive("b")
                    .autoComplete(-1)
                    .build();
    FreeAtomType quoted =
            new TypeBuilder("quoted")
                    .back(Helper.buildBackDataPrimitive("value"))
                    .front(new FrontMarkBuilder("\"").build())
                    .frontDataPrimitive("value")
                    .front(new FrontMarkBuilder("\"").build())
                    .autoComplete(1)
                    .build();
    FreeAtomType digits =
            new TypeBuilder("digits")
                    .back(Helper.buildBackDataPrimitiveDigits("value"))
                    .frontDataPrimitive("value")
                    .autoComplete(1)
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
                    .autoComplete(-1)
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
                    .autoComplete(-1)
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
                    .autoComplete(-1)
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
                    .autoComplete(1)
                    .build();
    FreeAtomType waddle =
            new TypeBuilder("waddle")
                    .back(
                            new BackRecordBuilder()
                                    .add("first", Helper.buildBackDataAtom("first", "any"))
                                    .build())
                    .frontDataNode("first")
                    .frontMark("?")
                    .autoComplete(1)
                    .build();
    FreeAtomType snooze =
            new TypeBuilder("snooze")
                    .back(
                            new BackRecordBuilder()
                                    .add("value", Helper.buildBackDataAtom("value", "any"))
                                    .build())
                    .frontMark("#")
                    .frontDataNode("value")
                    .autoComplete(1)
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
                    .autoComplete(1)
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
                    .autoComplete(1)
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
                    .autoComplete(1)
                    .build();
    FreeAtomType recordElement =
            new TypeBuilder("record_element")
                    .back(Helper.buildBackDataKey("key"))
                    .back(Helper.buildBackDataAtom("value", "any"))
                    .frontDataPrimitive("key")
                    .frontMark(": ")
                    .frontDataNode("value")
                    .autoComplete(1)
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
                    .autoComplete(1)
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
    final Atom target = new TreeBuilder(one).build();
    new GeneralTestWizard(
            syntax,
             new TreeBuilder(doubleArray)
                .addArray("first", new TreeBuilder(one).build())
                .addArray("second", target)
                .build())
        .run(context -> target.parent.selectChild(context))
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
    assertThat(context.cursor.getSyntaxPath(), equalTo(new Path("value", "0", "value", "3")));
  }

  public Context buildFive() {
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
                    .frontDataPrimitive("a")
                    .frontMark("^")
                    .frontDataPrimitive("b")
                    .autoComplete(-1)
                    .build();
    FreeAtomType quoted =
            new TypeBuilder("quoted")
                    .back(Helper.buildBackDataPrimitive("value"))
                    .front(new FrontMarkBuilder("\"").build())
                    .frontDataPrimitive("value")
                    .front(new FrontMarkBuilder("\"").build())
                    .autoComplete(1)
                    .build();
    FreeAtomType digits =
            new TypeBuilder("digits")
                    .back(Helper.buildBackDataPrimitiveDigits("value"))
                    .frontDataPrimitive("value")
                    .autoComplete(1)
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
                    .autoComplete(-1)
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
                    .autoComplete(-1)
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
                    .autoComplete(-1)
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
                    .autoComplete(1)
                    .build();
    FreeAtomType waddle =
            new TypeBuilder("waddle")
                    .back(
                            new BackRecordBuilder()
                                    .add("first", Helper.buildBackDataAtom("first", "any"))
                                    .build())
                    .frontDataNode("first")
                    .frontMark("?")
                    .autoComplete(1)
                    .build();
    FreeAtomType snooze =
            new TypeBuilder("snooze")
                    .back(
                            new BackRecordBuilder()
                                    .add("value", Helper.buildBackDataAtom("value", "any"))
                                    .build())
                    .frontMark("#")
                    .frontDataNode("value")
                    .autoComplete(1)
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
                    .autoComplete(1)
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
                    .autoComplete(1)
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
                    .autoComplete(1)
                    .build();
    FreeAtomType recordElement =
            new TypeBuilder("record_element")
                    .back(Helper.buildBackDataKey("key"))
                    .back(Helper.buildBackDataAtom("value", "any"))
                    .frontDataPrimitive("key")
                    .frontMark(": ")
                    .frontDataNode("value")
                    .autoComplete(1)
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
                    .autoComplete(1)
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
    return build(
            syntax, new TreeBuilder(one).build(),
        new TreeBuilder(two).build(),
        new TreeBuilder(three).build(),
        new TreeBuilder(four).build(),
        new TreeBuilder(five).build());
  }

  @Test
  public void testNextRange() {
    final Context context = buildFive();
    visual(context).select(context, true, 2, 3);
    Helper.act(context, "next_element");
    assertSelection(context, 4, 4);
  }

  public static void assertSelection(final Context context, final int begin, final int end) {
    final VisualFrontArray.ArrayCursor selection = (VisualFrontArray.ArrayCursor) context.cursor;
    assertThat(selection.beginIndex, equalTo(begin));
    assertThat(selection.endIndex, equalTo(end));
  }

  @Test
  public void testNextEnd() {
    final Context context = buildFive();
    visual(context).select(context, true, 4, 4);
    Helper.act(context, "next_element");
    assertThat(context.cursor.getSyntaxPath(), equalTo(new Path("value", "0", "value", "4")));
  }

  @Test
  public void testPreviousElement() {
    final Context context = buildFive();
    visual(context).select(context, true, 2, 2);
    Helper.act(context, "previous_element");
    assertThat(context.cursor.getSyntaxPath(), equalTo(new Path("value", "0", "value", "1")));
  }

  @Test
  public void testPreviousRange() {
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
    assertThat(context.cursor.getSyntaxPath(), equalTo(new Path("value", "0", "value", "0")));
  }

  @Test
  public void testGatherNext() {
    final Context context = buildFive();
    visual(context).select(context, true, 2, 2);
    Helper.act(context, "gather_next");
    final VisualFrontArray.ArrayCursor selection = (VisualFrontArray.ArrayCursor) context.cursor;
    assertThat(selection.beginIndex, equalTo(2));
    assertThat(selection.endIndex, equalTo(3));
  }

  @Test
  public void testGatherNextEnd() {
    final Context context = buildFive();
    visual(context).select(context, true, 4, 4);
    Helper.act(context, "gather_next");
    final VisualFrontArray.ArrayCursor selection = (VisualFrontArray.ArrayCursor) context.cursor;
    assertThat(selection.beginIndex, equalTo(4));
    assertThat(selection.endIndex, equalTo(4));
  }

  @Test
  public void testGatherPrevious() {
    final Context context = buildFive();
    visual(context).select(context, true, 2, 2);
    Helper.act(context, "gather_previous");
    final VisualFrontArray.ArrayCursor selection = (VisualFrontArray.ArrayCursor) context.cursor;
    assertThat(selection.beginIndex, equalTo(1));
    assertThat(selection.endIndex, equalTo(2));
  }

  @Test
  public void testGatherPreviousStart() {
    final Context context = buildFive();
    visual(context).select(context, true, 0, 0);
    Helper.act(context, "gather_previous");
    final VisualFrontArray.ArrayCursor selection = (VisualFrontArray.ArrayCursor) context.cursor;
    assertThat(selection.beginIndex, equalTo(0));
    assertThat(selection.endIndex, equalTo(0));
  }

  @Test
  public void testReleaseNext() {
    final Context context = buildFive();
    ((VisualFrontArray)
            ((ValueArray) ((Atom) context.syntaxLocate(new Path("value", "0"))).fields.getOpt("value"))
                .visual)
        .select(context, true, 2, 3);
    Helper.act(context, "release_next");
    final VisualFrontArray.ArrayCursor selection = (VisualFrontArray.ArrayCursor) context.cursor;
    assertThat(selection.beginIndex, equalTo(2));
    assertThat(selection.endIndex, equalTo(2));
  }

  @Test
  public void testReleaseNextMinimum() {
    final Context context = buildFive();
    visual(context).select(context, true, 2, 2);
    Helper.act(context, "release_next");
    final VisualFrontArray.ArrayCursor selection = (VisualFrontArray.ArrayCursor) context.cursor;
    assertThat(selection.beginIndex, equalTo(2));
    assertThat(selection.endIndex, equalTo(2));
  }

  @Test
  public void testReleasePrevious() {
    final Context context = buildFive();
    (((VisualFrontArray)
            ((ValueArray) ((Atom) context.syntaxLocate(new Path("value", "0"))).fields.getOpt("value"))
                .visual))
        .select(context, true, 1, 2);
    Helper.act(context, "release_previous");
    final VisualFrontArray.ArrayCursor selection = (VisualFrontArray.ArrayCursor) context.cursor;
    assertThat(selection.beginIndex, equalTo(2));
    assertThat(selection.endIndex, equalTo(2));
  }

  @Test
  public void testReleasePreviousMinimum() {
    final Context context = buildFive();
    visual(context).select(context, true, 2, 2);
    Helper.act(context, "release_previous");
    final VisualFrontArray.ArrayCursor selection = (VisualFrontArray.ArrayCursor) context.cursor;
    assertThat(selection.beginIndex, equalTo(2));
    assertThat(selection.endIndex, equalTo(2));
  }
}
