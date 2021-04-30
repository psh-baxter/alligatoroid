package com.zarbosoft.merman.editorcore;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.document.fields.FieldArray;
import com.zarbosoft.merman.core.document.fields.FieldPrimitive;
import com.zarbosoft.merman.core.syntax.FreeAtomType;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.core.syntax.front.FrontSymbol;
import com.zarbosoft.merman.core.syntax.style.Style;
import com.zarbosoft.merman.core.syntax.symbol.SymbolSpaceSpec;
import com.zarbosoft.merman.core.visual.Visual;
import com.zarbosoft.merman.editorcore.helper.BackArrayBuilder;
import com.zarbosoft.merman.editorcore.helper.FrontDataArrayBuilder;
import com.zarbosoft.merman.editorcore.helper.FrontDataPrimitiveBuilder;
import com.zarbosoft.merman.editorcore.helper.GeneralTestWizard;
import com.zarbosoft.merman.editorcore.helper.GroupBuilder;
import com.zarbosoft.merman.editorcore.helper.Helper;
import com.zarbosoft.merman.editorcore.helper.SyntaxBuilder;
import com.zarbosoft.merman.editorcore.helper.TreeBuilder;
import com.zarbosoft.merman.editorcore.helper.TypeBuilder;
import com.zarbosoft.merman.editorcore.history.changes.ChangeArray;
import com.zarbosoft.merman.editorcore.history.changes.ChangePrimitive;
import com.zarbosoft.rendaw.common.TSList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.function.Function;

@RunWith(Parameterized.class)
public class TestLayoutAlignment {
  private final int layBrickBatchSize;

  public TestLayoutAlignment(final int layBrickBatchSize) {
    this.layBrickBatchSize = layBrickBatchSize;
  }

  @Parameterized.Parameters
  public static Iterable<Object[]> parameters() {
    return TSList.of(new Object[] {1}, new Object[] {2}, new Object[] {10});
  }

  public class GeneralTestWizard extends com.zarbosoft.merman.editorcore.helper.GeneralTestWizard {
    public GeneralTestWizard(Syntax syntax, Atom... atoms) {
      super(syntax, atoms);
      inner.editor.context.layBrickBatchSize = TestLayoutAlignment.this.layBrickBatchSize;
    }
  }

  @Test
  public void testDynamicSecondShiftOut() {
    FreeAtomType pair = new TypeBuilder("pair")
            .back(
                    new BackArrayBuilder()
                            .add(Helper.buildBackDataPrimitive("first"))
                            .add(Helper.buildBackDataPrimitive("second"))
                            .build())
            .front(new FrontDataPrimitiveBuilder("first").build())
            .front(new FrontDataPrimitiveBuilder("second").alignment("concensus1").build())
            .build();
    Syntax syntax = new SyntaxBuilder("any")
            .type(pair)
            .group(
                    "any",
                    new GroupBuilder()
                            .type(pair)
                            .build())
            .concensusAlignment("concensus1")
            .addRootFrontPrefix(new FrontSymbol(new FrontSymbol.Config(new SymbolSpaceSpec(new SymbolSpaceSpec.Config().splitMode(Style.SplitMode.ALWAYS)))))
            .build();
    final Atom line2 = new TreeBuilder(pair).add("first", "c").add("second", "d").build();
    final FieldPrimitive line2_1 = (FieldPrimitive) line2.fields.getOpt("first");
    new GeneralTestWizard(
            syntax, new TreeBuilder(pair).add("first", "a").add("second", "b").build(), line2)
            .change(new ChangePrimitive(line2_1, 1, 0, "cc"))
        .checkBrick(0, 2, 30);
  }

