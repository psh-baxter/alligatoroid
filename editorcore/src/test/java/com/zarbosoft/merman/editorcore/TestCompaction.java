package com.zarbosoft.merman.editorcore;

import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.document.fields.FieldArray;
import com.zarbosoft.merman.core.document.fields.FieldPrimitive;
import com.zarbosoft.merman.core.syntax.FreeAtomType;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.core.syntax.front.FrontSymbol;
import com.zarbosoft.merman.core.syntax.style.Style;
import com.zarbosoft.merman.core.syntax.symbol.SymbolSpaceSpec;
import com.zarbosoft.merman.editorcore.helper.FrontDataArrayBuilder;
import com.zarbosoft.merman.editorcore.helper.FrontMarkBuilder;
import com.zarbosoft.merman.editorcore.helper.GroupBuilder;
import com.zarbosoft.merman.editorcore.helper.Helper;
import com.zarbosoft.merman.editorcore.helper.SyntaxBuilder;
import com.zarbosoft.merman.editorcore.helper.TreeBuilder;
import com.zarbosoft.merman.editorcore.helper.TypeBuilder;
import com.zarbosoft.merman.editorcore.history.changes.ChangeArray;
import com.zarbosoft.merman.editorcore.history.changes.ChangePrimitive;
import com.zarbosoft.rendaw.common.TSList;
import org.junit.Test;

public class TestCompaction {
  @Test
  public void testSplitDynamic() {
    final FreeAtomType one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .build();
    final FreeAtomType low =
        new TypeBuilder("low")
            .back(Helper.buildBackDataArray("value", "any"))
            .front(
                new FrontDataArrayBuilder("value")
                    .addPrefix(
                        new FrontSymbol(
                            new FrontSymbol.Config(
                                new SymbolSpaceSpec(
                                    new SymbolSpaceSpec.Config()
                                        .splitMode(Style.SplitMode.COMPACT)))))
                    .build())
            .precedence(0)
            .depthScore(1)
            .build();
    final Syntax syntax =
        new SyntaxBuilder("any")
            .type(one)
            .type(low)
            .group("any", new GroupBuilder().type(one).type(low).build())
            .build();
    final Atom lowAtom =
        new TreeBuilder(low)
            .addArray("value", new TreeBuilder(one).build(), new TreeBuilder(one).build())
            .build();
    final FieldArray array = (FieldArray) lowAtom.fields.getOpt("value");
    new GeneralTestWizard(syntax, lowAtom)
        .resize(70)
        .checkCourseCount(1)
        .checkTextBrick(0, 1, "one")
        .checkTextBrick(0, 3, "one")
        .change(new ChangeArray(array, 2, 0, TSList.of(new TreeBuilder(one).build())))
        .checkCourseCount(3)
        .checkTextBrick(0, 1, "one")
        .checkTextBrick(1, 1, "one")
        .checkTextBrick(2, 1, "one")
        .change(new ChangeArray(array, 2, 1, TSList.of()))
        .checkCourseCount(1)
        .checkTextBrick(0, 1, "one")
        .checkTextBrick(0, 3, "one");
  }

  @Test
  public void testSplitDynamicBrickChange() {
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
    final FreeAtomType low =
        new TypeBuilder("low")
            .back(Helper.buildBackDataArray("value", "any"))
            .front(
                new FrontDataArrayBuilder("value")
                    .addPrefix(
                        new FrontSymbol(
                            new FrontSymbol.Config(
                                new SymbolSpaceSpec(
                                    new SymbolSpaceSpec.Config()
                                        .splitMode(Style.SplitMode.COMPACT)))))
                    .build())
            .precedence(0)
            .depthScore(1)
            .build();
    final Syntax syntax =
        new SyntaxBuilder("any")
            .type(one)
            .type(textType)
            .type(low)
            .group("any", new GroupBuilder().type(one).type(low).type(textType).build())
            .build();
    final Atom textAtom = new TreeBuilder(textType).add("value", "oran").build();
    final FieldPrimitive text = (FieldPrimitive) textAtom.fields.getOpt("value");
    new GeneralTestWizard(
            syntax,
            new TreeBuilder(low).addArray("value", new TreeBuilder(one).build(), textAtom).build())
        .resize(80)
        .checkCourseCount(1)
        .checkTextBrick(0, 1, "one")
        .checkTextBrick(0, 3, "oran")
        .change(new ChangePrimitive(text, 4, 0, "ge"))
        .checkCourseCount(2)
        .checkTextBrick(0, 1, "one")
        .checkTextBrick(1, 1, "orange")
        .change(new ChangePrimitive(text, 4, 2, ""))
        .checkCourseCount(1)
        .checkTextBrick(0, 1, "one")
        .checkTextBrick(0, 3, "oran");
  }

