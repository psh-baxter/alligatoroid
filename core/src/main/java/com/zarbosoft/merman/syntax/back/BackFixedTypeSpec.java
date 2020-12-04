package com.zarbosoft.merman.syntax.back;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.editor.backevents.ETypeEvent;
import com.zarbosoft.merman.editor.serialization.Write;
import com.zarbosoft.merman.misc.TSMap;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.merman.syntax.error.TypeInvalidAtLocation;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.events.nodes.MatchingEventTerminal;
import com.zarbosoft.pidgoon.nodes.Sequence;

import java.util.Deque;
import java.util.Iterator;
import java.util.List;

public class BackFixedTypeSpec extends BackSpec {
  public String type;

  public BackSpec value;

  @Override
  public Node buildBackRule(final Syntax syntax) {
    return new Sequence()
        .add(new MatchingEventTerminal(new ETypeEvent(type)))
        .add(value.buildBackRule(syntax));
  }

  @Override
  public void finish(
    List<Object> errors,
    final Syntax syntax,
    final Path typePath,
    final TSMap<String, BackSpecData> fields,
    boolean singularRestriction, boolean typeRestriction
  ) {
    super.finish(errors, syntax, typePath, fields, singularRestriction, typeRestriction);
    if (typeRestriction) {
      errors.add(new TypeInvalidAtLocation(typePath));
    }
    value.finish(errors, syntax, typePath.add("value"), fields, true, true);
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
    Deque<Write.WriteState> stack, TSMap<String, Object> data, Write.EventConsumer writer) {
    writer.type(type);
    stack.addLast(new Write.WriteStateBack(data, ImmutableList.of(value).iterator()));
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
    return value.walkStep();
  }
}
