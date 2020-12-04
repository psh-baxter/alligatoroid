package com.zarbosoft.merman.syntax.back;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.ValueArray;
import com.zarbosoft.merman.document.values.ValueAtom;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.editor.serialization.Write;
import com.zarbosoft.merman.misc.TSMap;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.merman.syntax.error.PluralInvalidAtLocation;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.events.stores.StackStore;
import com.zarbosoft.pidgoon.nodes.Operator;
import com.zarbosoft.pidgoon.nodes.Repeat;
import com.zarbosoft.pidgoon.nodes.Sequence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

public class BackSubArraySpec extends BaseBackSimpleArraySpec {
  @Override
  public void finish(
      List<Object> errors,
      Syntax syntax,
      Path typePath,
      TSMap<String, BackSpecData> fields,
      boolean singularRestriction,
      boolean typeRestriction) {
    super.finish(errors, syntax, typePath, fields, singularRestriction, typeRestriction);
    if (singularRestriction) {
      errors.add(new PluralInvalidAtLocation(typePath));
    }
  }

  @Override
  protected Iterator<BackSpec> walkStep() {
    return element.walkStep();
  }

  @Override
  public Node buildBackRule(final Syntax syntax) {
    final Sequence sequence;
    sequence = new Sequence();
    sequence.add(StackStore.prepVarStack);
    sequence.add(new Repeat(element.buildBackRule(syntax)));
    return new Operator<StackStore>(sequence) {
      @Override
      protected StackStore process(StackStore store) {
        final List<Atom> temp = new ArrayList<>();
        store = store.<String, ValueAtom>popVarDouble((_k, v) -> temp.add(v.data));
        Collections.reverse(temp);
        final ValueArray value = new ValueArray(BackSubArraySpec.this, temp);
        return store.stackVarDoubleElement(id, value);
      }
    };
  }

  @Override
  public void write(
      Deque<Write.WriteState> stack, TSMap<String, Object> data, Write.EventConsumer writer) {
    stack.addLast(
        new Write.WriteStateDeepDataArray(element, elementAtom.id, ((List<Atom>) data.get(id))));
  }

  @Override
  protected boolean isSingularValue() {
    return false;
  }
}
