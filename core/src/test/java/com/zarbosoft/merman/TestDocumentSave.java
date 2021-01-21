package com.zarbosoft.merman;

import com.zarbosoft.merman.helper.BackArrayBuilder;
import com.zarbosoft.merman.helper.BackRecordBuilder;
import com.zarbosoft.merman.helper.GroupBuilder;
import com.zarbosoft.merman.helper.Helper;
import com.zarbosoft.merman.helper.SyntaxBuilder;
import com.zarbosoft.merman.helper.TreeBuilder;
import com.zarbosoft.merman.helper.TypeBuilder;
import com.zarbosoft.merman.syntax.FreeAtomType;
import com.zarbosoft.merman.syntax.Syntax;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static com.zarbosoft.merman.helper.Helper.buildDoc;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class TestDocumentSave {
  public void check(Syntax syntax, final TreeBuilder tree, final String result) {
    final ByteArrayOutputStream stream = new ByteArrayOutputStream();
    buildDoc(syntax, tree.build()).document.write(stream);
    assertThat(new String(stream.toByteArray(), StandardCharsets.UTF_8), equalTo(result));
  }

  @Test
  public void testPrimitive() {
    final FreeAtomType primitive =
        new TypeBuilder("primitive").back(Helper.buildBackPrimitive("x")).frontMark("x").build();
    final FreeAtomType typedPrimitive =
        new TypeBuilder("typedPrimitive")
            .back(Helper.buildBackType("z", Helper.buildBackPrimitive("x")))
            .frontMark("x")
            .build();
    final FreeAtomType doublePrimitive =
        new TypeBuilder("doublePrimitive")
            .back(Helper.buildBackPrimitive("x"))
            .back(Helper.buildBackPrimitive("y"))
            .frontMark("x")
            .build();
    final FreeAtomType array =
        new TypeBuilder("array")
            .back(
                Helper.buildBackType(
                    "typedArray",
                    new BackArrayBuilder()
                        .add(Helper.buildBackPrimitive("x"))
                        .add(Helper.buildBackPrimitive("y"))
                        .build()))
            .frontMark("x")
            .build();
    final FreeAtomType record =
        new TypeBuilder("record")
            .back(
                Helper.buildBackType(
                    "typedRecord",
                    new BackRecordBuilder()
                        .add("a", Helper.buildBackPrimitive("x"))
                        .add("b", Helper.buildBackPrimitive("y"))
                        .build()))
            .frontMark("x")
            .build();
    final FreeAtomType dataPrimitive =
        new TypeBuilder("dataPrimitive")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .build();
    final FreeAtomType dataArray =
        new TypeBuilder("dataArray")
            .back(Helper.buildBackDataArray("value", "array_value"))
            .frontDataArray("value")
            .build();
    final FreeAtomType dataRecord =
        new TypeBuilder("dataRecord")
            .back(Helper.buildBackDataRecord("value", "dataRecordElement"))
            .frontDataArray("value")
            .build();
    final FreeAtomType dataRecordElement =
        new TypeBuilder("dataRecordElement")
            .back(Helper.buildBackDataKey("key"))
            .back(Helper.buildBackDataAtom("value", "value"))
            .frontDataPrimitive("key")
            .frontDataNode("value")
            .build();
    final Syntax syntax =
        new SyntaxBuilder("array_value")
            .type(primitive)
            .type(typedPrimitive)
            .type(doublePrimitive)
            .type(array)
            .type(record)
            .type(dataPrimitive)
            .type(dataArray)
            .type(dataRecord)
            .type(dataRecordElement)
            .group(
                "array_value",
                new GroupBuilder()
                    .type(primitive)
                    .type(typedPrimitive)
                    .type(doublePrimitive)
                    .type(array)
                    .type(record)
                    .type(dataPrimitive)
                    .type(dataArray)
                    .type(dataRecord)
                    .build())
            .group(
                "value",
                new GroupBuilder()
                    .type(primitive)
                    .type(typedPrimitive)
                    .type(array)
                    .type(record)
                    .type(dataPrimitive)
                    .type(dataArray)
                    .type(dataRecord)
                    .build())
            .build();
    check(syntax, new TreeBuilder(primitive), "x,\n");
  }

  @Test
  public void testTypedPrimitive() {
    final FreeAtomType primitive =
        new TypeBuilder("primitive").back(Helper.buildBackPrimitive("x")).frontMark("x").build();
    final FreeAtomType typedPrimitive =
        new TypeBuilder("typedPrimitive")
            .back(Helper.buildBackType("z", Helper.buildBackPrimitive("x")))
            .frontMark("x")
            .build();
    final FreeAtomType doublePrimitive =
        new TypeBuilder("doublePrimitive")
            .back(Helper.buildBackPrimitive("x"))
            .back(Helper.buildBackPrimitive("y"))
            .frontMark("x")
            .build();
    final FreeAtomType array =
        new TypeBuilder("array")
            .back(
                Helper.buildBackType(
                    "typedArray",
                    new BackArrayBuilder()
                        .add(Helper.buildBackPrimitive("x"))
                        .add(Helper.buildBackPrimitive("y"))
                        .build()))
            .frontMark("x")
            .build();
    final FreeAtomType record =
        new TypeBuilder("record")
            .back(
                Helper.buildBackType(
                    "typedRecord",
                    new BackRecordBuilder()
                        .add("a", Helper.buildBackPrimitive("x"))
                        .add("b", Helper.buildBackPrimitive("y"))
                        .build()))
            .frontMark("x")
            .build();
    final FreeAtomType dataPrimitive =
        new TypeBuilder("dataPrimitive")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .build();
    final FreeAtomType dataArray =
        new TypeBuilder("dataArray")
            .back(Helper.buildBackDataArray("value", "array_value"))
            .frontDataArray("value")
            .build();
    final FreeAtomType dataRecord =
        new TypeBuilder("dataRecord")
            .back(Helper.buildBackDataRecord("value", "dataRecordElement"))
            .frontDataArray("value")
            .build();
    final FreeAtomType dataRecordElement =
        new TypeBuilder("dataRecordElement")
            .back(Helper.buildBackDataKey("key"))
            .back(Helper.buildBackDataAtom("value", "value"))
            .frontDataPrimitive("key")
            .frontDataNode("value")
            .build();
    final Syntax syntax =
        new SyntaxBuilder("array_value")
            .type(primitive)
            .type(typedPrimitive)
            .type(doublePrimitive)
            .type(array)
            .type(record)
            .type(dataPrimitive)
            .type(dataArray)
            .type(dataRecord)
            .type(dataRecordElement)
            .group(
                "array_value",
                new GroupBuilder()
                    .type(primitive)
                    .type(typedPrimitive)
                    .type(doublePrimitive)
                    .type(array)
                    .type(record)
                    .type(dataPrimitive)
                    .type(dataArray)
                    .type(dataRecord)
                    .build())
            .group(
                "value",
                new GroupBuilder()
                    .type(primitive)
                    .type(typedPrimitive)
                    .type(array)
                    .type(record)
                    .type(dataPrimitive)
                    .type(dataArray)
                    .type(dataRecord)
                    .build())
            .build();
    check(syntax, new TreeBuilder(typedPrimitive), "(z) x,\n");
  }

  @Test
  public void testDoublePrimitive() {
    final FreeAtomType primitive =
        new TypeBuilder("primitive").back(Helper.buildBackPrimitive("x")).frontMark("x").build();
    final FreeAtomType typedPrimitive =
        new TypeBuilder("typedPrimitive")
            .back(Helper.buildBackType("z", Helper.buildBackPrimitive("x")))
            .frontMark("x")
            .build();
    final FreeAtomType doublePrimitive =
        new TypeBuilder("doublePrimitive")
            .back(Helper.buildBackPrimitive("x"))
            .back(Helper.buildBackPrimitive("y"))
            .frontMark("x")
            .build();
    final FreeAtomType array =
        new TypeBuilder("array")
            .back(
                Helper.buildBackType(
                    "typedArray",
                    new BackArrayBuilder()
                        .add(Helper.buildBackPrimitive("x"))
                        .add(Helper.buildBackPrimitive("y"))
                        .build()))
            .frontMark("x")
            .build();
    final FreeAtomType record =
        new TypeBuilder("record")
            .back(
                Helper.buildBackType(
                    "typedRecord",
                    new BackRecordBuilder()
                        .add("a", Helper.buildBackPrimitive("x"))
                        .add("b", Helper.buildBackPrimitive("y"))
                        .build()))
            .frontMark("x")
            .build();
    final FreeAtomType dataPrimitive =
        new TypeBuilder("dataPrimitive")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .build();
    final FreeAtomType dataArray =
        new TypeBuilder("dataArray")
            .back(Helper.buildBackDataArray("value", "array_value"))
            .frontDataArray("value")
            .build();
    final FreeAtomType dataRecord =
        new TypeBuilder("dataRecord")
            .back(Helper.buildBackDataRecord("value", "dataRecordElement"))
            .frontDataArray("value")
            .build();
    final FreeAtomType dataRecordElement =
        new TypeBuilder("dataRecordElement")
            .back(Helper.buildBackDataKey("key"))
            .back(Helper.buildBackDataAtom("value", "value"))
            .frontDataPrimitive("key")
            .frontDataNode("value")
            .build();
    final Syntax syntax =
        new SyntaxBuilder("array_value")
            .type(primitive)
            .type(typedPrimitive)
            .type(doublePrimitive)
            .type(array)
            .type(record)
            .type(dataPrimitive)
            .type(dataArray)
            .type(dataRecord)
            .type(dataRecordElement)
            .group(
                "array_value",
                new GroupBuilder()
                    .type(primitive)
                    .type(typedPrimitive)
                    .type(doublePrimitive)
                    .type(array)
                    .type(record)
                    .type(dataPrimitive)
                    .type(dataArray)
                    .type(dataRecord)
                    .build())
            .group(
                "value",
                new GroupBuilder()
                    .type(primitive)
                    .type(typedPrimitive)
                    .type(array)
                    .type(record)
                    .type(dataPrimitive)
                    .type(dataArray)
                    .type(dataRecord)
                    .build())
            .build();
    check(syntax, new TreeBuilder(doublePrimitive), "x,\ny,\n");
  }

  @Test
  public void testArray() {
    final FreeAtomType primitive =
        new TypeBuilder("primitive").back(Helper.buildBackPrimitive("x")).frontMark("x").build();
    final FreeAtomType typedPrimitive =
        new TypeBuilder("typedPrimitive")
            .back(Helper.buildBackType("z", Helper.buildBackPrimitive("x")))
            .frontMark("x")
            .build();
    final FreeAtomType doublePrimitive =
        new TypeBuilder("doublePrimitive")
            .back(Helper.buildBackPrimitive("x"))
            .back(Helper.buildBackPrimitive("y"))
            .frontMark("x")
            .build();
    final FreeAtomType array =
        new TypeBuilder("array")
            .back(
                Helper.buildBackType(
                    "typedArray",
                    new BackArrayBuilder()
                        .add(Helper.buildBackPrimitive("x"))
                        .add(Helper.buildBackPrimitive("y"))
                        .build()))
            .frontMark("x")
            .build();
    final FreeAtomType record =
        new TypeBuilder("record")
            .back(
                Helper.buildBackType(
                    "typedRecord",
                    new BackRecordBuilder()
                        .add("a", Helper.buildBackPrimitive("x"))
                        .add("b", Helper.buildBackPrimitive("y"))
                        .build()))
            .frontMark("x")
            .build();
    final FreeAtomType dataPrimitive =
        new TypeBuilder("dataPrimitive")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .build();
    final FreeAtomType dataArray =
        new TypeBuilder("dataArray")
            .back(Helper.buildBackDataArray("value", "array_value"))
            .frontDataArray("value")
            .build();
    final FreeAtomType dataRecord =
        new TypeBuilder("dataRecord")
            .back(Helper.buildBackDataRecord("value", "dataRecordElement"))
            .frontDataArray("value")
            .build();
    final FreeAtomType dataRecordElement =
        new TypeBuilder("dataRecordElement")
            .back(Helper.buildBackDataKey("key"))
            .back(Helper.buildBackDataAtom("value", "value"))
            .frontDataPrimitive("key")
            .frontDataNode("value")
            .build();
    final Syntax syntax =
        new SyntaxBuilder("array_value")
            .type(primitive)
            .type(typedPrimitive)
            .type(doublePrimitive)
            .type(array)
            .type(record)
            .type(dataPrimitive)
            .type(dataArray)
            .type(dataRecord)
            .type(dataRecordElement)
            .group(
                "array_value",
                new GroupBuilder()
                    .type(primitive)
                    .type(typedPrimitive)
                    .type(doublePrimitive)
                    .type(array)
                    .type(record)
                    .type(dataPrimitive)
                    .type(dataArray)
                    .type(dataRecord)
                    .build())
            .group(
                "value",
                new GroupBuilder()
                    .type(primitive)
                    .type(typedPrimitive)
                    .type(array)
                    .type(record)
                    .type(dataPrimitive)
                    .type(dataArray)
                    .type(dataRecord)
                    .build())
            .build();
    check(syntax, new TreeBuilder(array), "(typedArray) [\n" + "    x,\n" + "    y,\n" + "],\n");
  }

  @Test
  public void testRecord() {
    final FreeAtomType primitive =
        new TypeBuilder("primitive").back(Helper.buildBackPrimitive("x")).frontMark("x").build();
    final FreeAtomType typedPrimitive =
        new TypeBuilder("typedPrimitive")
            .back(Helper.buildBackType("z", Helper.buildBackPrimitive("x")))
            .frontMark("x")
            .build();
    final FreeAtomType doublePrimitive =
        new TypeBuilder("doublePrimitive")
            .back(Helper.buildBackPrimitive("x"))
            .back(Helper.buildBackPrimitive("y"))
            .frontMark("x")
            .build();
    final FreeAtomType array =
        new TypeBuilder("array")
            .back(
                Helper.buildBackType(
                    "typedArray",
                    new BackArrayBuilder()
                        .add(Helper.buildBackPrimitive("x"))
                        .add(Helper.buildBackPrimitive("y"))
                        .build()))
            .frontMark("x")
            .build();
    final FreeAtomType record =
        new TypeBuilder("record")
            .back(
                Helper.buildBackType(
                    "typedRecord",
                    new BackRecordBuilder()
                        .add("a", Helper.buildBackPrimitive("x"))
                        .add("b", Helper.buildBackPrimitive("y"))
                        .build()))
            .frontMark("x")
            .build();
    final FreeAtomType dataPrimitive =
        new TypeBuilder("dataPrimitive")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .build();
    final FreeAtomType dataArray =
        new TypeBuilder("dataArray")
            .back(Helper.buildBackDataArray("value", "array_value"))
            .frontDataArray("value")
            .build();
    final FreeAtomType dataRecord =
        new TypeBuilder("dataRecord")
            .back(Helper.buildBackDataRecord("value", "dataRecordElement"))
            .frontDataArray("value")
            .build();
    final FreeAtomType dataRecordElement =
        new TypeBuilder("dataRecordElement")
            .back(Helper.buildBackDataKey("key"))
            .back(Helper.buildBackDataAtom("value", "value"))
            .frontDataPrimitive("key")
            .frontDataNode("value")
            .build();
    final Syntax syntax =
        new SyntaxBuilder("array_value")
            .type(primitive)
            .type(typedPrimitive)
            .type(doublePrimitive)
            .type(array)
            .type(record)
            .type(dataPrimitive)
            .type(dataArray)
            .type(dataRecord)
            .type(dataRecordElement)
            .group(
                "array_value",
                new GroupBuilder()
                    .type(primitive)
                    .type(typedPrimitive)
                    .type(doublePrimitive)
                    .type(array)
                    .type(record)
                    .type(dataPrimitive)
                    .type(dataArray)
                    .type(dataRecord)
                    .build())
            .group(
                "value",
                new GroupBuilder()
                    .type(primitive)
                    .type(typedPrimitive)
                    .type(array)
                    .type(record)
                    .type(dataPrimitive)
                    .type(dataArray)
                    .type(dataRecord)
                    .build())
            .build();
    check(syntax, new TreeBuilder(record), "(typedRecord) {\n" +
            "    a: x,\n" +
            "    b: y,\n" +
            "},\n");
  }

  @Test
  public void testDataPrimitive() {
    final FreeAtomType primitive =
        new TypeBuilder("primitive").back(Helper.buildBackPrimitive("x")).frontMark("x").build();
    final FreeAtomType typedPrimitive =
        new TypeBuilder("typedPrimitive")
            .back(Helper.buildBackType("z", Helper.buildBackPrimitive("x")))
            .frontMark("x")
            .build();
    final FreeAtomType doublePrimitive =
        new TypeBuilder("doublePrimitive")
            .back(Helper.buildBackPrimitive("x"))
            .back(Helper.buildBackPrimitive("y"))
            .frontMark("x")
            .build();
    final FreeAtomType array =
        new TypeBuilder("array")
            .back(
                Helper.buildBackType(
                    "typedArray",
                    new BackArrayBuilder()
                        .add(Helper.buildBackPrimitive("x"))
                        .add(Helper.buildBackPrimitive("y"))
                        .build()))
            .frontMark("x")
            .build();
    final FreeAtomType record =
        new TypeBuilder("record")
            .back(
                Helper.buildBackType(
                    "typedRecord",
                    new BackRecordBuilder()
                        .add("a", Helper.buildBackPrimitive("x"))
                        .add("b", Helper.buildBackPrimitive("y"))
                        .build()))
            .frontMark("x")
            .build();
    final FreeAtomType dataPrimitive =
        new TypeBuilder("dataPrimitive")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .build();
    final FreeAtomType dataArray =
        new TypeBuilder("dataArray")
            .back(Helper.buildBackDataArray("value", "array_value"))
            .frontDataArray("value")
            .build();
    final FreeAtomType dataRecord =
        new TypeBuilder("dataRecord")
            .back(Helper.buildBackDataRecord("value", "dataRecordElement"))
            .frontDataArray("value")
            .build();
    final FreeAtomType dataRecordElement =
        new TypeBuilder("dataRecordElement")
            .back(Helper.buildBackDataKey("key"))
            .back(Helper.buildBackDataAtom("value", "value"))
            .frontDataPrimitive("key")
            .frontDataNode("value")
            .build();
    final Syntax syntax =
        new SyntaxBuilder("array_value")
            .type(primitive)
            .type(typedPrimitive)
            .type(doublePrimitive)
            .type(array)
            .type(record)
            .type(dataPrimitive)
            .type(dataArray)
            .type(dataRecord)
            .type(dataRecordElement)
            .group(
                "array_value",
                new GroupBuilder()
                    .type(primitive)
                    .type(typedPrimitive)
                    .type(doublePrimitive)
                    .type(array)
                    .type(record)
                    .type(dataPrimitive)
                    .type(dataArray)
                    .type(dataRecord)
                    .build())
            .group(
                "value",
                new GroupBuilder()
                    .type(primitive)
                    .type(typedPrimitive)
                    .type(array)
                    .type(record)
                    .type(dataPrimitive)
                    .type(dataArray)
                    .type(dataRecord)
                    .build())
            .build();
    check(syntax, new TreeBuilder(dataPrimitive).add("value", "dog"), "dog,\n");
  }

  @Test
  public void testDataArray() {
    final FreeAtomType primitive =
        new TypeBuilder("primitive").back(Helper.buildBackPrimitive("x")).frontMark("x").build();
    final FreeAtomType typedPrimitive =
        new TypeBuilder("typedPrimitive")
            .back(Helper.buildBackType("z", Helper.buildBackPrimitive("x")))
            .frontMark("x")
            .build();
    final FreeAtomType doublePrimitive =
        new TypeBuilder("doublePrimitive")
            .back(Helper.buildBackPrimitive("x"))
            .back(Helper.buildBackPrimitive("y"))
            .frontMark("x")
            .build();
    final FreeAtomType array =
        new TypeBuilder("array")
            .back(
                Helper.buildBackType(
                    "typedArray",
                    new BackArrayBuilder()
                        .add(Helper.buildBackPrimitive("x"))
                        .add(Helper.buildBackPrimitive("y"))
                        .build()))
            .frontMark("x")
            .build();
    final FreeAtomType record =
        new TypeBuilder("record")
            .back(
                Helper.buildBackType(
                    "typedRecord",
                    new BackRecordBuilder()
                        .add("a", Helper.buildBackPrimitive("x"))
                        .add("b", Helper.buildBackPrimitive("y"))
                        .build()))
            .frontMark("x")
            .build();
    final FreeAtomType dataPrimitive =
        new TypeBuilder("dataPrimitive")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .build();
    final FreeAtomType dataArray =
        new TypeBuilder("dataArray")
            .back(Helper.buildBackDataArray("value", "array_value"))
            .frontDataArray("value")
            .build();
    final FreeAtomType dataRecord =
        new TypeBuilder("dataRecord")
            .back(Helper.buildBackDataRecord("value", "dataRecordElement"))
            .frontDataArray("value")
            .build();
    final FreeAtomType dataRecordElement =
        new TypeBuilder("dataRecordElement")
            .back(Helper.buildBackDataKey("key"))
            .back(Helper.buildBackDataAtom("value", "value"))
            .frontDataPrimitive("key")
            .frontDataNode("value")
            .build();
    final Syntax syntax =
        new SyntaxBuilder("array_value")
            .type(primitive)
            .type(typedPrimitive)
            .type(doublePrimitive)
            .type(array)
            .type(record)
            .type(dataPrimitive)
            .type(dataArray)
            .type(dataRecord)
            .type(dataRecordElement)
            .group(
                "array_value",
                new GroupBuilder()
                    .type(primitive)
                    .type(typedPrimitive)
                    .type(doublePrimitive)
                    .type(array)
                    .type(record)
                    .type(dataPrimitive)
                    .type(dataArray)
                    .type(dataRecord)
                    .build())
            .group(
                "value",
                new GroupBuilder()
                    .type(primitive)
                    .type(typedPrimitive)
                    .type(array)
                    .type(record)
                    .type(dataPrimitive)
                    .type(dataArray)
                    .type(dataRecord)
                    .build())
            .build();
    check(
        syntax,
        new TreeBuilder(dataArray).addArray("value", new TreeBuilder(primitive).build()),
        "[\n" +
                "    x,\n" +
                "],\n");
  }

  @Test
  public void testDataArrayWithType() {
    final FreeAtomType primitive =
        new TypeBuilder("primitive").back(Helper.buildBackPrimitive("x")).frontMark("x").build();
    final FreeAtomType typedPrimitive =
        new TypeBuilder("typedPrimitive")
            .back(Helper.buildBackType("z", Helper.buildBackPrimitive("x")))
            .frontMark("x")
            .build();
    final FreeAtomType doublePrimitive =
        new TypeBuilder("doublePrimitive")
            .back(Helper.buildBackPrimitive("x"))
            .back(Helper.buildBackPrimitive("y"))
            .frontMark("x")
            .build();
    final FreeAtomType array =
        new TypeBuilder("array")
            .back(
                Helper.buildBackType(
                    "typedArray",
                    new BackArrayBuilder()
                        .add(Helper.buildBackPrimitive("x"))
                        .add(Helper.buildBackPrimitive("y"))
                        .build()))
            .frontMark("x")
            .build();
    final FreeAtomType record =
        new TypeBuilder("record")
            .back(
                Helper.buildBackType(
                    "typedRecord",
                    new BackRecordBuilder()
                        .add("a", Helper.buildBackPrimitive("x"))
                        .add("b", Helper.buildBackPrimitive("y"))
                        .build()))
            .frontMark("x")
            .build();
    final FreeAtomType dataPrimitive =
        new TypeBuilder("dataPrimitive")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .build();
    final FreeAtomType dataArray =
        new TypeBuilder("dataArray")
            .back(Helper.buildBackDataArray("value", "array_value"))
            .frontDataArray("value")
            .build();
    final FreeAtomType dataRecord =
        new TypeBuilder("dataRecord")
            .back(Helper.buildBackDataRecord("value", "dataRecordElement"))
            .frontDataArray("value")
            .build();
    final FreeAtomType dataRecordElement =
        new TypeBuilder("dataRecordElement")
            .back(Helper.buildBackDataKey("key"))
            .back(Helper.buildBackDataAtom("value", "value"))
            .frontDataPrimitive("key")
            .frontDataNode("value")
            .build();
    final Syntax syntax =
        new SyntaxBuilder("array_value")
            .type(primitive)
            .type(typedPrimitive)
            .type(doublePrimitive)
            .type(array)
            .type(record)
            .type(dataPrimitive)
            .type(dataArray)
            .type(dataRecord)
            .type(dataRecordElement)
            .group(
                "array_value",
                new GroupBuilder()
                    .type(primitive)
                    .type(typedPrimitive)
                    .type(doublePrimitive)
                    .type(array)
                    .type(record)
                    .type(dataPrimitive)
                    .type(dataArray)
                    .type(dataRecord)
                    .build())
            .group(
                "value",
                new GroupBuilder()
                    .type(primitive)
                    .type(typedPrimitive)
                    .type(array)
                    .type(record)
                    .type(dataPrimitive)
                    .type(dataArray)
                    .type(dataRecord)
                    .build())
            .build();
    check(
        syntax,
        new TreeBuilder(dataArray).addArray("value", new TreeBuilder(typedPrimitive).build()),
        "[\n" +
                "    (z) x,\n" +
                "],\n");
  }

  @Test
  public void testDataArrayWithTwoElements() {
    final FreeAtomType primitive =
        new TypeBuilder("primitive").back(Helper.buildBackPrimitive("x")).frontMark("x").build();
    final FreeAtomType typedPrimitive =
        new TypeBuilder("typedPrimitive")
            .back(Helper.buildBackType("z", Helper.buildBackPrimitive("x")))
            .frontMark("x")
            .build();
    final FreeAtomType doublePrimitive =
        new TypeBuilder("doublePrimitive")
            .back(Helper.buildBackPrimitive("x"))
            .back(Helper.buildBackPrimitive("y"))
            .frontMark("x")
            .build();
    final FreeAtomType array =
        new TypeBuilder("array")
            .back(
                Helper.buildBackType(
                    "typedArray",
                    new BackArrayBuilder()
                        .add(Helper.buildBackPrimitive("x"))
                        .add(Helper.buildBackPrimitive("y"))
                        .build()))
            .frontMark("x")
            .build();
    final FreeAtomType record =
        new TypeBuilder("record")
            .back(
                Helper.buildBackType(
                    "typedRecord",
                    new BackRecordBuilder()
                        .add("a", Helper.buildBackPrimitive("x"))
                        .add("b", Helper.buildBackPrimitive("y"))
                        .build()))
            .frontMark("x")
            .build();
    final FreeAtomType dataPrimitive =
        new TypeBuilder("dataPrimitive")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .build();
    final FreeAtomType dataArray =
        new TypeBuilder("dataArray")
            .back(Helper.buildBackDataArray("value", "array_value"))
            .frontDataArray("value")
            .build();
    final FreeAtomType dataRecord =
        new TypeBuilder("dataRecord")
            .back(Helper.buildBackDataRecord("value", "dataRecordElement"))
            .frontDataArray("value")
            .build();
    final FreeAtomType dataRecordElement =
        new TypeBuilder("dataRecordElement")
            .back(Helper.buildBackDataKey("key"))
            .back(Helper.buildBackDataAtom("value", "value"))
            .frontDataPrimitive("key")
            .frontDataNode("value")
            .build();
    final Syntax syntax =
        new SyntaxBuilder("array_value")
            .type(primitive)
            .type(typedPrimitive)
            .type(doublePrimitive)
            .type(array)
            .type(record)
            .type(dataPrimitive)
            .type(dataArray)
            .type(dataRecord)
            .type(dataRecordElement)
            .group(
                "array_value",
                new GroupBuilder()
                    .type(primitive)
                    .type(typedPrimitive)
                    .type(doublePrimitive)
                    .type(array)
                    .type(record)
                    .type(dataPrimitive)
                    .type(dataArray)
                    .type(dataRecord)
                    .build())
            .group(
                "value",
                new GroupBuilder()
                    .type(primitive)
                    .type(typedPrimitive)
                    .type(array)
                    .type(record)
                    .type(dataPrimitive)
                    .type(dataArray)
                    .type(dataRecord)
                    .build())
            .build();
    check(
        syntax,
        new TreeBuilder(dataArray)
            .addArray(
                "value",
                new TreeBuilder(typedPrimitive).build(),
                new TreeBuilder(primitive).build()),
        "[\n" +
                "    (z) x,\n" +
                "    x,\n" +
                "],\n");
  }

  @Test
  public void testDataArrayDouble() {
    final FreeAtomType primitive =
        new TypeBuilder("primitive").back(Helper.buildBackPrimitive("x")).frontMark("x").build();
    final FreeAtomType typedPrimitive =
        new TypeBuilder("typedPrimitive")
            .back(Helper.buildBackType("z", Helper.buildBackPrimitive("x")))
            .frontMark("x")
            .build();
    final FreeAtomType doublePrimitive =
        new TypeBuilder("doublePrimitive")
            .back(Helper.buildBackPrimitive("x"))
            .back(Helper.buildBackPrimitive("y"))
            .frontMark("x")
            .build();
    final FreeAtomType array =
        new TypeBuilder("array")
            .back(
                Helper.buildBackType(
                    "typedArray",
                    new BackArrayBuilder()
                        .add(Helper.buildBackPrimitive("x"))
                        .add(Helper.buildBackPrimitive("y"))
                        .build()))
            .frontMark("x")
            .build();
    final FreeAtomType record =
        new TypeBuilder("record")
            .back(
                Helper.buildBackType(
                    "typedRecord",
                    new BackRecordBuilder()
                        .add("a", Helper.buildBackPrimitive("x"))
                        .add("b", Helper.buildBackPrimitive("y"))
                        .build()))
            .frontMark("x")
            .build();
    final FreeAtomType dataPrimitive =
        new TypeBuilder("dataPrimitive")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .build();
    final FreeAtomType dataArray =
        new TypeBuilder("dataArray")
            .back(Helper.buildBackDataArray("value", "array_value"))
            .frontDataArray("value")
            .build();
    final FreeAtomType dataRecord =
        new TypeBuilder("dataRecord")
            .back(Helper.buildBackDataRecord("value", "dataRecordElement"))
            .frontDataArray("value")
            .build();
    final FreeAtomType dataRecordElement =
        new TypeBuilder("dataRecordElement")
            .back(Helper.buildBackDataKey("key"))
            .back(Helper.buildBackDataAtom("value", "value"))
            .frontDataPrimitive("key")
            .frontDataNode("value")
            .build();
    final Syntax syntax =
        new SyntaxBuilder("array_value")
            .type(primitive)
            .type(typedPrimitive)
            .type(doublePrimitive)
            .type(array)
            .type(record)
            .type(dataPrimitive)
            .type(dataArray)
            .type(dataRecord)
            .type(dataRecordElement)
            .group(
                "array_value",
                new GroupBuilder()
                    .type(primitive)
                    .type(typedPrimitive)
                    .type(doublePrimitive)
                    .type(array)
                    .type(record)
                    .type(dataPrimitive)
                    .type(dataArray)
                    .type(dataRecord)
                    .build())
            .group(
                "value",
                new GroupBuilder()
                    .type(primitive)
                    .type(typedPrimitive)
                    .type(array)
                    .type(record)
                    .type(dataPrimitive)
                    .type(dataArray)
                    .type(dataRecord)
                    .build())
            .build();
    check(
        syntax,
        new TreeBuilder(dataArray).addArray("value", new TreeBuilder(doublePrimitive).build()),
        "[\n" +
                "    x,\n" +
                "    y,\n" +
                "],\n");
  }

  @Test
  public void testDataRecord() {
    final FreeAtomType primitive =
        new TypeBuilder("primitive").back(Helper.buildBackPrimitive("x")).frontMark("x").build();
    final FreeAtomType typedPrimitive =
        new TypeBuilder("typedPrimitive")
            .back(Helper.buildBackType("z", Helper.buildBackPrimitive("x")))
            .frontMark("x")
            .build();
    final FreeAtomType doublePrimitive =
        new TypeBuilder("doublePrimitive")
            .back(Helper.buildBackPrimitive("x"))
            .back(Helper.buildBackPrimitive("y"))
            .frontMark("x")
            .build();
    final FreeAtomType array =
        new TypeBuilder("array")
            .back(
                Helper.buildBackType(
                    "typedArray",
                    new BackArrayBuilder()
                        .add(Helper.buildBackPrimitive("x"))
                        .add(Helper.buildBackPrimitive("y"))
                        .build()))
            .frontMark("x")
            .build();
    final FreeAtomType record =
        new TypeBuilder("record")
            .back(
                Helper.buildBackType(
                    "typedRecord",
                    new BackRecordBuilder()
                        .add("a", Helper.buildBackPrimitive("x"))
                        .add("b", Helper.buildBackPrimitive("y"))
                        .build()))
            .frontMark("x")
            .build();
    final FreeAtomType dataPrimitive =
        new TypeBuilder("dataPrimitive")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .build();
    final FreeAtomType dataArray =
        new TypeBuilder("dataArray")
            .back(Helper.buildBackDataArray("value", "array_value"))
            .frontDataArray("value")
            .build();
    final FreeAtomType dataRecord =
        new TypeBuilder("dataRecord")
            .back(Helper.buildBackDataRecord("value", "dataRecordElement"))
            .frontDataArray("value")
            .build();
    final FreeAtomType dataRecordElement =
        new TypeBuilder("dataRecordElement")
            .back(Helper.buildBackDataKey("key"))
            .back(Helper.buildBackDataAtom("value", "value"))
            .frontDataPrimitive("key")
            .frontDataNode("value")
            .build();
    final Syntax syntax =
        new SyntaxBuilder("array_value")
            .type(primitive)
            .type(typedPrimitive)
            .type(doublePrimitive)
            .type(array)
            .type(record)
            .type(dataPrimitive)
            .type(dataArray)
            .type(dataRecord)
            .type(dataRecordElement)
            .group(
                "array_value",
                new GroupBuilder()
                    .type(primitive)
                    .type(typedPrimitive)
                    .type(doublePrimitive)
                    .type(array)
                    .type(record)
                    .type(dataPrimitive)
                    .type(dataArray)
                    .type(dataRecord)
                    .build())
            .group(
                "value",
                new GroupBuilder()
                    .type(primitive)
                    .type(typedPrimitive)
                    .type(array)
                    .type(record)
                    .type(dataPrimitive)
                    .type(dataArray)
                    .type(dataRecord)
                    .build())
            .build();
    check(
        syntax,
        new TreeBuilder(dataRecord)
            .addArray(
                "value",
                new TreeBuilder(dataRecordElement)
                    .add("key", "cat")
                    .add("value", new TreeBuilder(primitive).build())
                    .build()),
        "{\n" +
                "    cat: x,\n" +
                "},\n");
  }

  @Test
  public void testDataRecordWithType() {
    final FreeAtomType primitive =
        new TypeBuilder("primitive").back(Helper.buildBackPrimitive("x")).frontMark("x").build();
    final FreeAtomType typedPrimitive =
        new TypeBuilder("typedPrimitive")
            .back(Helper.buildBackType("z", Helper.buildBackPrimitive("x")))
            .frontMark("x")
            .build();
    final FreeAtomType doublePrimitive =
        new TypeBuilder("doublePrimitive")
            .back(Helper.buildBackPrimitive("x"))
            .back(Helper.buildBackPrimitive("y"))
            .frontMark("x")
            .build();
    final FreeAtomType array =
        new TypeBuilder("array")
            .back(
                Helper.buildBackType(
                    "typedArray",
                    new BackArrayBuilder()
                        .add(Helper.buildBackPrimitive("x"))
                        .add(Helper.buildBackPrimitive("y"))
                        .build()))
            .frontMark("x")
            .build();
    final FreeAtomType record =
        new TypeBuilder("record")
            .back(
                Helper.buildBackType(
                    "typedRecord",
                    new BackRecordBuilder()
                        .add("a", Helper.buildBackPrimitive("x"))
                        .add("b", Helper.buildBackPrimitive("y"))
                        .build()))
            .frontMark("x")
            .build();
    final FreeAtomType dataPrimitive =
        new TypeBuilder("dataPrimitive")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .build();
    final FreeAtomType dataArray =
        new TypeBuilder("dataArray")
            .back(Helper.buildBackDataArray("value", "array_value"))
            .frontDataArray("value")
            .build();
    final FreeAtomType dataRecord =
        new TypeBuilder("dataRecord")
            .back(Helper.buildBackDataRecord("value", "dataRecordElement"))
            .frontDataArray("value")
            .build();
    final FreeAtomType dataRecordElement =
        new TypeBuilder("dataRecordElement")
            .back(Helper.buildBackDataKey("key"))
            .back(Helper.buildBackDataAtom("value", "value"))
            .frontDataPrimitive("key")
            .frontDataNode("value")
            .build();
    final Syntax syntax =
        new SyntaxBuilder("array_value")
            .type(primitive)
            .type(typedPrimitive)
            .type(doublePrimitive)
            .type(array)
            .type(record)
            .type(dataPrimitive)
            .type(dataArray)
            .type(dataRecord)
            .type(dataRecordElement)
            .group(
                "array_value",
                new GroupBuilder()
                    .type(primitive)
                    .type(typedPrimitive)
                    .type(doublePrimitive)
                    .type(array)
                    .type(record)
                    .type(dataPrimitive)
                    .type(dataArray)
                    .type(dataRecord)
                    .build())
            .group(
                "value",
                new GroupBuilder()
                    .type(primitive)
                    .type(typedPrimitive)
                    .type(array)
                    .type(record)
                    .type(dataPrimitive)
                    .type(dataArray)
                    .type(dataRecord)
                    .build())
            .build();
    check(
        syntax,
        new TreeBuilder(dataRecord)
            .addArray(
                "value",
                new TreeBuilder(dataRecordElement)
                    .add("key", "cat")
                    .add("value", new TreeBuilder(typedPrimitive).build())
                    .build()),
        "{\n" + "    cat: (z) x,\n" + "},\n");
  }

  @Test
  public void testDataRecordWithTwoElements() {
    final FreeAtomType primitive =
        new TypeBuilder("primitive").back(Helper.buildBackPrimitive("x")).frontMark("x").build();
    final FreeAtomType typedPrimitive =
        new TypeBuilder("typedPrimitive")
            .back(Helper.buildBackType("z", Helper.buildBackPrimitive("x")))
            .frontMark("x")
            .build();
    final FreeAtomType doublePrimitive =
        new TypeBuilder("doublePrimitive")
            .back(Helper.buildBackPrimitive("x"))
            .back(Helper.buildBackPrimitive("y"))
            .frontMark("x")
            .build();
    final FreeAtomType array =
        new TypeBuilder("array")
            .back(
                Helper.buildBackType(
                    "typedArray",
                    new BackArrayBuilder()
                        .add(Helper.buildBackPrimitive("x"))
                        .add(Helper.buildBackPrimitive("y"))
                        .build()))
            .frontMark("x")
            .build();
    final FreeAtomType record =
        new TypeBuilder("record")
            .back(
                Helper.buildBackType(
                    "typedRecord",
                    new BackRecordBuilder()
                        .add("a", Helper.buildBackPrimitive("x"))
                        .add("b", Helper.buildBackPrimitive("y"))
                        .build()))
            .frontMark("x")
            .build();
    final FreeAtomType dataPrimitive =
        new TypeBuilder("dataPrimitive")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .build();
    final FreeAtomType dataArray =
        new TypeBuilder("dataArray")
            .back(Helper.buildBackDataArray("value", "array_value"))
            .frontDataArray("value")
            .build();
    final FreeAtomType dataRecord =
        new TypeBuilder("dataRecord")
            .back(Helper.buildBackDataRecord("value", "dataRecordElement"))
            .frontDataArray("value")
            .build();
    final FreeAtomType dataRecordElement =
        new TypeBuilder("dataRecordElement")
            .back(Helper.buildBackDataKey("key"))
            .back(Helper.buildBackDataAtom("value", "value"))
            .frontDataPrimitive("key")
            .frontDataNode("value")
            .build();
    final Syntax syntax =
        new SyntaxBuilder("array_value")
            .type(primitive)
            .type(typedPrimitive)
            .type(doublePrimitive)
            .type(array)
            .type(record)
            .type(dataPrimitive)
            .type(dataArray)
            .type(dataRecord)
            .type(dataRecordElement)
            .group(
                "array_value",
                new GroupBuilder()
                    .type(primitive)
                    .type(typedPrimitive)
                    .type(doublePrimitive)
                    .type(array)
                    .type(record)
                    .type(dataPrimitive)
                    .type(dataArray)
                    .type(dataRecord)
                    .build())
            .group(
                "value",
                new GroupBuilder()
                    .type(primitive)
                    .type(typedPrimitive)
                    .type(array)
                    .type(record)
                    .type(dataPrimitive)
                    .type(dataArray)
                    .type(dataRecord)
                    .build())
            .build();
    check(
        syntax,
        new TreeBuilder(dataRecord)
            .addArray(
                "value",
                new TreeBuilder(dataRecordElement)
                    .add("key", "cat")
                    .add("value", new TreeBuilder(typedPrimitive).build())
                    .build(),
                new TreeBuilder(dataRecordElement)
                    .add("key", "dog")
                    .add("value", new TreeBuilder(primitive).build())
                    .build()),
        "{\n" +
                "    cat: (z) x,\n" +
                "    dog: x,\n" +
                "},\n");
  }
}
