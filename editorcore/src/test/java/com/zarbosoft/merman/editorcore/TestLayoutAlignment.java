package com.zarbosoft.merman.editorcore;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.FieldArray;
import com.zarbosoft.merman.document.values.FieldPrimitive;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.visual.Visual;
import com.zarbosoft.merman.editor.visual.tags.FreeTag;
import com.zarbosoft.merman.editor.visual.tags.StateTag;
import com.zarbosoft.merman.editor.visual.tags.TagsChange;
import com.zarbosoft.merman.editor.visual.tags.TypeTag;
import com.zarbosoft.merman.editorcore.helper.BackArrayBuilder;
import com.zarbosoft.merman.editorcore.helper.FrontDataArrayBuilder;
import com.zarbosoft.merman.editorcore.helper.FrontMarkBuilder;
import com.zarbosoft.merman.editorcore.helper.GeneralTestWizard;
import com.zarbosoft.merman.editorcore.helper.GroupBuilder;
import com.zarbosoft.merman.editorcore.helper.Helper;
import com.zarbosoft.merman.editorcore.helper.SyntaxBuilder;
import com.zarbosoft.merman.editorcore.helper.TreeBuilder;
import com.zarbosoft.merman.editorcore.helper.TypeBuilder;
import com.zarbosoft.merman.editorcore.history.changes.ChangeArray;
import com.zarbosoft.merman.editorcore.history.changes.ChangePrimitiveAdd;
import com.zarbosoft.merman.editorcore.history.changes.ChangePrimitiveRemove;
import com.zarbosoft.merman.syntax.FreeAtomType;
import com.zarbosoft.merman.syntax.Syntax;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.function.Function;

@RunWith(Parameterized.class)
public class TestLayoutAlignment {

  public final FreeAtomType primitive;
  public final FreeAtomType relative;
  public final FreeAtomType absolute;
  public final FreeAtomType array;
  public final FreeAtomType compactArray;
  public final FreeAtomType line;
  public final FreeAtomType pair;
  public final FreeAtomType atomPair;
  public final FreeAtomType splitPair;
  public final FreeAtomType triple;
  public final FreeAtomType reverseTriple;
  public final FreeAtomType threeLine;
  public final FreeAtomType threeLine2;
  public final Syntax syntax;

