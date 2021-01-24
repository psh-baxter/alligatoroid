package com.zarbosoft.merman.syntax.back;

import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.editor.backevents.ETypeEvent;
import com.zarbosoft.merman.editor.serialization.EventConsumer;
import com.zarbosoft.merman.editor.serialization.WriteState;
import com.zarbosoft.merman.editor.serialization.WriteStateBack;
import com.zarbosoft.merman.misc.MultiError;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.merman.syntax.error.TypeInvalidAtLocation;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.events.nodes.MatchingEventTerminal;
import com.zarbosoft.pidgoon.nodes.Sequence;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;

import java.util.Arrays;
import java.util.Deque;
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
  public Node buildBackRule(final Syntax syntax) {
    return new Sequence()
        .add(new MatchingEventTerminal(new ETypeEvent(type)))
        .add(value.buildBackRule(syntax));
  }

  @Override
  public void finish(
          MultiError errors,
          final Syntax syntax,
          final Path typePath,
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
          Deque<WriteState> stack, TSMap<String, Object> data, EventConsumer writer) {
    writer.type(type);
    stack.addLast(new WriteStateBack(data, Arrays.asList(value).iterator()));
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
