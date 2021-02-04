package com.zarbosoft.merman.helper;

import com.zarbosoft.luxem.write.Writer;
import com.zarbosoft.merman.JavaI18nEngine;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.Document;
import com.zarbosoft.merman.document.values.Value;
import com.zarbosoft.merman.document.values.ValueArray;
import com.zarbosoft.merman.document.values.ValueAtom;
import com.zarbosoft.merman.document.values.ValuePrimitive;
import com.zarbosoft.merman.editor.Action;
import com.zarbosoft.merman.editor.ClipboardEngine;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.I18nEngine;
import com.zarbosoft.merman.editor.IterationTask;
import com.zarbosoft.merman.editor.display.MockeryDisplay;
import com.zarbosoft.merman.syntax.Direction;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.merman.syntax.back.BackArraySpec;
import com.zarbosoft.merman.syntax.back.BackAtomSpec;
import com.zarbosoft.merman.syntax.back.BackFixedPrimitiveSpec;
import com.zarbosoft.merman.syntax.back.BackFixedTypeSpec;
import com.zarbosoft.merman.syntax.back.BackKeySpec;
import com.zarbosoft.merman.syntax.back.BackPrimitiveSpec;
import com.zarbosoft.merman.syntax.back.BackRecordSpec;
import com.zarbosoft.merman.syntax.back.BackSpec;
import com.zarbosoft.merman.syntax.back.BackSubArraySpec;
import com.zarbosoft.merman.syntax.back.BaseBackArraySpec;
import com.zarbosoft.merman.syntax.back.BaseBackAtomSpec;
import com.zarbosoft.merman.syntax.back.BaseBackPrimitiveSpec;
import com.zarbosoft.merman.syntax.back.BaseBackSimpleArraySpec;
import com.zarbosoft.merman.syntax.primitivepattern.Digits;
import com.zarbosoft.merman.syntax.primitivepattern.Letters;
import com.zarbosoft.merman.syntax.primitivepattern.Repeat1;
import com.zarbosoft.rendaw.common.DeadCode;
import com.zarbosoft.rendaw.common.ROSet;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;
import com.zarbosoft.rendaw.common.TSSet;
import org.junit.ComparisonFailure;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.function.Consumer;

import static com.zarbosoft.rendaw.common.Common.uncheck;

public class Helper {
  public static I18nEngine i18n = new JavaI18nEngine(Locale.US);

  public static void dump(final Value value, final Writer writer) {
    uncheck(
        () -> {
          if (value.getClass() == ValueArray.class) {
            writer.arrayBegin();
            for (Atom element : ((ValueArray) value).data) {
              dump(element, writer);
            }
            writer.arrayEnd();
          } else if (value.getClass() == ValueAtom.class) {
            dump(((ValueAtom) value).get(), writer);
          } else if (value.getClass() == ValuePrimitive.class) {
            writer.quotedPrimitive(((ValuePrimitive) value).get().getBytes(StandardCharsets.UTF_8));
          } else throw new DeadCode();
        });
  }

  private static void dump(final Atom value, final Writer writer) {
    uncheck(
        () -> {
          writer.type(value.type.id().getBytes(StandardCharsets.UTF_8));
          writer.recordBegin();
          value
              .fields
              .keys()
              .forEach(
                  k ->
                      dump(
                          value.fields.getOpt(k),
                          uncheck(() -> writer.key(k.getBytes(StandardCharsets.UTF_8)))));
          writer.recordEnd();
        });
  }

  public static void dump(final Value value) {
    dump(value, new Writer(System.out, (byte) ' ', 4));
    System.out.write('\n');
    System.out.flush();
  }

  public static void act(final Context context, final String name) {
    for (final Action action : context.actions()) {
      if (action.id().equals(name)) {
        action.run(context);
        return;
      }
    }
    throw new AssertionError(String.format("No action named [%s]", name));
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
    return new BackPrimitiveSpec(i18n, new BaseBackPrimitiveSpec.Config(id, null));
  }

  public static BackSpec buildBackDataPrimitiveLetters(final String id) {
    return new BackPrimitiveSpec(
        i18n, new BaseBackPrimitiveSpec.Config(id, new Repeat1(new Letters())));
  }

  public static BackSpec buildBackDataPrimitiveDigits(final String id) {
    return new BackPrimitiveSpec(
        i18n, new BaseBackPrimitiveSpec.Config(id, new Repeat1(new Digits())));
  }

  public static BackSpec buildBackDataRecord(final String id, String type) {
    return new BackRecordSpec(new BackRecordSpec.Config(id, type));
  }

  public static BackKeySpec buildBackDataKey(final String id) {
    return new BackKeySpec(i18n, new BaseBackPrimitiveSpec.Config(id, null));
  }

