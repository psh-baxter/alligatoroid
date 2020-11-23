package com.zarbosoft.merman.syntax.back;

import com.zarbosoft.merman.document.values.ValuePrimitive;
import com.zarbosoft.merman.editor.backevents.ETypeEvent;
import com.zarbosoft.merman.syntax.AtomType;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.merman.syntax.middle.MiddlePrimitiveSpec;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.events.nodes.ClassEqTerminal;
import com.zarbosoft.pidgoon.events.stores.StackStore;
import com.zarbosoft.pidgoon.nodes.Operator;
import com.zarbosoft.pidgoon.nodes.Sequence;
import com.zarbosoft.rendaw.common.Pair;

import java.util.Set;

public class BackTypeSpec extends BackSpec {

  public String type;

  public BackSpec value;

  @Override
  public Node buildBackRule(final Syntax syntax, final AtomType atomType) {
    final MiddlePrimitiveSpec middle = atomType.getDataPrimitive(this.type);
    return new Sequence()
        .add(
            new Operator<StackStore>(new ClassEqTerminal(ETypeEvent.class)) {
              @Override
              protected StackStore process(StackStore store) {
                return store.stackSingleElement(
                    new Pair<>(
                        BackTypeSpec.this.type,
                        new ValuePrimitive(middle, ((ETypeEvent) store.top()).value)));
              }
            })
        .add(value.buildBackRule(syntax, atomType));
  }

  public void finish(final Syntax syntax, final AtomType atomType, final Set<String> middleUsed) {
    middleUsed.add(type);
    atomType.getDataPrimitive(type);
    value.finish(syntax, atomType, middleUsed);
    value.parent =
        new PartParent() {
          @Override
          public BackSpec part() {
            return BackTypeSpec.this;
          }

          @Override
          public String pathSection() {
            return null;
          }
        };
  }
}
