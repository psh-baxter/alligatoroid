package com.zarbosoft.merman;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.ValuePrimitive;
import com.zarbosoft.merman.editor.history.changes.ChangePrimitiveRemove;
import com.zarbosoft.merman.editor.visual.tags.FreeTag;
import com.zarbosoft.merman.editor.visual.tags.TypeTag;
import com.zarbosoft.merman.helper.FrontDataArrayBuilder;
import com.zarbosoft.merman.helper.FrontMarkBuilder;
import com.zarbosoft.merman.helper.FrontSpaceBuilder;
import com.zarbosoft.merman.helper.GeneralTestWizard;
import com.zarbosoft.merman.helper.GroupBuilder;
import com.zarbosoft.merman.helper.Helper;
import com.zarbosoft.merman.helper.StyleBuilder;
import com.zarbosoft.merman.helper.SyntaxBuilder;
import com.zarbosoft.merman.helper.TreeBuilder;
import com.zarbosoft.merman.helper.TypeBuilder;
import com.zarbosoft.merman.syntax.FreeAtomType;
import com.zarbosoft.merman.syntax.Syntax;
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
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .build();
    two =
        new TypeBuilder("two")
            .back(Helper.buildBackPrimitive("two"))
            .front(new FrontMarkBuilder("two").build())
            .build();
    big =
        new TypeBuilder("big")
            .back(Helper.buildBackPrimitive("big"))
            .front(new FrontSpaceBuilder().build())
            .build();
    text =
        new TypeBuilder("text")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .build();
    array =
        new TypeBuilder("array")
            .back(Helper.buildBackDataArray("value", "any"))
            .frontMark("[")
            .front(
                new FrontDataArrayBuilder("value")
                    .addSeparator(new FrontMarkBuilder(", ").tag("separator").build())
                    .build())
            .frontMark("]")
            .autoComplete(99)
            .build();
    syntax =
        new SyntaxBuilder("any")
            .type(one)
            .type(two)
            .type(text)
            .type(array)
            .group("any", new GroupBuilder().type(one).type(two).type(text).type(array).build())
            .style(new StyleBuilder().split(true).build())
            .style(new StyleBuilder().tag(new FreeTag("separator")).split(false).build())
            .style(new StyleBuilder().tag(new TypeTag("big")).spaceTransverseAfter(60).build())
            .build();
    syntaxPadded =
        new SyntaxBuilder("any")
            .type(one)
            .type(two)
            .type(big)
            .type(text)
            .type(array)
            .group(
                "any",
                new GroupBuilder().type(one).type(two).type(big).type(text).type(array).build())
            .style(new StyleBuilder().split(true).build())
            .style(new StyleBuilder().tag(new FreeTag("separator")).split(false).build())
            .build();
    syntaxPadded.pad.converseStart = 5;
    syntaxPadded.pad.converseEnd = 5;
    syntaxPadded.pad.transverseStart = 9;
    syntaxPadded.pad.transverseEnd = 9;
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
        .checkCourse(1, 17, 27)
        .checkBanner(8, 10)
        .checkDetails(20, 27);
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
        .resizeTransitive(40)
        .checkScroll(-10)
        .checkBanner(8, 10)
        .checkDetails(20, 27);
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
        .resizeTransitive(40)
        .run(
            context -> {
              Helper.rootArray(context.document).data.get(4).parent.selectUp(context);
            })
        .checkCourse(4, 47, 57)
        .checkScroll(24)
        .checkCourse(3, 27, 37)
        .checkCourse(5, 64, 74)
        .checkBanner(21, 23)
        .checkDetails(33, 40);
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
        .resizeTransitive(40)
        .run(
            context -> {
              Helper.rootArray(context.document).data.get(4).parent.selectUp(context);
            })
        .checkScroll(24)
        .run(
            context -> {
              Helper.rootArray(context.document).data.get(0).parent.selectUp(context);
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
        .resizeTransitive(50)
        .run(
            context -> {
              Helper.rootArray(context.document).data.get(4).parent.selectUp(context);
            })
        .checkCourse(4, 47, 57)
        .checkScroll(23)
        .checkCourse(3, 27, 37)
        .checkCourse(5, 64, 74)
        .checkBanner(22, 24)
        .checkDetails(34, 41);
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
        .resizeTransitive(50)
        .run(
            context -> {
              Helper.rootArray(context.document).data.get(4).parent.selectUp(context);
            })
        .checkScroll(23)
        .run(
            context -> {
              Helper.rootArray(context.document).data.get(0).parent.selectUp(context);
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
        .resizeTransitive(40)
        .run(
            context -> {
              Helper.rootArray(context.document).data.get(4).parent.selectUp(context);
            })
        .checkCourse(4, 47, 107)
        .checkScroll(37)
        .checkCourse(3, 27, 37)
        .checkCourse(5, 114, 124)
        .checkBanner(8, 10)
        .checkDetails(33, 40);
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
        .resizeTransitive(40)
        .run(
            context -> {
              Helper.rootArray(context.document).data.get(4).parent.selectUp(context);
            })
        .checkScroll(37)
        .run(
            context -> {
              Helper.rootArray(context.document).data.get(0).parent.selectUp(context);
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

  @Test
  public void testDynamicWrapLayout() {
    final Atom text = new TreeBuilder(this.text).add("value", "123").build();
    new GeneralTestWizard(syntax, text)
        .run(context -> text.fields.getOpt("value").selectDown(context))
        .resize(40)
        .run(context -> context.cursor.receiveText(context, "4"))
        .checkCourseCount(1)
        .checkCourse(0, 0, 10)
        .checkBrickNotCompact(0, 0)
        .run(context -> context.cursor.receiveText(context, "5"))
        .checkCourseCount(2)
        .checkCourse(0, -10, 0)
        .checkCourse(1, 10, 20)
        .checkBrickCompact(0, 0)
        .run(context -> context.cursor.receiveText(context, "6"))
        .checkCourseCount(2)
        .checkCourse(0, -10, 0)
        .checkCourse(1, 10, 20);
  }

  @Test
  public void testDynamicUnwrapLayout() {
    final Atom text = new TreeBuilder(this.text).add("value", "123456").build();
    final ValuePrimitive primitive = (ValuePrimitive) text.fields.getOpt("value");
    new GeneralTestWizard(syntax, text)
        .run(context -> text.fields.getOpt("value").selectDown(context))
        .resize(40)
        .run(context -> context.history.apply(context, new ChangePrimitiveRemove(primitive, 5, 1)))
        .checkCourseCount(2)
        .checkCourse(0, -10, 0)
        .checkCourse(1, 10, 20)
        .run(context -> context.history.apply(context, new ChangePrimitiveRemove(primitive, 4, 1)))
        .checkCourseCount(1)
        .checkCourse(0, -10, 0)
        .run(context -> context.history.apply(context, new ChangePrimitiveRemove(primitive, 3, 1)))
        .checkCourseCount(1)
        .checkCourse(0, -10, 0)
        .run(context -> context.history.apply(context, new ChangePrimitiveRemove(primitive, 2, 1)))
        .checkCourseCount(1)
        .checkCourse(0, -10, 0);
  }

  @Test
  public void testDynamicArrayLayout() {
    final Atom gap = syntax.gap.create();
    new GeneralTestWizard(syntax, gap)
        .run(
            context -> {
              gap.fields.getOpt("gap").selectDown(context);
              context.cursor.receiveText(context, "[");
            })
        .checkTextBrick(0, 0, "[")
        .checkTextBrick(1, 0, "")
        .checkTextBrick(2, 0, "]");
  }
}