  @Test
  public void testDynamicFirstShiftOut() {
    FreeAtomType pair = new TypeBuilder("pair")
            .back(
                    new BackArrayBuilder()
                            .add(Helper.buildBackDataPrimitive("first"))
                            .add(Helper.buildBackDataPrimitive("second"))
                            .build())
            .front(new FrontDataPrimitiveBuilder("first").build())
            .front(new FrontDataPrimitiveBuilder("second").alignment("concensus1").build())
            .build();
    Syntax syntax = new SyntaxBuilder("any")
            .type(pair)
            .group(
                    "any",
                    new GroupBuilder()
                            .type(pair)
                            .build())
            .concensusAlignment("concensus1")
            .addRootFrontPrefix(new FrontSymbol(new FrontSymbol.Config(new SymbolSpaceSpec(new SymbolSpaceSpec.Config().splitMode(Style.SplitMode.ALWAYS)))))
            .build();
    final Atom line = new TreeBuilder(pair).add("first", "a").add("second", "b").build();
    final FieldPrimitive text = (FieldPrimitive) line.fields.getOpt("first");
    new GeneralTestWizard(
            syntax, line, new TreeBuilder(pair).add("first", "c").add("second", "d").build())
            .change(new ChangePrimitive(text, 1, 0, "aa"))
        .checkBrick(0, 2, 30);
  }

  @Test
  public void testDynamicShiftIn() {
    FreeAtomType pair = new TypeBuilder("pair")
            .back(
                    new BackArrayBuilder()
                            .add(Helper.buildBackDataPrimitive("first"))
                            .add(Helper.buildBackDataPrimitive("second"))
                            .build())
            .front(new FrontDataPrimitiveBuilder("first").build())
            .front(new FrontDataPrimitiveBuilder("second").alignment("concensus1").build())
            .build();
    Syntax syntax = new SyntaxBuilder("any")
            .type(pair)
            .group(
                    "any",
                    new GroupBuilder()
                            .type(pair)
                            .build())
            .concensusAlignment("concensus1")
            .addRootFrontPrefix(new FrontSymbol(new FrontSymbol.Config(new SymbolSpaceSpec(new SymbolSpaceSpec.Config().splitMode(Style.SplitMode.ALWAYS)))))
            .build();
    final Atom line2 = new TreeBuilder(pair).add("first", "ccccc").add("second", "d").build();
    final FieldPrimitive line2_1 = (FieldPrimitive) line2.fields.getOpt("first");
    new GeneralTestWizard(
            syntax, new TreeBuilder(pair).add("first", "a").add("second", "b").build(), line2)
            .change(new ChangePrimitive(line2_1, 1, 4, ""))
        .checkBrick(0, 2, 10);
  }

  @Test
  public void testDynamicAddLine() {
    FreeAtomType pair = new TypeBuilder("pair")
            .back(
                    new BackArrayBuilder()
                            .add(Helper.buildBackDataPrimitive("first"))
                            .add(Helper.buildBackDataPrimitive("second"))
                            .build())
            .front(new FrontDataPrimitiveBuilder("first").build())
            .front(new FrontDataPrimitiveBuilder("second").alignment("concensus1").build())
            .build();
    Syntax syntax = new SyntaxBuilder("any")
            .type(pair)
            .group(
                    "any",
                    new GroupBuilder()
                            .type(pair)
                            .build())
            .concensusAlignment("concensus1")
            .addRootFrontPrefix(new FrontSymbol(new FrontSymbol.Config(new SymbolSpaceSpec(new SymbolSpaceSpec.Config().splitMode(Style.SplitMode.ALWAYS)))))
            .build();
    new GeneralTestWizard(
            syntax, new TreeBuilder(pair).add("first", "a").add("second", "b").build())
        .run(
            editor ->
                editor.history.record(editor.context, null, r -> r.apply(
                    editor.context,
                    new ChangeArray(
                        Helper.rootArray(editor.context.document),
                        1,
                        0,
                        TSList.of(
                            new TreeBuilder(pair).add("first", "ccc").add("second", "d").build())))))
        .checkBrick(0, 2, 30);
  }

