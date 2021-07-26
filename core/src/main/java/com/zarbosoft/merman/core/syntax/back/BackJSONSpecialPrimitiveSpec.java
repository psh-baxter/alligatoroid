package com.zarbosoft.merman.core.syntax.back;

import com.zarbosoft.merman.core.Environment;
import com.zarbosoft.merman.core.MultiError;
import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.backevents.BackEvent;
import com.zarbosoft.merman.core.backevents.EPrimitiveEvent;
import com.zarbosoft.merman.core.backevents.JSpecialPrimitiveEvent;
import com.zarbosoft.merman.core.document.fields.FieldPrimitive;
import com.zarbosoft.merman.core.serialization.EventConsumer;
import com.zarbosoft.merman.core.serialization.WriteState;
import com.zarbosoft.merman.core.syntax.AtomType;
import com.zarbosoft.merman.core.syntax.BackType;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.core.syntax.error.BackElementUnsupportedInBackFormat;
import com.zarbosoft.merman.core.syntax.primitivepattern.Integer;
import com.zarbosoft.merman.core.syntax.primitivepattern.JsonDecimal;
import com.zarbosoft.merman.core.syntax.primitivepattern.Pattern;
import com.zarbosoft.pidgoon.events.nodes.Terminal;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;

import java.util.Iterator;

public class BackJSONSpecialPrimitiveSpec extends BaseBackPrimitiveSpec {
  public static final String INVALID_INT_PREFIX = "invalid_json_int:";
  public static final String INVALID_DEC_PREFIX = "invalid_json_dec:";
  private final String invalidPrefix;

  public BackJSONSpecialPrimitiveSpec(Config config) {
    super(
        new BaseBackPrimitiveSpec.Config(config.id)
            .pattern(config.pattern, config.patternDescription));
    invalidPrefix = config.invalidPrefix;
  }

  public static Config integerConfig(String id) {
    return new Config(id, new Integer(), "json int", INVALID_INT_PREFIX);
  }

  public static Config decimalConfig(String id) {
    return new Config(id, new JsonDecimal(), "json decimal", INVALID_DEC_PREFIX);
  }

  @Override
  protected Iterator<BackSpec> walkStep() {
    return null;
  }

  @Override
  public void finish(
      MultiError errors,
      Syntax syntax,
      SyntaxPath typePath,
      boolean singularRestriction,
      boolean typeRestriction) {
    if (syntax.backType != BackType.JSON) {
      errors.add(
          new BackElementUnsupportedInBackFormat(
              "json special primitive", syntax.backType, typePath));
    }
    super.finish(errors, syntax, typePath, singularRestriction, typeRestriction);
  }

  @Override
  public Node<ROList<AtomType.FieldParseResult>> buildBackRule(Environment env, Syntax syntax) {
    return new Terminal<BackEvent, ROList<AtomType.FieldParseResult>>() {
      @Override
      protected ROPair<Boolean, ROList<AtomType.FieldParseResult>> matches(BackEvent event) {
        if (event instanceof JSpecialPrimitiveEvent) {
          boolean ok = matcher.match(env, ((JSpecialPrimitiveEvent) event).value);
          return new ROPair<>(
              ok,
              ok
                  ? TSList.of(
                      new AtomType.PrimitiveFieldParseResult(
                          id,
                          new FieldPrimitive(
                              BackJSONSpecialPrimitiveSpec.this,
                              ((JSpecialPrimitiveEvent) event).value)))
                  : ROList.empty);
        } else if (event instanceof EPrimitiveEvent
            && ((EPrimitiveEvent) event).value.startsWith(invalidPrefix)) {
          return new ROPair<>(
              true,
              TSList.of(
                  new AtomType.PrimitiveFieldParseResult(
                      id,
                      new FieldPrimitive(
                          BackJSONSpecialPrimitiveSpec.this,
                          ((JSpecialPrimitiveEvent) event).value))));
        } else return new ROPair<>(false, null);
      }

      @Override
      public String toString() {
        return "JSON SPECIAL PRIMITIVE - " + patternDescription;
      }
    };
  }

  @Override
  public void write(
      Environment env, TSList<WriteState> stack, TSMap<String, Object> data, EventConsumer writer) {
    String value = ((StringBuilder) data.get(id)).toString();
    if (matcher.match(env, value)) {
      writer.jsonSpecialPrimitive(value);
    } else {
      writer.primitive(invalidPrefix + value);
    }
  }

  @Override
  protected boolean isSingularValue() {
    return true;
  }

  @Override
  protected boolean isTypedValue() {
    return false;
  }

  public static class Config {
    public final String id;
    public final Pattern pattern;
    public final String patternDescription;
    public final String invalidPrefix;

    private Config(String id, Pattern pattern, String patternDescription, String invalidPrefix) {
      this.id = id;
      this.pattern = pattern;
      this.patternDescription = patternDescription;
      this.invalidPrefix = invalidPrefix;
    }
  }
}
