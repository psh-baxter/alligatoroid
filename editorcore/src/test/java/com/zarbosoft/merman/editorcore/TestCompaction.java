package com.zarbosoft.merman.editorcore;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.ValueArray;
import com.zarbosoft.merman.document.values.ValuePrimitive;
import com.zarbosoft.merman.editor.visual.tags.FreeTag;
import com.zarbosoft.merman.editor.visual.tags.StateTag;
import com.zarbosoft.merman.editorcore.helper.FrontDataArrayBuilder;
import com.zarbosoft.merman.editorcore.helper.FrontMarkBuilder;
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

public class TestCompaction {
  public static final FreeAtomType one;
  public static final FreeAtomType initialSplitOne;
  public static final FreeAtomType text;
  public static final FreeAtomType comboText;
  public static final FreeAtomType initialSplitText;
  public static final FreeAtomType infinity;
  public static final FreeAtomType line;
  public static final FreeAtomType low;
  public static final FreeAtomType unary;
  public static final FreeAtomType mid;
  public static final FreeAtomType high;
  public static final Syntax syntax;
  public static class GeneralTestWizard extends com.zarbosoft.merman.editorcore.helper.GeneralTestWizard {
    public GeneralTestWizard(Syntax syntax, Atom... atoms) {
      super(syntax, atoms);
      this.inner.context.ellipsizeThreshold = 2;
    }
  }

  static {
    infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .build();
    one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .build();
    initialSplitOne =
        new TypeBuilder("splitOne")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").tag("split").build())
            .build();
    text =
        new TypeBuilder("text")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .build();
    comboText =
        new TypeBuilder("comboText")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .frontMark("123")
            .build();
    initialSplitText =
        new TypeBuilder("splitText")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(new FrontDataPrimitiveBuilder("value").tag("split").build())
            .build();
    line =
        new TypeBuilder("line")
            .back(Helper.buildBackDataArray("value", "any"))
            .front(
                new FrontDataArrayBuilder("value")
                    .addPrefix(new FrontSpaceBuilder().build())
                    .build())
            .precedence(0)
            .depthScore(1)
            .build();
    low =
        new TypeBuilder("low")
            .back(Helper.buildBackDataArray("value", "any"))
            .front(
                new FrontDataArrayBuilder("value")
                    .addPrefix(new FrontSpaceBuilder().tag("split").build())
                    .build())
            .precedence(0)
            .depthScore(1)
            .build();
    unary =
        new TypeBuilder("unary")
            .back(Helper.buildBackDataAtom("value", "any"))
            .frontDataNode("value")
            .precedence(20)
            .depthScore(1)
            .build();
    mid =
        new TypeBuilder("mid")
            .back(Helper.buildBackDataArray("value", "any"))
            .front(
                new FrontDataArrayBuilder("value")
                    .addPrefix(new FrontSpaceBuilder().tag("split").build())
                    .build())
            .precedence(50)
            .depthScore(1)
            .build();
    high =
        new TypeBuilder("high")
            .back(Helper.buildBackDataArray("value", "any"))
            .front(
                new FrontDataArrayBuilder("value")
                    .addPrefix(new FrontSpaceBuilder().tag("split").build())
                    .build())
            .precedence(100)
            .depthScore(1)
            .build();
    syntax =
        new SyntaxBuilder("any")
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
            .style(
                new StyleBuilder()
                    .tag(new FreeTag("split"))
                    .tag(new StateTag("compact"))
                    .split(true)
                    .build())
            .build();
  }

  @Test
  public void testSplitDynamic() {
    final Atom lowAtom =
        new TreeBuilder(low)
            .addArray("value", new TreeBuilder(one).build(), new TreeBuilder(one).build())
            .build();
    final ValueArray array = (ValueArray) lowAtom.fields.getOpt("value");
    new GeneralTestWizard(syntax, lowAtom)
        .resize(70)
        .checkCourseCount(1)
        .checkTextBrick(0, 1, "one")
        .checkTextBrick(0, 3, "one")
        .run(
            context ->
                context.history.apply(
                    context,
                    new ChangeArray(array, 2, 0, ImmutableList.of(new TreeBuilder(one).build()))))
        .checkCourseCount(3)
        .checkTextBrick(0, 1, "one")
        .checkTextBrick(1, 1, "one")
        .checkTextBrick(2, 1, "one")
        .run(
            context ->
                context.history.apply(context, new ChangeArray(array, 2, 1, ImmutableList.of())))
        .checkCourseCount(1)
        .checkTextBrick(0, 1, "one")
        .checkTextBrick(0, 3, "one");
  }