  @Test
  public void testSplitDynamicComboBrick() {
    final FreeAtomType comboText =
        new TypeBuilder("comboText")
            .back(Helper.buildBackDataPrimitive("value"))
            .frontDataPrimitive("value")
            .frontMark("123")
            .build();
    final Syntax syntax =
        new SyntaxBuilder("any")
            .type(comboText)
            .group("any", new GroupBuilder().type(comboText).build())
            .build();
    final Atom primitive = new TreeBuilder(comboText).add("value", "I am a banana").build();
    new GeneralTestWizard(syntax, primitive)
        .resize(140)
        .checkTextBrick(0, 0, "I am a banana")
        .checkTextBrick(0, 1, "123")
        .change(
            new ChangePrimitive((FieldPrimitive) primitive.fields.getOpt("value"), 0, 0, "wigwam "))
        .checkTextBrick(0, 0, "wigwam I am a ")
        .checkTextBrick(1, 0, "banana")
        .checkTextBrick(1, 1, "123");
  }

  @Test
  public void testSplitNested() {
    final FreeAtomType one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .build();
    final FreeAtomType low =
        new TypeBuilder("low")
            .back(Helper.buildBackDataArray("value", "any"))
            .front(
                new FrontDataArrayBuilder("value")
                    .addPrefix(
                        new FrontSymbol(
                            new FrontSymbol.Config(
                                new SymbolSpaceSpec(
                                    new SymbolSpaceSpec.Config()
                                        .splitMode(Style.SplitMode.COMPACT)))))
                    .build())
            .precedence(0)
            .depthScore(1)
            .build();
    final FreeAtomType unary =
        new TypeBuilder("unary")
            .back(Helper.buildBackDataAtom("value", "any"))
            .frontDataNode("value")
            .precedence(20)
            .depthScore(1)
            .build();
    final Syntax syntax =
        new SyntaxBuilder("any")
            .type(one)
            .type(low)
            .type(unary)
            .group("any", new GroupBuilder().type(one).type(low).type(unary).build())
            .build();
    final Atom lowAtom =
        new TreeBuilder(low)
            .addArray("value", new TreeBuilder(one).build(), new TreeBuilder(one).build())
            .build();
    final FieldArray array = (FieldArray) lowAtom.fields.getOpt("value");
    new GeneralTestWizard(syntax, new TreeBuilder(unary).add("value", lowAtom).build())
        .resize(70)
        .checkCourseCount(1)
        .checkTextBrick(0, 1, "one")
        .checkTextBrick(0, 3, "one")
        .change(new ChangeArray(array, 2, 0, TSList.of(new TreeBuilder(one).build())))
        .checkCourseCount(3)
        .checkTextBrick(0, 1, "one")
        .checkTextBrick(1, 1, "one")
        .checkTextBrick(2, 1, "one")
        .change(new ChangeArray(array, 2, 1, TSList.of()))
        .checkCourseCount(1)
        .checkTextBrick(0, 1, "one")
        .checkTextBrick(0, 3, "one");
  }

