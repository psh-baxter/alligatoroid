package com.zarbosoft.merman.core.syntax.back;

import com.zarbosoft.merman.core.Environment;
import com.zarbosoft.merman.core.backevents.BackEvent;
import com.zarbosoft.merman.core.backevents.EKeyEvent;
import com.zarbosoft.merman.core.serialization.EventConsumer;
import com.zarbosoft.merman.core.serialization.WriteState;
import com.zarbosoft.merman.core.syntax.AtomType;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.pidgoon.events.nodes.MatchingEventTerminal;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.nodes.Discard;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;

import java.util.Iterator;

public class BackFixedKeySpec extends BackSpec {
  public final String value;

  public BackFixedKeySpec(String value) {
    this.value = value;
  }

  @Override
  public Node<ROList<AtomType.FieldParseResult>> buildBackRule(Environment env, Syntax syntax) {
    return new Discard<>(new MatchingEventTerminal<BackEvent>(new EKeyEvent(value)));
  }

  @Override
  public void write(TSList<WriteState> stack, TSMap<String, Object> data, EventConsumer writer) {
    writer.key(value);
  }

  @Override
  protected boolean isSingularValue() {
    return false;
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
