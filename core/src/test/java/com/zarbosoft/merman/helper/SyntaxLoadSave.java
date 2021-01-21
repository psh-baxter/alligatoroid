package com.zarbosoft.merman.helper;

import com.zarbosoft.merman.syntax.FreeAtomType;
import com.zarbosoft.merman.syntax.Syntax;

public class SyntaxLoadSave {
  public static final FreeAtomType primitive;
  public static final FreeAtomType typedPrimitive;
  public static final FreeAtomType doublePrimitive;
  public static final FreeAtomType array;
  public static final FreeAtomType record;
  public static final FreeAtomType dataPrimitive;
  public static final FreeAtomType dataArray;
  public static final FreeAtomType dataRecord;
  public static final FreeAtomType dataRecordElement;
  public static final Syntax syntax;

  static {
    primitive =
        new TypeBuilder("primitive").back(Helper.buildBackPrimitive("x")).frontMark("x").build();
    typedPrimitive =
        new TypeBuilder("typedPrimitive")
            .back(Helper.buildBackType("z", Helper.buildBackPrimitive("x")))
            .frontMark("x")
            .build();
    doublePrimitive =
        new TypeBuilder("doublePrimitive")
            .back(Helper.buildBackPrimitive("x"))
            .back(Helper.buildBackPrimitive("y"))
            .frontMark("x")
            .build();
    array =
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
    record =
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
    dataPrimitive =
        new TypeBuilder("dataPrimitive")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .build();
    dataArray =
        new TypeBuilder("dataArray")
            .back(Helper.buildBackDataArray("value", "array_value"))
            .frontDataArray("value")
            .build();
    dataRecord =
        new TypeBuilder("dataRecord")
            .back(Helper.buildBackDataRecord("value", "dataRecordElement"))
            .frontDataArray("value")
            .build();
    dataRecordElement =
        new TypeBuilder("dataRecordElement")
            .back(Helper.buildBackDataKey("key"))
            .back(Helper.buildBackDataAtom("value", "value"))
            .frontDataPrimitive("key")
            .frontDataNode("value")
            .build();
    syntax =
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


    {
      final FreeAtomType primitive = new TypeBuilder("primitive").back(Helper.buildBackPrimitive("x")).frontMark("x").build();
      final FreeAtomType typedPrimitive = new TypeBuilder("typedPrimitive")
              .back(Helper.buildBackType("z", Helper.buildBackPrimitive("x")))
              .frontMark("x")
              .build();
      final FreeAtomType doublePrimitive = new TypeBuilder("doublePrimitive")
              .back(Helper.buildBackPrimitive("x"))
              .back(Helper.buildBackPrimitive("y"))
              .frontMark("x")
              .build();
      final FreeAtomType array = new TypeBuilder("array")
              .back(
                      Helper.buildBackType(
                              "typedArray",
                              new BackArrayBuilder()
                                      .add(Helper.buildBackPrimitive("x"))
                                      .add(Helper.buildBackPrimitive("y"))
                                      .build()))
              .frontMark("x")
              .build();
      final FreeAtomType record = new TypeBuilder("record")
              .back(
                      Helper.buildBackType(
                              "typedRecord",
                              new BackRecordBuilder()
                                      .add("a", Helper.buildBackPrimitive("x"))
                                      .add("b", Helper.buildBackPrimitive("y"))
                                      .build()))
              .frontMark("x")
              .build();
      final FreeAtomType dataPrimitive = new TypeBuilder("dataPrimitive")
              .back(Helper.buildBackDataPrimitive("value"))
              .frontDataPrimitive("value")
              .build();
      final FreeAtomType dataArray = new TypeBuilder("dataArray")
              .back(Helper.buildBackDataArray("value", "array_value"))
              .frontDataArray("value")
              .build();
      final FreeAtomType dataRecord = new TypeBuilder("dataRecord")
              .back(Helper.buildBackDataRecord("value", "dataRecordElement"))
              .frontDataArray("value")
              .build();
      final FreeAtomType dataRecordElement = new TypeBuilder("dataRecordElement")
              .back(Helper.buildBackDataKey("key"))
              .back(Helper.buildBackDataAtom("value", "value"))
              .frontDataPrimitive("key")
              .frontDataNode("value")
              .build();
      final Syntax syntax = new SyntaxBuilder("array_value")
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
    }
  }
}
