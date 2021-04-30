package com.zarbosoft.merman.core.syntax.back;

import com.zarbosoft.merman.core.Environment;
import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.backevents.EArrayCloseEvent;
import com.zarbosoft.merman.core.backevents.EArrayOpenEvent;
import com.zarbosoft.merman.core.serialization.EventConsumer;
import com.zarbosoft.merman.core.serialization.WriteState;
import com.zarbosoft.merman.core.serialization.WriteStateArrayEnd;
import com.zarbosoft.merman.core.serialization.WriteStateBack;
import com.zarbosoft.merman.core.MultiError;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.events.nodes.MatchingEventTerminal;
import com.zarbosoft.pidgoon.nodes.Sequence;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BackFixedArraySpec extends BackSpec {
  public List<BackSpec> elements = new ArrayList<>();

  @Override
  protected Iterator<BackSpec> walkStep() {
    return elements.iterator();
  }

  @Override
  public Node buildBackRule(Environment env, final Syntax syntax) {
    final Sequence sequence;
    sequence = new Sequence();
    sequence.add(new MatchingEventTerminal(new EArrayOpenEvent()));
    for (final BackSpec element : elements) {
      sequence.add(element.buildBackRule(env, syntax));
    }
    sequence.add(new MatchingEventTerminal(new EArrayCloseEvent()));
    return sequence;
  }

  @Override
  public void finish(
          MultiError errors,
          final Syntax syntax,
          final SyntaxPath typePath,
          boolean singularRestriction,
          boolean typeRestriction) {
    super.finish(errors, syntax, typePath, singularRestriction, typeRestriction);
    for (int i = 0; i < elements.size(); ++i) {
      BackSpec element = elements.get(i);
      element.finish(errors, syntax, typePath.add(Integer.toString(i)), false, false);
      int finalI = i;
      element.parent =
          new PartParent() {
            @Override
            public BackSpec part() {
              return BackFixedArraySpec.this;
            }

            @Override
            public String pathSection() {
              return Integer.toString(finalI);
            }
          };
    }
  }

  @Override
  public void write(
          TSList<WriteState> stack, TSMap<String, Object> data, EventConsumer writer) {
    writer.arrayBegin();
    stack.add(new WriteStateArrayEnd());
    stack.add(new WriteStateBack(data, elements.iterator()));
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
