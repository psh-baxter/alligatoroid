package com.zarbosoft.merman.core.syntax.back;

import com.zarbosoft.merman.core.Environment;
import com.zarbosoft.merman.core.MultiError;
import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.backevents.BackEvent;
import com.zarbosoft.merman.core.backevents.ETypeEvent;
import com.zarbosoft.merman.core.serialization.EventConsumer;
import com.zarbosoft.merman.core.serialization.WriteState;
import com.zarbosoft.merman.core.serialization.WriteStateBack;
import com.zarbosoft.merman.core.syntax.AtomType;
import com.zarbosoft.merman.core.syntax.BackType;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.core.syntax.error.BackElementUnsupportedInBackFormat;
import com.zarbosoft.merman.core.syntax.error.TypeInvalidAtLocation;
import com.zarbosoft.pidgoon.events.nodes.MatchingEventTerminal;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.nodes.MergeSequence;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;

import java.util.Arrays;
import java.util.Iterator;

public class BackFixedTypeSpec extends BackSpec {
  public final String type;
  public final BackSpec value;

  public static class Config {
    public final String type;
    public final BackSpec value;

    public Config(String type, BackSpec value) {
      this.type = type;
      this.value = value;
    }
  }

  public BackFixedTypeSpec(Config config) {
    this.type = config.type;
    this.value = config.value;
  }

  @Override
  public Node<ROList<AtomType.FieldParseResult>> buildBackRule(Environment env, Syntax syntax) {
    return new MergeSequence<AtomType.FieldParseResult>()
            .addIgnored(new MatchingEventTerminal<BackEvent>(new ETypeEvent(type)))
            .add(value.buildBackRule(env, syntax));
  }


  @Override
  public void finish(
          MultiError errors,
          final Syntax syntax,
          final SyntaxPath typePath,
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
            return BackFixedTypeSpec.this;
          }

          @Override
          public String pathSection() {
            return null;
          }
        };
  }

  @Override
  public void write(
          TSList<WriteState> stack, TSMap<String, Object> data, EventConsumer writer) {
    writer.type(type);
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

  @Override
  protected Iterator<BackSpec> walkStep() {
    return TSList.of(value).iterator();
  }
}
