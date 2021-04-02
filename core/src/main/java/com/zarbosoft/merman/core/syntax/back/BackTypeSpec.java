package com.zarbosoft.merman.core.syntax.back;

import com.zarbosoft.merman.core.document.values.FieldPrimitive;
import com.zarbosoft.merman.core.editor.I18nEngine;
import com.zarbosoft.merman.core.editor.Path;
import com.zarbosoft.merman.core.editor.backevents.ETypeEvent;
import com.zarbosoft.merman.core.editor.serialization.EventConsumer;
import com.zarbosoft.merman.core.editor.serialization.WriteState;
import com.zarbosoft.merman.core.editor.serialization.WriteStateBack;
import com.zarbosoft.merman.core.misc.MultiError;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.core.syntax.error.TypeInvalidAtLocation;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.events.nodes.MatchingEventTerminal;
import com.zarbosoft.pidgoon.events.StackStore;
import com.zarbosoft.pidgoon.nodes.Operator;
import com.zarbosoft.pidgoon.nodes.Sequence;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;

import java.util.Arrays;
import java.util.Iterator;

public class BackTypeSpec extends BaseBackPrimitiveSpec {
  public final String type;

  public final BackSpec value;

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

  protected BackTypeSpec(I18nEngine i18n,Config config) {
    super(config.base);
    this.type = config.type;
    this.value = config.value;
  }

  @Override
  protected Iterator<BackSpec> walkStep() {
    return value.walkStep();
  }

  @Override
  public Node buildBackRule(I18nEngine i18n, final Syntax syntax) {
    return new Sequence()
        .add(
            new Operator<StackStore>(new MatchingEventTerminal(new ETypeEvent())) {
              @Override
              protected StackStore process(StackStore store) {
                return store.stackVarDoubleElement(
                    id, new FieldPrimitive(BackTypeSpec.this, ((ETypeEvent) store.top()).value));
              }
            })
        .add(value.buildBackRule(i18n, syntax));
  }

  @Override
  public void write(
          TSList<WriteState> stack, TSMap<String, Object> data, EventConsumer writer) {
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
      Path typePath,
      boolean singularRestriction,
      boolean typeRestriction) {
    super.finish(errors, syntax, typePath, singularRestriction, typeRestriction);
    if (typeRestriction) {
      errors.add(new TypeInvalidAtLocation(typePath));
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
}
