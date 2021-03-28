package com.zarbosoft.merman.core.syntax.back;

import com.zarbosoft.merman.core.editor.backevents.JSpecialPrimitiveEvent;
import com.zarbosoft.merman.core.editor.serialization.EventConsumer;
import com.zarbosoft.merman.core.editor.serialization.WriteState;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.events.nodes.MatchingEventTerminal;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;

import java.util.Iterator;

public class BackFixedJSONSpecialPrimitiveSpec extends BackSpec {
  public final String value;

  public BackFixedJSONSpecialPrimitiveSpec(String value) {
    this.value = value;
  }

  @Override
  public Node buildBackRule(final Syntax syntax) {
    return new MatchingEventTerminal(new JSpecialPrimitiveEvent(value));
  }

  @Override
  public void write(TSList<WriteState> stack, TSMap<String, Object> data, EventConsumer writer) {
    writer.jsonSpecialPrimitive(value);
  }

  @Override
  protected boolean isSingularValue() {
    return true;
  }

  @Override
  protected boolean isTypedValue() {
    return false;
  }

  @Override
  protected Iterator<BackSpec> walkStep() {
    return null;
  }
}
