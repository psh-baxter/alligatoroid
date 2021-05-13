package com.zarbosoft.merman.core.syntax.back;

import com.zarbosoft.merman.core.Environment;
import com.zarbosoft.merman.core.MultiError;
import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.backevents.BackEvent;
import com.zarbosoft.merman.core.backevents.ETypeEvent;
import com.zarbosoft.merman.core.document.fields.FieldPrimitive;
import com.zarbosoft.merman.core.serialization.EventConsumer;
import com.zarbosoft.merman.core.serialization.WriteState;
import com.zarbosoft.merman.core.serialization.WriteStateBack;
import com.zarbosoft.merman.core.syntax.AtomType;
import com.zarbosoft.merman.core.syntax.BackType;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.core.syntax.error.TypeInvalidAtLocation;
import com.zarbosoft.merman.core.syntax.error.BackElementUnsupportedInBackFormat;
import com.zarbosoft.pidgoon.events.nodes.Terminal;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;

import java.util.Arrays;
import java.util.Iterator;

public class BackTypeSpec extends BaseBackPrimitiveSpec {
  public final String type;

  public final BackSpec value;

  protected BackTypeSpec(Config config) {
    super(config.base);
    this.type = config.type;
    this.value = config.value;
  }

  @Override
  protected Iterator<BackSpec> walkStep() {
    return value.walkStep();
  }

  @Override
  public Node<ROList<AtomType.FieldParseResult>> buildBackRule(Environment env, Syntax syntax) {
    return new Terminal<BackEvent, ROList<AtomType.FieldParseResult>>() {
      @Override
      protected ROPair<Boolean, ROList<AtomType.FieldParseResult>> matches(BackEvent event) {
        if (!(event instanceof ETypeEvent)) return new ROPair<>(false, null);
        boolean ok = matcher == null || matcher.match(env, ((ETypeEvent) event).value);
        return new ROPair<>(
            ok,
            ok
                ? TSList.of(new AtomType.PrimitiveFieldParseResult(
                    id, new FieldPrimitive(BackTypeSpec.this, ((ETypeEvent) event).value)))
                : ROList.empty);
      }

      @Override
      public String toString() {
        return matcher == null ? "ANY TYPE" : ("TYPE - " + patternDescription);
      }
    };
  }

  @Override
  public void write(TSList<WriteState> stack, TSMap<String, Object> data, EventConsumer writer) {
    writer.type(((StringBuilder) data.get(type)).toString());
    stack.add(new WriteStateBack(data, Arrays.asList(value).iterator()));
  }

  @Override
  protected boolean isSingularValue() {
    return true;
  }

  @Override
  protected boolean isTypedValue() {
    return true;
  }

  public void finish(
      MultiError errors,
      final Syntax syntax,
      SyntaxPath typePath,
      boolean singularRestriction,
      boolean typeRestriction) {
    super.finish(errors, syntax, typePath, singularRestriction, typeRestriction);
    if (typeRestriction) {
      errors.add(new TypeInvalidAtLocation(typePath));
    }
    if (syntax.backType != BackType.LUXEM) {
      errors.add(new BackElementUnsupportedInBackFormat("type", syntax.backType, typePath));
    }
    value.finish(errors, syntax, typePath.add("value"), true, true);
    value.parent =
        new PartParent() {
          @Override
          public BackSpec part() {
            return BackTypeSpec.this;
          }

          @Override
          public String pathSection() {
            return null;
          }
        };
  }

  public static class Config {
    public final BaseBackPrimitiveSpec.Config base;
    public final String type;

    public final BackSpec value;

    public Config(BaseBackPrimitiveSpec.Config base, String type, BackSpec value) {
      this.base = base;
      this.type = type;
      this.value = value;
    }
  }
}
