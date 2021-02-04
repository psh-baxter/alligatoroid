package com.zarbosoft.merman.syntax.back;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.editor.backevents.EArrayCloseEvent;
import com.zarbosoft.merman.editor.backevents.EArrayOpenEvent;
import com.zarbosoft.merman.editor.serialization.EventConsumer;
import com.zarbosoft.merman.editor.serialization.WriteState;
import com.zarbosoft.merman.editor.serialization.WriteStateArrayEnd;
import com.zarbosoft.merman.editor.serialization.WriteStateDeepDataArray;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.events.nodes.MatchingEventTerminal;
import com.zarbosoft.pidgoon.nodes.Sequence;
import com.zarbosoft.rendaw.common.TSMap;

import java.util.Deque;
import java.util.List;

public class BackArraySpec extends BaseBackSimpleArraySpec {
  public BackArraySpec(Config config) {
    super(config);
  }

  @Override
  public Node buildBackRule(final Syntax syntax) {
    return new Sequence()
        .add(new MatchingEventTerminal(new EArrayOpenEvent()))
        .visit(s -> buildBackRuleInner(syntax, s))
        .add(new MatchingEventTerminal(new EArrayCloseEvent()))
        .visit(s -> buildBackRuleInnerEnd(s));
  }

  @Override
  public void write(Deque<WriteState> stack, TSMap<String, Object> data, EventConsumer writer) {
    writer.arrayBegin();
    stack.addLast(new WriteStateArrayEnd());
    stack.addLast(new WriteStateDeepDataArray(((List<Atom>) data.get(id)), splayedBoilerplate));
  }

  @Override
  protected boolean isSingularValue() {
    return true;
  }
}
