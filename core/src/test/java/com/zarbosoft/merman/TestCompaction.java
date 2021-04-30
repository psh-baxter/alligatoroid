package com.zarbosoft.merman;

import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.helper.FrontDataArrayBuilder;
import com.zarbosoft.merman.helper.FrontMarkBuilder;
import com.zarbosoft.merman.helper.GroupBuilder;
import com.zarbosoft.merman.helper.Helper;
import com.zarbosoft.merman.helper.SyntaxBuilder;
import com.zarbosoft.merman.helper.TreeBuilder;
import com.zarbosoft.merman.helper.TypeBuilder;
import com.zarbosoft.merman.core.syntax.FreeAtomType;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.core.syntax.front.FrontPrimitiveSpec;
import com.zarbosoft.merman.core.syntax.front.FrontSymbol;
import com.zarbosoft.merman.core.syntax.symbol.SymbolSpaceSpec;
import org.junit.Test;

import static com.zarbosoft.merman.core.syntax.style.Style.SplitMode.COMPACT;

public class TestCompaction {
  @Test
  public void testSplitOnResize() {
    final FreeAtomType infinity = new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .build();
    final FreeAtomType one = new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .build();
    final FreeAtomType initialSplitOne = new TypeBuilder("splitOne")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").split(COMPACT).build())
            .build();
    final FreeAtomType text = new TypeBuilder("text")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .build();
    final FreeAtomType comboText = new TypeBuilder("comboText")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .frontMark("123")
            .build();
    final FreeAtomType initialSplitText = new TypeBuilder("splitText")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(
                    new FrontPrimitiveSpec(
                            new FrontPrimitiveSpec.Config("value").splitMode(COMPACT)))
            .build();
    final FreeAtomType line = new TypeBuilder("line")
            .back(Helper.buildBackDataArray("value", "any"))
            .front(
                    new FrontDataArrayBuilder("value")
                            .addPrefix(
                                    new FrontSymbol(
                                            new FrontSymbol.Config(
                                                    new SymbolSpaceSpec(new SymbolSpaceSpec.Config()))))
                            .build())
            .precedence(0)
            .depthScore(1)
            .build();
    final FreeAtomType low = new TypeBuilder("low")
            .back(Helper.buildBackDataArray("value", "any"))
            .front(
                    new FrontDataArrayBuilder("value")
                            .addPrefix(
                                    new FrontSymbol(
                                            new FrontSymbol.Config(
                                                    new SymbolSpaceSpec(
                                                            new SymbolSpaceSpec.Config().splitMode(COMPACT)))))
                            .build())
            .precedence(0)
            .depthScore(1)
            .build();
    final FreeAtomType unary = new TypeBuilder("unary")
            .back(Helper.buildBackDataAtom("value", "any"))
            .frontDataNode("value")
            .precedence(20)
            .depthScore(1)
            .build();
    final FreeAtomType mid = new TypeBuilder("mid")
            .back(Helper.buildBackDataArray("value", "any"))
            .front(
                    new FrontDataArrayBuilder("value")
                            .addPrefix(
                                    new FrontSymbol(
                                            new FrontSymbol.Config(
                                                    new SymbolSpaceSpec(
                                                            new SymbolSpaceSpec.Config().splitMode(COMPACT)))))
                            .build())
            .precedence(50)
            .depthScore(1)
            .build();
    final FreeAtomType high = new TypeBuilder("high")
            .back(Helper.buildBackDataArray("value", "any"))
            .front(
                    new FrontDataArrayBuilder("value")
                            .addPrefix(
                                    new FrontSymbol(
                                            new FrontSymbol.Config(
                                                    new SymbolSpaceSpec(
                                                            new SymbolSpaceSpec.Config().splitMode(COMPACT)))))
                            .build())
            .precedence(100)
            .depthScore(1)
            .build();
    final Syntax syntax = new SyntaxBuilder("any")
            .type(one)
            .type(initialSplitOne)
            .type(text)
            .type(comboText)
            .type(initialSplitText)
            .type(infinity)
            .type(line)
            .type(low)
            .type(unary)
            .type(mid)
            .type(high)
            .group(
                    "any",
                    new GroupBuilder()
                            .type(infinity)
                            .type(one)
                            .type(initialSplitOne)
                            .type(line)
                            .type(low)
                            .type(unary)
                            .type(mid)
                            .type(high)
                            .type(text)
                            .type(comboText)
                            .type(initialSplitText)
                            .build())
            .build();
    new GeneralTestWizard(
            syntax,
            new TreeBuilder(low)
                .addArray("value", new TreeBuilder(one).build(), new TreeBuilder(one).build())
                .build())
        .checkTextBrick(0, 1, "one")
        .checkTextBrick(0, 3, "one")
        .displayWidth(40)
        .checkTextBrick(0, 1, "one")
        .checkTextBrick(1, 1, "one")
        .displayWidth(10_000_000)
        .checkTextBrick(0, 1, "one")
        .checkTextBrick(0, 3, "one");
  }

