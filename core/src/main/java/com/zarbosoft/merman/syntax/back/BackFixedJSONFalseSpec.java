package com.zarbosoft.merman.syntax.back;

import com.zarbosoft.merman.editor.backevents.JFalseEvent;
import com.zarbosoft.merman.editor.serialization.Write;
import com.zarbosoft.merman.misc.TSMap;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.events.nodes.MatchingEventTerminal;

import java.util.Deque;
import java.util.Iterator;

public class BackFixedJSONFalseSpec extends BackSpec {

  @Override
  public Node buildBackRule(final Syntax syntax) {
    return new MatchingEventTerminal(new JFalseEvent());
  }

  @Override
  public void write(
    Deque<Write.WriteState> stack, TSMap<String, Object> data, Write.EventConsumer writer) {
    writer.jsonFalse();
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
