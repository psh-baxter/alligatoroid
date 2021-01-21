package com.zarbosoft.merman.syntax;

import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.Value;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.misc.MultiError;
import com.zarbosoft.merman.misc.ROList;
import com.zarbosoft.merman.misc.ROMap;
import com.zarbosoft.merman.misc.ROSet;
import com.zarbosoft.merman.misc.TSMap;
import com.zarbosoft.merman.syntax.alignments.AlignmentSpec;
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
import com.zarbosoft.merman.syntax.back.BaseBackSimpleArraySpec;
import com.zarbosoft.merman.syntax.error.AtomTypeErrors;
import com.zarbosoft.merman.syntax.error.AtomTypeNoBack;
import com.zarbosoft.merman.syntax.error.BackFieldWrongType;
import com.zarbosoft.merman.syntax.error.DuplicateBackId;
import com.zarbosoft.merman.syntax.error.MissingBack;
import com.zarbosoft.merman.syntax.error.UnusedBackData;
import com.zarbosoft.merman.syntax.front.FrontArraySpec;
import com.zarbosoft.merman.syntax.front.FrontAtomSpec;
import com.zarbosoft.merman.syntax.front.FrontSpec;
import com.zarbosoft.pidgoon.events.stores.StackStore;
import com.zarbosoft.pidgoon.nodes.Color;
import com.zarbosoft.pidgoon.nodes.Operator;
import com.zarbosoft.pidgoon.nodes.Sequence;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.DeadCode;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public abstract class AtomType {
  public final ROMap<String, BackSpecData> fields;
  public final ROSet<String> tags;
  private final String id;
  private final ROList<BackSpec> back;
  private final ROList<FrontSpec> front;

  public static final class Config {
    public final String id;
    public ROSet<String> tags;
    public final ROList<BackSpec> back;
    public final ROList<FrontSpec> front;

    public Config(String id, ROList<BackSpec> back, ROList<FrontSpec> front) {
      this.id = id;
      this.back = back;
      this.front = front;
    }

    public Config(String id, ROSet<String> tags, ROList<BackSpec> back, ROList<FrontSpec> front) {
      this.id = id;
      this.tags = tags;
      this.back = back;
      this.front = front;
    }
  }

  public AtomType(Config config) {
    id = config.id;
    this.tags = config.tags;
    back = config.back;
    front = config.front;
    TSMap<String, BackSpecData> fields = new TSMap<>();
    MultiError errors = new MultiError();
    if (back.isEmpty()) {
      errors.add(new AtomTypeNoBack());
    } else
      for (BackSpec backSpec : back) {
        BackSpec.walk(
            backSpec,
            s -> {
              if (!(s instanceof BackSpecData)) return true;
              BackSpecData s1 = (BackSpecData) s;
              BackSpecData old = fields.put(s1.id, s1);
              if (old != null) errors.add(new DuplicateBackId(s1.id));
              if (s instanceof BaseBackSimpleArraySpec) return false;
              return true;
            });
      }
    this.fields = fields;
    errors.raise();
  }

  /**
   * @param type
   * @param test
   * @param allowed type is allowed to be placed here. Only for sliding suffix gaps.
   * @return
   */
  public static boolean isPrecedent(
      final FreeAtomType type, final Value.Parent test, final boolean allowed) {
    final Atom testAtom = test.child.parent.atom();

    // Can't move up if current level is bounded by any other front parts
    final int index = getIndexOfData(test, testAtom);
    final ROList<FrontSpec> front = testAtom.type.front();
    if (index != front.size() - 1) return false;
    final FrontSpec frontNext = front.get(index);
    if (frontNext instanceof FrontArraySpec && !((FrontArraySpec) frontNext).suffix.isEmpty())
      return false;

    if (allowed) {
      // Can't move up if next level has lower precedence
      if (testAtom.type.precedence() < type.precedence) return false;

      // Can't move up if next level has same precedence and parent is forward-associative
      if (testAtom.type.precedence() == type.precedence && testAtom.type.associateForward())
        return false;
    }

    return true;
  }

  private static int getIndexOfData(final Value.Parent parent, final Atom atom) {
    for (int i = 0; i < atom.type.front.size(); ++i) {
      FrontSpec front = atom.type.front.get(i);
      String id = null;
      if (front instanceof FrontAtomSpec) id = ((FrontAtomSpec) front).field();
      else if (front instanceof FrontArraySpec) id = ((FrontArraySpec) front).field();
      if (parent.id().equals(id)) return i;
    }
    throw new Assertion();
  }

  public abstract ROMap<String, AlignmentSpec> alignments();

  public abstract int precedence();

  public abstract boolean associateForward();

  public abstract int depthScore();

  public void finish(MultiError errors, final Syntax syntax) {
    MultiError subErrors = new MultiError();
    if (back().isEmpty()) {
      subErrors.add(new AtomTypeNoBack());
    }
    for (int i = 0; i < back().size(); ++i) {
      BackSpec e = back().get(i);
      e.finish(subErrors, syntax, new Path("back").add(Integer.toString(i)), false, false);
      e.parent = new NodeBackParent(i);
    }
    {
      final Set<String> fieldsUsedFront = new HashSet<>();
      for (int i = 0; i < front().size(); ++i) {
        FrontSpec e = front().get(i);
        e.finish(subErrors, new Path("front").add(Integer.toString(i)), this, fieldsUsedFront);
      }
      final Set<String> missing = Sets.difference(fields.keys(), fieldsUsedFront);
      if (!missing.isEmpty()) {
        subErrors.add(new UnusedBackData(missing));
      }
    }
    if (!subErrors.isEmpty()) {
      errors.add(new AtomTypeErrors(this, subErrors));
    }
  }

  public final ROList<FrontSpec> front() {
    return front;
  }

  public final ROList<BackSpec> back() {
    return back;
  }

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
        stack.addLast(((BackFixedRecordSpec) next).pairs.iterValues());
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
      MultiError errors, Path typePath, final String key) {
    return getBack(errors, typePath, BaseBackPrimitiveSpec.class, key);
  }

  public <D extends BackSpecData> D getBack(
      MultiError errors, Path typePath, final Class<D> type, final String id) {
    final BackSpecData found = fields.getOpt(id);
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

  public BaseBackAtomSpec getDataAtom(MultiError errors, Path typePath, final String key) {
    return getBack(errors, typePath, BaseBackAtomSpec.class, key);
  }

  public BaseBackArraySpec getDataArray(MultiError errors, Path typePath, final String key) {
    return getBack(errors, typePath, BaseBackArraySpec.class, key);
  }

  @Override
  public String toString() {
    return String.format("<type %s>", id());
  }

  public final String id() {
    return id;
  }

  public static class NodeBackParent extends BackSpec.Parent {
    public int index;

    public NodeBackParent(final int index) {
      this.index = index;
    }
  }
}