  @Test
  public void testSplitOrder() {
    final FreeAtomType infinity = new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .build();
    final FreeAtomType one = new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .build();
    final FreeAtomType initialSplitOne = new TypeBuilder("splitOne")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").split(COMPACT).build())
            .build();
    final FreeAtomType text = new TypeBuilder("text")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .build();
    final FreeAtomType comboText = new TypeBuilder("comboText")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .frontMark("123")
            .build();
    final FreeAtomType initialSplitText = new TypeBuilder("splitText")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(
                    new FrontPrimitiveSpec(
                            new FrontPrimitiveSpec.Config("value").splitMode(COMPACT)))
            .build();
    final FreeAtomType line = new TypeBuilder("line")
            .back(Helper.buildBackDataArray("value", "any"))
            .front(
                    new FrontDataArrayBuilder("value")
                            .addPrefix(
                                    new FrontSymbol(
                                            new FrontSymbol.Config(
                                                    new SymbolSpaceSpec(new SymbolSpaceSpec.Config()))))
                            .build())
            .precedence(0)
            .depthScore(1)
            .build();
    final FreeAtomType low = new TypeBuilder("low")
            .back(Helper.buildBackDataArray("value", "any"))
            .front(
                    new FrontDataArrayBuilder("value")
                            .addPrefix(
                                    new FrontSymbol(
                                            new FrontSymbol.Config(
                                                    new SymbolSpaceSpec(
                                                            new SymbolSpaceSpec.Config().splitMode(COMPACT)))))
                            .build())
            .precedence(0)
            .depthScore(1)
            .build();
    final FreeAtomType unary = new TypeBuilder("unary")
            .back(Helper.buildBackDataAtom("value", "any"))
            .frontDataNode("value")
            .precedence(20)
            .depthScore(1)
            .build();
    final FreeAtomType mid = new TypeBuilder("mid")
            .back(Helper.buildBackDataArray("value", "any"))
            .front(
                    new FrontDataArrayBuilder("value")
                            .addPrefix(
                                    new FrontSymbol(
                                            new FrontSymbol.Config(
                                                    new SymbolSpaceSpec(
                                                            new SymbolSpaceSpec.Config().splitMode(COMPACT)))))
                            .build())
            .precedence(50)
            .depthScore(1)
            .build();
    final FreeAtomType high = new TypeBuilder("high")
            .back(Helper.buildBackDataArray("value", "any"))
            .front(
                    new FrontDataArrayBuilder("value")
                            .addPrefix(
                                    new FrontSymbol(
                                            new FrontSymbol.Config(
                                                    new SymbolSpaceSpec(
                                                            new SymbolSpaceSpec.Config().splitMode(COMPACT)))))
                            .build())
            .precedence(100)
            .depthScore(1)
            .build();
    final Syntax syntax = new SyntaxBuilder("any")
            .type(one)
            .type(initialSplitOne)
            .type(text)
            .type(comboText)
            .type(initialSplitText)
            .type(infinity)
            .type(line)
            .type(low)
            .type(unary)
            .type(mid)
            .type(high)
            .group(
                    "any",
                    new GroupBuilder()
                            .type(infinity)
                            .type(one)
                            .type(initialSplitOne)
                            .type(line)
                            .type(low)
                            .type(unary)
                            .type(mid)
                            .type(high)
                            .type(text)
                            .type(comboText)
                            .type(initialSplitText)
                            .build())
            .build();
    new GeneralTestWizard(
            syntax,
            new TreeBuilder(low)
                .addArray(
                    "value",
                    new TreeBuilder(high)
                        .addArray(
                            "value", new TreeBuilder(one).build(), new TreeBuilder(one).build())
                        .build(),
                    new TreeBuilder(one).build())
                .build())
        .checkTextBrick(0, 2, "one")
        .checkTextBrick(0, 4, "one")
        .checkTextBrick(0, 6, "one")
        .displayWidth(70)
        .checkTextBrick(0, 2, "one")
        .checkTextBrick(0, 4, "one")
        .checkTextBrick(1, 1, "one")
        .displayWidth(40)
        .checkTextBrick(1, 1, "one")
        .checkTextBrick(2, 1, "one")
        .checkTextBrick(3, 1, "one")
        .displayWidth(70)
        .checkTextBrick(0, 2, "one")
        .checkTextBrick(0, 4, "one")
        .checkTextBrick(1, 1, "one")
        .displayWidth(10_000_000)
        .checkTextBrick(0, 2, "one")
        .checkTextBrick(0, 4, "one")
        .checkTextBrick(0, 6, "one");
  }

