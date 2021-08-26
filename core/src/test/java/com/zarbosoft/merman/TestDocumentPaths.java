package com.zarbosoft.merman;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.document.fields.Field;
import com.zarbosoft.merman.core.document.fields.FieldArray;
import com.zarbosoft.merman.core.document.fields.FieldAtom;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.helper.BackArrayBuilder;
import com.zarbosoft.merman.helper.BackRecordBuilder;
import com.zarbosoft.merman.helper.Helper;
import com.zarbosoft.merman.helper.MiscSyntax;
import com.zarbosoft.merman.helper.SyntaxBuilder;
import com.zarbosoft.merman.helper.TreeBuilder;
import com.zarbosoft.merman.helper.TypeBuilder;
import com.zarbosoft.rendaw.common.TSList;
import org.junit.Test;

import static com.zarbosoft.merman.helper.Helper.buildDoc;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class TestDocumentPaths {
  @Test
  public void testRoot() {
    final Context context =
        buildDoc(
            MiscSyntax.syntax,
            new TreeBuilder(MiscSyntax.quoted).add("value", "").build(),
            new TreeBuilder(MiscSyntax.multiback).add("a", "").add("b", "").build(),
            new TreeBuilder(MiscSyntax.quoted).add("value", "").build());
    final Field field1 = Helper.rootArray(context.document).data.get(0).namedFields.get("value");
    assertThat(field1.getSyntaxPath().toList(), equalTo(TSList.of("named", "value", "0", "named", "value")));
    assertThat(context.syntaxLocate(field1.getSyntaxPath()), equalTo(field1));
    final Field field2 = Helper.rootArray(context.document).data.get(1).namedFields.get("b");
    assertThat(field2.getSyntaxPath().toList(), equalTo(TSList.of("named", "value", "1", "named","b")));
    assertThat(context.syntaxLocate(field2.getSyntaxPath()), equalTo(field2));
    final Field field3 = Helper.rootArray(context.document).data.get(2).namedFields.get("value");
    assertThat(field3.getSyntaxPath().toList(), equalTo(TSList.of("named", "value", "2", "named", "value")));
    assertThat(context.syntaxLocate(field3.getSyntaxPath()), equalTo(field3));
  }

  @Test
  public void testRecord() {
    final Syntax syntax =
        new SyntaxBuilder("any")
            .type(
                new TypeBuilder("base")
                    .back(
                        new BackRecordBuilder()
                            .add("a", Helper.buildBackDataPrimitive("a"))
                            .build())
                    .frontDataPrimitive("a")
                    .build())
            .group("any", TSList.of("base"))
            .build();
    final Context context = buildDoc(syntax, new TreeBuilder(syntax, "base").add("a", "").build());
    final Field field1 = Helper.rootArray(context.document).data.get(0).namedFields.get("a");
    assertThat(field1.getSyntaxPath().toList(), equalTo(TSList.of("named", "value", "0", "named","a")));
    assertThat(context.syntaxLocate(field1.getSyntaxPath()), equalTo(field1));
  }

  @Test
  public void testArray() {
    final Syntax syntax =
        new SyntaxBuilder("any")
            .type(
                new TypeBuilder("base")
                    .back(new BackArrayBuilder().add(Helper.buildBackDataPrimitive("a")).build())
                    .frontDataPrimitive("a")
                    .build())
            .group("any", TSList.of("base"))
            .build();
    final Context context = buildDoc(syntax, new TreeBuilder(syntax, "base").add("a", "").build());
    final Field field1 = Helper.rootArray(context.document).data.get(0).namedFields.get("a");
    assertThat(field1.getSyntaxPath().toList(), equalTo(TSList.of("named", "value", "0", "named","a")));
    assertThat(context.syntaxLocate(field1.getSyntaxPath()), equalTo(field1));
  }

  @Test
  public void testDataNode() {
    final Syntax syntax =
        new SyntaxBuilder("any")
            .type(
                new TypeBuilder("base")
                    .back(Helper.buildBackDataAtom("a", "child"))
                    .frontDataNode("a")
                    .build())
            .type(
                new TypeBuilder("child")
                    .back(Helper.buildBackDataPrimitive("b"))
                    .frontDataPrimitive("b")
                    .build())
            .group("any", TSList.of("base"))
            .build();
    final Context context =
        buildDoc(
            syntax,
            new TreeBuilder(syntax, "base")
                .add("a", new TreeBuilder(syntax, "child").add("b", ""))
                .build());
    final Field field1 =
        ((FieldAtom) Helper.rootArray(context.document).data.get(0).namedFields.get("a"))
            .data.namedFields.get("b");
    assertThat(
        field1.getSyntaxPath().toList().inner_(),
        equalTo(TSList.of("named","value", "0", "named","a", "named","b").inner_()));
    assertThat(context.syntaxLocate(field1.getSyntaxPath()), equalTo(field1));
  }

  @Test
  public void testDataArray() {
    final Syntax syntax =
        new SyntaxBuilder("any")
            .type(
                new TypeBuilder("base")
                    .back(Helper.buildBackDataArray("a", "child"))
                    .frontDataArray("a")
                    .build())
            .type(
                new TypeBuilder("child")
                    .back(Helper.buildBackDataPrimitive("b"))
                    .frontDataPrimitive("b")
                    .build())
            .group("any", TSList.of("base"))
            .build();
    final Context context =
        buildDoc(
            syntax,
            new TreeBuilder(syntax, "base")
                .addArray("a", new TreeBuilder(syntax, "child").add("b", "").build())
                .build());
    final Field field1 =
        ((FieldArray) Helper.rootArray(context.document).data.get(0).namedFields.get("a"))
            .data
            .get(0)
            .namedFields
            .get("b");
    assertThat(field1.getSyntaxPath().toList(), equalTo(TSList.of("named", "value", "0", "named","a", "0", "named","b")));
    assertThat(context.syntaxLocate(field1.getSyntaxPath()), equalTo(field1));
  }

  @Test
  public void testDataRecord() {
    final Syntax syntax =
        new SyntaxBuilder("any")
            .type(
                new TypeBuilder("base")
                    .back(Helper.buildBackDataRecord("a", "element"))
                    .frontDataArray("a")
                    .build())
            .type(
                new TypeBuilder("element")
                    .back(Helper.buildBackDataKey("k"))
                    .frontDataPrimitive("k")
                    .back(Helper.buildBackDataPrimitive("v"))
                    .frontDataPrimitive("v")
                    .build())
            .group("any", TSList.of("base"))
            .build();
    final Context context =
        buildDoc(
            syntax,
            new TreeBuilder(syntax, "base")
                .addRecord(
                    "a", new TreeBuilder(syntax, "element").add("k", "K").add("v", "V").build())
                .build());
    final Field field1 =
        ((FieldArray) Helper.rootArray(context.document).data.get(0).namedFields.get("a"))
            .data
            .get(0)
            .namedFields
            .get("v");
    assertThat(field1.getSyntaxPath().toList(), equalTo(TSList.of("named", "value", "0", "named","a", "0", "named","v")));
    assertThat(context.syntaxLocate(field1.getSyntaxPath()), equalTo(field1));
  }

  @Test
  public void testLocateRootElement() {
    final Context context =
        buildDoc(
            MiscSyntax.syntax,
            new TreeBuilder(MiscSyntax.quoted).add("value", "").build(),
            new TreeBuilder(MiscSyntax.multiback).add("a", "").add("b", "").build(),
            new TreeBuilder(MiscSyntax.quoted).add("value", "").build());
    assertThat(
        context.syntaxLocate(new SyntaxPath("named", "value", "0")),
        equalTo(Helper.rootArray(context.document).data.get(0)));
    assertThat(
        context.syntaxLocate(new SyntaxPath("named", "value", "0", "named", "value")),
        equalTo(Helper.rootArray(context.document).data.get(0).namedFields.get("value")));
    assertThat(
        context.syntaxLocate(new SyntaxPath("named", "value", "1", "named","a")),
        equalTo(Helper.rootArray(context.document).data.get(1).namedFields.get("a")));
    assertThat(
        context.syntaxLocate(new SyntaxPath("named", "value", "1", "named","b")),
        equalTo(Helper.rootArray(context.document).data.get(1).namedFields.get("b")));
    assertThat(
        context.syntaxLocate(new SyntaxPath("named", "value", "2", "named", "value")),
        equalTo(Helper.rootArray(context.document).data.get(2).namedFields.get("value")));
  }

  @Test
  public void testLocateEmpty() {
    final Context context = buildDoc(MiscSyntax.syntax, new TreeBuilder(MiscSyntax.one).build());
    assertThat(
        context.syntaxLocate(new SyntaxPath("named", "value", "0")),
        equalTo(Helper.rootArray(context.document).data.get(0)));
  }

  @Test
  public void testLocateArrayPrimitiveLong() {
    final Context context =
        buildDoc(MiscSyntax.syntax, new TreeBuilder(MiscSyntax.quoted).add("value", "x").build());
    assertThat(
        context.syntaxLocate(new SyntaxPath("named", "value", "0", "named", "value")),
        equalTo(Helper.rootArray(context.document).data.get(0).namedFields.get("value")));
  }

  @Test
  public void testLocateArrayPrimitiveShort() {
    final Context context =
        buildDoc(MiscSyntax.syntax, new TreeBuilder(MiscSyntax.quoted).add("value", "x").build());
    assertThat(
        context.syntaxLocate(new SyntaxPath("named", "value", "0")),
        equalTo(Helper.rootArray(context.document).data.get(0)));
  }

  @Test
  public void testLocateNodePrimitiveLong() {
    final Atom quoted = new TreeBuilder(MiscSyntax.quoted).add("value", "x").build();
    final Context context =
        buildDoc(
            MiscSyntax.syntax, new TreeBuilder(MiscSyntax.snooze).add("value", quoted).build());
    assertThat(
        context.syntaxLocate(new SyntaxPath("named", "value", "0", "named", "value", "named", "value")),
        equalTo(quoted.namedFields.get("value")));
  }

  @Test
  public void testLocateNodePrimitiveShort() {
    final Atom quoted = new TreeBuilder(MiscSyntax.quoted).add("value", "x").build();
    final Context context =
        buildDoc(
            MiscSyntax.syntax, new TreeBuilder(MiscSyntax.snooze).add("value", quoted).build());
    assertThat(
        context.syntaxLocate(new SyntaxPath("named", "value", "0", "named", "value")),
        equalTo(quoted.fieldParentRef.field));
  }

  @Test
  public void testLocatePrimitive() {
    final Context context =
        buildDoc(MiscSyntax.syntax, new TreeBuilder(MiscSyntax.quoted).add("value", "").build());
    assertThat(
        context.syntaxLocate(new SyntaxPath("named", "value", "0", "named", "value")),
        equalTo(Helper.rootArray(context.document).data.get(0).namedFields.get("value")));
  }

  @Test
  public void testLocateRecordNode() {
    final Context context =
        buildDoc(
            MiscSyntax.syntax,
            new TreeBuilder(MiscSyntax.plus)
                .add("first", new TreeBuilder(MiscSyntax.one))
                .add("second", new TreeBuilder(MiscSyntax.one))
                .build());
    assertThat(
        context.syntaxLocate(new SyntaxPath("named", "value", "0")),
        equalTo(Helper.rootArray(context.document).data.get(0)));
    assertThat(
        context.syntaxLocate(new SyntaxPath("named", "value", "0", "named","first")),
        equalTo(((FieldAtom) Helper.rootArray(context.document).data.get(0).namedFields.get("first"))));
    assertThat(
        context.syntaxLocate(new SyntaxPath("named", "value", "0", "named","second")),
        equalTo(((FieldAtom) Helper.rootArray(context.document).data.get(0).namedFields.get("second"))));
  }

  @Test
  public void testLocateRecordPrimitive() {
    final Context context =
        buildDoc(
            MiscSyntax.syntax,
            new TreeBuilder(MiscSyntax.ratio).add("first", "").add("second", "").build());
    assertThat(
        context.syntaxLocate(new SyntaxPath("named", "value", "0", "named","first")),
        equalTo(Helper.rootArray(context.document).data.get(0).namedFields.get("first")));
    assertThat(
        context.syntaxLocate(new SyntaxPath("named", "value", "0", "named","second")),
        equalTo(Helper.rootArray(context.document).data.get(0).namedFields.get("second")));
  }

  @Test
  public void testLocateArrayElement() {
    final Context context =
        buildDoc(
            MiscSyntax.syntax,
            new TreeBuilder(MiscSyntax.pair)
                .add("first", new TreeBuilder(MiscSyntax.one))
                .add("second", new TreeBuilder(MiscSyntax.one))
                .build());
    assertThat(
        context.syntaxLocate(new SyntaxPath("named", "value", "0", "named","first")),
        equalTo(((FieldAtom) Helper.rootArray(context.document).data.get(0).namedFields.get("first"))));
    assertThat(
        context.syntaxLocate(new SyntaxPath("named", "value", "0", "named","second")),
        equalTo(((FieldAtom) Helper.rootArray(context.document).data.get(0).namedFields.get("second"))));
  }

  @Test
  public void testLocateDataRecordElement() {
    final Context context =
        buildDoc(
            MiscSyntax.syntax,
            new TreeBuilder(MiscSyntax.record)
                .addRecord(
                    "value",
                    new TreeBuilder(MiscSyntax.recordElement)
                        .add("key", "first")
                        .add("value", new TreeBuilder(MiscSyntax.one))
                        .build(),
                    new TreeBuilder(MiscSyntax.recordElement)
                        .add("key", "second")
                        .add("value", new TreeBuilder(MiscSyntax.one))
                        .build())
                .build());
    assertThat(
        context.syntaxLocate(new SyntaxPath("named", "value", "0", "named", "value", "0", "named", "value")),
        equalTo(
            ((FieldAtom)
                ((FieldArray) Helper.rootArray(context.document).data.get(0).namedFields.get("value"))
                    .data
                    .get(0)
                    .namedFields
                    .get("value"))));
    assertThat(
        context.syntaxLocate(new SyntaxPath("named", "value", "0", "named", "value", "1", "named", "value")),
        equalTo(
            ((FieldAtom)
                ((FieldArray) Helper.rootArray(context.document).data.get(0).namedFields.get("value"))
                    .data
                    .get(1)
                    .namedFields
                    .get("value"))));
  }

  @Test
  public void testLocateDataArrayElement() {
    final Context context =
        buildDoc(
            MiscSyntax.syntax,
            new TreeBuilder(MiscSyntax.array)
                .addArray(
                    "value",
                    new TreeBuilder(MiscSyntax.one).build(),
                    new TreeBuilder(MiscSyntax.one).build())
                .build());
    assertThat(
        context.syntaxLocate(new SyntaxPath("named", "value", "0", "named", "value", "0")),
        equalTo(
            ((FieldArray) Helper.rootArray(context.document).data.get(0).namedFields.get("value"))
                .data.get(0)));
    assertThat(
        context.syntaxLocate(new SyntaxPath("named", "value", "0", "named", "value", "1")),
        equalTo(
            ((FieldArray) Helper.rootArray(context.document).data.get(0).namedFields.get("value"))
                .data.get(1)));
  }
}
