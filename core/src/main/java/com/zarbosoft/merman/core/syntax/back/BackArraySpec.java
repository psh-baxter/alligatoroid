package com.zarbosoft.merman.core.syntax.back;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.Environment;
import com.zarbosoft.merman.core.backevents.EArrayCloseEvent;
import com.zarbosoft.merman.core.backevents.EArrayOpenEvent;
import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.serialization.EventConsumer;
import com.zarbosoft.merman.core.serialization.WriteState;
import com.zarbosoft.merman.core.serialization.WriteStateArrayEnd;
import com.zarbosoft.merman.core.serialization.WriteStateDeepDataArray;
import com.zarbosoft.merman.core.syntax.AtomType;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.pidgoon.events.nodes.MatchingEventTerminal;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.nodes.UnitSequence;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.TSList;

import java.util.Map;
import java.util.function.Consumer;

public class BackArraySpec extends BaseBackArraySpec {
  public BackArraySpec(Config config) {
    super(config);
  }

  @Override
  public void copy(Context context, TSList<Atom> children) {
    context.copy(Context.CopyContext.ARRAY, new TSList<>(writeContents(children)));
  }

  @Override
  public void uncopy(Context context, Consumer<ROList<Atom>> consumer) {
    context.uncopy(
        buildBackRuleInner(context.env, context.syntax), Context.CopyContext.ARRAY, consumer);
  }

  @Override
  public Node<ROList<AtomType.FieldParseResult>> buildBackRule(Environment env, Syntax syntax) {
    return buildBackRuleInnerEnd(
        new UnitSequence<ROList<AtomType.AtomParseResult>>()
            .addIgnored(new MatchingEventTerminal(new EArrayOpenEvent()))
            .add(buildBackRuleInner(env, syntax))
            .addIgnored(new MatchingEventTerminal(new EArrayCloseEvent())));
  }

  @Override
  public void write(Environment env, TSList<WriteState> stack, Map<Object, Object> data, EventConsumer writer) {
    writer.arrayBegin();
    stack.add(new WriteStateArrayEnd());
    stack.add(writeContents((TSList<Atom>) data.get(id)));
  }

  private WriteState writeContents(ROList<Atom> atoms) {
    return new WriteStateDeepDataArray(atoms, splayedBoilerplate);
  }

  @Override
  protected boolean isSingularValue() {
    return true;
  }
}