  @Test
  public void testSplitOrderInverted() {
    final FreeAtomType infinity = new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .build();
    final FreeAtomType one = new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .build();
    final FreeAtomType initialSplitOne = new TypeBuilder("splitOne")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").split(COMPACT).build())
            .build();
    final FreeAtomType text = new TypeBuilder("text")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .build();
    final FreeAtomType comboText = new TypeBuilder("comboText")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .frontMark("123")
            .build();
    final FreeAtomType initialSplitText = new TypeBuilder("splitText")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(
                    new FrontPrimitiveSpec(
                            new FrontPrimitiveSpec.Config("value").splitMode(COMPACT)))
            .build();
    final FreeAtomType line = new TypeBuilder("line")
            .back(Helper.buildBackDataArray("value", "any"))
            .front(
                    new FrontDataArrayBuilder("value")
                            .addPrefix(
                                    new FrontSymbol(
                                            new FrontSymbol.Config(
                                                    new SymbolSpaceSpec(new SymbolSpaceSpec.Config()))))
                            .build())
            .precedence(0)
            .depthScore(1)
            .build();
    final FreeAtomType low = new TypeBuilder("low")
            .back(Helper.buildBackDataArray("value", "any"))
            .front(
                    new FrontDataArrayBuilder("value")
                            .addPrefix(
                                    new FrontSymbol(
                                            new FrontSymbol.Config(
                                                    new SymbolSpaceSpec(
                                                            new SymbolSpaceSpec.Config().splitMode(COMPACT)))))
                            .build())
            .precedence(0)
            .depthScore(1)
            .build();
    final FreeAtomType unary = new TypeBuilder("unary")
            .back(Helper.buildBackDataAtom("value", "any"))
            .frontDataNode("value")
            .precedence(20)
            .depthScore(1)
            .build();
    final FreeAtomType mid = new TypeBuilder("mid")
            .back(Helper.buildBackDataArray("value", "any"))
            .front(
                    new FrontDataArrayBuilder("value")
                            .addPrefix(
                                    new FrontSymbol(
                                            new FrontSymbol.Config(
                                                    new SymbolSpaceSpec(
                                                            new SymbolSpaceSpec.Config().splitMode(COMPACT)))))
                            .build())
            .precedence(50)
            .depthScore(1)
            .build();
    final FreeAtomType high = new TypeBuilder("high")
            .back(Helper.buildBackDataArray("value", "any"))
            .front(
                    new FrontDataArrayBuilder("value")
                            .addPrefix(
                                    new FrontSymbol(
                                            new FrontSymbol.Config(
                                                    new SymbolSpaceSpec(
                                                            new SymbolSpaceSpec.Config().splitMode(COMPACT)))))
                            .build())
            .precedence(100)
            .depthScore(1)
            .build();
    final Syntax syntax = new SyntaxBuilder("any")
            .type(one)
            .type(initialSplitOne)
            .type(text)
            .type(comboText)
            .type(initialSplitText)
            .type(infinity)
            .type(line)
            .type(low)
            .type(unary)
            .type(mid)
            .type(high)
            .group(
                    "any",
                    new GroupBuilder()
                            .type(infinity)
                            .type(one)
                            .type(initialSplitOne)
                            .type(line)
                            .type(low)
                            .type(unary)
                            .type(mid)
                            .type(high)
                            .type(text)
                            .type(comboText)
                            .type(initialSplitText)
                            .build())
            .build();
    new GeneralTestWizard(
            syntax,
            new TreeBuilder(high)
                .addArray(
                    "value",
                    new TreeBuilder(low)
                        .addArray(
                            "value", new TreeBuilder(one).build(), new TreeBuilder(one).build())
                        .build(),
                    new TreeBuilder(one).build())
                .build())
        .displayWidth(70)
        .checkTextBrick(1, 1, "one")
        .checkTextBrick(2, 1, "one")
        .checkTextBrick(2, 3, "one")
        .displayWidth(40)
        .checkTextBrick(1, 1, "one")
        .checkTextBrick(2, 1, "one")
        .checkTextBrick(3, 1, "one")
        .displayWidth(70)
        .checkTextBrick(1, 1, "one")
        .checkTextBrick(2, 1, "one")
        .checkTextBrick(2, 3, "one")
        .displayWidth(10_000_000)
        .checkTextBrick(0, 2, "one")
        .checkTextBrick(0, 4, "one")
        .checkTextBrick(0, 6, "one");
  }

