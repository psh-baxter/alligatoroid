package com.zarbosoft.merman.syntax.back;

import com.zarbosoft.merman.document.values.ValuePrimitive;
import com.zarbosoft.merman.editor.backevents.JIntEvent;
import com.zarbosoft.merman.syntax.AtomType;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.events.nodes.MatchingEventTerminal;
import com.zarbosoft.pidgoon.events.stores.StackStore;
import com.zarbosoft.pidgoon.nodes.Operator;
import com.zarbosoft.rendaw.common.Pair;

import java.util.Set;

public class BackJSONIntSpec extends BackSpec {

  public String middle;

  @Override
  public Node buildBackRule(final Syntax syntax, final AtomType atomType) {
    return new Operator<StackStore>(new MatchingEventTerminal(new JIntEvent(null))) {
      @Override
      protected StackStore process(StackStore store) {
        return store.stackSingleElement(
            new Pair<>(
                middle,
                new ValuePrimitive(
                    atomType.getDataPrimitive(middle), ((JIntEvent) store.top()).value)));
      }
    };
  }

  public void finish(final Syntax syntax, final AtomType atomType, final Set<String> middleUsed) {
    middleUsed.add(middle);
    atomType.getDataPrimitive(middle);
  }
}
