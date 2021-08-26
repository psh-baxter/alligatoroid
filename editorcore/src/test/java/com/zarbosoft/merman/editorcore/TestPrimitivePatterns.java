package com.zarbosoft.merman.editorcore;

import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.syntax.FreeAtomType;
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
  public void testPatternAllowed() {
    final FreeAtomType quoted = new TypeBuilder("quoted")
            .back(Helper.buildBackDataPrimitiveLetters("value"))
            .frontDataPrimitive("value")
            .build();
    final Syntax syntax = new SyntaxBuilder("any")
            .type(quoted)
            .group("any", new GroupBuilder().type(quoted).build())
            .build();
    final Atom atom = new TreeBuilder(quoted).add("value", "").build();
    new GeneralTestWizard(syntax, atom)
        .run(editor -> atom.namedFields.getOpt("value").selectInto(editor.context))
        .sendText("a")
        .checkArrayTree(new TreeBuilder(quoted).add("value", "a").build());
  }

  @Test
  public void testPatternDisallowed() {
    final FreeAtomType quoted = new TypeBuilder("quoted")
            .back(Helper.buildBackDataPrimitiveLetters("value"))
            .frontDataPrimitive("value")
            .build();
    final Syntax syntax = new SyntaxBuilder("any")
            .type(quoted)
            .group("any", new GroupBuilder().type(quoted).build())
            .build();
    final Atom atom = new TreeBuilder(quoted).add("value", "").build();
    new GeneralTestWizard(syntax, atom)
        .run(editor -> atom.namedFields.getOpt("value").selectInto(editor.context))
        .sendText("1")
        .checkArrayTree(new TreeBuilder(quoted).add("value", "1").build());
  }
}
