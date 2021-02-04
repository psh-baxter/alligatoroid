package com.zarbosoft.merman;

import com.zarbosoft.merman.document.Atom;
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

import java.util.Arrays;

@RunWith(Parameterized.class)
public class TestLayoutAlignment {
  private final int layBrickBatchSize;

  public TestLayoutAlignment(final int layBrickBatchSize) {
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
    return Arrays.asList(new Object[] {1}, new Object[] {2}, new Object[] {10});
  }

  /**
   * Simply check that root level relatively aligned elements have correct converse offset relative
   * to a 0 baseline (no base)
   */
  @Test
  public void testRootRelativeAlignment() {
    FreeAtomType relative =
        new TypeBuilder("relative")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .build();
    final Syntax syntax =
        new SyntaxBuilder("any")
            .type(relative)
            .group("any", new GroupBuilder().type(relative).build())
            .relativeAlignment("relative", 3)
            .style(new StyleBuilder().tag("relative").alignment("relative").build())
            .build();
    new GeneralTestWizard(syntax, new TreeBuilder(relative).add("value", "hi").build())
        .checkBrick(0, 0, 3);
  }

  /**
   * Test scoped relative alignment is used instead of root alignment and is based on parent (root)
   * alignment.
   */
  @Test
  public void testRelativeAlignment() {
    FreeAtomType relative =
        new TypeBuilder("relative")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .build();
    final FreeAtomType array =
        new TypeBuilder("array")
            .back(Helper.buildBackDataArray("value", relative.id()))
            .front(new FrontDataArrayBuilder("value").build())
            .relativeAlignment("relative", "relative", 5)
            .build();
    final Syntax syntax =
        new SyntaxBuilder(array.id())
            .type(relative)
            .type(array)
            .relativeAlignment("relative", 3)
            .style(new StyleBuilder().tag("relative").alignment("relative").build())
            .build();
    new GeneralTestWizard(
            syntax,
            new TreeBuilder(array)
                .addArray("value", new TreeBuilder(relative).add("value", "hi").build())
                .build())
        .checkBrick(0, 0, 8);
  }

  /** Test that a single concensus brick doesn't do anything (no errors) */
  @Test
  public void testConcensusAlignmentSingle() {
    final FreeAtomType pair =
        new TypeBuilder("pair")
            .back(
                new BackArrayBuilder()
                    .add(Helper.buildBackDataPrimitive("first"))
                    .add(Helper.buildBackDataPrimitive("second"))
                    .build())
            .front(new FrontDataPrimitiveBuilder("first").build())
            .front(new FrontDataPrimitiveBuilder("second").tag("concensus1").build())
            .build();
    final Syntax syntax =
        new SyntaxBuilder(pair.id())
            .type(pair)
            .concensusAlignment("concensus1")
            .style(new StyleBuilder().tag("concensus1").alignment("concensus1").build())
            .build();
    new GeneralTestWizard(
            syntax, new TreeBuilder(pair).add("first", "three").add("second", "lumbar").build())
        .checkBrick(0, 1, 50);
  }

  /**
   * Test that a brick on each line in the same concensus alignment all have the same converse
   * offset despite differently sized preceding bricks
   */
  @Test
  public void testConcensusAlignmentMultiple() {
    final FreeAtomType pair =
        new TypeBuilder("pair")
            .back(
                new BackArrayBuilder()
                    .add(Helper.buildBackDataPrimitive("first"))
                    .add(Helper.buildBackDataPrimitive("second"))
                    .build())
            .front(new FrontDataPrimitiveBuilder("first").build())
            .front(new FrontDataPrimitiveBuilder("second").tag("concensus1").build())
            .build();
    final Syntax syntax =
        new SyntaxBuilder(pair.id())
            .type(pair)
            .concensusAlignment("concensus1")
            .addRootFrontPrefix(new FrontSpaceBuilder().tag("split").build())
            .style(new StyleBuilder().tag("split").split(true).build())
            .style(new StyleBuilder().tag("concensus1").alignment("concensus1").build())
            .build();
    new GeneralTestWizard(
            syntax,
            new TreeBuilder(pair).add("first", "three").add("second", "lumbar").build(),
            new TreeBuilder(pair).add("first", "elephant").add("second", "minx").build(),
            new TreeBuilder(pair).add("first", "tag").add("second", "peanut").build())
        .checkBrick(0, 2, 80)
        .checkBrick(1, 2, 80)
        .checkBrick(2, 2, 80);
  }