  public TestLayoutAlignment(final int layBrickBatchSize) {
    primitive =
        new TypeBuilder("primitive")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .build();
    relative =
        new TypeBuilder("relative")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .build();
    absolute =
        new TypeBuilder("absolute")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .build();
    array =
        new TypeBuilder("array")
            .back(Helper.buildBackDataArray("value", "any"))
            .front(
                new FrontDataArrayBuilder("value")
                    .addPrefix(new FrontSpaceBuilder().tag("split").build())
                    .build())
            .absoluteAlignment("absolute", 7)
            .relativeAlignment("relative", "relative", 3)
            .build();
    compactArray =
        new TypeBuilder("compactArray")
            .back(Helper.buildBackDataArray("value", "any"))
            .front(
                new FrontDataArrayBuilder("value")
                    .addPrefix(new FrontSpaceBuilder().tag("compact_split").build())
                    .build())
            .build();
    line =
        new TypeBuilder("line")
            .back(Helper.buildBackDataArray("value", "any"))
            .front(new FrontDataArrayBuilder("value").build())
            .build();
    pair =
        new TypeBuilder("pair")
            .back(
                new BackArrayBuilder()
                    .add(Helper.buildBackDataPrimitive("first"))
                    .add(Helper.buildBackDataPrimitive("second"))
                    .build())
            .front(new FrontDataPrimitiveBuilder("first").build())
            .front(new FrontDataPrimitiveBuilder("second").tag("concensus1").build())
            .build();
    atomPair =
        new TypeBuilder("atomPair")
            .back(
                new BackArrayBuilder()
                    .add(Helper.buildBackDataAtom("first", "any"))
                    .add(Helper.buildBackDataAtom("second", "any"))
                    .build())
            .front(new FrontSpaceBuilder().build())
            .frontDataNode("first")
            .front(new FrontSpaceBuilder().tag("concensus1unsplit").tag("compact_split").build())
            .frontDataNode("second")
            .build();
    splitPair =
        new TypeBuilder("splitPair")
            .back(
                new BackArrayBuilder()
                    .add(Helper.buildBackDataPrimitive("first"))
                    .add(Helper.buildBackDataPrimitive("second"))
                    .build())
            .front(new FrontDataPrimitiveBuilder("first").build())
            .front(
                new FrontDataPrimitiveBuilder("second")
                    .tag("concensus1unsplit")
                    .tag("compact_split")
                    .build())
            .build();
    triple =
        new TypeBuilder("triple")
            .back(
                new BackArrayBuilder()
                    .add(Helper.buildBackDataPrimitive("first"))
                    .add(Helper.buildBackDataPrimitive("second"))
                    .add(Helper.buildBackDataPrimitive("third"))
                    .build())
            .front(new FrontDataPrimitiveBuilder("first").build())
            .front(new FrontDataPrimitiveBuilder("second").tag("concensus1").build())
            .front(new FrontDataPrimitiveBuilder("third").tag("concensus2").build())
            .build();
    reverseTriple =
        new TypeBuilder("reverseTriple")
            .back(
                new BackArrayBuilder()
                    .add(Helper.buildBackDataPrimitive("first"))
                    .add(Helper.buildBackDataPrimitive("second"))
                    .add(Helper.buildBackDataPrimitive("third"))
                    .build())
            .front(new FrontDataPrimitiveBuilder("first").build())
            .front(new FrontDataPrimitiveBuilder("second").tag("concensus2").build())
            .front(new FrontDataPrimitiveBuilder("third").tag("concensus1").build())
            .build();
    threeLine =
        new TypeBuilder("threeLine")
            .back(Helper.buildBackPrimitive("threeLine"))
            /* Line 1 */
            .front(new FrontMarkBuilder("width2").tag("concensus1unsplit").build())
            .front(new FrontMarkBuilder("b").tag("concensus2").build())
            /* Line 2 */
            .front(new FrontSpaceBuilder().tag("split").build())
            .front(
                new FrontMarkBuilder("width3")
                    .tag("compact_split")
                    .tag("concensus2unsplit")
                    .build())
            /* Line 3 */
            .front(new FrontMarkBuilder("width1").tag("split").build())
            .front(new FrontMarkBuilder("a").tag("concensus1").build())
            .build();
    threeLine2 =
        new TypeBuilder("threeLine2")
            .back(Helper.buildBackPrimitive("threeLine"))
            .front(new FrontMarkBuilder("line1").build())
            .front(new FrontMarkBuilder("line2").tag("split").build())
            .front(new FrontMarkBuilder("line3").tag("compact_split").build())
            .build();
    syntax =
        new SyntaxBuilder("any")
            .type(primitive)
            .type(absolute)
            .type(relative)
            .type(array)
            .type(compactArray)
            .type(line)
            .type(pair)
            .type(atomPair)
            .type(splitPair)
            .type(triple)
            .type(reverseTriple)
            .type(threeLine)
            .type(threeLine2)
            .group(
                "any",
                new GroupBuilder()
                    .type(primitive)
                    .type(absolute)
                    .type(relative)
                    .type(array)
                    .type(compactArray)
                    .type(line)
                    .type(pair)
                    .type(atomPair)
                    .type(splitPair)
                    .type(triple)
                    .type(reverseTriple)
                    .type(threeLine)
                    .type(threeLine2)
                    .build())
            .absoluteAlignment("absolute", 7)
            .relativeAlignment("relative", 3)
            .concensusAlignment("concensus1")
            .concensusAlignment("concensus2")
            .addRootFrontPrefix(new FrontSpaceBuilder().tag("split").build())
            .style(new StyleBuilder().tag(new FreeTag("split")).split(true).build())
            .style(
                new StyleBuilder()
                    .tag(new FreeTag("compact_split"))
                    .tag(new StateTag("compact"))
                    .split(true)
                    .build())
            .style(new StyleBuilder().tag(new TypeTag("absolute")).alignment("absolute").build())
            .style(
                new StyleBuilder().tag(new FreeTag("concensus1")).alignment("concensus1").build())
            .style(
                new StyleBuilder()
                    .tag(new FreeTag("concensus1unsplit"))
                    .notag(new StateTag("compact"))
                    .alignment("concensus1")
                    .build())
            .style(
                new StyleBuilder().tag(new FreeTag("concensus2")).alignment("concensus2").build())
            .style(
                new StyleBuilder()
                    .tag(new FreeTag("concensus2unsplit"))
                    .notag(new StateTag("compact"))
                    .alignment("concensus2")
                    .build())
            .style(new StyleBuilder().tag(new TypeTag("relative")).alignment("relative").build())
            .build();
    syntax.layBrickBatchSize = layBrickBatchSize;
  }

