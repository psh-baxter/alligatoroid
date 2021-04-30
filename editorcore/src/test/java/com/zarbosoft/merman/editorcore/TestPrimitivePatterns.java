package com.zarbosoft.merman.editorcore;

import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.syntax.FreeAtomType;
import com.zarbosoft.merman.core.syntax.GapAtomType;
import com.zarbosoft.merman.core.syntax.SuffixGapAtomType;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.editorcore.helper.GeneralTestWizard;
import com.zarbosoft.merman.editorcore.helper.GroupBuilder;
import com.zarbosoft.merman.editorcore.helper.Helper;
import com.zarbosoft.merman.editorcore.helper.SyntaxBuilder;
import com.zarbosoft.merman.editorcore.helper.TreeBuilder;
import com.zarbosoft.merman.editorcore.helper.TypeBuilder;
import org.junit.Test;

public class TestPrimitivePatterns {
  @Test
  public void testQuotedAllowed() {
    final FreeAtomType unquoted = new TypeBuilder("unquoted")
            .back(Helper.buildBackDataPrimitiveLetters("value"))
            .frontDataPrimitive("value")
            .build();
    final FreeAtomType quoted = new TypeBuilder("quoted")
            .back(Helper.buildBackDataPrimitiveLetters("value"))
            .frontMark("\"")
            .frontDataPrimitive("value")
            .frontMark("\"")
            .build();
    final Syntax syntax = new SyntaxBuilder("any")
            .type(unquoted)
            .type(quoted)
            .group("any", new GroupBuilder().type(unquoted).type(quoted).build())
            .build();
    final Atom atom = new TreeBuilder(quoted).add("value", "").build();
    new GeneralTestWizard(syntax, atom)
            .run(editor -> editor.context.retryExpandFactor = 1.05)
        .run(editor -> atom.fields.getOpt("value").selectInto(editor.context))
        .sendText("a")
        .checkArrayTree(new TreeBuilder(quoted).add("value", "a").build());
  }

  @Test
  public void testQuotedDisallowed() {
    final FreeAtomType unquoted = new TypeBuilder("unquoted")
            .back(Helper.buildBackDataPrimitiveLetters("value"))
            .frontDataPrimitive("value")
            .build();
    final FreeAtomType quoted = new TypeBuilder("quoted")
            .back(Helper.buildBackDataPrimitiveLetters("value"))
            .frontMark("\"")
            .frontDataPrimitive("value")
            .frontMark("\"")
            .build();
    final Syntax syntax = new SyntaxBuilder("any")
            .type(unquoted)
            .type(quoted)
            .group("any", new GroupBuilder().type(unquoted).type(quoted).build())
            .build();
    final Atom atom = new TreeBuilder(quoted).add("value", "").build();
    new GeneralTestWizard(syntax, atom)
            .run(editor -> editor.context.retryExpandFactor = 1.05)
        .run(editor -> atom.fields.getOpt("value").selectInto(editor.context))
        .sendText("1")
        .checkArrayTree(new TreeBuilder(quoted).add("value", "").build());
  }

  @Test
  public void testDisallowedSuffix() {
    final FreeAtomType unquoted = new TypeBuilder("unquoted")
            .back(Helper.buildBackDataPrimitiveLetters("value"))
            .frontDataPrimitive("value")
            .build();
    final FreeAtomType quoted = new TypeBuilder("quoted")
            .back(Helper.buildBackDataPrimitiveLetters("value"))
            .frontMark("\"")
            .frontDataPrimitive("value")
            .frontMark("\"")
            .build();
    final Syntax syntax = new SyntaxBuilder("any")
            .type(unquoted)
            .type(quoted)
            .group("any", new GroupBuilder().type(unquoted).type(quoted).build())
            .build();
    final Atom atom = new TreeBuilder(unquoted).add("value", "").build();
    new GeneralTestWizard(syntax, atom)
            .run(editor -> editor.context.retryExpandFactor = 1.05)
        .run(editor -> atom.fields.getOpt("value").selectInto(editor.context))
        .sendText("1")
        .checkArrayTree(
            new TreeBuilder(syntax.suffixGap)
                .addArray(SuffixGapAtomType.PRECEDING_KEY, new TreeBuilder(unquoted).add("value", "").build())
                .add(GapAtomType.GAP_PRIMITIVE_KEY, "1")
                .build());
  }
}