  @Test
  public void testSplitOrderRule() {
    final FreeAtomType infinity = new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .build();
    final FreeAtomType one = new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .build();
    final FreeAtomType initialSplitOne = new TypeBuilder("splitOne")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").split(COMPACT).build())
            .build();
    final FreeAtomType text = new TypeBuilder("text")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .build();
    final FreeAtomType comboText = new TypeBuilder("comboText")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .frontMark("123")
            .build();
    final FreeAtomType initialSplitText = new TypeBuilder("splitText")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(
                    new FrontPrimitiveSpec(
                            new FrontPrimitiveSpec.Config("value").splitMode(COMPACT)))
            .build();
    final FreeAtomType line = new TypeBuilder("line")
            .back(Helper.buildBackDataArray("value", "any"))
            .front(
                    new FrontDataArrayBuilder("value")
                            .addPrefix(
                                    new FrontSymbol(
                                            new FrontSymbol.Config(
                                                    new SymbolSpaceSpec(new SymbolSpaceSpec.Config()))))
                            .build())
            .precedence(0)
            .depthScore(1)
            .build();
    final FreeAtomType low = new TypeBuilder("low")
            .back(Helper.buildBackDataArray("value", "any"))
            .front(
                    new FrontDataArrayBuilder("value")
                            .addPrefix(
                                    new FrontSymbol(
                                            new FrontSymbol.Config(
                                                    new SymbolSpaceSpec(
                                                            new SymbolSpaceSpec.Config().splitMode(COMPACT)))))
                            .build())
            .precedence(0)
            .depthScore(1)
            .build();
    final FreeAtomType unary = new TypeBuilder("unary")
            .back(Helper.buildBackDataAtom("value", "any"))
            .frontDataNode("value")
            .precedence(20)
            .depthScore(1)
            .build();
    final FreeAtomType mid = new TypeBuilder("mid")
            .back(Helper.buildBackDataArray("value", "any"))
            .front(
                    new FrontDataArrayBuilder("value")
                            .addPrefix(
                                    new FrontSymbol(
                                            new FrontSymbol.Config(
                                                    new SymbolSpaceSpec(
                                                            new SymbolSpaceSpec.Config().splitMode(COMPACT)))))
                            .build())
            .precedence(50)
            .depthScore(1)
            .build();
    final FreeAtomType high = new TypeBuilder("high")
            .back(Helper.buildBackDataArray("value", "any"))
            .front(
                    new FrontDataArrayBuilder("value")
                            .addPrefix(
                                    new FrontSymbol(
                                            new FrontSymbol.Config(
                                                    new SymbolSpaceSpec(
                                                            new SymbolSpaceSpec.Config().splitMode(COMPACT)))))
                            .build())
            .precedence(100)
            .depthScore(1)
            .build();
    final Syntax syntax = new SyntaxBuilder("any")
            .type(one)
            .type(initialSplitOne)
            .type(text)
            .type(comboText)
            .type(initialSplitText)
            .type(infinity)
            .type(line)
            .type(low)
            .type(unary)
            .type(mid)
            .type(high)
            .group(
                    "any",
                    new GroupBuilder()
                            .type(infinity)
                            .type(one)
                            .type(initialSplitOne)
                            .type(line)
                            .type(low)
                            .type(unary)
                            .type(mid)
                            .type(high)
                            .type(text)
                            .type(comboText)
                            .type(initialSplitText)
                            .build())
            .build();
    new GeneralTestWizard(
            syntax,
            new TreeBuilder(mid)
                .addArray(
                    "value",
                    new TreeBuilder(low)
                        .addArray(
                            "value", new TreeBuilder(one).build(), new TreeBuilder(one).build())
                        .build(),
                    new TreeBuilder(high)
                        .addArray(
                            "value",
                            new TreeBuilder(one).build(),
                            new TreeBuilder(one).build(),
                            new TreeBuilder(one).build())
                        .build())
                .build())
        .displayWidth(110)
        .checkTextBrick(1, 1, "one")
        .checkTextBrick(2, 1, "one")
        .checkTextBrick(3, 2, "one")
        .checkTextBrick(3, 4, "one")
        .checkTextBrick(3, 6, "one")
        .displayWidth(80)
        .checkTextBrick(1, 1, "one")
        .checkTextBrick(2, 1, "one")
        .checkTextBrick(4, 1, "one")
        .checkTextBrick(5, 1, "one")
        .checkTextBrick(6, 1, "one")
        .displayWidth(40)
        .checkTextBrick(1, 1, "one")
        .checkTextBrick(2, 1, "one")
        .checkTextBrick(4, 1, "one")
        .checkTextBrick(5, 1, "one")
        .checkTextBrick(6, 1, "one")
        .displayWidth(80)
        .checkTextBrick(1, 1, "one")
        .checkTextBrick(2, 1, "one")
        .checkTextBrick(4, 1, "one")
        .checkTextBrick(5, 1, "one")
        .checkTextBrick(6, 1, "one")
        .displayWidth(110)
        .checkTextBrick(1, 1, "one")
        .checkTextBrick(2, 1, "one")
        .checkTextBrick(3, 2, "one")
        .checkTextBrick(3, 4, "one")
        .checkTextBrick(3, 6, "one")
        .displayWidth(10_000_000)
        .checkTextBrick(0, 2, "one")
        .checkTextBrick(0, 4, "one")
        .checkTextBrick(0, 7, "one")
        .checkTextBrick(0, 9, "one")
        .checkTextBrick(0, 11, "one");
  }