  @Parameterized.Parameters
  public static Iterable<Object[]> parameters() {
    return ImmutableList.of(new Object[] {1}, new Object[] {2}, new Object[] {10});
  }

  @Test
  public void testDynamicSecondShiftOut() {
    final Atom line2 = new TreeBuilder(pair).add("first", "c").add("second", "d").build();
    final FieldPrimitive line2_1 = (FieldPrimitive) line2.fields.getOpt("first");
    new GeneralTestWizard(
            syntax, new TreeBuilder(pair).add("first", "a").add("second", "b").build(), line2)
        .run(context -> context.history.apply(context, new ChangePrimitiveAdd(line2_1, 1, "cc")))
        .checkBrick(0, 2, 30);
  }

  @Test
  public void testDynamicFirstShiftOut() {
    final Atom line = new TreeBuilder(pair).add("first", "a").add("second", "b").build();
    final FieldPrimitive text = (FieldPrimitive) line.fields.getOpt("first");
    new GeneralTestWizard(
            syntax, line, new TreeBuilder(pair).add("first", "c").add("second", "d").build())
        .run(context -> context.history.apply(context, new ChangePrimitiveAdd(text, 1, "aa")))
        .checkBrick(0, 2, 30);
  }

  @Test
  public void testDynamicShiftIn() {
    final Atom line2 = new TreeBuilder(pair).add("first", "ccccc").add("second", "d").build();
    final FieldPrimitive line2_1 = (FieldPrimitive) line2.fields.getOpt("first");
    new GeneralTestWizard(
            syntax, new TreeBuilder(pair).add("first", "a").add("second", "b").build(), line2)
        .run(context -> context.history.apply(context, new ChangePrimitiveRemove(line2_1, 1, 4)))
        .checkBrick(0, 2, 10);
  }

  @Test
  public void testDynamicAddLine() {
    new GeneralTestWizard(
            syntax, new TreeBuilder(pair).add("first", "a").add("second", "b").build())
        .run(
            context ->
                context.history.apply(
                    context,
                    new ChangeArray(
                        Helper.rootArray(context.document),
                        1,
                        0,
                        ImmutableList.of(
                            new TreeBuilder(pair).add("first", "ccc").add("second", "d").build()))))
        .checkBrick(0, 2, 30);
  }

  @Test
  public void testDynamicRemoveLine() {
    new GeneralTestWizard(
            syntax,
            new TreeBuilder(pair).add("first", "a").add("second", "b").build(),
            new TreeBuilder(pair).add("first", "ccc").add("second", "d").build())
        .run(
            context ->
                context.history.apply(
                    context,
                    new ChangeArray(Helper.rootArray(context.document), 1, 1, ImmutableList.of())))
        .checkBrick(0, 2, 10);
  }

  @Test
  public void testConcensusSameLineDynamicAdd() {
    final Atom line2 = new TreeBuilder(pair).add("first", "").add("second", "d").build();
    final FieldPrimitive line2_1 = (FieldPrimitive) line2.fields.getOpt("first");
    new GeneralTestWizard(
            syntax,
            new TreeBuilder(line)
                .addArray(
                    "value",
                    new TreeBuilder(pair).add("first", "a").add("second", "b").build(),
                    line2)
                .build())
        .run(context -> context.history.apply(context, new ChangePrimitiveAdd(line2_1, 0, "cc")))
        .checkBrick(0, 2, 10)
        .checkBrick(0, 4, 40);
  }

