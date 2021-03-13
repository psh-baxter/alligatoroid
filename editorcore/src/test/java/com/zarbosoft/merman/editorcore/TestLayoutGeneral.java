package com.zarbosoft.merman.editorcore;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.FieldPrimitive;
import com.zarbosoft.merman.editor.visual.tags.FreeTag;
import com.zarbosoft.merman.editor.visual.tags.TypeTag;
import com.zarbosoft.merman.editorcore.helper.FrontMarkBuilder;
import com.zarbosoft.merman.editorcore.helper.GeneralTestWizard;
import com.zarbosoft.merman.editorcore.helper.GroupBuilder;
import com.zarbosoft.merman.editorcore.helper.SyntaxBuilder;
import com.zarbosoft.merman.editorcore.helper.TreeBuilder;
import com.zarbosoft.merman.editorcore.helper.TypeBuilder;
import com.zarbosoft.merman.editorcore.history.changes.ChangePrimitiveRemove;
import com.zarbosoft.merman.syntax.FreeAtomType;
import com.zarbosoft.merman.syntax.Padding;
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
                .pad(new Padding(5,5,9,9))
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
  }

  @Test
  public void testDynamicWrapLayout() {
    final Atom text = new TreeBuilder(this.text).add("value", "123").build();
    new GeneralTestWizard(syntax, text)
        .run(context -> text.fields.getOpt("value").selectInto(context))
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
    final FieldPrimitive primitive = (FieldPrimitive) text.fields.getOpt("value");
    new GeneralTestWizard(syntax, text)
        .run(context -> text.fields.getOpt("value").selectInto(context))
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
              gap.fields.getOpt("gap").selectInto(context);
              context.cursor.receiveText(context, "[");
            })
        .checkTextBrick(0, 0, "[")
        .checkTextBrick(1, 0, "")
        .checkTextBrick(2, 0, "]");
  }
}
