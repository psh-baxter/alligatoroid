package com.zarbosoft.merman.syntax.back;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.editor.backevents.ETypeEvent;
import com.zarbosoft.merman.editor.serialization.Write;
import com.zarbosoft.merman.syntax.AtomType;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.events.nodes.MatchingEventTerminal;
import com.zarbosoft.pidgoon.nodes.Sequence;

import java.util.Deque;
import java.util.Map;

public class BackFixedTypeSpec extends BackSpec {
  public String type;

  public BackSpec value;

  @Override
  public Node buildBackRule(final Syntax syntax, final AtomType atomType) {
    return new Sequence()
        .add(new MatchingEventTerminal(new ETypeEvent(type)))
        .add(value.buildBackRule(syntax, atomType));
  }

  @Override
  public void finish(
      final Syntax syntax, final AtomType atomType, final Map<String, BackSpecData> fields) {
    super.finish(syntax, atomType, fields);
    value.finish(syntax, atomType, fields);
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
  public void write(Deque<Write.WriteState> stack, Atom base, Write.EventConsumer writer) {
    writer.type(type);
    stack.addLast(new Write.WriteStateBack(base, ImmutableList.of(value).iterator()));
  }
}
