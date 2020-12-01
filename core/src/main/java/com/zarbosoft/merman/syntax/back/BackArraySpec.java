package com.zarbosoft.merman.syntax.back;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.ValueArray;
import com.zarbosoft.merman.editor.backevents.EArrayCloseEvent;
import com.zarbosoft.merman.editor.backevents.EArrayOpenEvent;
import com.zarbosoft.merman.editor.serialization.Write;
import com.zarbosoft.merman.syntax.AtomType;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.events.nodes.MatchingEventTerminal;
import com.zarbosoft.pidgoon.events.stores.StackStore;
import com.zarbosoft.pidgoon.nodes.Operator;
import com.zarbosoft.pidgoon.nodes.Repeat;
import com.zarbosoft.pidgoon.nodes.Sequence;
import com.zarbosoft.rendaw.common.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;

public class BackArraySpec extends BaseBackArraySpec {
  @Override
  public Node buildBackRule(final Syntax syntax, final AtomType atomType) {
    return new Operator<StackStore>(
        new Sequence()
            .add(new MatchingEventTerminal(new EArrayOpenEvent()))
            .add(StackStore.prepVarStack)
            .add(
                new Repeat(
                    new Sequence()
                        .add(syntax.backRuleRef(type))
                        .add(StackStore.pushVarStackSingle)))
            .add(new MatchingEventTerminal(new EArrayCloseEvent()))) {
      @Override
      protected StackStore process(StackStore store) {
        final List<Atom> temp = new ArrayList<>();
        store = store.popVarSingleList(temp);
        Collections.reverse(temp);
        return store.stackSingleElement(new Pair<>(id, new ValueArray(BackArraySpec.this, temp)));
      }
    };
  }

  @Override
  public void finish(
      final Syntax syntax, final AtomType atomType, final Map<String, BackSpecData> fields) {
    fields.put(id, this);
  }

  @Override
  public void write(Deque<Write.WriteState> stack, Atom base, Write.EventConsumer writer) {
    writer.arrayBegin();
    stack.addLast(new Write.WriteStateArrayEnd());
    stack.addLast(new Write.WriteStateDataArray(((ValueArray) base.fields.get(id))));
  }
}
