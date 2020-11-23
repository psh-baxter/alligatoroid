package com.zarbosoft.merman.syntax.back;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.ValueArray;
import com.zarbosoft.merman.editor.backevents.EArrayCloseEvent;
import com.zarbosoft.merman.editor.backevents.EArrayOpenEvent;
import com.zarbosoft.merman.syntax.AtomType;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.events.nodes.MatchingEventTerminal;
import com.zarbosoft.pidgoon.events.stores.StackStore;
import com.zarbosoft.pidgoon.nodes.Operator;
import com.zarbosoft.pidgoon.nodes.Repeat;
import com.zarbosoft.pidgoon.nodes.Sequence;
import com.zarbosoft.rendaw.common.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class BackArraySpec extends BackSpec {

  public String middle;

  @Override
  public Node buildBackRule(final Syntax syntax, final AtomType atomType) {
    return new Operator<StackStore>(
        new Sequence()
            .add(new MatchingEventTerminal(new EArrayOpenEvent()))
            .add(StackStore.prepVarStack)
            .add(
                new Repeat(
                    new Sequence()
                        .add(syntax.backRuleRef(atomType.getDataArray(middle).type))
                        .add(StackStore.pushVarStackSingle)))
            .add(new MatchingEventTerminal(new EArrayCloseEvent()))) {
      @Override
      protected StackStore process(StackStore store) {
        final List<Atom> temp = new ArrayList<>();
        store = store.popVarSingleList(temp);
        Collections.reverse(temp);
        return store.stackSingleElement(
            new Pair<>(middle, new ValueArray(atomType.getDataArray(middle), temp)));
      }
    };
  }

  @Override
  public void finish(final Syntax syntax, final AtomType atomType, final Set<String> middleUsed) {
    middleUsed.add(middle);
    atomType.getDataArray(middle);
  }
}