  @Test
  public void testExpandPrimitiveOrder() {
    final FreeAtomType one = new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .build();
    final FreeAtomType initialSplitText = new TypeBuilder("splitText")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(
                    new FrontPrimitiveSpec(
                            new FrontPrimitiveSpec.Config("value").splitMode(COMPACT)))
            .build();
    final FreeAtomType low = new TypeBuilder("low")
            .back(Helper.buildBackDataArray("value", "any"))
            .front(
                    new FrontDataArrayBuilder("value")
                            .addPrefix(
                                    new FrontSymbol(
                                            new FrontSymbol.Config(
                                                    new SymbolSpaceSpec(
                                                            new SymbolSpaceSpec.Config().splitMode(COMPACT)))))
                            .build())
            .precedence(0)
            .depthScore(1)
            .build();
    final FreeAtomType mid = new TypeBuilder("mid")
            .back(Helper.buildBackDataArray("value", "any"))
            .front(
                    new FrontDataArrayBuilder("value")
                            .addPrefix(
                                    new FrontSymbol(
                                            new FrontSymbol.Config(
                                                    new SymbolSpaceSpec(
                                                            new SymbolSpaceSpec.Config().splitMode(COMPACT)))))
                            .build())
            .precedence(50)
            .depthScore(1)
            .build();
    final Syntax syntax = new SyntaxBuilder("any")
            .type(one)
            .type(initialSplitText)
            .type(low)
            .type(mid)
            .group(
                    "any",
                    new GroupBuilder()
                            .type(one)
                            .type(low)
                            .type(mid)
                            .type(initialSplitText)
                            .build())
            .build();
    new GeneralTestWizard(
            syntax,
            new TreeBuilder(low)
                .addArray(
                    "value",
                    new TreeBuilder(one).build(),
                    new TreeBuilder(mid)
                        .addArray(
                            "value",
                            new TreeBuilder(initialSplitText).add("value", "zet xor").build())
                        .build(),
                    new TreeBuilder(one).build())
                .build())
        .displayWidth(130)
        .checkCourseCount(1)
        .checkTextBrick(0, 1, "one")
        .checkTextBrick(0, 4, "zet xor")
        .checkTextBrick(0, 6, "one")
        .displayWidth(40)
        .checkCourseCount(6)
        .checkTextBrick(0, 1, "one")
        .checkTextBrick(3, 0, "zet ")
        .checkTextBrick(4, 0, "xor")
        .checkTextBrick(5, 1, "one")
        .dumpCourses()
        .displayWidth(130)
        .checkCourseCount(1)
        .checkTextBrick(0, 1, "one")
        .checkTextBrick(0, 4, "zet xor")
        .checkTextBrick(0, 6, "one");
  }

