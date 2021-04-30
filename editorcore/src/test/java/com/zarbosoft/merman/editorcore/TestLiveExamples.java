package com.zarbosoft.merman.editorcore;

import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.syntax.FreeAtomType;
import com.zarbosoft.merman.core.syntax.GapAtomType;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.core.syntax.front.FrontPrimitiveSpec;
import com.zarbosoft.merman.core.syntax.style.Style;
import com.zarbosoft.merman.editorcore.helper.FrontDataArrayBuilder;
import com.zarbosoft.merman.editorcore.helper.FrontMarkBuilder;
import com.zarbosoft.merman.editorcore.helper.GeneralTestWizard;
import com.zarbosoft.merman.editorcore.helper.GroupBuilder;
import com.zarbosoft.merman.editorcore.helper.Helper;
import com.zarbosoft.merman.editorcore.helper.SyntaxBuilder;
import com.zarbosoft.merman.editorcore.helper.TreeBuilder;
import com.zarbosoft.merman.editorcore.helper.TypeBuilder;
import com.zarbosoft.merman.editorcore.history.changes.ChangeArray;
import com.zarbosoft.rendaw.common.TSList;
import org.junit.Test;

public class TestLiveExamples {

  @Test
  public void testDeselectGapInArray() {
    FreeAtomType array =
        new TypeBuilder("array")
            .back(Helper.buildBackDataArray("value", "any"))
            .frontMark("[")
            .front(
                new FrontDataArrayBuilder("value")
                    .addSeparator(new FrontMarkBuilder(", ").build())
                    .build())
            .frontMark("]")
            .autoComplete(true)
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(array)
            .group("any", new GroupBuilder().type(array).build())
            .build();
    new GeneralTestWizard(
            syntax, new TreeBuilder(syntax.gap).add(GapAtomType.GAP_PRIMITIVE_KEY, "").build())
        .actEnter()
        .sendText("[")
        .checkTotalBrickCount(3)
        .sendText("e")
        .checkArrayTree(
            new TreeBuilder(array)
                .addArray("value", new TreeBuilder(syntax.gap).add("gap", "e").build())
                .build())
        .actExit()
        .actExit();
  }

  @Test
  public void testDeleteArray() {
    FreeAtomType array =
        new TypeBuilder("array")
            .back(Helper.buildBackDataArray("value", "any"))
            .frontMark("[")
            .front(
                new FrontDataArrayBuilder("value")
                    .addSeparator(new FrontMarkBuilder(", ").build())
                    .build())
            .frontMark("]")
            .autoComplete(true)
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(array)
            .group("any", new GroupBuilder().type(array).build())
            .build();
    new GeneralTestWizard(
            syntax, new TreeBuilder(syntax.gap).add(GapAtomType.GAP_PRIMITIVE_KEY, "").build())
        .actEnter()
        .sendText("[")
        .actExit()
        .actExit()
        .checkArrayTree(new TreeBuilder(array).addArray("value").build())
        .editDelete()
        .checkTotalBrickCount(1);
  }

  @Test
  public void testAddElementAfterArray() {
    FreeAtomType array =
        new TypeBuilder("array")
            .back(Helper.buildBackDataArray("value", "any"))
            .frontMark("[")
            .front(
                new FrontDataArrayBuilder("value")
                    .addSeparator(new FrontMarkBuilder(", ").build())
                    .build())
            .frontMark("]")
            .autoComplete(true)
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(array)
            .group("any", new GroupBuilder().type(array).build())
            .build();
    new GeneralTestWizard(
            syntax, new TreeBuilder(syntax.gap).add(GapAtomType.GAP_PRIMITIVE_KEY, "").build())
        .actEnter()
        .sendText("[")
        .checkTextBrick(0, 0, "[")
        .checkTextBrick(0, 2, "]")
        .actExit()
        .checkTextBrick(0, 0, "[")
        .checkSpaceBrick(0, 1)
        .checkTextBrick(0, 2, "]")
        .editInsertAfter()
        .checkTextBrick(0, 0, "[")
        .checkSpaceBrick(0, 1)
        .checkTextBrick(0, 2, "]")
        .checkTextBrick(0, 3, "");
  }

  @Test
  public void testDeleteCornerstoneCourseJoin() {
    /*
    Conditions
    1. Remove primitive, since it clears bricks and getFirst/Last brick will cause bounds error
    2. Removing primitive causes join:
    	2.1 Primitive is first in course
    	2.2 Primitive followed by something
    3. Primitive brick in join was cornerstone; join resets cornerstone so removed brick is readded
    4. Selection in parent (primitive not selected) so attachments survive join.
    5. Selection attachment base is primitive.
     */
    final FreeAtomType one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .precedence(20)
            .build();
    final FreeAtomType text =
        new TypeBuilder("text")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(
                new FrontPrimitiveSpec(
                    new FrontPrimitiveSpec.Config("value").splitMode(Style.SplitMode.ALWAYS)))
            .frontDataPrimitive("value")
            .precedence(10)
            .build();
    final Syntax syntax =
        new SyntaxBuilder("any")
            .type(text)
            .type(one)
            .group("any", new GroupBuilder().type(text).type(one).build())
            .build();
    final Atom atom = new TreeBuilder(text).add("value", "alo").build();
    new GeneralTestWizard(syntax, new TreeBuilder(one).build(), atom, new TreeBuilder(one).build())
        .run(
            editor -> {
              Helper.rootArray(editor.context.document).selectInto(editor.context, true, 1, 1);
            })
        .checkCourseCount(2)
        .run(
            editor ->
                editor.history.record(
                    editor.context,
                    null,
                    r ->
                        r.apply(
                            editor.context,
                            new ChangeArray(
                                Helper.rootArray(editor.context.document), 1, 1, TSList.of()))));
  }
}
