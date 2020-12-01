package com.zarbosoft.merman.syntax.back;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.ValueAtom;
import com.zarbosoft.merman.editor.serialization.Write;
import com.zarbosoft.merman.syntax.AtomType;
import com.zarbosoft.merman.syntax.FreeAtomType;
import com.zarbosoft.merman.syntax.InvalidSyntax;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.events.stores.StackStore;
import com.zarbosoft.pidgoon.nodes.Operator;
import com.zarbosoft.rendaw.common.Pair;

import java.util.Deque;
import java.util.Map;

import static com.zarbosoft.rendaw.common.Common.iterable;

public class BackAtomSpec extends BaseBackAtomSpec {

  public Node buildBackRule(final Syntax syntax, final AtomType atomType) {
    return new Operator<StackStore>(syntax.backRuleRef(type)) {
      @Override
      protected StackStore process(StackStore store) {
        final Atom value = store.stackTop();
        store = store.popStack();
        return store.stackSingleElement(new Pair<>(id, new ValueAtom(BackAtomSpec.this, value)));
      }
    };
  }

  public void finish(
      final Syntax syntax, final AtomType atomType, final Map<String, BackSpecData> fields) {
    fields.put(id, this);
    for (final FreeAtomType child : iterable(syntax.getLeafTypes(type))) {
      if (child.back.size() > 1)
        throw new InvalidSyntax(
            String.format(
                "Type [%s] is a child of [%s] at back [%s], but deserializes as an array segment.",
                child.id(), atomType.id(), id));
    }
  }

  @Override
  public void write(Deque<Write.WriteState> stack, Atom base, Write.EventConsumer writer) {
    final Atom child = ((ValueAtom) base.fields.get(id)).data;
    stack.addLast(new Write.WriteStateBack(child, child.type.back().iterator()));
  }
}
