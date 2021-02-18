package com.zarbosoft.merman.helper;

import com.zarbosoft.merman.syntax.FreeAtomType;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.merman.syntax.front.FrontSymbol;
import com.zarbosoft.merman.syntax.style.Style;
import com.zarbosoft.merman.syntax.symbol.SymbolSpaceSpec;

public class PrimitiveSyntax {
  public static final FreeAtomType primitive;
  public static final FreeAtomType low;
  public static final FreeAtomType high;
  public static final FreeAtomType quoted;
  public static final FreeAtomType array;
  public static final Syntax syntax;

  static {
    primitive =
        new TypeBuilder("primitive")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .autoComplete(99)
            .build();
    low =
        new TypeBuilder("low")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .precedence(0)
            .build();
    high =
        new TypeBuilder("high")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .precedence(100)
            .build();
    quoted =
        new TypeBuilder("quoted")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontMark("\"")
            .frontDataPrimitive("value")
            .frontMark("\"")
            .autoComplete(99)
            .build();
    array =
        new TypeBuilder("array")
            .back(Helper.buildBackDataArray("value", "any"))
            .front(new FrontDataArrayBuilder("value").build())
            .autoComplete(99)
            .build();
    syntax =
        new SyntaxBuilder("any")
            .type(primitive)
            .type(low)
            .type(high)
            .type(quoted)
            .type(array)
            .group(
                "any",
                new GroupBuilder()
                    .type(primitive)
                    .type(low)
                    .type(high)
                    .type(quoted)
                    .type(array)
                    .build())
            .addRootFrontPrefix(
                new FrontSymbol(
                    new FrontSymbol.Config(
                        new SymbolSpaceSpec(new SymbolSpaceSpec.Config().splitMode(Style.SplitMode.COMPACT)))))
            .build();
  }
}
