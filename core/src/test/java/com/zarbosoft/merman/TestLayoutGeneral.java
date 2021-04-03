package com.zarbosoft.merman;

import com.zarbosoft.merman.core.syntax.FreeAtomType;
import com.zarbosoft.merman.core.syntax.style.Padding;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.core.syntax.front.FrontPrimitiveSpec;
import com.zarbosoft.merman.core.syntax.front.FrontSymbol;
import com.zarbosoft.merman.core.syntax.style.Style;
import com.zarbosoft.merman.core.syntax.symbol.SymbolSpaceSpec;
import com.zarbosoft.merman.helper.FrontDataArrayBuilder;
import com.zarbosoft.merman.helper.FrontMarkBuilder;
import com.zarbosoft.merman.helper.GeneralTestWizard;
import com.zarbosoft.merman.helper.GroupBuilder;
import com.zarbosoft.merman.helper.Helper;
import com.zarbosoft.merman.helper.SyntaxBuilder;
import com.zarbosoft.merman.helper.TreeBuilder;
import com.zarbosoft.merman.helper.TypeBuilder;
import org.junit.Test;

public class TestLayoutGeneral {
  public static final FreeAtomType one;
  public static final FreeAtomType two;
  public static final FreeAtomType big;
  public static final FreeAtomType text;
  public static final FreeAtomType array;
  public static final Syntax syntax;
  public static final Syntax syntaxPadded;

  static {
    one =
        new TypeBuilder("one").back(Helper.buildBackPrimitive("one")).frontSplitMark("one").build();
    two =
        new TypeBuilder("two").back(Helper.buildBackPrimitive("two")).frontSplitMark("two").build();
    big =
        new TypeBuilder("big")
            .back(Helper.buildBackPrimitive("big"))
            .front(
                new FrontSymbol(
                    new FrontSymbol.Config(
                        new SymbolSpaceSpec(
                            new SymbolSpaceSpec.Config()
                                .splitMode(Style.SplitMode.ALWAYS)
                                .style(
                                    new Style.Config()
                                        .
                                        /* only in first syntax */ spaceTransverseAfter(60)
                                        .create())))))
            .build();
    text =
        new TypeBuilder("text")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(
                new FrontPrimitiveSpec(
                    new FrontPrimitiveSpec.Config("value").splitMode(Style.SplitMode.ALWAYS)))
            .build();
    array =
        new TypeBuilder("array")
            .back(Helper.buildBackDataArray("value", "any"))
            .frontSplitMark("[")
            .front(
                new FrontDataArrayBuilder("value")
                    .addSeparator(new FrontMarkBuilder(", ").build())
                    .build())
            .frontSplitMark("]")
            .autoComplete(99)
            .build();
    syntax =
        new SyntaxBuilder("any")
            .type(one)
            .type(two)
            .type(text)
            .type(array)
            .group("any", new GroupBuilder().type(one).type(two).type(text).type(array).build())
            .build();
    syntaxPadded =
        new SyntaxBuilder("any")
            .pad(new Padding(5, 5, 9, 9))
            .type(one)
            .type(two)
            .type(big)
            .type(text)
            .type(array)
            .group(
                "any",
                new GroupBuilder().type(one).type(two).type(big).type(text).type(array).build())
            .build();
  }

  @Test
  public void testInitialLayout() {
    new GeneralTestWizard(
            syntax,
            new TreeBuilder(one).build(),
            new TreeBuilder(one).build(),
            new TreeBuilder(one).build(),
            new TreeBuilder(two).build(),
            new TreeBuilder(one).build(),
            new TreeBuilder(one).build())
        .checkScroll(-10)
        .checkCourse(0, 0, 10)
        .checkCourse(1, 17, 27);
  }

  @Test
  public void testClippedLayout() {
    new GeneralTestWizard(
            syntax,
            new TreeBuilder(one).build(),
            new TreeBuilder(one).build(),
            new TreeBuilder(one).build(),
            new TreeBuilder(two).build(),
            new TreeBuilder(one).build(),
            new TreeBuilder(one).build())
        .displayHeight(40)
        .checkScroll(-10);
  }

