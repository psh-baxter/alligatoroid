package com.zarbosoft.merman.syntax.back;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.ValuePrimitive;
import com.zarbosoft.merman.editor.backevents.JFloatEvent;
import com.zarbosoft.merman.editor.serialization.Write;
import com.zarbosoft.merman.syntax.AtomType;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.events.nodes.MatchingEventTerminal;
import com.zarbosoft.pidgoon.events.stores.StackStore;
import com.zarbosoft.pidgoon.nodes.Operator;
import com.zarbosoft.rendaw.common.Pair;

import java.util.Deque;
import java.util.Map;

public class BackJSONFloatSpec extends BaseBackPrimitiveSpec {
  @Override
  public Node buildBackRule(final Syntax syntax, final AtomType atomType) {
    return new Operator<StackStore>(new MatchingEventTerminal(new JFloatEvent(null))) {
      @Override
      protected StackStore process(StackStore store) {
        return store.stackSingleElement(
            new Pair<>(
                id, new ValuePrimitive(BackJSONFloatSpec.this, ((JFloatEvent) store.top()).value)));
      }
    };
  }

  public void finish(
      final Syntax syntax, final AtomType atomType, final Map<String, BackSpecData> fields) {
    fields.put(id, this);
  }

  @Override
  public void write(Deque<Write.WriteState> stack, Atom base, Write.EventConsumer writer) {
    writer.jsonFloat(((ValuePrimitive) base.fields.get(id)).get());
  }
}
