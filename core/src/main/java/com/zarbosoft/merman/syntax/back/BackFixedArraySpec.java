package com.zarbosoft.merman.syntax.back;

import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.editor.backevents.EArrayCloseEvent;
import com.zarbosoft.merman.editor.backevents.EArrayOpenEvent;
import com.zarbosoft.merman.editor.serialization.Write;
import com.zarbosoft.merman.misc.MultiError;
import com.zarbosoft.merman.misc.TSMap;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.events.nodes.MatchingEventTerminal;
import com.zarbosoft.pidgoon.nodes.Sequence;

import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

public class BackFixedArraySpec extends BackSpec {
  public List<BackSpec> elements = new ArrayList<>();

  @Override
  protected Iterator<BackSpec> walkStep() {
    return elements.iterator();
  }

  @Override
  public Node buildBackRule(final Syntax syntax) {
    final Sequence sequence;
    sequence = new Sequence();
    sequence.add(new MatchingEventTerminal(new EArrayOpenEvent()));
    for (final BackSpec element : elements) {
      sequence.add(element.buildBackRule(syntax));
    }
    sequence.add(new MatchingEventTerminal(new EArrayCloseEvent()));
    return sequence;
  }

  @Override
  public void finish(
          MultiError errors,
          final Syntax syntax,
          final Path typePath,
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
    Deque<Write.WriteState> stack, TSMap<String, Object> data, Write.EventConsumer writer) {
    writer.arrayBegin();
    stack.addLast(new Write.WriteStateArrayEnd());
    stack.addLast(new Write.WriteStateBack(data, elements.iterator()));
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
