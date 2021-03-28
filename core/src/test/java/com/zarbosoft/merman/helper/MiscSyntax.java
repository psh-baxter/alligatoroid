package com.zarbosoft.merman.helper;

import com.zarbosoft.merman.core.syntax.FreeAtomType;
import com.zarbosoft.merman.core.syntax.Syntax;

public class MiscSyntax {
  public static final FreeAtomType infinity;
  public static final FreeAtomType one;
  public static final FreeAtomType two;
  public static final FreeAtomType three;
  public static final FreeAtomType four;
  public static final FreeAtomType five;
  public static final FreeAtomType seven;
  public static final FreeAtomType multiback;
  public static final FreeAtomType quoted;
  public static final FreeAtomType digits;
  public static final FreeAtomType doubleQuoted;
  public static final FreeAtomType binaryBang;
  public static final FreeAtomType plusEqual;
  public static final FreeAtomType plus;
  public static final FreeAtomType waddle;
  public static final FreeAtomType snooze;
  public static final FreeAtomType multiplier;
  public static final FreeAtomType array;
  public static final FreeAtomType doubleArray;
  public static final FreeAtomType record;
  public static final FreeAtomType recordElement;
  public static final FreeAtomType pair;
  public static final FreeAtomType ratio;
  public static final FreeAtomType restricted;
  public static final FreeAtomType restrictedArray;
  public static final Syntax syntax;

  static {
    infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(1)
            .build();
    one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .autoComplete(-1)
            .build();
    two =
        new TypeBuilder("two")
            .back(Helper.buildBackPrimitive("two"))
            .front(new FrontMarkBuilder("two").build())
            .autoComplete(-1)
            .build();
    three =
        new TypeBuilder("three")
            .back(Helper.buildBackPrimitive("three"))
            .front(new FrontMarkBuilder("three").build())
            .autoComplete(-1)
            .build();
    four =
        new TypeBuilder("four")
            .back(Helper.buildBackPrimitive("four"))
            .front(new FrontMarkBuilder("four").build())
            .autoComplete(-1)
            .build();
    five =
        new TypeBuilder("five")
            .back(Helper.buildBackPrimitive("five"))
            .front(new FrontMarkBuilder("five").build())
            .autoComplete(-1)
            .build();
    seven =
        new TypeBuilder("seven")
            .back(Helper.buildBackPrimitive("7"))
            .front(new FrontMarkBuilder("7").build())
            .autoComplete(-1)
            .build();
    multiback =
        new TypeBuilder("multiback")
            .back(Helper.buildBackDataPrimitive("a"))
            .back(Helper.buildBackDataPrimitive("b"))
            .frontDataPrimitive("a")
            .frontMark("^")
            .frontDataPrimitive("b")
            .autoComplete(-1)
            .build();
    quoted =
        new TypeBuilder("quoted")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("value")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(1)
            .build();
    digits =
        new TypeBuilder("digits")
            .back(Helper.buildBackDataPrimitiveDigits("value"))
            .frontDataPrimitive("value")
            .autoComplete(1)
            .build();
    doubleQuoted =
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
    plus =
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
    plusEqual =
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
    binaryBang =
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
    waddle =
        new TypeBuilder("waddle")
            .back(
                new BackRecordBuilder()
                    .add("first", Helper.buildBackDataAtom("first", "any"))
                    .build())
            .frontDataNode("first")
            .frontMark("?")
            .autoComplete(1)
            .build();
    snooze =
        new TypeBuilder("snooze")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "any"))
                    .build())
            .frontMark("#")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    multiplier =
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
    array =
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
    doubleArray =
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
    record =
        new TypeBuilder("record")
            .back(Helper.buildBackDataRecord("value", "record_element"))
            .frontMark("{")
            .frontDataArray("value")
            .frontMark("}")
            .autoComplete(1)
            .build();
    recordElement =
        new TypeBuilder("record_element")
            .back(Helper.buildBackDataKey("key"))
            .back(Helper.buildBackDataAtom("value", "any"))
            .frontDataPrimitive("key")
            .frontMark(": ")
            .frontDataNode("value")
            .autoComplete(1)
            .build();
    pair =
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
    ratio =
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
    restricted =
        new TypeBuilder("restricted")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "restricted_group"))
                    .build())
            .frontDataNode("value")
            .build();
    restrictedArray =
        new TypeBuilder("restricted_array")
            .back(Helper.buildBackDataArray("value", "restricted_array_group"))
            .frontMark("_")
            .front(new FrontDataArrayBuilder("value").build())
            .autoComplete(1)
            .build();
    syntax =
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
                    .type(MiscSyntax.one)
                    .type(MiscSyntax.multiback)
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
            .group("arrayChildren", new GroupBuilder().type(one).type(MiscSyntax.multiback).build())
            .build();
  }

  {
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
  }
}
