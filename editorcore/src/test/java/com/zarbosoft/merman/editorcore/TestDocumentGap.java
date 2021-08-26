package com.zarbosoft.merman.editorcore;

import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.document.fields.FieldArray;
import com.zarbosoft.merman.core.document.fields.FieldPrimitive;
import com.zarbosoft.merman.core.syntax.FreeAtomType;
import com.zarbosoft.merman.core.syntax.GapAtomType;
import com.zarbosoft.merman.core.syntax.SuffixGapAtomType;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.core.syntax.back.BackArraySpec;
import com.zarbosoft.merman.core.syntax.back.BackAtomSpec;
import com.zarbosoft.merman.core.syntax.back.BackPrimitiveSpec;
import com.zarbosoft.merman.core.syntax.back.BaseBackArraySpec;
import com.zarbosoft.merman.core.syntax.back.BaseBackAtomSpec;
import com.zarbosoft.merman.core.syntax.back.BaseBackPrimitiveSpec;
import com.zarbosoft.merman.core.syntax.primitivepattern.Digits;
import com.zarbosoft.merman.core.syntax.primitivepattern.Letters;
import com.zarbosoft.merman.editorcore.gap.EditGapCursorFieldPrimitive;
import com.zarbosoft.merman.editorcore.helper.BackRecordBuilder;
import com.zarbosoft.merman.editorcore.helper.FrontDataArrayBuilder;
import com.zarbosoft.merman.editorcore.helper.FrontMarkBuilder;
import com.zarbosoft.merman.editorcore.helper.GeneralTestWizard;
import com.zarbosoft.merman.editorcore.helper.GroupBuilder;
import com.zarbosoft.merman.editorcore.helper.Helper;
import com.zarbosoft.merman.editorcore.helper.SyntaxBuilder;
import com.zarbosoft.merman.editorcore.helper.TreeBuilder;
import com.zarbosoft.merman.editorcore.helper.TypeBuilder;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSOrderedSet;
import org.junit.Test;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.zarbosoft.merman.editorcore.helper.Helper.assertTreeEqual;
import static com.zarbosoft.merman.editorcore.helper.Helper.buildDoc;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class TestDocumentGap {

  public static void assertChoices(Editor editor, int count) {
    if (count == 0)
      assertThat(((EditGapCursorFieldPrimitive) editor.context.cursor).choicePage, equalTo(null));
    else
      assertThat(
          ((EditGapCursorFieldPrimitive) editor.context.cursor).choicePage.choices.size(),
          is(count));
  }

  /** Confirm all concrete atom types are found */
  @Test
  public void syntaxLeafNodes() {
    // Find leaves
    // Of group
    // Of transitive group
    // No duplicates (first appearance only)
    // Nothing from unrelated groups
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .build();
    FreeAtomType one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .build();
    FreeAtomType two =
        new TypeBuilder("two")
            .back(Helper.buildBackPrimitive("two"))
            .front(new FrontMarkBuilder("two").build())
            .build();
    FreeAtomType three =
        new TypeBuilder("three")
            .back(Helper.buildBackPrimitive("three"))
            .front(new FrontMarkBuilder("three").build())
            .build();
    FreeAtomType four =
        new TypeBuilder("four")
            .back(Helper.buildBackPrimitive("four"))
            .front(new FrontMarkBuilder("four").build())
            .build();
    Syntax syntax =
        new SyntaxBuilder("test_group_1")
            .type(infinity)
            .type(one)
            .type(two)
            .type(three)
            .type(four)
            .group(
                "test_group_1",
                new GroupBuilder().type(infinity).type(one).type(two).group("test_group_2").build())
            .group("test_group_2", new GroupBuilder().type(three).type(one).build())
            .group("unused", new GroupBuilder().type(four).type(one).build())
            .build();

    final Editor editor = blank(syntax);
    assertThat(
        editor.context.syntax.splayedTypes.get("test_group_1").inner_(),
        equalTo(TSOrderedSet.of(infinity, one, two, three).inner_()));
  }

  // ========================================================================
  // ========================================================================
  // Decision making and replacement

  private Editor blank(Syntax syntax) {
    final Atom gap = Helper.createGap(syntax);
    final Editor editor = buildDoc(syntax, gap);
    gap.visual.selectIntoAnyChild(editor.context);
    return editor;
  }

  /** Check that typing a letter that matches a type produces that type as a choice */
  @Test
  public void decisionMaking_choiceCount() {
    FreeAtomType quoted =
        new TypeBuilder("quoted")
            .back(Helper.buildBackDataPrimitive("value"))
            .front(new FrontMarkBuilder("\"").build())
            .frontDataPrimitive("value")
            .front(new FrontMarkBuilder("\"").build())
            .autoComplete(false)
            .build();
    FreeAtomType restricted =
        new TypeBuilder("restricted")
            .back(
                new BackRecordBuilder()
                    .add("value", Helper.buildBackDataAtom("value", "restricted_group"))
                    .build())
            .frontDataAtom("value")
            .build();
    Syntax syntax =
        new SyntaxBuilder("restricted_group")
            .type(quoted)
            .type(restricted)
            .group("restricted_group", new GroupBuilder().type(quoted).build())
            .build();

    final Atom gap = Helper.createGap(syntax);
    new GeneralTestWizard(syntax, new TreeBuilder(restricted).add("value", gap).build())
        .run(editor -> gap.namedFields.getOpt("gap").selectInto(editor.context))
        .sendText("q")
        .run(editor -> assertChoices(editor, 1));
  }

  /**
   * With an ambiguous character (potentially matching multiple autocompletable types) a gap
   * autocompletes neither
   */
  @Test
  public void decisionMaking_undecided() {
    FreeAtomType one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .autoComplete(true)
            .build();
    FreeAtomType onyx =
        new TypeBuilder("onyx")
            .back(Helper.buildBackPrimitive("onyx"))
            .front(new FrontMarkBuilder("onyx").build())
            .autoComplete(true)
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(one)
            .type(onyx)
            .group("any", new GroupBuilder().type(one).type(onyx).build())
            .build();

    final Editor editor = blank(syntax);
    editor.context.cursor.handleTyping(editor.context, "o");
    assertTreeEqual(
        editor.context,
        new TreeBuilder(syntax.gap).add("gap", "o").build(),
        Helper.rootArray(editor.context.document));
  }

  /**
   * If ambiguous input is entered into a gap, even if the input exactly matches a choice, that
   * choice isn't autocompleted.
   */
  @Test
  public void decisionMaking_undecidedFull() {
    FreeAtomType one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .autoComplete(true)
            .build();
    FreeAtomType onep =
        new TypeBuilder("onep")
            .back(Helper.buildBackPrimitive("onep"))
            .front(new FrontMarkBuilder("onep").build())
            .autoComplete(true)
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(one)
            .type(onep)
            .group("any", new GroupBuilder().type(one).type(onep).build())
            .build();

    final Editor editor = blank(syntax);
    editor.context.cursor.handleTyping(editor.context, "o");
    editor.context.cursor.handleTyping(editor.context, "n");
    editor.context.cursor.handleTyping(editor.context, "e");
    assertTreeEqual(
        editor.context,
        new TreeBuilder(syntax.gap).add("gap", "one").build(),
        Helper.rootArray(editor.context.document));
  }

  /** Autocomplete as soon as unambiguous. */
  @Test
  public void decisionMaking_immediate() {
    FreeAtomType one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .autoComplete(true)
            .build();
    FreeAtomType onut =
        new TypeBuilder("orgol")
            .back(Helper.buildBackPrimitive("orgol"))
            .front(new FrontMarkBuilder("orgol").build())
            .autoComplete(true)
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(one)
            .type(onut)
            .group("any", new GroupBuilder().type(one).type(onut).build())
            .build();

    innerTestTransform(
        syntax,
        () -> new TreeBuilder(syntax.gap).add(GapAtomType.PRIMITIVE_KEY, "o").build(),
        editor -> {
          selectInitialGap(editor);
          editor.context.cursor.handleTyping(editor.context, "n");
        },
        new TreeBuilder(syntax.suffixGap)
            .addArray(SuffixGapAtomType.PRECEDING_KEY, TSList.of(new TreeBuilder(one).build()))
            .add(GapAtomType.PRIMITIVE_KEY, "")
            .build());
  }

  /** Text that doesn't match triggers complete and is carried over */
  @Test
  public void decisionMaking_chooseOnPositiveNoMatch() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(false)
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(infinity)
            .group("any", new GroupBuilder().type(infinity).build())
            .build();

    innerTestTransform(
        syntax,
        () -> new TreeBuilder(syntax.gap).add(GapAtomType.PRIMITIVE_KEY, "i").build(),
        editor -> {
          selectInitialGap(editor);
          editor.context.cursor.handleTyping(editor.context, "o");
        },
        new TreeBuilder(syntax.suffixGap)
            .addArray(SuffixGapAtomType.PRECEDING_KEY, TSList.of(new TreeBuilder(infinity).build()))
            .add(GapAtomType.PRIMITIVE_KEY, "o")
            .build());
  }

  /** Carryover that matches w/ autocomplete gets completed */
  @Test
  public void decisionMaking_carryoverImmediate() {
    FreeAtomType pref =
        new TypeBuilder("pref")
            .back(
                new BackRecordBuilder()
                    .add("second", Helper.buildBackDataAtom("second", "any"))
                    .build())
            .frontMark("+")
            .frontDataAtom("second")
            .autoComplete(false)
            .build();
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(true)
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(infinity)
            .type(pref)
            .group("any", new GroupBuilder().type(infinity).type(pref).build())
            .build();

    innerTestTransform(
        syntax,
        () -> new TreeBuilder(syntax.gap).add(GapAtomType.PRIMITIVE_KEY, "+").build(),
        editor -> {
          selectInitialGap(editor);
          editor.context.cursor.handleTyping(editor.context, "i");
        },
        new TreeBuilder(pref)
            .add(
                "second",
                new TreeBuilder(syntax.suffixGap)
                    .addArray(
                        SuffixGapAtomType.PRECEDING_KEY,
                        TSList.of(new TreeBuilder(infinity).build()))
                    .add(GapAtomType.PRIMITIVE_KEY, "")
                    .build())
            .build());
  }

  public void selectInitialGap(Editor editor) {
    ((FieldArray) editor.context.document.root.namedFields.get("value"))
        .data
        .get(0)
        .namedFields
        .get(GapAtomType.PRIMITIVE_KEY)
        .selectInto(editor.context);
  }

  /**
   * Typing a character that only matches dynamic primitive pattern autocompletes to that type
   *
   * <p>ex: 9 can only appear in digit type, so pressing 9 autocompletes to digit type (1 choice)
   */
  @Test
  public void decisionMaking_textPrimitiveImmediate() {
    FreeAtomType digits =
        new TypeBuilder("digits")
            .back(
                new BackPrimitiveSpec(
                    new BaseBackPrimitiveSpec.Config("value").pattern(new Digits(), "digits")))
            .frontDataPrimitive("value")
            .autoComplete(true)
            .build();
    FreeAtomType letters =
        new TypeBuilder("letters")
            .back(
                new BackPrimitiveSpec(
                    new BaseBackPrimitiveSpec.Config("value").pattern(new Letters(), "letters")))
            .frontDataPrimitive("value")
            .autoComplete(true)
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(digits)
            .type(letters)
            .group("any", new GroupBuilder().type(digits).type(letters).build())
            .build();

    innerTestTransform(
        syntax,
        () -> new TreeBuilder(syntax.gap).add(GapAtomType.PRIMITIVE_KEY, "").build(),
        editor -> {
          selectInitialGap(editor);
          editor.context.cursor.handleTyping(editor.context, "9");
        },
        new TreeBuilder(digits).add("value", "9").build());
  }

  /** After completing a gap, if the next field is a primitive the primitive is selected */
  @Test
  public void selectNextPrimitive() {
    FreeAtomType pref =
        new TypeBuilder("pref")
            .back(new BackPrimitiveSpec(new BaseBackPrimitiveSpec.Config("text")))
            .frontMark("+")
            .frontDataPrimitive("text")
            .autoComplete(true)
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(pref)
            .group("any", new GroupBuilder().type(pref).build())
            .build();
    final Editor editor = blank(syntax);
    editor.context.cursor.handleTyping(editor.context, "+");
    assertThat(
        editor.context.cursor.getSyntaxPath(),
        equalTo(new SyntaxPath("named", "value", "0", "named", "text", "0")));
  }

  /** After completing a gap, if the next field is an atom the atom's inner gap is selected */
  @Test
  public void selectNextAtom() {
    // TODO check for infinite default value loops (arrays/atoms with only one child type -> chain
    // to self)
    FreeAtomType infinity = // to introduce ambiguity to prevent autocomplete
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(false)
            .build();
    FreeAtomType pref =
        new TypeBuilder("pref")
            .back(new BackAtomSpec(new BaseBackAtomSpec.Config("value", "any")))
            .frontMark("+")
            .frontDataAtom("value")
            .autoComplete(true)
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(pref)
            .type(infinity)
            .group("any", new GroupBuilder().type(pref).type(infinity).build())
            .build();
    final Editor editor = blank(syntax);
    editor.context.cursor.handleTyping(editor.context, "+");
    assertThat(
        editor.context.cursor.getSyntaxPath(),
        equalTo(
            new SyntaxPath(
                "named", "value", "0", "named", "value", "named", GapAtomType.PRIMITIVE_KEY, "0")));
  }

  /** After completing a gap, if the next field is an array the array/inner gap is selected */
  @Test
  public void selectNextArray() {
    // TODO check for infinite default value loops (arrays/atoms with only one child type -> chain
    // to self)
    FreeAtomType infinity = // to introduce ambiguity to prevent autocomplete
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(false)
            .build();
    FreeAtomType pref =
        new TypeBuilder("pref")
            .back(new BackArraySpec(new BaseBackArraySpec.Config("value", "any", ROList.empty)))
            .frontMark("+")
            .frontDataArray("value")
            .autoComplete(true)
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(pref)
            .type(infinity)
            .group("any", new GroupBuilder().type(pref).type(infinity).build())
            .build();
    final Editor editor = blank(syntax);
    editor.context.cursor.handleTyping(editor.context, "+");
    assertNotNull(editor.context.cursor);
  }

  /** If no following field, a completed gap is wrapped in a suffix gap */
  @Test
  public void selectNextWrap() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(true)
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(infinity)
            .group("any", new GroupBuilder().type(infinity).build())
            .build();
    final Editor editor = blank(syntax);
    editor.context.cursor.handleTyping(editor.context, "i");
    assertTreeEqual(
        editor.context,
        new TreeBuilder(syntax.suffixGap)
            .addArray(SuffixGapAtomType.PRECEDING_KEY, TSList.of(new TreeBuilder(infinity).build()))
            .add(GapAtomType.PRIMITIVE_KEY, "")
            .build(),
        Helper.rootArray(editor.context.document));
    assertThat(
        editor.context.cursor.getSyntaxPath(),
        equalTo(new SyntaxPath("named", "value", "0", "named", GapAtomType.PRIMITIVE_KEY, "0")));
  }

  /** Suffix gap identifies choices that match preceding atoms */
  @Test
  public void suffixMatchPrevious() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(false)
            .build();
    FreeAtomType suf =
        new TypeBuilder("suf")
            .back(new BackAtomSpec(new BaseBackAtomSpec.Config("pre1", "infinity")))
            .frontDataAtom("pre1")
            .frontMark("+")
            .autoComplete(false)
            .build();
    Syntax syntax =
        new SyntaxBuilder("suf")
            .type(suf)
            .type(infinity)
            .group("any", new GroupBuilder().type(suf).type(infinity).build())
            .build();
    Editor editor =
        buildDoc(
            syntax,
            new TreeBuilder(syntax.suffixGap)
                .addArray(
                    SuffixGapAtomType.PRECEDING_KEY, TSList.of(new TreeBuilder(infinity).build()))
                .add(GapAtomType.PRIMITIVE_KEY, "")
                .build());
    selectInitialGap(editor);
    assertChoices(editor, 1);
  }

  /**
   * Suffix gap identifies choices that match preceding atoms when the candidate field is an array
   */
  @Test
  public void suffixMatchPreviousArray() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(false)
            .build();
    FreeAtomType suf =
        new TypeBuilder("suf")
            .back(new BackArraySpec(new BaseBackArraySpec.Config("pre1", "infinity", ROList.empty)))
            .frontDataArray("pre1")
            .frontMark("+")
            .autoComplete(false)
            .build();
    Syntax syntax =
        new SyntaxBuilder("suf")
            .type(suf)
            .type(infinity)
            .group("any", new GroupBuilder().type(suf).type(infinity).build())
            .build();
    Editor editor =
        buildDoc(
            syntax,
            new TreeBuilder(syntax.suffixGap)
                .addArray(
                    SuffixGapAtomType.PRECEDING_KEY, TSList.of(new TreeBuilder(infinity).build()))
                .add(GapAtomType.PRIMITIVE_KEY, "")
                .build());
    selectInitialGap(editor);
    assertChoices(editor, 1);
  }

  /** Suffix choices that don't match preceding atoms are filtered */
  @Test
  public void suffixDontMatchPrevious() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(false)
            .build();
    FreeAtomType one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .autoComplete(false)
            .build();
    FreeAtomType suf =
        new TypeBuilder("suf")
            .back(new BackAtomSpec(new BaseBackAtomSpec.Config("pre1", "infinity")))
            .frontDataAtom("pre1")
            .frontMark("+")
            .autoComplete(false)
            .build();
    Syntax syntax =
        new SyntaxBuilder("suf")
            .type(suf)
            .type(infinity)
            .type(one)
            .group("any", new GroupBuilder().type(suf).type(infinity).type(one).build())
            .build();
    Editor editor =
        buildDoc(
            syntax,
            new TreeBuilder(syntax.suffixGap)
                .addArray(SuffixGapAtomType.PRECEDING_KEY, TSList.of(new TreeBuilder(one).build()))
                .add(GapAtomType.PRIMITIVE_KEY, "")
                .build());
    selectInitialGap(editor);
    assertChoices(editor, 0);
  }

  /** A suffix gap places the preceding atom properly after completing */
  @Test
  public void suffixPlacePreviousAtom() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(false)
            .build();
    FreeAtomType suf =
        new TypeBuilder("suf")
            .back(new BackAtomSpec(new BaseBackAtomSpec.Config("pre1", "infinity")))
            .back(new BackPrimitiveSpec(new BaseBackPrimitiveSpec.Config("post")))
            .frontDataAtom("pre1")
            .frontMark("+")
            .frontDataPrimitive("post")
            .autoComplete(true)
            .build();
    Syntax syntax =
        new SyntaxBuilder("suf")
            .type(suf)
            .type(infinity)
            .group("any", new GroupBuilder().type(suf).type(infinity).build())
            .build();
    innerTestTransform(
        syntax,
        () ->
            new TreeBuilder(syntax.suffixGap)
                .addArray(
                    SuffixGapAtomType.PRECEDING_KEY, TSList.of(new TreeBuilder(infinity).build()))
                .add(GapAtomType.PRIMITIVE_KEY, "")
                .build(),
        editor -> {
          selectInitialGap(editor);
          editor.context.cursor.handleTyping(editor.context, "+");
        },
        new TreeBuilder(suf)
            .add("pre1", new TreeBuilder(infinity).build())
            .add("post", "")
            .build());
  }

  /** A suffix atom places two preceding atoms correctly when completing */
  @Test
  public void suffixPlacePreviousTwoAtoms() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(false)
            .build();
    FreeAtomType one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .autoComplete(false)
            .build();
    FreeAtomType suf =
        new TypeBuilder("suf")
            .back(new BackAtomSpec(new BaseBackAtomSpec.Config("pre1", "infinity")))
            .back(new BackAtomSpec(new BaseBackAtomSpec.Config("pre2", "one")))
            .back(new BackPrimitiveSpec(new BaseBackPrimitiveSpec.Config("post")))
            .frontDataAtom("pre1")
            .frontDataAtom("pre2")
            .frontMark("+")
            .frontDataPrimitive("post")
            .autoComplete(true)
            .build();
    Syntax syntax =
        new SyntaxBuilder("suf")
            .type(suf)
            .type(infinity)
            .type(one)
            .group("any", new GroupBuilder().type(suf).type(infinity).type(one).build())
            .build();
    innerTestTransform(
        syntax,
        () ->
            new TreeBuilder(syntax.suffixGap)
                .addArray(
                    SuffixGapAtomType.PRECEDING_KEY,
                    TSList.of(new TreeBuilder(infinity).build(), new TreeBuilder(one).build()))
                .add(GapAtomType.PRIMITIVE_KEY, "")
                .build(),
        editor -> {
          selectInitialGap(editor);
          editor.context.cursor.handleTyping(editor.context, "+");
        },
        new TreeBuilder(suf)
            .add("pre1", new TreeBuilder(infinity).build())
            .add("pre2", new TreeBuilder(one).build())
            .add("post", "")
            .build());
  }

  /** A suffix atom places preceding atoms in array when completing */
  @Test
  public void suffixPlacePreviousArray() {
    FreeAtomType one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .autoComplete(false)
            .build();
    FreeAtomType suf =
        new TypeBuilder("suf")
            .back(new BackArraySpec(new BaseBackArraySpec.Config("pre1", "any", ROList.empty)))
            .back(new BackPrimitiveSpec(new BaseBackPrimitiveSpec.Config("post")))
            .frontDataArray("pre1")
            .frontMark("+")
            .frontDataPrimitive("post")
            .autoComplete(true)
            .build();
    Syntax syntax =
        new SyntaxBuilder("suf")
            .type(suf)
            .type(one)
            .group("any", new GroupBuilder().type(suf).type(one).build())
            .build();
    innerTestTransform(
        syntax,
        () ->
            new TreeBuilder(syntax.suffixGap)
                .addArray(
                    SuffixGapAtomType.PRECEDING_KEY,
                    TSList.of(new TreeBuilder(one).build(), new TreeBuilder(one).build()))
                .add(GapAtomType.PRIMITIVE_KEY, "")
                .build(),
        editor -> {
          selectInitialGap(editor);
          editor.context.cursor.handleTyping(editor.context, "+");
        },
        new TreeBuilder(suf)
            .addArray("pre1", new TreeBuilder(one).build(), new TreeBuilder(one).build())
            .add("post", "")
            .build());
  }

  /** A suffix atom with no following field is wrapped in a new suffix */
  @Test
  public void suffixSelectNextWrap() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(false)
            .build();
    FreeAtomType suf =
        new TypeBuilder("suf")
            .back(new BackAtomSpec(new BaseBackAtomSpec.Config("pre1", "infinity")))
            .frontDataAtom("pre1")
            .frontMark("+")
            .autoComplete(true)
            .build();
    Syntax syntax =
        new SyntaxBuilder("suf")
            .type(suf)
            .type(infinity)
            .group("any", new GroupBuilder().type(suf).type(infinity).build())
            .build();
    innerTestTransform(
        syntax,
        () ->
            new TreeBuilder(syntax.suffixGap)
                .addArray(
                    SuffixGapAtomType.PRECEDING_KEY, TSList.of(new TreeBuilder(infinity).build()))
                .add(GapAtomType.PRIMITIVE_KEY, "")
                .build(),
        editor -> {
          selectInitialGap(editor);
          editor.context.cursor.handleTyping(editor.context, "+");
        },
        new TreeBuilder(syntax.suffixGap)
            .addArray(
                SuffixGapAtomType.PRECEDING_KEY,
                TSList.of(
                    new TreeBuilder(suf).add("pre1", new TreeBuilder(infinity).build()).build()))
            .add(GapAtomType.PRIMITIVE_KEY, "")
            .build());
  }

  private void innerTestTransform(
      final Syntax syntax,
      final Supplier<Atom> start,
      final Consumer<Editor> transform,
      final Atom end) {
    final Editor editor = buildDoc(syntax, start.get());
    transform.accept(editor);
    Helper.dumpTree(editor);
    assertTreeEqual(editor.context, end, Helper.rootArray(editor.context.document));
    editor.history.undo(editor);
    assertTreeEqual(editor.context, start.get(), Helper.rootArray(editor.context.document));
    editor.history.redo(editor);
    assertTreeEqual(editor.context, end, Helper.rootArray(editor.context.document));
  }

  @Test
  public void deselectionRemoval_testDropArrayElement() {
    FreeAtomType array =
        new TypeBuilder("array")
            .back(Helper.buildBackDataArray("value", "any"))
            .front(new FrontDataArrayBuilder("value").build())
            .autoComplete(true)
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(array)
            .group("any", new GroupBuilder().type(array).build())
            .build();
    innerTestTransform(
        syntax,
        () -> new TreeBuilder(array).addArray("value", Helper.createGap(syntax)).build(),
        editor -> {
          ((FieldPrimitive)
                  editor.context.syntaxLocate(
                      new SyntaxPath("named", "value", "0", "named", "value", "0", "named", "gap")))
              .selectInto(editor.context);
          ((EditGapCursorFieldPrimitive) editor.context.cursor).editExit(editor);
        },
        new TreeBuilder(array).addArray("value").build());
  }

  @Test
  public void deselectionRemoval_testDropSuffix() {
    FreeAtomType infinity =
        new TypeBuilder("infinity")
            .back(Helper.buildBackPrimitive("infinity"))
            .front(new FrontMarkBuilder("infinity").build())
            .autoComplete(true)
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(infinity)
            .group("any", new GroupBuilder().type(infinity).build())
            .build();

    innerTestTransform(
        syntax,
        () -> Helper.createSuffixGap(syntax, new TreeBuilder(infinity).build()),
        editor -> {
          ((FieldPrimitive)
                  editor.context.syntaxLocate(
                      new SyntaxPath("named", "value", "0", "named", "gap")))
              .selectInto(editor.context);
          ((EditGapCursorFieldPrimitive) editor.context.cursor).editExit(editor);
        },
        new TreeBuilder(infinity).build());
  }

  @Test
  public void selectEmptyArrayCreateGap() {
    FreeAtomType one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .autoComplete(false)
            .build();
    FreeAtomType two =
        new TypeBuilder("two")
            .back(Helper.buildBackPrimitive("two"))
            .front(new FrontMarkBuilder("two").build())
            .autoComplete(false)
            .build();
    FreeAtomType array =
        new TypeBuilder("array")
            .back(Helper.buildBackDataArray("value", "any"))
            .frontMark("[")
            .front(
                new FrontDataArrayBuilder("value")
                    .addSeparator(new FrontMarkBuilder(", ").build())
                    .build())
            .frontMark("]")
            .autoComplete(true)
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(one)
            .type(two)
            .type(array)
            .group("any", new GroupBuilder().type(one).type(two).type(array).build())
            .build();

    innerTestTransform(
        syntax,
        () -> new TreeBuilder(array).addArray("value").build(),
        editor -> {
          ((FieldArray)
                  editor.context.syntaxLocate(
                      new SyntaxPath("named", "value", "0", "named", "value")))
              .visual.selectIntoAnyChild(editor.context);
        },
        new TreeBuilder(array).addArray("value", Helper.createGap(syntax)).build());
  }

  @Test
  public void selectEmptyArrayCreateDefault() {
    FreeAtomType one =
        new TypeBuilder("one")
            .back(Helper.buildBackPrimitive("one"))
            .front(new FrontMarkBuilder("one").build())
            .autoComplete(false)
            .build();
    FreeAtomType array =
        new TypeBuilder("array")
            .back(Helper.buildBackDataArray("value", "one"))
            .front(
                new FrontDataArrayBuilder("value")
                    .addSeparator(new FrontMarkBuilder(", ").build())
                    .build())
            .autoComplete(true)
            .build();
    Syntax syntax =
        new SyntaxBuilder("any")
            .type(one)
            .type(array)
            .group("any", new GroupBuilder().type(one).type(array).build())
            .build();
    innerTestTransform(
        syntax,
        () -> new TreeBuilder(array).addArray("value").build(),
        editor -> {
          ((FieldArray)
                  editor.context.syntaxLocate(
                      new SyntaxPath("named", "value", "0", "named", "value")))
              .visual.selectIntoAnyChild(editor.context);
        },
        new TreeBuilder(array).addArray("value", new TreeBuilder(one).build()).build());
  }
}
