package com.zarbosoft.merman.syntax.back;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.ValueArray;
import com.zarbosoft.merman.document.values.ValueAtom;
import com.zarbosoft.merman.editor.backevents.EArrayCloseEvent;
import com.zarbosoft.merman.editor.backevents.EArrayOpenEvent;
import com.zarbosoft.merman.editor.serialization.Write;
import com.zarbosoft.merman.misc.TSMap;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.events.nodes.MatchingEventTerminal;
import com.zarbosoft.pidgoon.events.stores.StackStore;
import com.zarbosoft.pidgoon.nodes.Operator;
import com.zarbosoft.pidgoon.nodes.Repeat;
import com.zarbosoft.pidgoon.nodes.Sequence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

public class BackArraySpec extends BaseBackSimpleArraySpec {
  @Override
  protected Iterator<BackSpec> walkStep() {
    return element.walkStep();
  }

  @Override
  public Node buildBackRule(final Syntax syntax) {
    return new Operator<StackStore>(
        new Sequence()
            .add(new MatchingEventTerminal(new EArrayOpenEvent()))
            .add(StackStore.prepVarStack)
            .add(new Repeat(element.buildBackRule(syntax)))
            .add(new MatchingEventTerminal(new EArrayCloseEvent()))) {
      @Override
      protected StackStore process(StackStore store) {
        final List<Atom> temp = new ArrayList<>();
        store = store.<String, ValueAtom>popVarDouble((_k, v) -> temp.add(v.data));
        Collections.reverse(temp);
        return store.stackVarDoubleElement(id, new ValueArray(BackArraySpec.this, temp));
      }
    };
  }

  @Override
  public void write(
      Deque<Write.WriteState> stack, TSMap<String, Object> data, Write.EventConsumer writer) {
    writer.arrayBegin();
    stack.addLast(new Write.WriteStateArrayEnd());
    stack.addLast(
        new Write.WriteStateDeepDataArray(element, elementAtom.id, ((List<Atom>) data.get(id))));
  }

  @Override
  protected boolean isSingularValue() {
    return true;
  }
}