  @Test
  public void testDynamicRemoveLine() {
    FreeAtomType line = new TypeBuilder("line")
            .back(Helper.buildBackDataArray("value", "any"))
            .front(new FrontDataArrayBuilder("value").build())
            .build();
    FreeAtomType pair = new TypeBuilder("pair")
            .back(
                    new BackArrayBuilder()
                            .add(Helper.buildBackDataPrimitive("first"))
                            .add(Helper.buildBackDataPrimitive("second"))
                            .build())
            .front(new FrontDataPrimitiveBuilder("first").build())
            .front(new FrontDataPrimitiveBuilder("second").alignment("concensus1").build())
            .build();
    Syntax syntax = new SyntaxBuilder("any")
            .type(line)
            .type(pair)
            .group(
                    "any",
                    new GroupBuilder()
                            .type(line)
                            .type(pair)
                            .build())
            .relativeAlignment("relative", 3)
            .concensusAlignment("concensus1")
            .concensusAlignment("concensus2")
            .addRootFrontPrefix(new FrontSymbol(new FrontSymbol.Config(new SymbolSpaceSpec(new SymbolSpaceSpec.Config().splitMode(Style.SplitMode.ALWAYS)))))
            .build();
    new GeneralTestWizard(
            syntax,
            new TreeBuilder(pair).add("first", "a").add("second", "b").build(),
            new TreeBuilder(pair).add("first", "ccc").add("second", "d").build())
        .run(
            editor ->
                editor.history.record(editor.context, null, r -> r.apply(
                    editor.context,
                    new ChangeArray(Helper.rootArray(editor.context.document), 1, 1, TSList.of()))))
        .checkBrick(0, 2, 10);
  }

  @Test
  public void testConcensusSameLineDynamicAdd() {
    FreeAtomType line = new TypeBuilder("line")
            .back(Helper.buildBackDataArray("value", "any"))
            .front(new FrontDataArrayBuilder("value").build())
            .build();
    FreeAtomType pair = new TypeBuilder("pair")
            .back(
                    new BackArrayBuilder()
                            .add(Helper.buildBackDataPrimitive("first"))
                            .add(Helper.buildBackDataPrimitive("second"))
                            .build())
            .front(new FrontDataPrimitiveBuilder("first").build())
            .front(new FrontDataPrimitiveBuilder("second").alignment("concensus1").build())
            .build();
    Syntax syntax = new SyntaxBuilder("any")
            .type(line)
            .type(pair)
            .group(
                    "any",
                    new GroupBuilder()
                            .type(line)
                            .type(pair)
                            .build())
            .concensusAlignment("concensus1")
            .addRootFrontPrefix(new FrontSymbol(new FrontSymbol.Config(new SymbolSpaceSpec(new SymbolSpaceSpec.Config().splitMode(Style.SplitMode.ALWAYS)))))
            .build();
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
            .change(new ChangePrimitive(line2_1, 0, 0, "cc"))
        .checkBrick(0, 2, 10)
        .checkBrick(0, 4, 40);
  }

  @Test
  public void testConcensusSameLineDynamicAddPairBefore() {
    FreeAtomType line = new TypeBuilder("line")
            .back(Helper.buildBackDataArray("value", "any"))
            .front(new FrontDataArrayBuilder("value").build())
            .build();
    FreeAtomType pair = new TypeBuilder("pair")
            .back(
                    new BackArrayBuilder()
                            .add(Helper.buildBackDataPrimitive("first"))
                            .add(Helper.buildBackDataPrimitive("second"))
                            .build())
            .front(new FrontDataPrimitiveBuilder("first").build())
            .front(new FrontDataPrimitiveBuilder("second").alignment("concensus1").build())
            .build();
    Syntax syntax = new SyntaxBuilder("any")
            .type(line)
            .type(pair)
            .group(
                    "any",
                    new GroupBuilder()
                            .type(line)
                            .type(pair)
                            .build())
            .concensusAlignment("concensus1")
            .addRootFrontPrefix(new FrontSymbol(new FrontSymbol.Config(new SymbolSpaceSpec(new SymbolSpaceSpec.Config().splitMode(Style.SplitMode.ALWAYS)))))
            .build();
    final Atom line2 =
        new TreeBuilder(line)
            .addArray("value", new TreeBuilder(pair).add("first", "ccc").add("second", "d").build())
            .build();
    final FieldArray array = (FieldArray) line2.fields.getOpt("value");
    new GeneralTestWizard(syntax, line2)
            .change(
                    new ChangeArray(
                        array,
                        0,
                        0,
                        TSList.of(
                            new TreeBuilder(pair).add("first", "a").add("second", "b").build())))
        .checkBrick(0, 2, 10)
        .checkBrick(0, 4, 50);
  }