  @Test
  public void testExpandEdge() {
    final FreeAtomType one = new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .build();
    final FreeAtomType initialSplitOne = new TypeBuilder("splitOne")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").split(COMPACT).build())
            .build();
    final FreeAtomType line = new TypeBuilder("line")
            .back(Helper.buildBackDataArray("value", "any"))
            .front(
                    new FrontDataArrayBuilder("value")
                            .build())
            .precedence(0)
            .depthScore(1)
            .build();
    final Syntax syntax = new SyntaxBuilder("any")
            .type(one)
            .type(initialSplitOne)
            .type(line)
            .group(
                    "any",
                    new GroupBuilder()
                            .type(one)
                            .type(initialSplitOne)
                            .type(line)
                            .build())
            .build();
    new GeneralTestWizard(
            syntax,
            new TreeBuilder(line)
                .addArray(
                    "value", new TreeBuilder(one).build(), new TreeBuilder(initialSplitOne).build())
                .build())
        .displayWidth(10)
        .checkCourseCount(2)
        .displayWidth(50)
        .checkCourseCount(2);
  }

  @Test
  public void testCompactWindowDownSimple() {
    final FreeAtomType infinity = new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .build();
    final FreeAtomType one = new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .build();
    final FreeAtomType initialSplitOne = new TypeBuilder("splitOne")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").split(COMPACT).build())
            .build();
    final FreeAtomType text = new TypeBuilder("text")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .build();
    final FreeAtomType comboText = new TypeBuilder("comboText")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .frontMark("123")
            .build();
    final FreeAtomType initialSplitText = new TypeBuilder("splitText")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(
                    new FrontPrimitiveSpec(
                            new FrontPrimitiveSpec.Config("value").splitMode(COMPACT)))
            .build();
    final FreeAtomType line = new TypeBuilder("line")
            .back(Helper.buildBackDataArray("value", "any"))
            .front(
                    new FrontDataArrayBuilder("value")
                            .addPrefix(
                                    new FrontSymbol(
                                            new FrontSymbol.Config(
                                                    new SymbolSpaceSpec(new SymbolSpaceSpec.Config()))))
                            .build())
            .precedence(0)
            .depthScore(1)
            .build();
    final FreeAtomType low = new TypeBuilder("low")
            .back(Helper.buildBackDataArray("value", "any"))
            .front(
                    new FrontDataArrayBuilder("value")
                            .addPrefix(
                                    new FrontSymbol(
                                            new FrontSymbol.Config(
                                                    new SymbolSpaceSpec(
                                                            new SymbolSpaceSpec.Config().splitMode(COMPACT)))))
                            .build())
            .precedence(0)
            .depthScore(1)
            .build();
    final FreeAtomType unary = new TypeBuilder("unary")
            .back(Helper.buildBackDataAtom("value", "any"))
            .frontDataNode("value")
            .precedence(20)
            .depthScore(1)
            .build();
    final FreeAtomType mid = new TypeBuilder("mid")
            .back(Helper.buildBackDataArray("value", "any"))
            .front(
                    new FrontDataArrayBuilder("value")
                            .addPrefix(
                                    new FrontSymbol(
                                            new FrontSymbol.Config(
                                                    new SymbolSpaceSpec(
                                                            new SymbolSpaceSpec.Config().splitMode(COMPACT)))))
                            .build())
            .precedence(50)
            .depthScore(1)
            .build();
    final FreeAtomType high = new TypeBuilder("high")
            .back(Helper.buildBackDataArray("value", "any"))
            .front(
                    new FrontDataArrayBuilder("value")
                            .addPrefix(
                                    new FrontSymbol(
                                            new FrontSymbol.Config(
                                                    new SymbolSpaceSpec(
                                                            new SymbolSpaceSpec.Config().splitMode(COMPACT)))))
                            .build())
            .precedence(100)
            .depthScore(1)
            .build();
    final Syntax syntax = new SyntaxBuilder("any")
            .type(one)
            .type(initialSplitOne)
            .type(text)
            .type(comboText)
            .type(initialSplitText)
            .type(infinity)
            .type(line)
            .type(low)
            .type(unary)
            .type(mid)
            .type(high)
            .group(
                    "any",
                    new GroupBuilder()
                            .type(infinity)
                            .type(one)
                            .type(initialSplitOne)
                            .type(line)
                            .type(low)
                            .type(unary)
                            .type(mid)
                            .type(high)
                            .type(text)
                            .type(comboText)
                            .type(initialSplitText)
                            .build())
            .build();
    final Atom midAtom =
        new TreeBuilder(mid)
            .addArray(
                "value",
                new TreeBuilder(one).build(),
                new TreeBuilder(high)
                    .addArray("value", new TreeBuilder(one).build(), new TreeBuilder(one).build())
                    .build(),
                new TreeBuilder(one).build())
            .build();
    new GeneralTestWizard(syntax, new TreeBuilder(low).addArray("value", midAtom).build())
        .actWindow()
        .displayWidth(70)
        .checkTextBrick(1, 1, "one")
        .checkTextBrick(2, 1, "...")
        .checkTextBrick(3, 1, "one")
        .run(context -> midAtom.valueParentRef.selectValue(context))
        .actWindow()
        .checkTextBrick(0, 1, "one")
        .checkTextBrick(1, 2, "one")
        .checkTextBrick(1, 4, "one")
        .checkTextBrick(2, 1, "one");
  }

