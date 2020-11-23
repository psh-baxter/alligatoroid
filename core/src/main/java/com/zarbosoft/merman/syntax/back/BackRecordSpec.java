package com.zarbosoft.merman.syntax.back;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.ValueArray;
import com.zarbosoft.merman.editor.backevents.EObjectCloseEvent;
import com.zarbosoft.merman.editor.backevents.EObjectOpenEvent;
import com.zarbosoft.merman.syntax.AtomType;
import com.zarbosoft.merman.syntax.FreeAtomType;
import com.zarbosoft.merman.syntax.InvalidSyntax;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.merman.syntax.middle.MiddleRecordSpec;
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

import static com.zarbosoft.rendaw.common.Common.iterable;
import static com.zarbosoft.rendaw.common.Common.last;

public class BackRecordSpec extends BackSpec {

  public String middle;

  @Override
  public Node buildBackRule(final Syntax syntax, final AtomType atomType) {
    final Sequence sequence;
    sequence = new Sequence();
    sequence.add(new MatchingEventTerminal(new EObjectOpenEvent()));
    sequence.add(StackStore.prepVarStack);
    sequence.add(
        new Repeat(
            new Sequence()
                .add(syntax.backRuleRef(atomType.getDataArray(middle).type))
                .add(StackStore.pushVarStackSingle)));
    sequence.add(new MatchingEventTerminal(new EObjectCloseEvent()));
    return new Operator<StackStore>(sequence) {
      @Override
      protected StackStore process(StackStore store) {
        final List<Atom> temp = new ArrayList<>();
        store = store.popVarSingleList(temp);
        Collections.reverse(temp);
        final ValueArray value = new ValueArray(atomType.getDataArray(middle), temp);
        return store.stackSingleElement(new Pair<>(middle, value));
      }
    };
  }

  @Override
  public void finish(final Syntax syntax, final AtomType atomType, final Set<String> middleUsed) {
    middleUsed.add(middle);
    final MiddleRecordSpec dataType = atomType.getDataRecord(middle);
    for (final FreeAtomType element : iterable(syntax.getLeafTypes(dataType.type))) {
      if (element.back.size() < 1) finishThrow(element);
      if (!(element.back.get(0) instanceof BackKeySpec)) finishThrow(element);
      if (element.back.size() >= 2 && element.back.size() <= 3) {
        final BackSpec lastBack = last(element.back);
        if (ImmutableList.of(BackKeySpec.class, BackFixedTypeSpec.class).stream()
            .anyMatch(klass -> klass.equals(lastBack.getClass()))) finishThrow(element);
        if (element.back.size() == 3 && !(element.back.get(1) instanceof BackFixedTypeSpec))
          finishThrow(element);
      } else finishThrow(element);
    }
  }

  private static void finishThrow(final AtomType type) {
    throw new InvalidSyntax(
        String.format(
            "As the element type of a back record, [%s] must have exactly a back key, followed by a back type "
                + "then value or just a back value.",
            type.id()));
  }
}
