package com.zarbosoft.merman.editorcore;

import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.document.fields.FieldPrimitive;
import com.zarbosoft.merman.core.syntax.FreeAtomType;
import com.zarbosoft.merman.core.syntax.GapAtomType;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.core.syntax.front.FrontPrimitiveSpec;
import com.zarbosoft.merman.core.syntax.front.FrontSymbol;
import com.zarbosoft.merman.core.syntax.style.Style;
import com.zarbosoft.merman.core.syntax.symbol.SymbolTextSpec;
import com.zarbosoft.merman.editorcore.helper.FrontDataArrayBuilder;
import com.zarbosoft.merman.editorcore.helper.FrontMarkBuilder;
import com.zarbosoft.merman.editorcore.helper.GeneralTestWizard;
import com.zarbosoft.merman.editorcore.helper.GroupBuilder;
import com.zarbosoft.merman.editorcore.helper.Helper;
import com.zarbosoft.merman.editorcore.helper.SyntaxBuilder;
import com.zarbosoft.merman.editorcore.helper.TreeBuilder;
import com.zarbosoft.merman.editorcore.helper.TypeBuilder;
import com.zarbosoft.merman.editorcore.history.changes.ChangePrimitive;
import org.junit.Test;

public class TestLayoutGeneral {
  @Test
  public void testDynamicWrapLayout() {
    final FreeAtomType textType =
        new TypeBuilder("text")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(
                new FrontPrimitiveSpec(
                    new FrontPrimitiveSpec.Config("value").splitMode(Style.SplitMode.ALWAYS)))
            .build();
    final Syntax syntax =
        new SyntaxBuilder("any")
            .type(textType)
            .group("any", new GroupBuilder().type(textType).build())
            .build();
    final Atom text = new TreeBuilder(textType).add("value", "123").build();
    new GeneralTestWizard(syntax, text)
        .run(editor -> text.fields.getOpt("value").selectInto(editor.context))
        .resize(40)
        .sendText("4")
        .checkCourseCount(1)
        .checkCourse(0, 0, 10)
        .checkBrickNotCompact(0, 0)
        .sendText("5")
        .checkCourseCount(2)
        .checkCourse(0, -10, 0)
        .checkCourse(1, 10, 20)
        .checkBrickCompact(0, 0)
        .sendText("6")
        .checkCourseCount(2)
        .checkCourse(0, -10, 0)
        .checkCourse(1, 10, 20);
  }

  @Test
  public void testDynamicUnwrapLayout() {
    final FreeAtomType textType =
        new TypeBuilder("text")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(
                new FrontPrimitiveSpec(
                    new FrontPrimitiveSpec.Config("value").splitMode(Style.SplitMode.ALWAYS)))
            .frontDataPrimitive("value")
            .build();
    final Syntax syntax =
        new SyntaxBuilder("any")
            .type(textType)
            .group("any", new GroupBuilder().type(textType).build())
            .build();
    final Atom text = new TreeBuilder(textType).add("value", "123456").build();
    final FieldPrimitive primitive = (FieldPrimitive) text.fields.getOpt("value");
    new GeneralTestWizard(syntax, text)
        .run(editor -> text.fields.getOpt("value").selectInto(editor.context))
        .resize(40)
        .change(new ChangePrimitive(primitive, 5, 1, ""))
        .checkCourseCount(2)
        .checkCourse(0, -10, 0)
        .checkCourse(1, 10, 20)
        .change(new ChangePrimitive(primitive, 4, 1, ""))
        .checkCourseCount(1)
        .checkCourse(0, -10, 0)
        .change(new ChangePrimitive(primitive, 3, 1, ""))
        .checkCourseCount(1)
        .checkCourse(0, -10, 0)
        .change(new ChangePrimitive(primitive, 2, 1, ""))
        .checkCourseCount(1)
        .checkCourse(0, -10, 0);
  }

  @Test
  public void testDynamicArrayLayout() {
    final FreeAtomType array =
        new TypeBuilder("array")
            .back(Helper.buildBackDataArray("value", "any"))
            .frontSplitMark("[")
            .front(
                new FrontDataArrayBuilder("value")
                    .addSeparator(
                        new FrontSymbol(
                            new FrontSymbol.Config(
                                new SymbolTextSpec(new SymbolTextSpec.Config(", ")))))
                    .build())
            .frontSplitMark("]")
            .autoComplete(true)
            .build();
    FreeAtomType infinity = // to prevent default creation
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(true)
            .build();
    final Syntax syntax =
        new SyntaxBuilder("any")
            .type(array)
            .type(infinity)
            .group("any", new GroupBuilder().type(array).type(infinity).build())
            .build();
    final Atom gap = new TreeBuilder(syntax.gap).add(GapAtomType.PRIMITIVE_KEY, "").build();
    new GeneralTestWizard(syntax, gap)
        .run(
            editor -> {
              gap.fields.getOpt("gap").selectInto(editor.context);
            })
        .sendText("[")
        .checkTextBrick(0, 0, "[")
        .checkTextBrick(1, 0, "]");
  }
}