  @Test
  public void testScrollLayoutPositive() {
    new GeneralTestWizard(
            syntax,
            new TreeBuilder(one).build(),
            new TreeBuilder(one).build(),
            new TreeBuilder(one).build(),
            new TreeBuilder(two).build(),
            new TreeBuilder(one).build(),
            new TreeBuilder(one).build())
        .displayHeight(40)
        .run(
            context -> {
              Helper.rootArray(context.document).data.get(4).valueParentRef.selectValue(context);
            })
        .checkCourse(4, 47, 57)
        .checkScroll(24)
        .checkCourse(3, 27, 37)
        .checkCourse(5, 64, 74);
  }

  @Test
  public void testScrollLayoutNegative() {
    new GeneralTestWizard(
            syntax,
            new TreeBuilder(one).build(),
            new TreeBuilder(one).build(),
            new TreeBuilder(one).build(),
            new TreeBuilder(two).build(),
            new TreeBuilder(one).build(),
            new TreeBuilder(one).build())
        .displayHeight(40)
        .run(
            context -> {
              Helper.rootArray(context.document).data.get(4).valueParentRef.selectValue(context);
            })
        .checkScroll(24)
        .run(
            context -> {
              Helper.rootArray(context.document).data.get(0).valueParentRef.selectValue(context);
            })
        .checkCourse(0, -3, 7)
        .checkScroll(-13);
  }

  @Test
  public void testScrollLayoutPaddedPositive() {
    new GeneralTestWizard(
            syntaxPadded,
            new TreeBuilder(one).build(),
            new TreeBuilder(one).build(),
            new TreeBuilder(one).build(),
            new TreeBuilder(two).build(),
            new TreeBuilder(one).build(),
            new TreeBuilder(one).build())
        .displayHeight(50)
        .run(
            context -> {
              Helper.rootArray(context.document).data.get(4).valueParentRef.selectValue(context);
            })
        .checkCourse(4, 47, 57)
        .checkScroll(23)
        .checkCourse(3, 27, 37)
        .checkCourse(5, 64, 74);
  }

  @Test
  public void testScrollLayoutPaddedNegative() {
    new GeneralTestWizard(
            syntaxPadded,
            new TreeBuilder(one).build(),
            new TreeBuilder(one).build(),
            new TreeBuilder(one).build(),
            new TreeBuilder(two).build(),
            new TreeBuilder(one).build(),
            new TreeBuilder(one).build())
        .displayHeight(50)
        .run(
            context -> {
              Helper.rootArray(context.document).data.get(4).valueParentRef.selectValue(context);
            })
        .checkScroll(23)
        .run(
            context -> {
              Helper.rootArray(context.document).data.get(0).valueParentRef.selectValue(context);
            })
        .checkCourse(0, -3, 7)
        .checkScroll(-22);
  }

  @Test
  public void testScrollLayoutPositiveTooBig() {
    new GeneralTestWizard(
            syntax,
            new TreeBuilder(one).build(),
            new TreeBuilder(one).build(),
            new TreeBuilder(one).build(),
            new TreeBuilder(two).build(),
            new TreeBuilder(big).build(),
            new TreeBuilder(one).build())
        .displayHeight(40)
        .run(
            context -> {
              Helper.rootArray(context.document).data.get(4).valueParentRef.selectValue(context);
            })
        .checkCourse(4, 47, 107)
        .checkScroll(37)
        .checkCourse(3, 27, 37)
        .checkCourse(5, 114, 124);
  }

  @Test
  public void testScrollLayoutNegativeTooBig() {
    new GeneralTestWizard(
            syntax,
            new TreeBuilder(one).build(),
            new TreeBuilder(one).build(),
            new TreeBuilder(one).build(),
            new TreeBuilder(two).build(),
            new TreeBuilder(big).build(),
            new TreeBuilder(one).build())
        .displayHeight(40)
        .run(
            context -> {
              Helper.rootArray(context.document).data.get(4).valueParentRef.selectValue(context);
            })
        .checkScroll(37)
        .run(
            context -> {
              Helper.rootArray(context.document).data.get(0).valueParentRef.selectValue(context);
            })
        .checkCourse(0, -3, 7)
        .checkScroll(-13);
  }

  @Test
  public void testStaticArrayLayout() {
    new GeneralTestWizard(
            syntax,
            new TreeBuilder(array)
                .addArray("value", new TreeBuilder(one).build(), new TreeBuilder(two).build())
                .build())
        .checkTextBrick(0, 0, "[")
        .checkTextBrick(1, 0, "one")
        .checkTextBrick(1, 1, ", ")
        .checkTextBrick(2, 0, "two")
        .checkTextBrick(3, 0, "]");
  }
}