  @Test
  public void testCompactWindowDown() {
    final FreeAtomType one = new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .build();
    final FreeAtomType two = new TypeBuilder("two")
            .back(Helper.buildBackPrimitive("two"))
            .front(new FrontMarkBuilder("two").build())
            .build();
    final FreeAtomType low = new TypeBuilder("low")
            .back(Helper.buildBackDataArray("value", "any"))
            .front(
                    new FrontDataArrayBuilder("value")
                            .addPrefix(
                                    new FrontSymbol(
                                            new FrontSymbol.Config(
                                                    new SymbolSpaceSpec(
                                                            new SymbolSpaceSpec.Config().splitMode(COMPACT)))))
                            .build())
            .precedence(0)
            .depthScore(1)
            .build();
    final FreeAtomType mid = new TypeBuilder("mid")
            .back(Helper.buildBackDataArray("value", "any"))
            .front(
                    new FrontDataArrayBuilder("value")
                            .addPrefix(
                                    new FrontSymbol(
                                            new FrontSymbol.Config(
                                                    new SymbolSpaceSpec(
                                                            new SymbolSpaceSpec.Config().splitMode(COMPACT)))))
                            .build())
            .precedence(50)
            .depthScore(1)
            .build();
    final FreeAtomType high = new TypeBuilder("high")
            .back(Helper.buildBackDataArray("value", "any"))
            .front(
                    new FrontDataArrayBuilder("value")
                            .addPrefix(
                                    new FrontSymbol(
                                            new FrontSymbol.Config(
                                                    new SymbolSpaceSpec(
                                                            new SymbolSpaceSpec.Config().splitMode(COMPACT)))))
                            .build())
            .precedence(100)
            .depthScore(1)
            .build();
    final Syntax syntax = new SyntaxBuilder("any")
            .type(one)
            .type(two)
            .type(low)
            .type(mid)
            .type(high)
            .group(
                    "any",
                    new GroupBuilder()
                            .type(one)
                            .type(two)
                            .type(low)
                            .type(mid)
                            .type(high)
                            .build())
            .build();
    final Atom midAtom =
        new TreeBuilder(mid)
            .addArray(
                "value",
                new TreeBuilder(two).build(),
                new TreeBuilder(low)
                    .addArray("value", new TreeBuilder(one).build(), new TreeBuilder(one).build())
                    .build(),
                new TreeBuilder(two).build())
            .build();
    Atom highAtom = new TreeBuilder(high).addArray("value", midAtom).build();
    new GeneralTestWizard(syntax, highAtom)
        .run(context -> highAtom.valueParentRef.selectValue(context))
        .actWindow()
        .displayWidth(70)
        .checkTextBrick(1, 1, "two")
        .checkTextBrick(2, 1, "...")
        .checkTextBrick(3, 1, "two")
        .run(context -> midAtom.valueParentRef.selectValue(context))
        .actWindow()
        .checkTextBrick(0, 1, "two")
        .checkTextBrick(1, 1, "one")
        .checkTextBrick(2, 1, "one")
        .checkTextBrick(2, 3, "two");
  }

