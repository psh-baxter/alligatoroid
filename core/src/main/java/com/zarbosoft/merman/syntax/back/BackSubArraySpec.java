package com.zarbosoft.merman.syntax.back;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.ValueArray;
import com.zarbosoft.merman.document.values.ValueAtom;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.editor.serialization.EventConsumer;
import com.zarbosoft.merman.editor.serialization.WriteState;
import com.zarbosoft.merman.editor.serialization.WriteStateDeepDataArray;
import com.zarbosoft.merman.misc.MultiError;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.merman.syntax.error.PluralInvalidAtLocation;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.events.stores.StackStore;
import com.zarbosoft.pidgoon.nodes.Operator;
import com.zarbosoft.pidgoon.nodes.Repeat;
import com.zarbosoft.pidgoon.nodes.Sequence;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;

import java.util.Deque;
import java.util.Iterator;
import java.util.List;

public class BackSubArraySpec extends BaseBackSimpleArraySpec {

  public BackSubArraySpec(Config config) {
    super(config);
  }

  @Override
  public void finish(
            MultiError errors,
            Syntax syntax,
            Path typePath,
            boolean singularRestriction,
            boolean typeRestriction) {
    super.finish(errors, syntax, typePath, singularRestriction, typeRestriction);
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
        final TSList<Atom> temp = new TSList<>();
        store = store.<String, ValueAtom>popVarDouble((_k, v) -> temp.add(v.data));
        temp.reverse();
        final ValueArray value = new ValueArray(BackSubArraySpec.this, temp);
        return store.stackVarDoubleElement(id, value);
      }
    };
  }

  @Override
  public void write(
          Deque<WriteState> stack, TSMap<String, Object> data, EventConsumer writer) {
    stack.addLast(
        new WriteStateDeepDataArray(element, elementAtom.id, ((List<Atom>) data.get(id))));
  }

  @Override
  protected boolean isSingularValue() {
    return false;
  }
}
