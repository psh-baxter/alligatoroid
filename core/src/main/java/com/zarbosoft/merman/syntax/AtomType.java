package com.zarbosoft.merman.syntax;

import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.Value;
import com.zarbosoft.merman.misc.TSMap;
import com.zarbosoft.merman.syntax.alignments.AlignmentDefinition;
import com.zarbosoft.merman.syntax.back.BackArraySpec;
import com.zarbosoft.merman.syntax.back.BackAtomSpec;
import com.zarbosoft.merman.syntax.back.BackFixedArraySpec;
import com.zarbosoft.merman.syntax.back.BackFixedRecordSpec;
import com.zarbosoft.merman.syntax.back.BackFixedTypeSpec;
import com.zarbosoft.merman.syntax.back.BackKeySpec;
import com.zarbosoft.merman.syntax.back.BackPrimitiveSpec;
import com.zarbosoft.merman.syntax.back.BackRecordSpec;
import com.zarbosoft.merman.syntax.back.BackRootArraySpec;
import com.zarbosoft.merman.syntax.back.BackSpec;
import com.zarbosoft.merman.syntax.back.BackSpecData;
import com.zarbosoft.merman.syntax.back.BackTypeSpec;
import com.zarbosoft.merman.syntax.back.BaseBackArraySpec;
import com.zarbosoft.merman.syntax.back.BaseBackAtomSpec;
import com.zarbosoft.merman.syntax.back.BaseBackPrimitiveSpec;
import com.zarbosoft.merman.syntax.front.FrontSpec;
import com.zarbosoft.pidgoon.events.stores.StackStore;
import com.zarbosoft.pidgoon.nodes.Operator;
import com.zarbosoft.pidgoon.nodes.Sequence;
import com.zarbosoft.rendaw.common.DeadCode;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.zarbosoft.rendaw.common.Common.enumerate;

public abstract class AtomType {

  public Set<String> tags = new HashSet<>();
  public Map<String, BackSpecData> fields;

  public abstract Map<String, AlignmentDefinition> alignments();

  public abstract int precedence();

  public abstract boolean associateForward();

  public abstract int depthScore();

  public void finish(
      final Syntax syntax, final Set<String> allTypes, final Set<String> scalarTypes) {
    fields = new HashMap<>();
    enumerate(back().stream())
        .forEach(
            pair -> {
              final Integer i = pair.first;
              final BackSpec p = pair.second;
              p.finish(syntax, this, fields);
              p.parent = new NodeBackParent(i);
            });
    {
      final Set<String> fieldsUsedFront = new HashSet<>();
      front().forEach(p -> p.finish(this, fieldsUsedFront));
      final Set<String> missing = Sets.difference(fields.keySet(), fieldsUsedFront);
      if (!missing.isEmpty())
        throw new InvalidSyntax(
            String.format("Middle elements %s in %s are unused by front parts.", missing, id()));
    }
  }

  public abstract List<FrontSpec> front();

  public abstract List<BackSpec> back();

  public abstract String id();

  public com.zarbosoft.pidgoon.Node buildBackRule(final Syntax syntax) {
    final Sequence seq = new Sequence();
    seq.add(StackStore.prepVarStack);
    back().forEach(p -> seq.add(p.buildBackRule(syntax, this)));
    return new Operator<StackStore>(seq) {
      @Override
      protected StackStore process(StackStore store) {
        final TSMap<String, Value> data = new TSMap<>();
        store = store.popVarMap(data.inner);
        final Atom atom = new Atom(AtomType.this, data);
        return store.pushStack(atom);
      }
    };
  }

  public abstract String name();

  public BackSpec getBackPart(final String id) {
    final Deque<Iterator<BackSpec>> stack = new ArrayDeque<>();
    stack.addLast(back().iterator());
    while (!stack.isEmpty()) {
      final Iterator<BackSpec> iterator = stack.pollLast();
      if (!iterator.hasNext()) continue;
      stack.addLast(iterator);
      final BackSpec next = iterator.next();
      if (next instanceof BackFixedArraySpec) {
        stack.addLast(((BackFixedArraySpec) next).elements.iterator());
      } else if (next instanceof BackFixedRecordSpec) {
        stack.addLast(((BackFixedRecordSpec) next).pairs.values().iterator());
      } else if (next instanceof BackArraySpec) {
        if (((BackArraySpec) next).id.equals(id)) return next;
      } else if (next instanceof BackRootArraySpec) {
        if (((BackRootArraySpec) next).id.equals(id)) return next;
      } else if (next instanceof BackKeySpec) {
        if (((BackKeySpec) next).id.equals(id)) return next;
      } else if (next instanceof BackAtomSpec) {
        if (((BackAtomSpec) next).id.equals(id)) return next;
      } else if (next instanceof BackFixedTypeSpec) {
        stack.addLast(Iterators.singletonIterator(((BackFixedTypeSpec) next).value));
      } else if (next instanceof BackTypeSpec) {
        if (((BackTypeSpec) next).type.equals(id)) return next;
      } else if (next instanceof BackPrimitiveSpec) {
        if (((BackPrimitiveSpec) next).id.equals(id)) return next;
      } else if (next instanceof BackRecordSpec) {
        if (((BackRecordSpec) next).id.equals(id)) return next;
      }
    }
    throw new DeadCode();
  }

  public BaseBackPrimitiveSpec getDataPrimitive(final String key) {
    return getBack(BaseBackPrimitiveSpec.class, key);
  }

  private <D extends BackSpecData> D getBack(
      final Class<? extends BackSpecData> type, final String id) {
    final BackSpecData found = fields.get(id);
    if (found == null)
      throw new InvalidSyntax(String.format("No middle element [%s] in [%s]", id, this.id()));
    if (!type.isAssignableFrom(found.getClass()))
      throw new InvalidSyntax(
          String.format(
              "Conflicting types for middle element [%s] in [%s]: %s, %s",
              id, this.id(), found.getClass(), type));
    return (D) found;
  }

  public BaseBackAtomSpec getDataNode(final String key) {
    return getBack(BaseBackAtomSpec.class, key);
  }

  public BaseBackArraySpec getDataArray(final String key) {
    return getBack(BaseBackArraySpec.class, key);
  }

  @Override
  public String toString() {
    return String.format("<type %s>", id());
  }

  public Atom create(final Syntax syntax) {
    final TSMap<String, Value> data = new TSMap<>();
    fields.entrySet().stream().forEach(e -> data.put(e.getKey(), e.getValue().create(syntax)));
    return new Atom(this, data);
  }

  public static class NodeBackParent extends BackSpec.Parent {
    public int index;

    public NodeBackParent(final int index) {
      this.index = index;
    }
  }
}