  @Test
  public void testSplitDynamicBrickChange() {
    final Atom textAtom = new TreeBuilder(text).add("value", "oran").build();
    final ValuePrimitive text = (ValuePrimitive) textAtom.fields.getOpt("value");
    new GeneralTestWizard(
            syntax,
            new TreeBuilder(low).addArray("value", new TreeBuilder(one).build(), textAtom).build())
        .resize(80)
        .checkCourseCount(1)
        .checkTextBrick(0, 1, "one")
        .checkTextBrick(0, 3, "oran")
        .run(context -> context.history.apply(context, new ChangePrimitiveAdd(text, 4, "ge")))
        .checkCourseCount(2)
        .checkTextBrick(0, 1, "one")
        .checkTextBrick(1, 1, "orange")
        .run(context -> context.history.apply(context, new ChangePrimitiveRemove(text, 4, 2)))
        .checkCourseCount(1)
        .checkTextBrick(0, 1, "one")
        .checkTextBrick(0, 3, "oran");
  }

  @Test
  public void testSplitDynamicComboBrick() {
    final Atom primitive = new TreeBuilder(comboText).add("value", "I am a banana").build();
    new GeneralTestWizard(syntax, primitive)
        .resize(140)
        .checkTextBrick(0, 0, "I am a banana")
        .checkTextBrick(0, 1, "123")
        .run(
            context ->
                context.history.apply(
                    context,
                    new ChangePrimitiveAdd(
                        (ValuePrimitive) primitive.fields.getOpt("value"), 0, "wigwam ")))
        .checkTextBrick(0, 0, "wigwam I am a ")
        .checkTextBrick(1, 0, "banana")
        .checkTextBrick(1, 1, "123");
  }

  @Test
  public void testSplitNested() {
    final Atom lowAtom =
        new TreeBuilder(low)
            .addArray("value", new TreeBuilder(one).build(), new TreeBuilder(one).build())
            .build();
    final ValueArray array = (ValueArray) lowAtom.fields.getOpt("value");
    new GeneralTestWizard(syntax, new TreeBuilder(unary).add("value", lowAtom).build())
        .resize(70)
        .checkCourseCount(1)
        .checkTextBrick(0, 1, "one")
        .checkTextBrick(0, 3, "one")
        .run(
            context ->
                context.history.apply(
                    context,
                    new ChangeArray(array, 2, 0, ImmutableList.of(new TreeBuilder(one).build()))))
        .checkCourseCount(3)
        .checkTextBrick(0, 1, "one")
        .checkTextBrick(1, 1, "one")
        .checkTextBrick(2, 1, "one")
        .run(
            context ->
                context.history.apply(context, new ChangeArray(array, 2, 1, ImmutableList.of())))
        .checkCourseCount(1)
        .checkTextBrick(0, 1, "one")
        .checkTextBrick(0, 3, "one");
  }

  @Test
  public void testSplitOrderRuleDynamic() {
    final Atom highAtom =
        new TreeBuilder(high)
            .addArray("value", new TreeBuilder(one).build(), new TreeBuilder(one).build())
            .build();
    final ValueArray array = (ValueArray) highAtom.fields.getOpt("value");
    new GeneralTestWizard(
            syntax,
            new TreeBuilder(mid)
                .addArray(
                    "value",
                    new TreeBuilder(low)
                        .addArray(
                            "value", new TreeBuilder(one).build(), new TreeBuilder(one).build())
                        .build(),
                    highAtom)
                .build())
        .resize(80)
        .checkCourseCount(4)
        .checkTextBrick(1, 1, "one")
        .checkTextBrick(2, 1, "one")
        .checkTextBrick(3, 2, "one")
        .checkTextBrick(3, 4, "one")
        .run(
            context ->
                context.history.apply(
                    context,
                    new ChangeArray(array, 2, 0, ImmutableList.of(new TreeBuilder(one).build()))))
        .checkCourseCount(7)
        .checkTextBrick(1, 1, "one")
        .checkTextBrick(2, 1, "one")
        .checkTextBrick(4, 1, "one")
        .checkTextBrick(5, 1, "one")
        .checkTextBrick(6, 1, "one")
        .run(
            context ->
                context.history.apply(context, new ChangeArray(array, 2, 1, ImmutableList.of())))
        .checkCourseCount(4)
        .checkTextBrick(1, 1, "one")
        .checkTextBrick(2, 1, "one")
        .checkTextBrick(3, 2, "one")
        .checkTextBrick(3, 4, "one");
  }

  @Test
  public void testStartCompactDynamic() {
    final Atom lowAtom =
        new TreeBuilder(low)
            .addArray(
                "value",
                new TreeBuilder(one).build(),
                new TreeBuilder(one).build(),
                new TreeBuilder(infinity).build())
            .build();
    final ValueArray array = (ValueArray) lowAtom.fields.getOpt("value");
    new GeneralTestWizard(syntax, lowAtom)
        .resize(100)
        .checkTextBrick(0, 1, "one")
        .checkTextBrick(1, 1, "one")
        .checkTextBrick(2, 1, "infinity")
        .run(
            context ->
                context.history.apply(
                    context,
                    new ChangeArray(array, 1, 0, ImmutableList.of(new TreeBuilder(one).build()))))
        .checkTextBrick(0, 1, "one")
        .checkTextBrick(1, 1, "one")
        .checkTextBrick(2, 1, "one")
        .checkTextBrick(3, 1, "infinity");
  }
}
