package com.zarbosoft.merman;

import com.zarbosoft.merman.editor.visual.tags.PartTag;
import com.zarbosoft.merman.helper.FrontDataArrayBuilder;
import com.zarbosoft.merman.helper.FrontMarkBuilder;
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

public class TestLayoutSpace {
  public static final FreeAtomType one;
  public static final FreeAtomType array;
  public static final Syntax syntax;

  static {
    one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .frontSpace()
            .front(new FrontMarkBuilder("one").build())
            .frontSpace()
            .build();
    array =
        new TypeBuilder("array")
            .back(Helper.buildBackDataArray("value", "any"))
            .frontSpace()
            .frontMark("[")
            .front(
                new FrontDataArrayBuilder("value")
                    .addSeparator(new FrontMarkBuilder(", ").build())
                    .build())
            .frontSpace()
            .frontMark("]")
            .autoComplete(99)
            .build();
    syntax =
        new SyntaxBuilder("any")
            .type(one)
            .type(array)
            .group("any", new GroupBuilder().type(one).type(array).build())
            .style(new StyleBuilder().tag(new PartTag("symbol-space")).split(true).build())
            .build();
  }

  @Test
  public void testLayoutInitial() {
    new GeneralTestWizard(syntax, new TreeBuilder(one).build())
        .checkSpaceBrick(0, 0)
        .checkTextBrick(0, 1, "one")
        .checkSpaceBrick(1, 0);
  }
}