  public static BackArraySpec buildBackDataArray(final String id, String type) {
    return new BackArraySpec(new BaseBackSimpleArraySpec.Config(id, type, new TSList<>()));
  }

  public static BackSubArraySpec buildBackDataRootArray(final String id, String type) {
    return new BackSubArraySpec(new BaseBackSimpleArraySpec.Config(id, type, new TSList<>()));
  }

  public static void assertTreeEqual(final Atom expected, final Atom got) {
    if (expected.type != got.type)
      throw new AssertionError(
          String.format(
              "Atom type mismatch.\nExpected: %s\nGot: %s\nAt: %s",
              expected.type, got.type, got.getSyntaxPath()));
    final ROSet<String> expectedKeys = expected.fields.keys();
    final ROSet<String> gotKeys = got.fields.keys();
    {
      final TSSet<String> missing = expectedKeys.difference(gotKeys);
      if (!missing.isEmpty())
        throw new AssertionError(
            String.format("Missing fields: %s\nAt: %s", missing, got.getSyntaxPath()));
    }
    {
      final TSSet<String> extra = gotKeys.difference(expectedKeys);
      if (!extra.isEmpty())
        throw new AssertionError(
            String.format("Unknown fields: %s\nAt: %s", extra, got.getSyntaxPath()));
    }
    for (final String key : expectedKeys.intersect(gotKeys)) {
      assertTreeEqual(expected.fields.getOpt(key), got.fields.getOpt(key));
    }
  }

  public static void assertTreeEqual(final Value expected, final Value got) {
    if (expected.getClass() == ValueArray.class) {
      final ValueArray expectedValue = (ValueArray) expected;
      final ValueArray gotValue = (ValueArray) got;
      if (expectedValue.data.size() != gotValue.data.size())
        throw new AssertionError(
            String.format(
                "Array length mismatch.\nExpected: %s\nGot: %s\nAt: %s",
                expectedValue.data.size(), gotValue.data.size(), got.getSyntaxPath()));
      for (int i = 0; i < expectedValue.data.size(); ++i) {
        assertTreeEqual(expectedValue.data.get(i), gotValue.data.get(i));
      }
    } else if (expected.getClass() == ValueAtom.class) {
      final ValueAtom expectedValue = (ValueAtom) expected;
      final ValueAtom gotValue = (ValueAtom) got;
      assertTreeEqual(expectedValue.get(), gotValue.get());
    } else if (expected.getClass() == ValuePrimitive.class) {
      final ValuePrimitive expectedValue = (ValuePrimitive) expected;
      final ValuePrimitive gotValue = (ValuePrimitive) got;
      if (!expectedValue.get().equals(gotValue.get()))
        throw new ComparisonFailure(
            String.format("Array length mismatch.\nAt: %s", got.getSyntaxPath()),
            expectedValue.get(),
            gotValue.get());
    } else
      throw new AssertionError(
          String.format(
              "Atom type mismatch.\nExpected: %s\nGot: %s\nAt: %s",
              expected.getClass(), got.getClass(), got.getSyntaxPath()));
  }

  public static void assertTreeEqual(final Context context, final Atom expected, final Value got) {
    assertTreeEqual(
        new ValueArray(
            (BaseBackArraySpec) context.syntax.root.fields.get("value"), TSList.of(expected)),
        got);
  }

  public static ValueArray rootArray(final Document doc) {
    return (ValueArray) doc.root.fields.getOpt("value");
  }

  public static Context buildDoc(final Syntax syntax, final Atom... root) {
    return buildDoc(new Context.InitialConfig(), idleTask -> {}, limit -> {}, syntax, false, root);
  }

  public static Context buildDoc(
      Context.InitialConfig contextConfig,
      final Consumer<IterationTask> addIteration,
      final Consumer<Integer> flushIteration,
      final Syntax syntax,
      boolean startWindowed,
      final Atom... root) {
    final Document doc =
        new Document(
            syntax,
            new Atom(
                syntax.root,
                new TSMap<String, Value>()
                    .put(
                        "value",
                        new ValueArray(
                            (BaseBackArraySpec) syntax.root.fields.get("value"),
                            TSList.of(root)))));
    final Context context =
        new Context(
            contextConfig,
            syntax,
            doc,
            new MockeryDisplay(Direction.RIGHT, Direction.DOWN),
            addIteration,
            flushIteration,
            new ClipboardEngine() {
              byte[] data = null;
              String string = null;

              @Override
              public void set(final byte[] bytes) {
                data = bytes;
              }

              @Override
              public byte[] get() {
                return data;
              }

              @Override
              public String getString() {
                return string;
              }

              @Override
              public void setString(final String string) {
                this.string = string;
              }
            },
            null,
            startWindowed,
            i18n);
    return context;
  }
}
