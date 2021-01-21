package com.zarbosoft.merman.syntax.back;

import com.zarbosoft.merman.document.values.ValuePrimitive;
import com.zarbosoft.merman.editor.backevents.EKeyEvent;
import com.zarbosoft.merman.editor.serialization.Write;
import com.zarbosoft.merman.misc.TSMap;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.events.nodes.ClassEqTerminal;
import com.zarbosoft.pidgoon.events.stores.StackStore;
import com.zarbosoft.pidgoon.nodes.Operator;

import java.util.Deque;
import java.util.Iterator;

public class BackKeySpec extends BaseBackPrimitiveSpec {

  public BackKeySpec(Config config) {
    super(config);
  }

  @Override
  protected Iterator<BackSpec> walkStep() {
    return null;
  }

  @Override
  public Node buildBackRule(final Syntax syntax) {
    return new Operator<StackStore>(new ClassEqTerminal(EKeyEvent.class)) {
      @Override
      protected StackStore process(StackStore store) {
        return store.stackVarDoubleElement(
            id, new ValuePrimitive(BackKeySpec.this, ((EKeyEvent) store.top()).value));
      }
    };
  }

  @Override
  public void write(
      Deque<Write.WriteState> stack, TSMap<String, Object> data, Write.EventConsumer writer) {
    writer.key(((StringBuilder) data.get(id)).toString());
  }

  @Override
  protected boolean isSingularValue() {
    return false;
  }

  @Override
  protected boolean isTypedValue() {
    return false;
  }
}
