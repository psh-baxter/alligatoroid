package com.zarbosoft.merman.core.syntax.back;

import com.zarbosoft.merman.core.document.values.FieldPrimitive;
import com.zarbosoft.merman.core.editor.I18nEngine;
import com.zarbosoft.merman.core.editor.backevents.JSpecialPrimitiveEvent;
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

public class BackJSONSpecialPrimitiveSpec extends BaseBackPrimitiveSpec {

  public BackJSONSpecialPrimitiveSpec(Config config) {
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
                if (!(event instanceof JSpecialPrimitiveEvent)) return false;
                if (matcher == null) return true;
                return matcher.match(i18n, ((JSpecialPrimitiveEvent) event).value);
              }

              @Override
              public String toString() {
                return matcher == null ? "ANY JSON SPECIAL PRIMITIVE" : "JSON SPECIAL PRIMITIVE MATCHING PATTERN";
              }
            })
        .add(
            new Operator<StackStore>() {
              @Override
              protected StackStore process(StackStore store) {
                return store.stackVarDoubleElement(
                    id,
                    new ROPair<>(
                        new FieldPrimitive(
                            BackJSONSpecialPrimitiveSpec.this,
                            ((JSpecialPrimitiveEvent) store.top()).value),
                        null));
              }
            });
  }

  @Override
  public void write(TSList<WriteState> stack, TSMap<String, Object> data, EventConsumer writer) {
    writer.jsonSpecialPrimitive(((StringBuilder) data.get(id)).toString());
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
