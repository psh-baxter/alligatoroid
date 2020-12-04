package com.zarbosoft.merman;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.helper.GeneralTestWizard;
import com.zarbosoft.merman.helper.GroupBuilder;
import com.zarbosoft.merman.helper.Helper;
import com.zarbosoft.merman.helper.SyntaxBuilder;
import com.zarbosoft.merman.helper.TreeBuilder;
import com.zarbosoft.merman.helper.TypeBuilder;
import com.zarbosoft.merman.syntax.FreeAtomType;
import com.zarbosoft.merman.syntax.Syntax;
import org.junit.Test;

public class TestPrimitivePatterns {
  public static final FreeAtomType unquoted;
  public static final FreeAtomType quoted;
  public static final Syntax syntax;

  static {
    unquoted =
        new TypeBuilder("unquoted")
            .back(Helper.buildBackDataPrimitiveLetters("value"))
            .frontDataPrimitive("value")
            .build();
    quoted =
        new TypeBuilder("quoted")
            .back(Helper.buildBackDataPrimitiveLetters("value"))
            .frontMark("\"")
            .frontDataPrimitive("value")
            .frontMark("\"")
            .build();
    syntax =
        new SyntaxBuilder("any")
            .type(unquoted)
            .type(quoted)
            .group("any", new GroupBuilder().type(unquoted).type(quoted).build())
            .build();
    syntax.retryExpandFactor = 1.05;
  }

  @Test
  public void testQuotedAllowed() {
    final Atom atom = new TreeBuilder(quoted).add("value", "").build();
    new GeneralTestWizard(syntax, atom)
        .run(context -> atom.fields.getOpt("value").selectDown(context))
        .sendText("a")
        .checkArrayTree(new TreeBuilder(quoted).add("value", "a").build());
  }

  @Test
  public void testQuotedDisallowed() {
    final Atom atom = new TreeBuilder(quoted).add("value", "").build();
    new GeneralTestWizard(syntax, atom)
        .run(context -> atom.fields.getOpt("value").selectDown(context))
        .sendText("1")
        .checkArrayTree(new TreeBuilder(quoted).add("value", "").build());
  }

  @Test
  public void testDisallowedSuffix() {
    final Atom atom = new TreeBuilder(unquoted).add("value", "").build();
    new GeneralTestWizard(syntax, atom)
        .run(context -> atom.fields.getOpt("value").selectDown(context))
        .sendText("1")
        .checkArrayTree(
            new TreeBuilder(syntax.suffixGap)
                .addArray("value", new TreeBuilder(unquoted).add("value", "").build())
                .add("gap", "1")
                .build());
  }
}