  /**
   * Alignment ignored for 2 bricks on the same line in same concensus alignment. No offset since
   * there's no other lines with bricks in concensus.
   */
  @Test
  public void testConcensusSameLine() {
    String pairId = "pair";
    final FreeAtomType pair =
        new TypeBuilder(pairId)
            .back(
                new BackArrayBuilder()
                    .add(Helper.buildBackDataPrimitive("first"))
                    .add(Helper.buildBackDataPrimitive("second"))
                    .build())
            .front(new FrontDataPrimitiveBuilder("first").build())
            .front(new FrontDataPrimitiveBuilder("second").tag("concensus1").build())
            .build();
    final Syntax syntax =
        new SyntaxBuilder(pair.id())
            .type(pair)
            .concensusAlignment("concensus1")
            .style(new StyleBuilder().tag("concensus1").alignment("concensus1").build())
            .build();
    new GeneralTestWizard(
            syntax,
            new TreeBuilder(pair).add("first", "a").add("second", "b").build(),
            new TreeBuilder(pair).add("first", "").add("second", "d").build())
        .checkBrick(0, 1, 10)
        .checkBrick(0, 3, 20);
  }

  @Test
  public void testConcensusExpand() {
    // It's okay if it doesn't expand when there's a concensus involved until much wider
    final FreeAtomType pair =
        new TypeBuilder("pair")
            .back(
                new BackArrayBuilder()
                    .add(Helper.buildBackDataPrimitive("first"))
                    .add(Helper.buildBackDataPrimitive("second"))
                    .build())
            .front(new FrontDataPrimitiveBuilder("first").build())
            .front(new FrontDataPrimitiveBuilder("second").tag("concensus1").build())
            .build();
    final Syntax syntax =
        new SyntaxBuilder(pair.id())
            .type(pair)
            .concensusAlignment("concensus1")
            .addRootFrontPrefix(new FrontSpaceBuilder().tag("compact_split").build())
            .style(
                new StyleBuilder().tag("compact_split").tag(Tags.TAG_COMPACT).split(true).build())
            .style(new StyleBuilder().tag("concensus1").alignment("concensus1").build())
            .build();
    new GeneralTestWizard(
            syntax,
            new TreeBuilder(pair).add("first", "a").add("second", "b").build(),
            new TreeBuilder(pair).add("first", "cccc").add("second", "d").build())
        .checkCourseCount(1)
        .displayWidth(60)
        .checkCourseCount(2)
        .checkTextBrick(0, 1, "a")
        .checkTextBrick(1, 1, "cccc")
        .displayWidth(70)
        .checkCourseCount(2)
        .displayWidth(95)
        .checkCourseCount(2)
        .displayWidth(100)
        .checkCourseCount(1);
  }

  /**
   * The concensus value lingers after breaking, so an element that goes past the edge of the screen
   * will stay offscreen at first placement Then the concensus resets greatly reducing the line
   * length, triggers expand -> endless loop
   */
  @Test
  public void testConcensusBreak() {
    FreeAtomType primitive =
        new TypeBuilder("primitive")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .build();
    final FreeAtomType pair =
        new TypeBuilder("pair")
            .back(
                new BackArrayBuilder()
                    .add(Helper.buildBackDataPrimitive("first"))
                    .add(Helper.buildBackDataPrimitive("second"))
                    .build())
            .front(new FrontDataPrimitiveBuilder("first").build())
            .front(new FrontDataPrimitiveBuilder("second").tag("concensus1").build())
            .build();
    final Syntax syntax =
        new SyntaxBuilder("any")
            .type(primitive)
            .type(pair)
            .group("any", new GroupBuilder().type(primitive).type(pair).build())
            .concensusAlignment("concensus1")
            .addRootFrontPrefix(new FrontSpaceBuilder().tag("compact_split").build())
            .style(
                new StyleBuilder().tag("compact_split").tag(Tags.TAG_COMPACT).split(true).build())
            .style(new StyleBuilder().tag("concensus1").alignment("concensus1").build())
            .build();
    new GeneralTestWizard(
            syntax,
            new TreeBuilder(primitive).add("value", "one").build(),
            new TreeBuilder(pair).add("first", "two").add("second", "three").build())
        .checkCourseCount(1)
        .displayWidth(80)
        .checkCourseCount(2)
        .checkTextBrick(0, 1, "one")
        .checkTextBrick(1, 1, "two")
        .displayWidth(10000)
        .checkCourseCount(1);
  }

