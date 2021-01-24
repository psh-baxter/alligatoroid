package com.zarbosoft.merman.syntax.back;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.ValueAtom;
import com.zarbosoft.merman.editor.serialization.EventConsumer;
import com.zarbosoft.merman.editor.serialization.WriteState;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.events.stores.StackStore;
import com.zarbosoft.pidgoon.nodes.Operator;
import com.zarbosoft.pidgoon.nodes.Reference;
import com.zarbosoft.rendaw.common.TSMap;

import java.util.Deque;
import java.util.Iterator;

public class BackAtomSpec extends BaseBackAtomSpec {
  public BackAtomSpec(Config config) {
    super(config);
  }

  @Override
  protected Iterator<BackSpec> walkStep() {
    return null;
  }

  public Node buildBackRule(final Syntax syntax) {
    return new Operator<StackStore>(new Reference(type)) {
      @Override
      protected StackStore process(StackStore store) {
        final Atom value = store.stackTop();
        store = store.popStack();
        return store.stackVarDoubleElement(id, new ValueAtom(BackAtomSpec.this, value));
      }
    };
  }

  @Override
  public void write(
          Deque<WriteState> stack, TSMap<String, Object> data, EventConsumer writer) {
    ((Atom) data.getNull(id)).write(stack);
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
