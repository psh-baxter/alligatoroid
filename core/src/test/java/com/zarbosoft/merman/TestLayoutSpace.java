package com.zarbosoft.merman;

import com.zarbosoft.merman.editor.visual.tags.Tags;
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
  @Test
  public void testLayoutInitial() {
    final FreeAtomType one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .frontSpace()
            .front(new FrontMarkBuilder("one").build())
            .frontSpace()
            .build();
    final Syntax syntax =
        new SyntaxBuilder("any")
            .type(one)
            .group("any", new GroupBuilder().type(one).build())
            .style(new StyleBuilder().tag(Tags.TAG_SYMBOL_SPACE).split(true).build())
            .build();
    new GeneralTestWizard(syntax, new TreeBuilder(one).build())
        .checkSpaceBrick(0, 0)
        .checkTextBrick(0, 1, "one")
        .checkSpaceBrick(1, 0);
  }
}
