package com.zarbosoft.merman.syntax.back;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.ValuePrimitive;
import com.zarbosoft.merman.editor.backevents.ETypeEvent;
import com.zarbosoft.merman.editor.serialization.Write;
import com.zarbosoft.merman.syntax.AtomType;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.events.nodes.ClassEqTerminal;
import com.zarbosoft.pidgoon.events.stores.StackStore;
import com.zarbosoft.pidgoon.nodes.Operator;
import com.zarbosoft.pidgoon.nodes.Sequence;
import com.zarbosoft.rendaw.common.Pair;

import java.util.Deque;
import java.util.Map;

public class BackTypeSpec extends BaseBackPrimitiveSpec {
  public String type;

  public BackSpec value;

  @Override
  public Node buildBackRule(final Syntax syntax, final AtomType atomType) {
    return new Sequence()
        .add(
            new Operator<StackStore>(new ClassEqTerminal(ETypeEvent.class)) {
              @Override
              protected StackStore process(StackStore store) {
                return store.stackSingleElement(
                    new Pair<>(
                        id,
                        new ValuePrimitive(BackTypeSpec.this, ((ETypeEvent) store.top()).value)));
              }
            })
        .add(value.buildBackRule(syntax, atomType));
  }

  public void finish(
      final Syntax syntax, final AtomType atomType, final Map<String, BackSpecData> fields) {
    fields.put(id, this);
    value.finish(syntax, atomType, fields);
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

  @Override
  public void write(Deque<Write.WriteState> stack, Atom base, Write.EventConsumer writer) {
    writer.type(((ValuePrimitive) base.fields.get(type)).get());
    stack.addLast(new Write.WriteStateBack(base, ImmutableList.of(value).iterator()));
  }
}
