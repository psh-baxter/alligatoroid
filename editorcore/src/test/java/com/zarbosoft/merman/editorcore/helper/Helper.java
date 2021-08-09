package com.zarbosoft.merman.editorcore.helper;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.Environment;
import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.document.Document;
import com.zarbosoft.merman.core.document.fields.Field;
import com.zarbosoft.merman.core.document.fields.FieldArray;
import com.zarbosoft.merman.core.document.fields.FieldAtom;
import com.zarbosoft.merman.core.document.fields.FieldPrimitive;
import com.zarbosoft.merman.core.syntax.Direction;
import com.zarbosoft.merman.core.syntax.GapAtomType;
import com.zarbosoft.merman.core.syntax.SuffixGapAtomType;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.core.syntax.back.BackArraySpec;
import com.zarbosoft.merman.core.syntax.back.BackAtomSpec;
import com.zarbosoft.merman.core.syntax.back.BackFixedPrimitiveSpec;
import com.zarbosoft.merman.core.syntax.back.BackFixedTypeSpec;
import com.zarbosoft.merman.core.syntax.back.BackKeySpec;
import com.zarbosoft.merman.core.syntax.back.BackPrimitiveSpec;
import com.zarbosoft.merman.core.syntax.back.BackRecordSpec;
import com.zarbosoft.merman.core.syntax.back.BackSpec;
import com.zarbosoft.merman.core.syntax.back.BackSubArraySpec;
import com.zarbosoft.merman.core.syntax.back.BaseBackAtomSpec;
import com.zarbosoft.merman.core.syntax.back.BaseBackPrimitiveSpec;
import com.zarbosoft.merman.core.syntax.back.BaseBackArraySpec;
import com.zarbosoft.merman.core.syntax.primitivepattern.Digits;
import com.zarbosoft.merman.core.syntax.primitivepattern.Letters;
import com.zarbosoft.merman.core.syntax.primitivepattern.Repeat1;
import com.zarbosoft.merman.editorcore.Editor;
import com.zarbosoft.merman.editorcore.EditorCursorFactory;
import com.zarbosoft.merman.editorcore.cursors.EditCursorFieldArray;
import com.zarbosoft.merman.editorcore.cursors.EditCursorAtom;
import com.zarbosoft.merman.editorcore.cursors.EditCursorFieldPrimitive;
import com.zarbosoft.merman.editorcore.display.MockeryDisplay;
import com.zarbosoft.merman.editorcore.history.FileIds;
import com.zarbosoft.merman.editorcore.history.History;
import com.zarbosoft.merman.jfxcore.serialization.JavaSerializer;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.Format;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROSet;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;
import com.zarbosoft.rendaw.common.TSSet;
import org.junit.ComparisonFailure;

import java.util.Map;

public class Helper {

  public static Environment env = new TestEnvironment();

  public static Atom createGap(Syntax syntax) {
    return new TreeBuilder(syntax.gap).add(GapAtomType.PRIMITIVE_KEY, "").build();
  }

  public static Atom createSuffixGap(Syntax syntax, Atom... preceding) {
    return new TreeBuilder(syntax.suffixGap)
        .add(SuffixGapAtomType.PRIMITIVE_KEY, "")
        .addArray(SuffixGapAtomType.PRECEDING_KEY, preceding)
        .build();
  }

  public static EditCursorAtom cursorAtom(Context context) {
    return (EditCursorAtom) context.cursor;
  }

  public static EditCursorFieldArray cursorArray(Context context) {
    return (EditCursorFieldArray) context.cursor;
  }

  public static EditCursorFieldPrimitive cursorPrimitive(Context context) {
    return (EditCursorFieldPrimitive) context.cursor;
  }

  public static BackSpec buildBackType(final String type, final BackSpec child) {
    return new BackFixedTypeSpec(new BackFixedTypeSpec.Config(type, child));
  }

  public static BackSpec buildBackPrimitive(final String value) {
    return new BackFixedPrimitiveSpec(value);
  }

  public static BackSpec buildBackDataAtom(final String id, String type) {
    return new BackAtomSpec(new BaseBackAtomSpec.Config(id, type));
  }

  public static BackSpec buildBackDataPrimitive(final String id) {
    return new BackPrimitiveSpec(new BaseBackPrimitiveSpec.Config(id));
  }

  public static BackSpec buildBackDataPrimitiveLetters(final String id) {
    return new BackPrimitiveSpec(
        new BaseBackPrimitiveSpec.Config(id).pattern(new Repeat1(new Letters()), "1+ letters"));
  }

  public static BackSpec buildBackDataPrimitiveDigits(final String id) {
    return new BackPrimitiveSpec(
        new BaseBackPrimitiveSpec.Config(id).pattern(new Repeat1(new Digits()), "1+ digits"));
  }

  public static BackSpec buildBackDataRecord(final String id, String type) {
    return new BackRecordSpec(new BaseBackArraySpec.Config(id, type, ROList.empty));
  }

  public static BackKeySpec buildBackDataKey(final String id) {
    return new BackKeySpec(new BaseBackPrimitiveSpec.Config(id));
  }

  public static BackArraySpec buildBackDataArray(final String id, String type) {
    return new BackArraySpec(new BaseBackArraySpec.Config(id, type, new TSList<>()));
  }

  public static BackSubArraySpec buildBackDataRootArray(final String id, String type) {
    return new BackSubArraySpec(new BaseBackArraySpec.Config(id, type, new TSList<>()));
  }

