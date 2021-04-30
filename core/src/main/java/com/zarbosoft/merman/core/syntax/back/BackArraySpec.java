package com.zarbosoft.merman.core.syntax.back;

import com.zarbosoft.merman.core.Environment;
import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.backevents.EArrayCloseEvent;
import com.zarbosoft.merman.core.backevents.EArrayOpenEvent;
import com.zarbosoft.merman.core.serialization.EventConsumer;
import com.zarbosoft.merman.core.serialization.WriteState;
import com.zarbosoft.merman.core.serialization.WriteStateArrayEnd;
import com.zarbosoft.merman.core.serialization.WriteStateDeepDataArray;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.events.nodes.MatchingEventTerminal;
import com.zarbosoft.pidgoon.nodes.Sequence;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;

public class BackArraySpec extends BaseBackSimpleArraySpec {
  public BackArraySpec(Config config) {
    super(config);
  }

  @Override
  public Node buildBackRule(Environment env, final Syntax syntax) {
    return new Sequence()
        .add(new MatchingEventTerminal(new EArrayOpenEvent()))
        .visit(s -> buildBackRuleInner(env, syntax, s))
        .add(new MatchingEventTerminal(new EArrayCloseEvent()))
        .visit(s -> buildBackRuleInnerEnd(s));
  }

  @Override
  public void write(TSList<WriteState> stack, TSMap<String, Object> data, EventConsumer writer) {
    writer.arrayBegin();
    stack.add(new WriteStateArrayEnd());
    stack.add(new WriteStateDeepDataArray(((TSList<Atom>) data.get(id)), splayedBoilerplate));
  }

  @Override
  protected boolean isSingularValue() {
    return true;
  }
}
