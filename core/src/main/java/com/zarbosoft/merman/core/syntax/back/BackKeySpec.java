package com.zarbosoft.merman.core.syntax.back;

import com.zarbosoft.merman.core.document.values.FieldPrimitive;
import com.zarbosoft.merman.core.editor.I18nEngine;
import com.zarbosoft.merman.core.editor.backevents.EKeyEvent;
import com.zarbosoft.merman.core.editor.serialization.EventConsumer;
import com.zarbosoft.merman.core.editor.serialization.WriteState;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.pidgoon.events.Event;
import com.zarbosoft.pidgoon.events.StackStore;
import com.zarbosoft.pidgoon.events.nodes.Terminal;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.model.Store;
import com.zarbosoft.pidgoon.nodes.Operator;
import com.zarbosoft.pidgoon.nodes.Sequence;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;

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
  public Node buildBackRule(I18nEngine i18n, final Syntax syntax) {
    return new Sequence()
        .add(
            new Terminal() {
              @Override
              protected boolean matches(Event event, Store store) {
                if (!(event instanceof EKeyEvent)) return false;
                if (matcher == null) return true;
                return matcher.match(i18n, ((EKeyEvent) event).value);
              }

              @Override
              public String toString() {
                return matcher == null ? "ANY KEY" : "KEY MATCHING PATTERN";
              }
            })
        .add(
            new Operator<StackStore>() {
              @Override
              protected StackStore process(StackStore store) {
                return store.stackVarDoubleElement(
                    id,
                    new ROPair<>(
                        new FieldPrimitive(BackKeySpec.this, ((EKeyEvent) store.top()).value),
                        null));
              }
            });
  }

  @Override
  public void write(TSList<WriteState> stack, TSMap<String, Object> data, EventConsumer writer) {
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
