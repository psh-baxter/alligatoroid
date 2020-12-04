package com.zarbosoft.merman.syntax;

import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.Value;
import com.zarbosoft.merman.editor.Path;
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
import com.zarbosoft.merman.syntax.back.BackSpec;
import com.zarbosoft.merman.syntax.back.BackSpecData;
import com.zarbosoft.merman.syntax.back.BackSubArraySpec;
import com.zarbosoft.merman.syntax.back.BackTypeSpec;
import com.zarbosoft.merman.syntax.back.BaseBackArraySpec;
import com.zarbosoft.merman.syntax.back.BaseBackAtomSpec;
import com.zarbosoft.merman.syntax.back.BaseBackPrimitiveSpec;
import com.zarbosoft.merman.syntax.error.AtomTypeErrors;
import com.zarbosoft.merman.syntax.error.AtomTypeNoBack;
import com.zarbosoft.merman.syntax.error.BackFieldWrongType;
import com.zarbosoft.merman.syntax.error.MissingBack;
import com.zarbosoft.merman.syntax.error.UnusedBackData;
import com.zarbosoft.merman.syntax.front.FrontSpec;
import com.zarbosoft.pidgoon.events.stores.StackStore;
import com.zarbosoft.pidgoon.nodes.Color;
import com.zarbosoft.pidgoon.nodes.Operator;
import com.zarbosoft.pidgoon.nodes.Sequence;
import com.zarbosoft.rendaw.common.DeadCode;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AtomType {

  public Set<String> tags = new HashSet<>();
  public TSMap<String, BackSpecData> fields;

  public abstract Map<String, AlignmentDefinition> alignments();

  public abstract int precedence();

  public abstract boolean associateForward();

  public abstract int depthScore();

  public void finish(List<Object> errors, final Syntax syntax) {
    fields = new TSMap<>();
    List<Object> subErrors = new ArrayList<>();
    if (back().isEmpty()) {
      subErrors.add(new AtomTypeNoBack());
    }
    for (int i = 0; i < back().size(); ++i) {
      BackSpec e = back().get(i);
      e.finish(subErrors, syntax, new Path("back").add(Integer.toString(i)), fields, false, false);
      e.parent = new NodeBackParent(i);
    }
    {
      final Set<String> fieldsUsedFront = new HashSet<>();
      for (int i = 0; i < front().size(); ++i) {
        FrontSpec e = front().get(i);
        e.finish(subErrors, new Path("front").add(Integer.toString(i)), this, fieldsUsedFront);
      }
      final Set<String> missing = Sets.difference(fields.keySet(), fieldsUsedFront);
      if (!missing.isEmpty()) {
        subErrors.add(new UnusedBackData(missing));
      }
    }
    if (!subErrors.isEmpty()) {
      errors.add(new AtomTypeErrors(this, subErrors));
    }
  }

  public abstract List<FrontSpec> front();

  public abstract List<BackSpec> back();

  public com.zarbosoft.pidgoon.Node buildBackRule(final Syntax syntax) {
    final Sequence seq = new Sequence();
    seq.add(StackStore.prepVarStack);
    back().forEach(p -> seq.add(p.buildBackRule(syntax)));
    return new Color(
        this,
        new Operator<StackStore>(seq) {
          @Override
          protected StackStore process(StackStore store) {
            final TSMap<String, Value> data = new TSMap<>();
            store = store.popVarMap(data.inner);
            final Atom atom = new Atom(AtomType.this, data);
            return store.pushStack(atom);
          }
        });
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
      } else if (next instanceof BackSubArraySpec) {
        if (((BackSubArraySpec) next).id.equals(id)) return next;
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

  public BaseBackPrimitiveSpec getDataPrimitive(
      List<Object> errors, Path typePath, final String key) {
    return getBack(errors, typePath, BaseBackPrimitiveSpec.class, key);
  }

  public <D extends BackSpecData> D getBack(
      List<Object> errors, Path typePath, final Class<D> type, final String id) {
    final BackSpecData found = fields.get(id);
    if (found == null) {
      errors.add(new MissingBack(typePath, id));
      return null;
    }
    if (!type.isAssignableFrom(found.getClass())) {
      errors.add(new BackFieldWrongType(typePath, id, found, type));
      return null;
    }
    return (D) found;
  }

  public BaseBackAtomSpec getDataAtom(List<Object> errors, Path typePath, final String key) {
    return getBack(errors, typePath, BaseBackAtomSpec.class, key);
  }

  public BaseBackArraySpec getDataArray(List<Object> errors, Path typePath, final String key) {
    return getBack(errors, typePath, BaseBackArraySpec.class, key);
  }

  @Override
  public String toString() {
    return String.format("<type %s>", id());
  }

  public abstract String id();

  public Atom create(final Syntax syntax) {
    final TSMap<String, Value> data = new TSMap<>();
    for (Map.Entry<String, BackSpecData> e : fields.entries()) {
      data.putNew(e.getKey(), e.getValue().create(syntax));
    }
    return new Atom(this, data);
  }

  public static class NodeBackParent extends BackSpec.Parent {
    public int index;

    public NodeBackParent(final int index) {
      this.index = index;
    }
  }
}