  /** Accidentally found an issue where the primitive doesn't get tagged compact */
  @Test
  public void testConcensusBreak2() {
    final FreeAtomType pair =
        new TypeBuilder("pair")
            .back(
                new BackArrayBuilder()
                    .add(Helper.buildBackDataPrimitive("first"))
                    .add(Helper.buildBackDataPrimitive("second"))
                    .build())
            .front(new FrontDataPrimitiveBuilder("first").build())
            .front(new FrontDataPrimitiveBuilder("second").tag("concensus1").build())
            .build();
    final FreeAtomType splitPair =
        new TypeBuilder("splitPair")
            .back(
                new BackArrayBuilder()
                    .add(Helper.buildBackDataPrimitive("first"))
                    .add(Helper.buildBackDataPrimitive("second"))
                    .build())
            .front(new FrontDataPrimitiveBuilder("first").build())
            .front(
                new FrontDataPrimitiveBuilder("second")
                    .tag("expandedconcensus1")
                    .tag("compact_split")
                    .build())
            .build();
    final Syntax syntax =
        new SyntaxBuilder("any")
            .type(pair)
            .type(splitPair)
            .group("any", new GroupBuilder().type(pair).type(splitPair).build())
            .concensusAlignment("concensus1")
            .addRootFrontPrefix(new FrontSpaceBuilder().tag("split").build())
            .style(new StyleBuilder().tag("split").split(true).build())
            .style(
                new StyleBuilder().tag("compact_split").tag(Tags.TAG_COMPACT).split(true).build())
            .style(new StyleBuilder().tag("concensus1").alignment("concensus1").build())
            .style(
                new StyleBuilder()
                    .tag("expandedconcensus1")
                    .notag(Tags.TAG_COMPACT)
                    .alignment("concensus1")
                    .build())
            .build();
    new GeneralTestWizard(
            syntax,
            new TreeBuilder(pair).add("first", "lumberpass").add("second", "ink").build(),
            new TreeBuilder(splitPair).add("first", "dog").add("second", "equifortress").build())
        .checkCourseCount(2)
        .displayWidth(200)
        .checkCourseCount(3)
        .checkTextBrick(0, 1, "lumberpass")
        .checkTextBrick(0, 2, "ink")
        .checkTextBrick(1, 1, "dog")
        .checkTextBrick(2, 0, "equifortress")
        .displayWidth(10000)
        .checkCourseCount(2);
  }

