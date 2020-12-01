package com.zarbosoft.merman.syntax.back;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.editor.backevents.EArrayCloseEvent;
import com.zarbosoft.merman.editor.backevents.EArrayOpenEvent;
import com.zarbosoft.merman.editor.serialization.Write;
import com.zarbosoft.merman.syntax.AtomType;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.events.nodes.MatchingEventTerminal;
import com.zarbosoft.pidgoon.nodes.Sequence;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import static com.zarbosoft.rendaw.common.Common.enumerate;

public class BackFixedArraySpec extends BackSpec {

  public List<BackSpec> elements = new ArrayList<>();

  @Override
  public Node buildBackRule(final Syntax syntax, final AtomType atomType) {
    final Sequence sequence;
    sequence = new Sequence();
    sequence.add(new MatchingEventTerminal(new EArrayOpenEvent()));
    for (final BackSpec element : elements) {
      sequence.add(element.buildBackRule(syntax, atomType));
    }
    sequence.add(new MatchingEventTerminal(new EArrayCloseEvent()));
    return sequence;
  }

  @Override
  public void finish(
      final Syntax syntax, final AtomType atomType, final Map<String, BackSpecData> fields) {
    enumerate(elements.stream())
        .forEach(
            pair -> {
              pair.second.finish(syntax, atomType, fields);
              pair.second.parent =
                  new PartParent() {
                    @Override
                    public BackSpec part() {
                      return BackFixedArraySpec.this;
                    }

                    @Override
                    public String pathSection() {
                      return pair.first.toString();
                    }
                  };
            });
  }

  @Override
  public void write(Deque<Write.WriteState> stack, Atom base, Write.EventConsumer writer) {
    writer.arrayBegin();
    stack.addLast(new Write.WriteStateArrayEnd());
    stack.addLast(new Write.WriteStateBack(base, elements.iterator()));
  }
}
