package com.zarbosoft.merman.editorcore;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.editor.visual.tags.FreeTag;
import com.zarbosoft.merman.editor.visual.tags.StateTag;
import com.zarbosoft.merman.editorcore.helper.FrontMarkBuilder;
import com.zarbosoft.merman.editorcore.helper.GeneralTestWizard;
import com.zarbosoft.merman.editorcore.helper.GroupBuilder;
import com.zarbosoft.merman.editorcore.helper.Helper;
import com.zarbosoft.merman.editorcore.helper.SyntaxBuilder;
import com.zarbosoft.merman.editorcore.helper.TreeBuilder;
import com.zarbosoft.merman.editorcore.helper.TypeBuilder;
import com.zarbosoft.merman.syntax.FreeAtomType;
import com.zarbosoft.merman.syntax.Syntax;
import org.junit.Test;

public class TestLayoutRootArray {
  public static final FreeAtomType one;
  public static final FreeAtomType text;
  public static final Syntax syntax;

  static {
    one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .build();
    text =
        new TypeBuilder("text")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .build();
    syntax =
        new SyntaxBuilder("any")
            .type(one)
            .type(text)
            .group("any", new GroupBuilder().type(one).type(text).build())
            .style(
                new StyleBuilder()
                    .tag(new StateTag("compact"))
                    .tag(new FreeTag("split"))
                    .split(true)
                    .build())
            .addRootFrontPrefix(new FrontSpaceBuilder().tag("split").build())
            .build();
  }

  @Test
  public void testDynamicCompact() {
    final Atom text = new TreeBuilder(this.text).add("value", "").build();
    new GeneralTestWizard(syntax, new TreeBuilder(one).build(), text)
        .run(context -> text.fields.getOpt("value").selectInto(context))
        .resize(40)
        .checkSpaceBrick(0, 0)
        .checkTextBrick(0, 1, "one")
        .checkSpaceBrick(0, 2)
        .checkTextBrick(0, 3, "")
        .run(
            context -> {
              context.cursor.receiveText(context, "x");
              context.cursor.receiveText(context, "x");
              context.cursor.receiveText(context, "x");
            })
        .checkSpaceBrick(0, 0)
        .checkTextBrick(0, 1, "one")
        .checkSpaceBrick(1, 0)
        .checkTextBrick(1, 1, "xxx");
  }
}