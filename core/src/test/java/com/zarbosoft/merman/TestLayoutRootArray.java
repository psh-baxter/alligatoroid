package com.zarbosoft.merman;

import com.zarbosoft.merman.helper.FrontMarkBuilder;
import com.zarbosoft.merman.helper.GeneralTestWizard;
import com.zarbosoft.merman.helper.GroupBuilder;
import com.zarbosoft.merman.helper.Helper;
import com.zarbosoft.merman.helper.SyntaxBuilder;
import com.zarbosoft.merman.helper.TreeBuilder;
import com.zarbosoft.merman.helper.TypeBuilder;
import com.zarbosoft.merman.core.syntax.FreeAtomType;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.core.syntax.front.FrontSymbol;
import com.zarbosoft.merman.core.syntax.style.Style;
import com.zarbosoft.merman.core.syntax.symbol.SymbolSpaceSpec;
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
            .addRootFrontPrefix(
                new FrontSymbol(
                    new FrontSymbol.Config(
                        new SymbolSpaceSpec(new SymbolSpaceSpec.Config().splitMode(Style.SplitMode.COMPACT)))))
            .build();
  }

  @Test
  public void testLayoutInitial() {
    new GeneralTestWizard(syntax, new TreeBuilder(one).build())
        .checkSpaceBrick(0, 0)
        .checkTextBrick(0, 1, "one");
  }

  @Test
  public void testCompact() {
    new GeneralTestWizard(syntax, new TreeBuilder(one).build(), new TreeBuilder(one).build())
        .checkSpaceBrick(0, 0)
        .checkTextBrick(0, 1, "one")
        .checkSpaceBrick(0, 2)
        .checkTextBrick(0, 3, "one")
        .displayWidth(40)
        .checkSpaceBrick(0, 0)
        .checkTextBrick(0, 1, "one")
        .checkSpaceBrick(1, 0)
        .checkTextBrick(1, 1, "one");
  }
}
