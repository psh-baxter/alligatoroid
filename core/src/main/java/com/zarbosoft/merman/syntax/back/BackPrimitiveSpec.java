package com.zarbosoft.merman.syntax.back;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.ValuePrimitive;
import com.zarbosoft.merman.editor.backevents.EPrimitiveEvent;
import com.zarbosoft.merman.editor.serialization.Write;
import com.zarbosoft.merman.syntax.AtomType;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.events.nodes.ClassEqTerminal;
import com.zarbosoft.pidgoon.events.stores.StackStore;
import com.zarbosoft.pidgoon.nodes.Operator;

import java.util.Deque;
import java.util.Map;

public class BackPrimitiveSpec extends BaseBackPrimitiveSpec {
  @Override
  public Node buildBackRule(final Syntax syntax, final AtomType atomType) {
    return new Operator<StackStore>(new ClassEqTerminal(EPrimitiveEvent.class)) {
      @Override
      protected StackStore process(StackStore store) {
        return store.stackVarDoubleElement(
            id, new ValuePrimitive(BackPrimitiveSpec.this, ((EPrimitiveEvent) store.top()).value));
      }
    };
  }

  public void finish(
      final Syntax syntax, final AtomType atomType, final Map<String, BackSpecData> fields) {
    fields.put(id, this);
  }

  @Override
  public void write(Deque<Write.WriteState> stack, Atom base, Write.EventConsumer writer) {
    writer.primitive(((ValuePrimitive) base.fields.get(id)).get());
  }
}
