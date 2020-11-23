package com.zarbosoft.merman.syntax;

import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.Value;
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
import com.zarbosoft.merman.syntax.back.BackTypeSpec;
import com.zarbosoft.merman.syntax.front.FrontSpec;
import com.zarbosoft.merman.syntax.middle.MiddleArraySpecBase;
import com.zarbosoft.merman.syntax.middle.MiddleAtomSpec;
import com.zarbosoft.merman.syntax.middle.MiddlePrimitiveSpec;
import com.zarbosoft.merman.syntax.middle.MiddleRecordSpec;
import com.zarbosoft.merman.syntax.middle.MiddleSpec;
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

  public abstract Map<String, AlignmentDefinition> alignments();

  public abstract int precedence();

  public abstract boolean associateForward();

  public abstract int depthScore();

  public void finish(
      final Syntax syntax, final Set<String> allTypes, final Set<String> scalarTypes) {
    middle()
        .forEach(
            (k, v) -> {
              v.id = k;
              v.finish(allTypes, scalarTypes);
            });
    {
      final Set<String> middleUsedBack = new HashSet<>();
      enumerate(back().stream())
          .forEach(
              pair -> {
                final Integer i = pair.first;
                final BackSpec p = pair.second;
                p.finish(syntax, this, middleUsedBack);
                p.parent = new NodeBackParent(i);
              });
      final Set<String> missing = Sets.difference(middle().keySet(), middleUsedBack);
      if (!missing.isEmpty())
        throw new InvalidSyntax(
            String.format("Middle elements %s in %s are unused by back parts.", missing, id()));
    }
    {
      final Set<String> middleUsedFront = new HashSet<>();
      front().forEach(p -> p.finish(this, middleUsedFront));
      final Set<String> missing = Sets.difference(middle().keySet(), middleUsedFront);
      if (!missing.isEmpty())
        throw new InvalidSyntax(
            String.format("Middle elements %s in %s are unused by front parts.", missing, id()));
    }
  }

  public abstract List<FrontSpec> front();

  public abstract Map<String, MiddleSpec> middle();

  public abstract List<BackSpec> back();

  public abstract String id();

  public com.zarbosoft.pidgoon.Node buildBackRule(final Syntax syntax) {
    final Sequence seq = new Sequence();
    seq.add(StackStore.prepVarStack);
    back().forEach(p -> seq.add(p.buildBackRule(syntax, this)));
    return new Operator<StackStore>(seq) {
      @Override
      protected StackStore process(StackStore store) {
        final Map<String, Value> data = new HashMap<>();
        store = store.popVarMap(data);
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
        if (((BackArraySpec) next).middle.equals(id)) return next;
      } else if (next instanceof BackRootArraySpec) {
        if (((BackRootArraySpec) next).middle.equals(id)) return next;
      } else if (next instanceof BackKeySpec) {
        if (((BackKeySpec) next).middle.equals(id)) return next;
      } else if (next instanceof BackAtomSpec) {
        if (((BackAtomSpec) next).middle.equals(id)) return next;
      } else if (next instanceof BackFixedTypeSpec) {
        stack.addLast(Iterators.singletonIterator(((BackFixedTypeSpec) next).value));
      } else if (next instanceof BackTypeSpec) {
        if (((BackTypeSpec) next).type.equals(id)) return next;
      } else if (next instanceof BackPrimitiveSpec) {
        if (((BackPrimitiveSpec) next).middle.equals(id)) return next;
      } else if (next instanceof BackRecordSpec) {
        if (((BackRecordSpec) next).middle.equals(id)) return next;
      }
    }
    throw new DeadCode();
  }

  public MiddleRecordSpec getDataRecord(final String middle) {
    return getData(MiddleRecordSpec.class, middle);
  }

  private <D extends MiddleSpec> D getData(
      final Class<? extends MiddleSpec> type, final String id) {
    final MiddleSpec found = middle().get(id);
    if (found == null)
      throw new InvalidSyntax(String.format("No middle element [%s] in [%s]", id, this.id()));
    if (!type.isAssignableFrom(found.getClass()))
      throw new InvalidSyntax(
          String.format(
              "Conflicting types for middle element [%s] in [%s]: %s, %s",
              id, this.id(), found.getClass(), type));
    return (D) found;
  }

  public MiddlePrimitiveSpec getDataPrimitive(final String key) {
    return getData(MiddlePrimitiveSpec.class, key);
  }

  public MiddleAtomSpec getDataNode(final String key) {
    return getData(MiddleAtomSpec.class, key);
  }

  public MiddleArraySpecBase getDataArray(final String key) {
    return getData(MiddleArraySpecBase.class, key);
  }

  @Override
  public String toString() {
    return String.format("<type %s>", id());
  }

  public static class NodeBackParent extends BackSpec.Parent {
    public int index;

    public NodeBackParent(final int index) {
      this.index = index;
    }
  }
}