  @Test
  public void testConcensusSameLineDynamicRemove() {
    FreeAtomType line = new TypeBuilder("line")
            .back(Helper.buildBackDataArray("value", "any"))
            .front(new FrontDataArrayBuilder("value").build())
            .build();
    FreeAtomType pair = new TypeBuilder("pair")
            .back(
                    new BackArrayBuilder()
                            .add(Helper.buildBackDataPrimitive("first"))
                            .add(Helper.buildBackDataPrimitive("second"))
                            .build())
            .front(new FrontDataPrimitiveBuilder("first").build())
            .front(new FrontDataPrimitiveBuilder("second").alignment("concensus1").build())
            .build();
    Syntax syntax = new SyntaxBuilder("any")
            .type(line)
            .type(pair)
            .group(
                    "any",
                    new GroupBuilder()
                            .type(line)
                            .type(pair)
                            .build())
            .concensusAlignment("concensus1")
            .addRootFrontPrefix(new FrontSymbol(new FrontSymbol.Config(new SymbolSpaceSpec(new SymbolSpaceSpec.Config().splitMode(Style.SplitMode.ALWAYS)))))
            .build();
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
            .change(new ChangePrimitive(line2_1, 0, 2, ""))
        .checkBrick(0, 2, 10)
        .checkBrick(0, 4, 20);
  }

  /*
  @Test
  public void testDynamicConcensusSplitAdjust() {
    FreeAtomType pair = new TypeBuilder("pair")
            .back(
                    new BackArrayBuilder()
                            .add(Helper.buildBackDataPrimitive("first"))
                            .add(Helper.buildBackDataPrimitive("second"))
                            .build())
            .front(new FrontDataPrimitiveBuilder("first").build())
            .front(new FrontDataPrimitiveBuilder("second").alignment("concensus1").build())
            .build();
    Syntax syntax = new SyntaxBuilder("any")
            .type(pair)
            .group(
                    "any",
                    new GroupBuilder()
                            .type(pair)
                            .build())
            .concensusAlignment("concensus1")
            .addRootFrontPrefix(new FrontSymbol(new FrontSymbol.Config(new SymbolSpaceSpec(new SymbolSpaceSpec.Config().splitMode(Style.SplitMode.ALWAYS)))))
            .build();
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
                    .apply(context.context)
                    .changeTags(context, new TagsChange(add, remove).remove(new FreeTag("split"))))
        .checkCourseCount(1)
        .run(
            context ->
                line2Visual
                    .apply(context.context)
                    .changeTags(context, new TagsChange(add, remove).add(new FreeTag("split"))))
        .run(
            context ->
                context.history.apply(
                    context,
                    new ChangePrimitive((FieldPrimitive) pair1.fields.getOpt("first"), 8, 0, "9X")))
        .checkCourseCount(2)
        .checkBrick(0, 2, 100)
        .checkBrick(1, 2, 100);
  }

  @Test
  public void testDisabledConcensusSplit() {
    FreeAtomType line = new TypeBuilder("line")
            .back(Helper.buildBackDataArray("value", "any"))
            .front(new FrontDataArrayBuilder("value").build())
            .build();
    FreeAtomType pair = new TypeBuilder("pair")
            .back(
                    new BackArrayBuilder()
                            .add(Helper.buildBackDataPrimitive("first"))
                            .add(Helper.buildBackDataPrimitive("second"))
                            .build())
            .front(new FrontDataPrimitiveBuilder("first").build())
            .front(new FrontDataPrimitiveBuilder("second").alignment("concensus1").build())
            .build();
    Syntax syntax = new SyntaxBuilder("any")
            .type(line)
            .type(pair)
            .group(
                    "any",
                    new GroupBuilder()
                            .type(line)
                            .type(pair)
                            .build())
            .concensusAlignment("concensus1")
            .addRootFrontPrefix(new FrontSymbol(new FrontSymbol.Config(new SymbolSpaceSpec(new SymbolSpaceSpec.Config().splitMode(Style.SplitMode.ALWAYS)))))
            .build();
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
                    new ChangePrimitive(
                        (FieldPrimitive) pairAtom1.fields.getOpt("first"), 7, 0, "9X")))
        .run(
            context ->
                ((FieldPrimitive) pairAtom1.fields.getOpt("first"))
                    .visual.changeTags(
                        context, new TagsChange(add, remove).add(new FreeTag("split"))))
        .checkCourseCount(2);
  }
   */
}