  @Test
  public void testIdentical() {
    final FreeAtomType infinity = new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .build();
    final FreeAtomType one = new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .build();
    final FreeAtomType initialSplitOne = new TypeBuilder("splitOne")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").split(COMPACT).build())
            .build();
    final FreeAtomType text = new TypeBuilder("text")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .build();
    final FreeAtomType comboText = new TypeBuilder("comboText")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .frontMark("123")
            .build();
    final FreeAtomType initialSplitText = new TypeBuilder("splitText")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(
                    new FrontPrimitiveSpec(
                            new FrontPrimitiveSpec.Config("value").splitMode(COMPACT)))
            .build();
    final FreeAtomType line = new TypeBuilder("line")
            .back(Helper.buildBackDataArray("value", "any"))
            .front(
                    new FrontDataArrayBuilder("value")
                            .addPrefix(
                                    new FrontSymbol(
                                            new FrontSymbol.Config(
                                                    new SymbolSpaceSpec(new SymbolSpaceSpec.Config()))))
                            .build())
            .precedence(0)
            .depthScore(1)
            .build();
    final FreeAtomType low = new TypeBuilder("low")
            .back(Helper.buildBackDataArray("value", "any"))
            .front(
                    new FrontDataArrayBuilder("value")
                            .addPrefix(
                                    new FrontSymbol(
                                            new FrontSymbol.Config(
                                                    new SymbolSpaceSpec(
                                                            new SymbolSpaceSpec.Config().splitMode(COMPACT)))))
                            .build())
            .precedence(0)
            .depthScore(1)
            .build();
    final FreeAtomType unary = new TypeBuilder("unary")
            .back(Helper.buildBackDataAtom("value", "any"))
            .frontDataNode("value")
            .precedence(20)
            .depthScore(1)
            .build();
    final FreeAtomType mid = new TypeBuilder("mid")
            .back(Helper.buildBackDataArray("value", "any"))
            .front(
                    new FrontDataArrayBuilder("value")
                            .addPrefix(
                                    new FrontSymbol(
                                            new FrontSymbol.Config(
                                                    new SymbolSpaceSpec(
                                                            new SymbolSpaceSpec.Config().splitMode(COMPACT)))))
                            .build())
            .precedence(50)
            .depthScore(1)
            .build();
    final FreeAtomType high = new TypeBuilder("high")
            .back(Helper.buildBackDataArray("value", "any"))
            .front(
                    new FrontDataArrayBuilder("value")
                            .addPrefix(
                                    new FrontSymbol(
                                            new FrontSymbol.Config(
                                                    new SymbolSpaceSpec(
                                                            new SymbolSpaceSpec.Config().splitMode(COMPACT)))))
                            .build())
            .precedence(100)
            .depthScore(1)
            .build();
    final Syntax syntax = new SyntaxBuilder("any")
            .type(one)
            .type(initialSplitOne)
            .type(text)
            .type(comboText)
            .type(initialSplitText)
            .type(infinity)
            .type(line)
            .type(low)
            .type(unary)
            .type(mid)
            .type(high)
            .group(
                    "any",
                    new GroupBuilder()
                            .type(infinity)
                            .type(one)
                            .type(initialSplitOne)
                            .type(line)
                            .type(low)
                            .type(unary)
                            .type(mid)
                            .type(high)
                            .type(text)
                            .type(comboText)
                            .type(initialSplitText)
                            .build())
            .build();
    new GeneralTestWizard(
            syntax,
            new TreeBuilder(low)
                .addArray(
                    "value",
                    new TreeBuilder(infinity).build(),
                    new TreeBuilder(high)
                        .addArray(
                            "value",
                            new TreeBuilder(low)
                                .addArray(
                                    "value",
                                    new TreeBuilder(one).build(),
                                    new TreeBuilder(one).build())
                                .build())
                        .build(),
                    new TreeBuilder(infinity).build())
                .build())
        .checkTextBrick(0, 1, "infinity")
        .checkTextBrick(0, 5, "one")
        .checkTextBrick(0, 7, "one")
        .checkTextBrick(0, 9, "infinity")
        .displayWidth(100)
        .checkTextBrick(0, 1, "infinity")
        .checkTextBrick(1, 3, "one")
        .checkTextBrick(1, 5, "one")
        .checkTextBrick(2, 1, "infinity")
        .displayWidth(50)
        .checkTextBrick(0, 1, "infinity")
        .checkTextBrick(2, 1, "one")
        .checkTextBrick(3, 1, "one")
        .checkTextBrick(4, 1, "infinity")
        .displayWidth(100)
        .checkTextBrick(0, 1, "infinity")
        .checkTextBrick(1, 3, "one")
        .checkTextBrick(1, 5, "one")
        .checkTextBrick(2, 1, "infinity")
        .displayWidth(10_000_000)
        .checkTextBrick(0, 1, "infinity")
        .checkTextBrick(0, 5, "one")
        .checkTextBrick(0, 7, "one")
        .checkTextBrick(0, 9, "infinity");
  }

  public static class GeneralTestWizard extends com.zarbosoft.merman.helper.GeneralTestWizard {
    public GeneralTestWizard(Syntax syntax, Atom... atoms) {
      super(syntax, atoms);
      this.inner.context.ellipsizeThreshold = 2;
    }
  }
}
