package com.zarbosoft.merman;

import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.document.fields.FieldArray;
import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.visual.visuals.FieldArrayCursor;
import com.zarbosoft.merman.core.visual.visuals.VisualFieldArray;
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
import com.zarbosoft.merman.core.syntax.FreeAtomType;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.core.syntax.back.BackArraySpec;
import com.zarbosoft.merman.core.syntax.back.BaseBackArraySpec;
import com.zarbosoft.rendaw.common.TSList;
import org.junit.Test;

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
                    .build();
    FreeAtomType snooze =
            new TypeBuilder("snooze")
                    .back(new BackArraySpec(new BaseBackArraySpec.Config("value", infinity.id(), TSList.of())))
                    .frontDataArray("value")
                    .build();
    Syntax syntax =
            new SyntaxBuilder(snooze.id())
                    .type(infinity)
                    .type(snooze)
                    .build();
    final Context context =
        build(syntax,
            new TreeBuilder(snooze)
                .addArray("value", new TreeBuilder(infinity).build())
                .build());
    visual(context).select(context, true, 0, 0);
    Helper.cursorArray(context).actionEnter(context);
    assertThat(
        context.cursor.getSyntaxPath(),
        equalTo(new SyntaxPath("value", "0", "value", "0", "value", "0")));
  }

  public Context build(Syntax syntax, final Atom... atoms) {
    final Context context =
        buildDoc(
            syntax, new TreeBuilder(MiscSyntax.array).addArray("value", atoms).build());
    ((FieldArray) Helper.rootArray(context.document).data.get(0).fields.getOpt("value"))
        .selectInto(context);
    return context;
  }

  public static VisualFieldArray visual(final Context context) {
    return (VisualFieldArray) context.cursor.getVisual().parent().visual();
  }

  @Test
  public void testExit() {
    FreeAtomType infinity =
            new TypeBuilder("infinity")
                    .back(Helper.buildBackPrimitive("infinity"))
                    .front(new FrontMarkBuilder("infinity").build())
                    .build();
    FreeAtomType snooze =
            new TypeBuilder("snooze")
                    .back(new BackArraySpec(new BaseBackArraySpec.Config("value", infinity.id(), TSList.of())))
                    .frontDataArray("value")
                    .build();
    Syntax syntax =
            new SyntaxBuilder(snooze.id())
                    .type(infinity)
                    .type(snooze)
                    .build();
    final Context context =
        build(
                syntax, new TreeBuilder(snooze)
                .addArray("value", new TreeBuilder(infinity).build())
                .build());
    visual(context).select(context, true, 0, 0);
    Helper.cursorArray(context).actionExit(context);
    assertThat(context.cursor.getSyntaxPath(), equalTo(new SyntaxPath("value", "0")));
  }

  @Test
  public void testNext() {
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
                    .back(Helper.buildBackDataRecord("value", "recordElement"))
                    .frontMark("{")
                    .frontDataArray("value")
                    .frontMark("}")
                    .autoComplete(true)
                    .build();
    FreeAtomType recordElement =
            new TypeBuilder("recordElement")
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
    final Atom target = new TreeBuilder(one).build();
    new GeneralTestWizard(
            syntax,
             new TreeBuilder(doubleArray)
                .addArray("first", target)
                .addArray("second", new TreeBuilder(one).build())
                .build())
        .run(context -> target.fieldParentRef.selectValue(context))
        .actNext()
        .run(
            context ->
                assertThat(
                    context.cursor.getSyntaxPath(),
                    equalTo(new SyntaxPath("value", "0", "second", "0"))));
  }

  @Test
  public void testPrevious() {
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
                    .back(Helper.buildBackDataRecord("value", "recordElement"))
                    .frontMark("{")
                    .frontDataArray("value")
                    .frontMark("}")
                    .autoComplete(true)
                    .build();
    FreeAtomType recordElement =
            new TypeBuilder("recordElement")
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
    final Atom target = new TreeBuilder(one).build();
    new GeneralTestWizard(
            syntax,
             new TreeBuilder(doubleArray)
                .addArray("first", new TreeBuilder(one).build())
                .addArray("second", target)
                .build())
        .run(context -> target.fieldParentRef.selectValue(context))
        .actPrevious()
        .run(
            context ->
                assertThat(
                    context.cursor.getSyntaxPath(),
                    equalTo(new SyntaxPath("value", "0", "first", "0"))));
  }

  @Test
  public void testNextElement() {
    final Context context = buildFive();
    visual(context).select(context, true, 2, 2);
    Helper.cursorArray(context).actionNextElement(context);
    assertThat(context.cursor.getSyntaxPath(), equalTo(new SyntaxPath("value", "0", "value", "3")));
  }

  public Context buildFive() {
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
                    .back(Helper.buildBackDataRecord("value", "recordElement"))
                    .frontMark("{")
                    .frontDataArray("value")
                    .frontMark("}")
                    .autoComplete(true)
                    .build();
    FreeAtomType recordElement =
            new TypeBuilder("recordElement")
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
    Helper.cursorArray(context).actionNextElement(context);
    assertSelection(context, 4, 4);
  }

  public static void assertSelection(final Context context, final int begin, final int end) {
    final FieldArrayCursor selection = (FieldArrayCursor) context.cursor;
    assertThat(selection.beginIndex, equalTo(begin));
    assertThat(selection.endIndex, equalTo(end));
  }

  @Test
  public void testNextEnd() {
    final Context context = buildFive();
    visual(context).select(context, true, 4, 4);
    Helper.cursorArray(context).actionNextElement(context);
    assertThat(context.cursor.getSyntaxPath(), equalTo(new SyntaxPath("value", "0", "value", "4")));
  }

  @Test
  public void testPreviousElement() {
    final Context context = buildFive();
    visual(context).select(context, true, 2, 2);
    Helper.cursorArray(context).actionPreviousElement(context);
    assertThat(context.cursor.getSyntaxPath(), equalTo(new SyntaxPath("value", "0", "value", "1")));
  }

  @Test
  public void testPreviousRange() {
    final Context context = buildFive();
    visual(context).select(context, true, 2, 3);
    Helper.cursorArray(context).actionPreviousElement(context);
    assertSelection(context, 1, 1);
  }

  @Test
  public void testPreviousStart() {
    final Context context = buildFive();
    visual(context).select(context, true, 0, 0);
    Helper.cursorArray(context).actionPreviousElement(context);
    assertThat(context.cursor.getSyntaxPath(), equalTo(new SyntaxPath("value", "0", "value", "0")));
  }

  @Test
  public void testGatherNext() {
    final Context context = buildFive();
    visual(context).select(context, true, 2, 2);
    Helper.cursorArray(context).actionGatherNext(context);
    final FieldArrayCursor selection = (FieldArrayCursor) context.cursor;
    assertThat(selection.beginIndex, equalTo(2));
    assertThat(selection.endIndex, equalTo(3));
  }

  @Test
  public void testGatherNextEnd() {
    final Context context = buildFive();
    visual(context).select(context, true, 4, 4);
    Helper.cursorArray(context).actionGatherNext(context);
    final FieldArrayCursor selection = (FieldArrayCursor) context.cursor;
    assertThat(selection.beginIndex, equalTo(4));
    assertThat(selection.endIndex, equalTo(4));
  }

  @Test
  public void testGatherPrevious() {
    final Context context = buildFive();
    visual(context).select(context, true, 2, 2);
    Helper.cursorArray(context).actionGatherPrevious(context);
    final FieldArrayCursor selection = (FieldArrayCursor) context.cursor;
    assertThat(selection.beginIndex, equalTo(1));
    assertThat(selection.endIndex, equalTo(2));
  }

  @Test
  public void testGatherPreviousStart() {
    final Context context = buildFive();
    visual(context).select(context, true, 0, 0);
    Helper.cursorArray(context).actionGatherPrevious(context);
    final FieldArrayCursor selection = (FieldArrayCursor) context.cursor;
    assertThat(selection.beginIndex, equalTo(0));
    assertThat(selection.endIndex, equalTo(0));
  }

  @Test
  public void testReleaseNext() {
    final Context context = buildFive();
    ((VisualFieldArray)
            ((FieldArray) ((Atom) context.syntaxLocate(new SyntaxPath("value", "0"))).fields.getOpt("value"))
                .visual)
        .select(context, true, 2, 3);
    Helper.cursorArray(context).actionReleaseNext(context);
    final FieldArrayCursor selection = (FieldArrayCursor) context.cursor;
    assertThat(selection.beginIndex, equalTo(2));
    assertThat(selection.endIndex, equalTo(2));
  }

  @Test
  public void testReleaseNextMinimum() {
    final Context context = buildFive();
    visual(context).select(context, true, 2, 2);
    Helper.cursorArray(context).actionReleaseNext(context);
    final FieldArrayCursor selection = (FieldArrayCursor) context.cursor;
    assertThat(selection.beginIndex, equalTo(2));
    assertThat(selection.endIndex, equalTo(2));
  }

  @Test
  public void testReleasePrevious() {
    final Context context = buildFive();
    (((VisualFieldArray)
            ((FieldArray) ((Atom) context.syntaxLocate(new SyntaxPath("value", "0"))).fields.getOpt("value"))
                .visual))
        .select(context, true, 1, 2);
    Helper.cursorArray(context).actionReleasePrevious(context);
    final FieldArrayCursor selection = (FieldArrayCursor) context.cursor;
    assertThat(selection.beginIndex, equalTo(2));
    assertThat(selection.endIndex, equalTo(2));
  }

  @Test
  public void testReleasePreviousMinimum() {
    final Context context = buildFive();
    visual(context).select(context, true, 2, 2);
    Helper.cursorArray(context).actionReleasePrevious(context);
    final FieldArrayCursor selection = (FieldArrayCursor) context.cursor;
    assertThat(selection.beginIndex, equalTo(2));
    assertThat(selection.endIndex, equalTo(2));
  }
}
