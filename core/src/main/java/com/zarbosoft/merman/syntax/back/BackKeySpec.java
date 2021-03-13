package com.zarbosoft.merman.syntax.back;

import com.zarbosoft.merman.document.values.FieldPrimitive;
import com.zarbosoft.merman.editor.I18nEngine;
import com.zarbosoft.merman.editor.backevents.EKeyEvent;
import com.zarbosoft.merman.editor.serialization.EventConsumer;
import com.zarbosoft.merman.editor.serialization.WriteState;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.events.nodes.MatchingEventTerminal;
import com.zarbosoft.pidgoon.events.StackStore;
import com.zarbosoft.pidgoon.nodes.Operator;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;

import java.util.Iterator;

public class BackKeySpec extends BaseBackPrimitiveSpec {

  public BackKeySpec(I18nEngine i18n,Config config) {
    super(i18n,config);
  }

  @Override
  protected Iterator<BackSpec> walkStep() {
    return null;
  }

  @Override
  public Node buildBackRule(final Syntax syntax) {
    return new Operator<StackStore>(new MatchingEventTerminal(new EKeyEvent())) {
      @Override
      protected StackStore process(StackStore store) {
        return store.stackVarDoubleElement(
            id, new FieldPrimitive(BackKeySpec.this, ((EKeyEvent) store.top()).value));
      }
    };
  }

  @Override
  public void write(
          TSList<WriteState> stack, TSMap<String, Object> data, EventConsumer writer) {
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