  public static void assertTreeEqual(final Atom expected, final Atom got) {
    if (expected.type != got.type)
      throw new AssertionError(
          Format.format(
              "Atom type mismatch.\nExpected: %s\nGot: %s\nAt: %s",
              expected.type, got.type, got.getSyntaxPath()));
    final ROSet<String> expectedKeys = expected.namedFields.keys();
    final ROSet<String> gotKeys = got.namedFields.keys();
    {
      final TSSet<String> missing = expectedKeys.difference(gotKeys);
      if (!missing.isEmpty())
        throw new AssertionError(
            Format.format("Missing fields: %s\nAt: %s", missing, got.getSyntaxPath()));
    }
    {
      final TSSet<String> extra = gotKeys.difference(expectedKeys);
      if (!extra.isEmpty())
        throw new AssertionError(
            Format.format("Unknown fields: %s\nAt: %s", extra, got.getSyntaxPath()));
    }
    for (final String key : expectedKeys.intersect(gotKeys)) {
      assertTreeEqual(expected.namedFields.getOpt(key), got.namedFields.getOpt(key));
    }
  }

  public static void assertTreeEqual(final Field expected, final Field got) {
    if (expected.getClass() == FieldArray.class) {
      final FieldArray expectedValue = (FieldArray) expected;
      final FieldArray gotValue = (FieldArray) got;
      if (expectedValue.data.size() != gotValue.data.size())
        throw new AssertionError(
            Format.format(
                "Array length mismatch.\nExpected: %s\nGot: %s\nAt: %s",
                expectedValue.data.size(), gotValue.data.size(), got.getSyntaxPath()));
      for (int i = 0; i < expectedValue.data.size(); ++i) {
        assertTreeEqual(expectedValue.data.get(i), gotValue.data.get(i));
      }
    } else if (expected.getClass() == FieldAtom.class) {
      final FieldAtom expectedValue = (FieldAtom) expected;
      final FieldAtom gotValue = (FieldAtom) got;
      assertTreeEqual(expectedValue.get(), gotValue.get());
    } else if (expected.getClass() == FieldPrimitive.class) {
      final FieldPrimitive expectedValue = (FieldPrimitive) expected;
      final FieldPrimitive gotValue = (FieldPrimitive) got;
      if (!expectedValue.get().equals(gotValue.get()))
        throw new ComparisonFailure(
            Format.format("Primitive length mismatch.\nAt: %s", got.getSyntaxPath()),
            expectedValue.get(),
            gotValue.get());
    } else
      throw new AssertionError(
          Format.format(
              "Atom type mismatch.\nExpected: %s\nGot: %s\nAt: %s",
              expected.getClass(), got.getClass(), got.getSyntaxPath()));
  }

  public static void assertTreeEqual(final Context context, final Atom expected, final Field got) {
    FieldArray value = new FieldArray((BaseBackArraySpec) context.syntax.root.namedFields.get("value"));
    value.initialSet(TSList.of(expected)); // TODO this shouldn't really be setting the value
    assertTreeEqual(value, got);
  }

  public static FieldArray rootArray(final Document doc) {
    return (FieldArray) doc.root.namedFields.getOpt("value");
  }

  public static Editor buildDoc(final Syntax syntax, final Atom... root) {
    return buildDoc(new Context.InitialConfig(), syntax, root);
  }

  public static Editor buildDoc(
      Context.InitialConfig contextConfig, final Syntax syntax, final Atom... root) {
    FieldArray rootArray = new FieldArray((BaseBackArraySpec) syntax.root.namedFields.get("value"));
    rootArray.initialSet(TSList.of(root));
    Atom rootAtom = new Atom(syntax.root);
    rootAtom.initialSet(new TSList<>(),new TSMap<String, Field>().put("value", rootArray));
    final Document doc = new Document(syntax, rootAtom);
    final Editor editor =
        new Editor(
            syntax,
            new FileIds(),
            doc,
            new MockeryDisplay(Direction.RIGHT, Direction.DOWN),
            new TestEnvironment(),
            new History(),
            new JavaSerializer(syntax.backType),
            e -> new EditorCursorFactory(e),
            new Editor.Config(contextConfig));
    return editor;
  }

  private static void dumpTreeInner(Atom a, String indentText) {
    String indentChunk = "   ";
    System.out.format("%satom type [%s]\n", indentText, a.type.id);
    indentText += indentChunk;
    for (Map.Entry<String, Field> field : a.namedFields) {
      System.out.format(
          "%sfield [%s] %s\n", indentText, field.getKey(), field.getValue().getClass().getName());
      if (field.getValue() instanceof FieldArray) {
        String subIndent = indentText + indentChunk;
        int index = 0;
        for (Atom el : ((FieldArray) field.getValue()).data) {
          dumpTreeInner(el, subIndent + " " + index + " ");
          index += 1;
        }
      } else if (field.getValue() instanceof FieldAtom) {
        dumpTreeInner(((FieldAtom) field.getValue()).data, indentText + indentChunk);
      } else if (field.getValue() instanceof FieldPrimitive) {
        System.out.format(
            "%sprimitive text [%s]\n", indentText + indentChunk, ((FieldPrimitive) field.getValue()).get());
      } else throw new Assertion();
    }
  }

  public static void dumpTree(Editor editor) {
    dumpTreeInner(editor.context.document.root, "");
  }
}