  @Test
  public void testConcensusSameLineDynamicAddPairBefore() {
    final Atom line2 =
        new TreeBuilder(line)
            .addArray("value", new TreeBuilder(pair).add("first", "ccc").add("second", "d").build())
            .build();
    final FieldArray array = (FieldArray) line2.fields.getOpt("value");
    new GeneralTestWizard(syntax, line2)
        .run(
            context ->
                context.history.apply(
                    context,
                    new ChangeArray(
                        array,
                        0,
                        0,
                        ImmutableList.of(
                            new TreeBuilder(pair).add("first", "a").add("second", "b").build()))))
        .checkBrick(0, 2, 10)
        .checkBrick(0, 4, 50);
  }

  @Test
  public void testConcensusSameLineDynamicRemove() {
    final Atom line2 = new TreeBuilder(pair).add("first", "cc").add("second", "d").build();
    final FieldPrimitive line2_1 = (FieldPrimitive) line2.fields.getOpt("first");
    new GeneralTestWizard(
            syntax,
            new TreeBuilder(line)
                .addArray(
                    "value",
                    new TreeBuilder(pair).add("first", "a").add("second", "b").build(),
                    line2)
                .build())
        .run(context -> context.history.apply(context, new ChangePrimitiveRemove(line2_1, 0, 2)))
        .checkBrick(0, 2, 10)
        .checkBrick(0, 4, 20);
  }

  @Test
  public void testDynamicConcensusSplitAdjust() {
    final Function<Context, Visual> line2Visual =
        context -> Helper.rootArray(context.document).data.get(1).visual.parent().visual();
    final Atom pair1 = new TreeBuilder(pair).add("first", "12345678").add("second", "x").build();
    new GeneralTestWizard(
            syntax, pair1, new TreeBuilder(pair).add("first", "1").add("second", "y").build())
        .checkCourseCount(2)
        .checkBrick(0, 2, 80)
        .checkBrick(1, 2, 80)
        .run(
            context ->
                line2Visual
                    .apply(context)
                    .changeTags(context, new TagsChange(add, remove).remove(new FreeTag("split"))))
        .checkCourseCount(1)
        .run(
            context ->
                line2Visual
                    .apply(context)
                    .changeTags(context, new TagsChange(add, remove).add(new FreeTag("split"))))
        .run(
            context ->
                context.history.apply(
                    context,
                    new ChangePrimitiveAdd((FieldPrimitive) pair1.fields.getOpt("first"), 8, "9X")))
        .checkCourseCount(2)
        .checkBrick(0, 2, 100)
        .checkBrick(1, 2, 100);
  }

  @Test
  public void testDisabledConcensusSplit() {
    final Atom pairAtom1 =
        new TreeBuilder(pair).add("first", "gmippii").add("second", "mmm").build();
    final Atom lineAtom = new TreeBuilder(line).addArray("value", pairAtom1).build();
    new GeneralTestWizard(syntax, lineAtom)
        .checkCourseCount(1)
        .run(
            context ->
                context.history.apply(
                    context,
                    new ChangeArray(
                        (FieldArray) lineAtom.fields.getOpt("value"),
                        1,
                        0,
                        ImmutableList.of(
                            new TreeBuilder(pair).add("first", "mo").add("second", "oo").build(),
                            new TreeBuilder(pair).add("first", "g").add("second", "q").build()))))
        .run(
            context ->
                context.history.apply(
                    context,
                    new ChangePrimitiveAdd(
                        (FieldPrimitive) pairAtom1.fields.getOpt("first"), 7, "9X")))
        .run(
            context ->
                ((FieldPrimitive) pairAtom1.fields.getOpt("first"))
                    .visual.changeTags(context, new TagsChange(add, remove).add(new FreeTag("split"))))
        .checkCourseCount(2);
  }
}
