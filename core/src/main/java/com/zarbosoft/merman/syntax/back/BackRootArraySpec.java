package com.zarbosoft.merman.syntax.back;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.ValueArray;
import com.zarbosoft.merman.editor.serialization.Write;
import com.zarbosoft.merman.syntax.AtomType;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.events.stores.StackStore;
import com.zarbosoft.pidgoon.nodes.Operator;
import com.zarbosoft.pidgoon.nodes.Reference;
import com.zarbosoft.pidgoon.nodes.Repeat;
import com.zarbosoft.pidgoon.nodes.Sequence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;

public class BackRootArraySpec extends BaseBackArraySpec {
  @Override
  public Node buildBackRule(final Syntax syntax, final AtomType atomType) {
    final Sequence sequence;
    sequence = new Sequence();
    sequence.add(StackStore.prepVarStack);
    sequence.add(
        new Repeat(new Sequence().add(new Reference(type)).add(StackStore.pushVarStackSingle)));
    return new Operator<StackStore>(sequence) {
      @Override
      protected StackStore process(StackStore store) {
        final List<Atom> temp = new ArrayList<>();
        store = store.popVarSingleList(temp);
        Collections.reverse(temp);
        final ValueArray value = new ValueArray(BackRootArraySpec.this, temp);
        return store.stackVarDoubleElement(id, value);
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
    stack.addLast(new Write.WriteStateDataArray(((ValueArray) base.fields.get(id))));
  }
}
