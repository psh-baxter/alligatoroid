package com.zarbosoft.merman.editorcore;

import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.document.fields.FieldPrimitive;
import com.zarbosoft.merman.core.syntax.FreeAtomType;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.core.syntax.front.FrontSymbol;
import com.zarbosoft.merman.core.syntax.style.Style;
import com.zarbosoft.merman.core.syntax.symbol.SymbolSpaceSpec;
import com.zarbosoft.merman.editorcore.helper.GeneralTestWizard;
import com.zarbosoft.merman.editorcore.helper.GroupBuilder;
import com.zarbosoft.merman.editorcore.helper.Helper;
import com.zarbosoft.merman.editorcore.helper.SyntaxBuilder;
import com.zarbosoft.merman.editorcore.helper.TreeBuilder;
import com.zarbosoft.merman.editorcore.helper.TypeBuilder;
import com.zarbosoft.merman.editorcore.history.changes.ChangePrimitive;
import org.hamcrest.number.IsCloseTo;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TestPrimitiveBreaking {
  @Test
  public void testUnbreakableDynamic() {
    final FreeAtomType primitiveType =
        new TypeBuilder("primitive")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .autoComplete(true) // was 99
            .build();
    final Syntax syntax =
        new SyntaxBuilder("any")
            .type(primitiveType)
            .group("any", new GroupBuilder().type(primitiveType).build())
            .addRootFrontPrefix(
                new FrontSymbol(
                    new FrontSymbol.Config(
                        new SymbolSpaceSpec(
                            new SymbolSpaceSpec.Config().splitMode(Style.SplitMode.COMPACT)))))
            .build();
    final Atom primitive = new TreeBuilder(primitiveType).add("value", "123").build();
    new GeneralTestWizard(syntax, primitive)
        .resize(40)
        .run(editor -> primitive.fields.getOpt("value").selectInto(editor.context))
        .sendText("4")
        .checkTextBrick(0, 1, "1234")
        .sendText("5")
        .checkTextBrick(0, 1, "1234")
        .checkTextBrick(1, 0, "5")
        .sendText("6")
        .checkTextBrick(0, 1, "1234")
        .checkTextBrick(1, 0, "56");
  }

  @Test
  public void testUnbreakCursor() {
    final FreeAtomType quoted =
        new TypeBuilder("quoted")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontMark("\"")
            .frontDataPrimitive("value")
            .frontMark("\"")
            .autoComplete(true) // was 99
            .build();
    final Syntax syntax =
        new SyntaxBuilder("any")
            .type(quoted)
            .group("any", new GroupBuilder().type(quoted).build())
            .addRootFrontPrefix(
                new FrontSymbol(
                    new FrontSymbol.Config(
                        new SymbolSpaceSpec(
                            new SymbolSpaceSpec.Config().splitMode(Style.SplitMode.COMPACT)))))
            .build();
    final Atom primitiveAtom = new TreeBuilder(quoted).add("value", "12345").build();
    final FieldPrimitive primitive = (FieldPrimitive) primitiveAtom.fields.getOpt("value");
    new GeneralTestWizard(syntax, primitiveAtom)
        .run(editor -> ((FieldPrimitive) primitive).visual.select(editor.context, true, 5, 5))
        .resize(50)
        .checkCourseCount(2)
        .checkCourse(0, -10, 0)
        .checkCourse(1, 10, 20)
        .run(
            editor ->
                editor.history.record(
                    editor.context,
                    null,
                    r -> r.apply(editor.context, new ChangePrimitive(primitive, 3, 2, ""))))
        .checkCourseCount(1)
        .checkCourse(0, -10, 0)
        .run(
            context -> {
              assertThat(primitive.visual.cursor.range.cursor.drawing.transverse(), new IsCloseTo(-13.0, 0.01));
              assertThat(primitive.visual.cursor.range.cursor.drawing.transverseEdge(), new IsCloseTo(1, 0.01));
            });
  }

  @Test
  public void testUnbreakCursorSplit() {
    final FreeAtomType primitiveType =
        new TypeBuilder("primitive")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .autoComplete(true) // was 99
            .build();
    final FreeAtomType quoted =
        new TypeBuilder("quoted")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontMark("\"")
            .frontDataPrimitive("value")
            .frontMark("\"")
            .autoComplete(true) // was 99
            .build();
    final Syntax syntax =
        new SyntaxBuilder("any")
            .type(primitiveType)
            .type(quoted)
            .group("any", new GroupBuilder().type(primitiveType).type(quoted).build())
            .addRootFrontPrefix(
                new FrontSymbol(
                    new FrontSymbol.Config(
                        new SymbolSpaceSpec(
                            new SymbolSpaceSpec.Config().splitMode(Style.SplitMode.COMPACT)))))
            .build();
    final Atom primitiveAtom = new TreeBuilder(quoted).add("value", "123456").build();
    final FieldPrimitive primitive = (FieldPrimitive) primitiveAtom.fields.getOpt("value");
    new GeneralTestWizard(
            syntax, new TreeBuilder(primitiveType).add("value", "aaaaa").build(), primitiveAtom)
        .run(editor -> ((FieldPrimitive) primitive).visual.select(editor.context, true, 6, 6))
        .resize(100)
        .checkCourseCount(2)
        .checkCourse(0, -10, 0)
        .checkCourse(1, 10, 20)
        .run(
            editor ->
                editor.history.record(
                    editor.context,
                    null,
                    r -> r.apply(editor.context, new ChangePrimitive(primitive, 1, 5, ""))))
        .checkCourseCount(1)
        .checkCourse(0, -10, 0)
        .run(
            context -> {
              assertThat(primitive.visual.cursor.range.cursor.drawing.transverse(), new IsCloseTo(-13, 0.01));
              assertThat(primitive.visual.cursor.range.cursor.drawing.transverseEdge(), new IsCloseTo(1, 0.01));
            });
  }

  @Test
  public void testUnbreakClear() {
    final FreeAtomType primitiveType =
        new TypeBuilder("primitive")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .autoComplete(true) // was 99
            .build();
    final Syntax syntax =
        new SyntaxBuilder("any")
            .type(primitiveType)
            .group("any", new GroupBuilder().type(primitiveType).build())
            .addRootFrontPrefix(
                new FrontSymbol(
                    new FrontSymbol.Config(
                        new SymbolSpaceSpec(
                            new SymbolSpaceSpec.Config().splitMode(Style.SplitMode.COMPACT)))))
            .build();
    final Atom primitiveAtom = new TreeBuilder(primitiveType).add("value", "word egg").build();
    final FieldPrimitive primitive = (FieldPrimitive) primitiveAtom.fields.getOpt("value");
    new GeneralTestWizard(syntax, primitiveAtom)
        .resize(40)
        .checkCourseCount(2)
        .run(editor -> ((FieldPrimitive) primitive).visual.select(editor.context, true, 0, 0))
        .run(
            editor -> {
              editor.history.record(
                  editor.context,
                  null,
                  r -> r.apply(editor.context, new ChangePrimitive(primitive, 0, 8, "")));
            })
        .checkCourseCount(1)
        .checkTextBrick(0, 1, "");
  }

  @Test
  public void testUnbreakClearEnd() {
    final FreeAtomType primitiveType =
        new TypeBuilder("primitive")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .autoComplete(true) // was 99
            .build();
    final Syntax syntax =
        new SyntaxBuilder("any")
            .type(primitiveType)
            .group("any", new GroupBuilder().type(primitiveType).build())
            .addRootFrontPrefix(
                new FrontSymbol(
                    new FrontSymbol.Config(
                        new SymbolSpaceSpec(
                            new SymbolSpaceSpec.Config().splitMode(Style.SplitMode.COMPACT)))))
            .build();
    final Atom primitiveAtom =
        new TreeBuilder(primitiveType).add("value", "gate\nword egg").build();
    final FieldPrimitive primitive = (FieldPrimitive) primitiveAtom.fields.getOpt("value");
    new GeneralTestWizard(syntax, primitiveAtom)
        .resize(40)
        .checkCourseCount(3)
        .run(editor -> ((FieldPrimitive) primitive).visual.select(editor.context, true, 8, 8))
        .run(
            editor -> {
              editor.history.record(
                  editor.context,
                  null,
                  r -> r.apply(editor.context, new ChangePrimitive(primitive, 5, 8, "")));
            })
        .checkCourseCount(2)
        .checkTextBrick(1, 0, "");
  }

  @Test
  public void testUnbreakClearStart() {
    final FreeAtomType primitiveType =
        new TypeBuilder("primitive")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .autoComplete(true) // was 99
            .build();
    final Syntax syntax =
        new SyntaxBuilder("any")
            .type(primitiveType)
            .group("any", new GroupBuilder().type(primitiveType).build())
            .addRootFrontPrefix(
                new FrontSymbol(
                    new FrontSymbol.Config(
                        new SymbolSpaceSpec(
                            new SymbolSpaceSpec.Config().splitMode(Style.SplitMode.COMPACT)))))
            .build();
    final Atom primitiveAtom =
        new TreeBuilder(primitiveType).add("value", "word egg\nroad").build();
    final FieldPrimitive primitive = (FieldPrimitive) primitiveAtom.fields.getOpt("value");
    new GeneralTestWizard(syntax, primitiveAtom)
        .resize(40)
        .checkCourseCount(3)
        .run(editor -> ((FieldPrimitive) primitive).visual.select(editor.context, true, 0, 0))
        .run(
            editor -> {
              editor.history.record(
                  editor.context,
                  null,
                  r -> r.apply(editor.context, new ChangePrimitive(primitive, 0, 8, "")));
            })
        .checkCourseCount(2)
        .checkTextBrick(0, 1, "");
  }

  @Test
  public void testAddThenSplit() {
    final FreeAtomType primitiveType =
        new TypeBuilder("primitive")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .autoComplete(true) // was 99
            .build();
    final Syntax syntax =
        new SyntaxBuilder("any")
            .type(primitiveType)
            .group("any", new GroupBuilder().type(primitiveType).build())
            .addRootFrontPrefix(
                new FrontSymbol(
                    new FrontSymbol.Config(
                        new SymbolSpaceSpec(
                            new SymbolSpaceSpec.Config().splitMode(Style.SplitMode.COMPACT)))))
            .build();
    final Atom primitiveAtom = new TreeBuilder(primitiveType).add("value", "ab").build();
    final FieldPrimitive primitive = (FieldPrimitive) primitiveAtom.fields.getOpt("value");
    new GeneralTestWizard(syntax, primitiveAtom)
        .resize(40)
        .checkCourseCount(1)
        .run(editor -> primitive.visual.select(editor.context, true, 0, 2))
        .run(
            editor -> {
              editor.history.record(
                  editor.context,
                  null,
                  r -> r.apply(editor.context, new ChangePrimitive(primitive, 1, 0, "ord eg")));
              primitive.visual.select(editor.context, true, 0, 8);
            })
        .checkCourseCount(2)
        .checkTextBrick(0, 1, "aord")
        .checkTextBrick(1, 0, " egb");
  }
}