  @Test
  public void testSplitOrderRuleDynamic() {
    final FreeAtomType one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .build();
    final FreeAtomType low =
        new TypeBuilder("low")
            .back(Helper.buildBackDataArray("value", "any"))
            .front(
                new FrontDataArrayBuilder("value")
                    .addPrefix(
                        new FrontSymbol(
                            new FrontSymbol.Config(
                                new SymbolSpaceSpec(
                                    new SymbolSpaceSpec.Config()
                                        .splitMode(Style.SplitMode.COMPACT)))))
                    .build())
            .precedence(0)
            .depthScore(1)
            .build();
    final FreeAtomType mid =
        new TypeBuilder("mid")
            .back(Helper.buildBackDataArray("value", "any"))
            .front(
                new FrontDataArrayBuilder("value")
                    .addPrefix(
                        new FrontSymbol(
                            new FrontSymbol.Config(
                                new SymbolSpaceSpec(
                                    new SymbolSpaceSpec.Config()
                                        .splitMode(Style.SplitMode.COMPACT)))))
                    .build())
            .precedence(50)
            .depthScore(1)
            .build();
    final FreeAtomType high =
        new TypeBuilder("high")
            .back(Helper.buildBackDataArray("value", "any"))
            .front(
                new FrontDataArrayBuilder("value")
                    .addPrefix(
                        new FrontSymbol(
                            new FrontSymbol.Config(
                                new SymbolSpaceSpec(
                                    new SymbolSpaceSpec.Config()
                                        .splitMode(Style.SplitMode.COMPACT)))))
                    .build())
            .precedence(100)
            .depthScore(1)
            .build();
    final Syntax syntax =
        new SyntaxBuilder("any")
            .type(one)
            .type(low)
            .type(mid)
            .type(high)
            .group("any", new GroupBuilder().type(one).type(low).type(mid).type(high).build())
            .build();
    final Atom highAtom =
        new TreeBuilder(high)
            .addArray("value", new TreeBuilder(one).build(), new TreeBuilder(one).build())
            .build();
    final FieldArray array = (FieldArray) highAtom.fields.getOpt("value");
    new GeneralTestWizard(
            syntax,
            new TreeBuilder(mid)
                .addArray(
                    "value",
                    new TreeBuilder(low)
                        .addArray(
                            "value", new TreeBuilder(one).build(), new TreeBuilder(one).build())
                        .build(),
                    highAtom)
                .build())
        .resize(80)
        .checkCourseCount(4)
        .checkTextBrick(1, 1, "one")
        .checkTextBrick(2, 1, "one")
        .checkTextBrick(3, 2, "one")
        .checkTextBrick(3, 4, "one")
        .change(new ChangeArray(array, 2, 0, TSList.of(new TreeBuilder(one).build())))
        .checkCourseCount(7)
        .checkTextBrick(1, 1, "one")
        .checkTextBrick(2, 1, "one")
        .checkTextBrick(4, 1, "one")
        .checkTextBrick(5, 1, "one")
        .checkTextBrick(6, 1, "one")
        .change(new ChangeArray(array, 2, 1, TSList.of()))
        .checkCourseCount(4)
        .checkTextBrick(1, 1, "one")
        .checkTextBrick(2, 1, "one")
        .checkTextBrick(3, 2, "one")
        .checkTextBrick(3, 4, "one");
  }

  @Test
  public void testStartCompactDynamic() {
    final FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .build();
    final FreeAtomType one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .build();
    final FreeAtomType low =
        new TypeBuilder("low")
            .back(Helper.buildBackDataArray("value", "any"))
            .front(
                new FrontDataArrayBuilder("value")
                    .addPrefix(
                        new FrontSymbol(
                            new FrontSymbol.Config(
                                new SymbolSpaceSpec(
                                    new SymbolSpaceSpec.Config()
                                        .splitMode(Style.SplitMode.COMPACT)))))
                    .build())
            .precedence(0)
            .depthScore(1)
            .build();
    final Syntax syntax =
        new SyntaxBuilder("any")
            .type(one)
            .type(infinity)
            .type(low)
            .group("any", new GroupBuilder().type(infinity).type(one).type(low).build())
            .build();
    final Atom lowAtom =
        new TreeBuilder(low)
            .addArray(
                "value",
                new TreeBuilder(one).build(),
                new TreeBuilder(one).build(),
                new TreeBuilder(infinity).build())
            .build();
    final FieldArray array = (FieldArray) lowAtom.fields.getOpt("value");
    new GeneralTestWizard(syntax, lowAtom)
        .resize(100)
        .checkTextBrick(0, 1, "one")
        .checkTextBrick(1, 1, "one")
        .checkTextBrick(2, 1, "infinity")
        .change(new ChangeArray(array, 1, 0, TSList.of(new TreeBuilder(one).build())))
        .checkTextBrick(0, 1, "one")
        .checkTextBrick(1, 1, "one")
        .checkTextBrick(2, 1, "one")
        .checkTextBrick(3, 1, "infinity");
  }

  public static class GeneralTestWizard
      extends com.zarbosoft.merman.editorcore.helper.GeneralTestWizard {
    public GeneralTestWizard(Syntax syntax, Atom... atoms) {
      super(syntax, atoms);
      this.inner.editor.context.ellipsizeThreshold = 2;
    }
  }
}
