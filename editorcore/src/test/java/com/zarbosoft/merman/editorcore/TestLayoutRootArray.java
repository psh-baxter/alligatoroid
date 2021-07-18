package com.zarbosoft.merman.editorcore;

import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.syntax.FreeAtomType;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.core.syntax.front.FrontSymbolSpec;
import com.zarbosoft.merman.core.syntax.style.Style;
import com.zarbosoft.merman.core.syntax.symbol.SymbolSpaceSpec;
import com.zarbosoft.merman.editorcore.helper.FrontMarkBuilder;
import com.zarbosoft.merman.editorcore.helper.GeneralTestWizard;
import com.zarbosoft.merman.editorcore.helper.GroupBuilder;
import com.zarbosoft.merman.editorcore.helper.Helper;
import com.zarbosoft.merman.editorcore.helper.SyntaxBuilder;
import com.zarbosoft.merman.editorcore.helper.TreeBuilder;
import com.zarbosoft.merman.editorcore.helper.TypeBuilder;
import org.junit.Test;

public class TestLayoutRootArray {
  @Test
  public void testDynamicCompact() {
    final FreeAtomType one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .build();
    final FreeAtomType textType =
        new TypeBuilder("text")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .build();
    final Syntax syntax =
        new SyntaxBuilder("any")
            .type(one)
            .type(textType)
            .group("any", new GroupBuilder().type(one).type(textType).build())
            .addRootFrontPrefix(
                new FrontSymbolSpec(
                    new FrontSymbolSpec.Config(
                        new SymbolSpaceSpec(
                            new SymbolSpaceSpec.Config().splitMode(Style.SplitMode.COMPACT)))))
            .build();
    final Atom text = new TreeBuilder(textType).add("value", "").build();
    new GeneralTestWizard(syntax, new TreeBuilder(one).build(), text)
        .run(editor -> text.fields.getOpt("value").selectInto(editor.context))
        .resize(40)
        .checkSpaceBrick(0, 0)
        .checkTextBrick(0, 1, "one")
        .checkSpaceBrick(0, 2)
        .checkTextBrick(0, 3, "")
        .run(
            editor -> {
              editor.context.cursor.handleTyping(editor.context, "x");
              editor.context.cursor.handleTyping(editor.context, "x");
              editor.context.cursor.handleTyping(editor.context, "x");
            })
        .checkSpaceBrick(0, 0)
        .checkTextBrick(0, 1, "one")
        .checkSpaceBrick(1, 0)
        .checkTextBrick(1, 1, "xxx");
  }
}
