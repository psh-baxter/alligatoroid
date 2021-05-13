package com.zarbosoft.merman.core.syntax.back;

import com.zarbosoft.merman.core.Environment;
import com.zarbosoft.merman.core.MultiError;
import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.backevents.BackEvent;
import com.zarbosoft.merman.core.backevents.JSpecialPrimitiveEvent;
import com.zarbosoft.merman.core.document.fields.FieldPrimitive;
import com.zarbosoft.merman.core.serialization.EventConsumer;
import com.zarbosoft.merman.core.serialization.WriteState;
import com.zarbosoft.merman.core.syntax.AtomType;
import com.zarbosoft.merman.core.syntax.BackType;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.core.syntax.error.BackElementUnsupportedInBackFormat;
import com.zarbosoft.pidgoon.events.nodes.Terminal;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;

import java.util.Iterator;

public class BackJSONSpecialPrimitiveSpec extends BaseBackPrimitiveSpec {
  public BackJSONSpecialPrimitiveSpec(Config config) {
    super(config);
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
        if (!(event instanceof JSpecialPrimitiveEvent)) return new ROPair<>(false, null);
        boolean ok = matcher == null || matcher.match(env, ((JSpecialPrimitiveEvent) event).value);
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
      }

      @Override
      public String toString() {
        return matcher == null
            ? "ANY JSON SPECIAL PRIMITIVE"
            : ("JSON SPECIAL PRIMITIVE - " + patternDescription);
      }
    };
  }

  @Override
  public void write(TSList<WriteState> stack, TSMap<String, Object> data, EventConsumer writer) {
    writer.jsonSpecialPrimitive(((StringBuilder) data.get(id)).toString());
  }

  @Override
  protected boolean isSingularValue() {
    return true;
  }

  @Override
  protected boolean isTypedValue() {
    return false;
  }
}
