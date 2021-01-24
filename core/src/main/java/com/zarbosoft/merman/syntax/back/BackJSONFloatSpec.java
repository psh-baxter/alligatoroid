package com.zarbosoft.merman.syntax.back;

import com.zarbosoft.merman.document.values.ValuePrimitive;
import com.zarbosoft.merman.editor.I18nEngine;
import com.zarbosoft.merman.editor.backevents.JFloatEvent;
import com.zarbosoft.merman.editor.serialization.EventConsumer;
import com.zarbosoft.merman.editor.serialization.WriteState;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.merman.syntax.primitivepattern.JsonDecimal;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.events.nodes.MatchingEventTerminal;
import com.zarbosoft.pidgoon.events.stores.StackStore;
import com.zarbosoft.pidgoon.nodes.Operator;
import com.zarbosoft.rendaw.common.TSMap;

import java.util.Deque;
import java.util.Iterator;

public class BackJSONFloatSpec extends BaseBackPrimitiveSpec {
  protected BackJSONFloatSpec(I18nEngine i18n, String id) {
    super(i18n,new Config(id, new JsonDecimal()));
  }

  @Override
  protected Iterator<BackSpec> walkStep() {
    return null;
  }

  @Override
  public Node buildBackRule(final Syntax syntax) {
    return new Operator<StackStore>(new MatchingEventTerminal(new JFloatEvent(null))) {
      @Override
      protected StackStore process(StackStore store) {
        return store.stackVarDoubleElement(
            id, new ValuePrimitive(BackJSONFloatSpec.this, ((JFloatEvent) store.top()).value));
      }
    };
  }

  @Override
  public void write(
          Deque<WriteState> stack, TSMap<String, Object> data, EventConsumer writer) {
    writer.jsonFloat(((StringBuilder) data.get(id)).toString());
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