  @Test
  public void testConcensusBreak3() {
    FreeAtomType primitive =
        new TypeBuilder("primitive")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .build();
    final FreeAtomType pair =
        new TypeBuilder("pair")
            .back(
                new BackArrayBuilder()
                    .add(Helper.buildBackDataPrimitive("first"))
                    .add(Helper.buildBackDataPrimitive("second"))
                    .build())
            .front(new FrontDataPrimitiveBuilder("first").build())
            .front(new FrontDataPrimitiveBuilder("second").tag("concensus1").build())
            .build();
    final FreeAtomType atomPair =
        new TypeBuilder("atomPair")
            .back(
                new BackArrayBuilder()
                    .add(Helper.buildBackDataAtom("first", "any"))
                    .add(Helper.buildBackDataAtom("second", "any"))
                    .build())
            .front(new FrontSpaceBuilder().build())
            .frontDataNode("first")
            .front(new FrontSpaceBuilder().tag("expandedconcensus1").tag("compact_split").build())
            .frontDataNode("second")
            .build();
    final Syntax syntax =
        new SyntaxBuilder("any")
            .type(primitive)
            .type(pair)
            .type(atomPair)
            .group("any", new GroupBuilder().type(primitive).type(pair).type(atomPair).build())
            .concensusAlignment("concensus1")
            .concensusAlignment("concensus2")
            .addRootFrontPrefix(new FrontSpaceBuilder().tag("split").build())
            .style(new StyleBuilder().tag("split").split(true).build())
            .style(
                new StyleBuilder().tag("compact_split").tag(Tags.TAG_COMPACT).split(true).build())
            .style(new StyleBuilder().tag("concensus1").alignment("concensus1").build())
            .style(
                new StyleBuilder()
                    .tag("expandedconcensus1")
                    .notag(Tags.TAG_COMPACT)
                    .alignment("concensus1")
                    .build())
            .build();
    new GeneralTestWizard(
            syntax,
            new TreeBuilder(pair).add("first", "lumberpass").add("second", "ink").build(),
            new TreeBuilder(atomPair)
                .add("first", new TreeBuilder(primitive).add("value", "dog").build())
                .add("second", new TreeBuilder(primitive).add("value", "equifortress").build())
                .build())
        .checkCourseCount(2)
        .displayWidth(200)
        .displayWidth(205)
        .checkCourseCount(3)
        .checkTextBrick(0, 1, "lumberpass")
        .checkTextBrick(0, 2, "ink")
        .checkTextBrick(1, 2, "dog")
        .checkTextBrick(2, 1, "equifortress");
  }

  /** Test issue with split line not expandedting */
  @Test
  public void testPartiallyExpandConsecutiveLines() {
    final FreeAtomType threeLine2 =
        new TypeBuilder("threeLine2")
            .back(Helper.buildBackPrimitive("threeLine"))
            .front(new FrontMarkBuilder("line1").build())
            .front(new FrontMarkBuilder("line2").tag("split").build())
            .front(new FrontMarkBuilder("line3").tag("compact_split").build())
            .build();
    final Syntax syntax =
        new SyntaxBuilder("any")
            .type(threeLine2)
            .group("any", new GroupBuilder().type(threeLine2).build())
            .addRootFrontPrefix(new FrontSpaceBuilder().tag("split").build())
            .style(new StyleBuilder().tag("split").split(true).build())
            .style(
                new StyleBuilder().tag("compact_split").tag(Tags.TAG_COMPACT).split(true).build())
            .build();
    new GeneralTestWizard(syntax, new TreeBuilder(threeLine2).build())
        .displayWidth(50)
        .displayWidth(120)
        .checkCourseCount(2);
  }

  /**
   * A concensus with only one element is disabled and won't affect alignment when the line is
   * broken (as compared to when the line is broken causing the alignment to activate due to
   * elements being on separate lines)
   */
  @Test
  public void testDisabledConcensusSplit2() {
    final FreeAtomType line =
        new TypeBuilder("line")
            .back(Helper.buildBackDataArray("value", "any"))
            .front(new FrontDataArrayBuilder("value").build())
            .build();
    final FreeAtomType pair =
        new TypeBuilder("pair")
            .back(
                new BackArrayBuilder()
                    .add(Helper.buildBackDataPrimitive("first"))
                    .add(Helper.buildBackDataPrimitive("second"))
                    .build())
            .front(new FrontDataPrimitiveBuilder("first").build())
            .front(new FrontDataPrimitiveBuilder("second").tag("a").build())
            .build();
    final Syntax syntax =
        new SyntaxBuilder("any")
            .type(line)
            .type(pair)
            .group("any", new GroupBuilder().type(line).type(pair).build())
            .concensusAlignment("a")
            .addRootFrontPrefix(new FrontSpaceBuilder().tag("split").build())
            .style(new StyleBuilder().tag("split").tag("a").split(true).build())
            .style(new StyleBuilder().tag("a").alignment("a").build())
            .build();
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
