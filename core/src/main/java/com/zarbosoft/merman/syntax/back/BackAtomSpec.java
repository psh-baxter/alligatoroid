package com.zarbosoft.merman.syntax.back;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.ValueAtom;
import com.zarbosoft.merman.syntax.AtomType;
import com.zarbosoft.merman.syntax.FreeAtomType;
import com.zarbosoft.merman.syntax.InvalidSyntax;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.merman.syntax.middle.MiddleAtomSpec;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.events.stores.StackStore;
import com.zarbosoft.pidgoon.nodes.Operator;
import com.zarbosoft.rendaw.common.Pair;

import java.util.Set;

import static com.zarbosoft.rendaw.common.Common.iterable;

public class BackAtomSpec extends BackSpec {

  public String middle;

  public Node buildBackRule(final Syntax syntax, final AtomType atomType) {
    return new Operator<StackStore>(syntax.backRuleRef(atomType.getDataNode(middle).type)) {
      @Override
      protected StackStore process(StackStore store) {
        final Atom value = store.stackTop();
        store = store.popStack();
        return store.stackSingleElement(
            new Pair<>(middle, new ValueAtom(atomType.getDataNode(middle), value)));
      }
    };
  }

  public void finish(final Syntax syntax, final AtomType atomType, final Set<String> middleUsed) {
    middleUsed.add(middle);
    final MiddleAtomSpec data = atomType.getDataNode(middle);
    for (final FreeAtomType child : iterable(syntax.getLeafTypes(data.type))) {
      if (child.back.size() > 1)
        throw new InvalidSyntax(
            String.format(
                "Type [%s] is a child of [%s] at middle [%s], but deserializes as an array segment.",
                child.id(), atomType.id(), middle));
    }
  }
}
