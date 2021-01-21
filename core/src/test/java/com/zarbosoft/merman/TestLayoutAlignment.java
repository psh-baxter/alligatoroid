package com.zarbosoft.merman;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.ValuePrimitive;
import com.zarbosoft.merman.editor.visual.tags.Tags;
import com.zarbosoft.merman.editor.visual.tags.TagsChange;
import com.zarbosoft.merman.helper.BackArrayBuilder;
import com.zarbosoft.merman.helper.FrontDataArrayBuilder;
import com.zarbosoft.merman.helper.FrontDataPrimitiveBuilder;
import com.zarbosoft.merman.helper.FrontMarkBuilder;
import com.zarbosoft.merman.helper.FrontSpaceBuilder;
import com.zarbosoft.merman.helper.GroupBuilder;
import com.zarbosoft.merman.helper.Helper;
import com.zarbosoft.merman.helper.StyleBuilder;
import com.zarbosoft.merman.helper.SyntaxBuilder;
import com.zarbosoft.merman.helper.TreeBuilder;
import com.zarbosoft.merman.helper.TypeBuilder;
import com.zarbosoft.merman.syntax.FreeAtomType;
import com.zarbosoft.merman.syntax.Syntax;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

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
  private final int layBrickBatchSize;

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
            .style(new StyleBuilder().tag("split").split(true).build())
            .style(
                new StyleBuilder().tag("compact_split").tag(Tags.TAG_COMPACT).split(true).build())
            .style(new StyleBuilder().tag("absolute").alignment("absolute").build())
            .style(new StyleBuilder().tag("concensus1").alignment("concensus1").build())
            .style(
                new StyleBuilder()
                    .tag("concensus1unsplit")
                    .notag(Tags.TAG_COMPACT)
                    .alignment("concensus1")
                    .build())
            .style(new StyleBuilder().tag("concensus2").alignment("concensus2").build())
            .style(
                new StyleBuilder()
                    .tag("concensus2unsplit")
                    .notag(Tags.TAG_COMPACT)
                    .alignment("concensus2")
                    .build())
            .style(new StyleBuilder().tag("relative").alignment("relative").build())
            .build();
    this.layBrickBatchSize = layBrickBatchSize;
  }

  public class GeneralTestWizard extends com.zarbosoft.merman.helper.GeneralTestWizard {
    public GeneralTestWizard(Syntax syntax, Atom... atoms) {
      super(syntax, atoms);
      this.inner.context.layBrickBatchSize = layBrickBatchSize;
    }
  }

  @Parameterized.Parameters
  public static Iterable<Object[]> parameters() {
    return ImmutableList.of(new Object[] {1}, new Object[] {2}, new Object[] {10});
  }

  @Test
  public void testRootAbsoluteAlignment() {
    new GeneralTestWizard(syntax, new TreeBuilder(absolute).add("value", "hi").build())
        .checkBrick(0, 1, 7);
  }

  @Test
  public void testRootRelativeAlignment() {
    new GeneralTestWizard(syntax, new TreeBuilder(relative).add("value", "hi").build())
        .checkBrick(0, 1, 3);
  }

  @Test
  public void testAbsoluteAlignment() {
    new GeneralTestWizard(
            syntax,
            new TreeBuilder(array)
                .addArray("value", new TreeBuilder(absolute).add("value", "hi").build())
                .build())
        .checkBrick(1, 1, 7);
  }

  @Test
  public void testRelativeAlignment() {
    new GeneralTestWizard(
            syntax,
            new TreeBuilder(array)
                .addArray("value", new TreeBuilder(relative).add("value", "hi").build())
                .build())
        .checkBrick(1, 1, 6);
  }

  @Test
  public void testConcensusAlignmentSingle() {
    new GeneralTestWizard(
            syntax, new TreeBuilder(pair).add("first", "three").add("second", "lumbar").build())
        .checkBrick(0, 2, 50);
  }

  @Test
  public void testConcensusAlignmentMultiple() {
    new GeneralTestWizard(
            syntax,
            new TreeBuilder(pair).add("first", "three").add("second", "lumbar").build(),
            new TreeBuilder(pair).add("first", "elephant").add("second", "minx").build(),
            new TreeBuilder(pair).add("first", "tag").add("second", "peanut").build())
        .checkBrick(0, 2, 80)
        .checkBrick(1, 2, 80)
        .checkBrick(2, 2, 80);
  }

  @Test
  public void testDoubleConcensusAlignmentMultiple() {
    new GeneralTestWizard(
            syntax,
            new TreeBuilder(triple)
                .add("first", "three")
                .add("second", "lumbar")
                .add("third", "a")
                .build(),
            new TreeBuilder(triple)
                .add("first", "elephant")
                .add("second", "minx")
                .add("third", "b")
                .build(),
            new TreeBuilder(triple)
                .add("first", "tag")
                .add("second", "pedantic")
                .add("third", "c")
                .build())
        .checkBrick(0, 3, 160)
        .checkBrick(1, 3, 160)
        .checkBrick(2, 3, 160);
  }

  @Test
  public void testConcensusSameLine() {
    new GeneralTestWizard(
            syntax,
            new TreeBuilder(line)
                .addArray(
                    "value",
                    new TreeBuilder(pair).add("first", "a").add("second", "b").build(),
                    new TreeBuilder(pair).add("first", "").add("second", "d").build())
                .build())
        .checkBrick(0, 2, 10)
        .checkBrick(0, 4, 20);
  }

  @Test
  public void testConcensusExpand() {
    // It's okay if it doesn't expand when there's a concensus involved until much wider
    new GeneralTestWizard(
            syntax,
            new TreeBuilder(compactArray)
                .addArray(
                    "value",
                    new TreeBuilder(pair).add("first", "a").add("second", "b").build(),
                    new TreeBuilder(pair).add("first", "cccc").add("second", "d").build())
                .build())
        .resize(60)
        .checkCourseCount(3)
        .checkTextBrick(1, 1, "a")
        .checkTextBrick(2, 1, "cccc")
        .resize(70)
        .checkCourseCount(3)
        .resize(95)
        .checkCourseCount(3)
        .resize(100)
        .checkCourseCount(1);
  }

  @Test
  public void testConcensusBreak() {
    // The concensus value lingers after breaking, so an element that goes offscreen will stay
    // offscreen at first placement
    // Then the concensus resets greatly reducing the line length, triggers expand -> endless loop
    new GeneralTestWizard(
            syntax,
            new TreeBuilder(compactArray)
                .addArray(
                    "value",
                    new TreeBuilder(primitive).add("value", "one").build(),
                    new TreeBuilder(pair).add("first", "two").add("second", "three").build())
                .build())
        .checkCourseCount(1)
        .resize(80)
        .checkCourseCount(3)
        .checkTextBrick(1, 1, "one")
        .checkTextBrick(2, 1, "two")
        .resize(10000)
        .checkCourseCount(1);
  }

  @Test
  public void testConcensusBreak2() {
    // Accidentally found an issue where the primitive doesn't get tagged compact
    new GeneralTestWizard(
            syntax,
            new TreeBuilder(pair).add("first", "lumberpass").add("second", "ink").build(),
            new TreeBuilder(splitPair).add("first", "dog").add("second", "equifortress").build())
        .checkCourseCount(2)
        .resize(200)
        .checkCourseCount(3)
        .checkTextBrick(0, 1, "lumberpass")
        .checkTextBrick(0, 2, "ink")
        .checkTextBrick(1, 1, "dog")
        .checkTextBrick(2, 0, "equifortress")
        .resize(10000)
        .checkCourseCount(2);
  }

  @Test
  public void testConcensusBreak3() {
    new GeneralTestWizard(
            syntax,
            new TreeBuilder(pair).add("first", "lumberpass").add("second", "ink").build(),
            new TreeBuilder(atomPair)
                .add("first", new TreeBuilder(primitive).add("value", "dog").build())
                .add("second", new TreeBuilder(primitive).add("value", "equifortress").build())
                .build())
        .checkCourseCount(2)
        .resize(200)
        .resize(205)
        .checkCourseCount(3)
        .checkTextBrick(0, 1, "lumberpass")
        .checkTextBrick(0, 2, "ink")
        .checkTextBrick(1, 2, "dog")
        .checkTextBrick(2, 1, "equifortress");
  }

  @Test
  public void testMultiCourseConcensusLoop() {
    new GeneralTestWizard(
            syntax,
            new TreeBuilder(triple)
                .add("first", "hower")
                .add("second", "tuber")
                .add("third", "breem")
                .build(),
            new TreeBuilder(reverseTriple)
                .add("first", "ank")
                .add("second", "reindeerkick")
                .add("third", "whatever")
                .build())
        .checkBrick(0, 2, 50)
        .checkBrick(0, 3, 100)
        .checkBrick(1, 2, 100)
        .checkBrick(1, 3, 220);
  }

  @Test
  public void testPartiallyExpandConsecutiveLines() {
    new GeneralTestWizard(syntax, new TreeBuilder(threeLine2).build())
        .resize(50)
        .resize(120)
        .checkCourseCount(2);
  }

  @Test
  public void testSplitMultiCourseStackedAlignments() {
    new GeneralTestWizard(syntax, new TreeBuilder(threeLine).build())
        .resize(160)
        .resize(170)
        .checkCourseCount(4);
  }

  @Test
  public void testDisabledConcensusSplit2() {
    final Atom pairAtom1 =
        new TreeBuilder(pair).add("first", "gmippii").add("second", "mmm").build();
    new GeneralTestWizard(
            syntax,
            new TreeBuilder(line)
                .addArray(
                    "value",
                    pairAtom1,
                    new TreeBuilder(pair).add("first", "mo").add("second", "oo").build())
                .build())
        .checkCourseCount(1)
        .run(context -> pairAtom1.changeTags(context, TagsChange.add("split")))
        .checkCourseCount(2);
  }
}
