package com.zarbosoft.merman.core.syntax;

import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.document.values.Field;
import com.zarbosoft.merman.core.editor.Path;
import com.zarbosoft.merman.core.misc.MultiError;
import com.zarbosoft.merman.core.syntax.alignments.AlignmentSpec;
import com.zarbosoft.merman.core.syntax.back.BackArraySpec;
import com.zarbosoft.merman.core.syntax.back.BackAtomSpec;
import com.zarbosoft.merman.core.syntax.back.BackFixedArraySpec;
import com.zarbosoft.merman.core.syntax.back.BackFixedRecordSpec;
import com.zarbosoft.merman.core.syntax.back.BackFixedTypeSpec;
import com.zarbosoft.merman.core.syntax.back.BackKeySpec;
import com.zarbosoft.merman.core.syntax.back.BackPrimitiveSpec;
import com.zarbosoft.merman.core.syntax.back.BackRecordSpec;
import com.zarbosoft.merman.core.syntax.back.BackSpec;
import com.zarbosoft.merman.core.syntax.back.BackSpecData;
import com.zarbosoft.merman.core.syntax.back.BackSubArraySpec;
import com.zarbosoft.merman.core.syntax.back.BackTypeSpec;
import com.zarbosoft.merman.core.syntax.back.BaseBackArraySpec;
import com.zarbosoft.merman.core.syntax.back.BaseBackAtomSpec;
import com.zarbosoft.merman.core.syntax.back.BaseBackPrimitiveSpec;
import com.zarbosoft.merman.core.syntax.back.BaseBackSimpleArraySpec;
import com.zarbosoft.merman.core.syntax.error.AtomTypeErrors;
import com.zarbosoft.merman.core.syntax.error.AtomTypeNoBack;
import com.zarbosoft.merman.core.syntax.error.BackFieldWrongType;
import com.zarbosoft.merman.core.syntax.error.DuplicateBackId;
import com.zarbosoft.merman.core.syntax.error.MissingBack;
import com.zarbosoft.merman.core.syntax.error.UnusedBackData;
import com.zarbosoft.merman.core.syntax.front.FrontArraySpec;
import com.zarbosoft.merman.core.syntax.front.FrontAtomSpec;
import com.zarbosoft.merman.core.syntax.front.FrontSpec;
import com.zarbosoft.pidgoon.events.StackStore;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.nodes.Color;
import com.zarbosoft.pidgoon.nodes.Operator;
import com.zarbosoft.pidgoon.nodes.Sequence;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.DeadCode;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROMap;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;
import com.zarbosoft.rendaw.common.TSSet;

import java.util.Arrays;
import java.util.Iterator;

public abstract class AtomType {
  public final ROMap<String, BackSpecData> fields;
  private final String id;
  private final ROList<BackSpec> back;
  private final ROList<FrontSpec> front;

  public AtomType(Config config) {
    id = config.id;
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
              BackSpecData old = fields.putReplace(s1.id, s1);
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
          final FreeAtomType type, final Field.Parent test, final boolean allowed) {
    final Atom testAtom = test.value.atomParentRef.atom();

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

  private static int getIndexOfData(final Field.Parent parent, final Atom atom) {
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
      final TSSet<String> fieldsUsedFront = new TSSet<>();
      for (int i = 0; i < front().size(); ++i) {
        FrontSpec e = front().get(i);
        e.finish(subErrors, new Path("front").add(Integer.toString(i)), this, fieldsUsedFront);
      }
      final TSSet<String> missing = fields.keys().difference(fieldsUsedFront);
      if (!missing.isEmpty()) {
        subErrors.add(new UnusedBackData(missing.ro()));
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

  public Node buildBackRule(final Syntax syntax) {
    final Sequence seq = new Sequence();
    seq.add(StackStore.prepVarStack);
    for (BackSpec p : back()) {
      seq.add(p.buildBackRule(syntax));
    }
    return new Color(
        "atom " + id,
        new Operator<StackStore>(seq) {
          @Override
          protected StackStore process(StackStore store) {
            final TSMap<String, Field> data = new TSMap<>();
            store = store.popVarMap(data.inner);
            final Atom atom = new Atom(AtomType.this, data);
            return store.pushStack(atom);
          }
        });
  }

  public abstract String name();

  public BackSpec getBackPart(final String id) {
    final TSList<Iterator<BackSpec>> stack = new TSList<>();
    stack.add(back().iterator());
    while (!stack.isEmpty()) {
      final Iterator<BackSpec> iterator = stack.last();
      if (!iterator.hasNext()) {
        stack.removeLast();
        continue;
      }
      final BackSpec next = iterator.next();
      if (next instanceof BackFixedArraySpec) {
        stack.add(((BackFixedArraySpec) next).elements.iterator());
      } else if (next instanceof BackFixedRecordSpec) {
        stack.add(((BackFixedRecordSpec) next).pairs.iterValues());
      } else if (next instanceof BackArraySpec) {
        if (((BackArraySpec) next).id.equals(id)) return next;
      } else if (next instanceof BackSubArraySpec) {
        if (((BackSubArraySpec) next).id.equals(id)) return next;
      } else if (next instanceof BackKeySpec) {
        if (((BackKeySpec) next).id.equals(id)) return next;
      } else if (next instanceof BackAtomSpec) {
        if (((BackAtomSpec) next).id.equals(id)) return next;
      } else if (next instanceof BackFixedTypeSpec) {
        stack.add(Arrays.asList(((BackFixedTypeSpec) next).value).iterator());
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
    BackSpecData found = getBack(errors, typePath, key);
    try {
      return (BaseBackPrimitiveSpec) found;
    } catch (ClassCastException e) {
      errors.add(new BackFieldWrongType(typePath, id, found, "primitive"));
      return null;
    }
  }

  private BackSpecData getBack(MultiError errors, Path typePath, final String id) {
    final BackSpecData found = fields.getOpt(id);
    if (found == null) {
      errors.add(new MissingBack(typePath, id));
      return null;
    }
    return found;
  }

  public BaseBackAtomSpec getDataAtom(MultiError errors, Path typePath, final String key) {
    BackSpecData found = getBack(errors, typePath, key);
    try {
      return (BaseBackAtomSpec) found;
    } catch (ClassCastException e) {
      errors.add(new BackFieldWrongType(typePath, id, found, "atom"));
      return null;
    }
  }

  public BaseBackArraySpec getDataArray(MultiError errors, Path typePath, final String key) {
    BackSpecData found = getBack(errors, typePath, key);
    try {
      return (BaseBackArraySpec) found;
    } catch (ClassCastException e) {
      errors.add(new BackFieldWrongType(typePath, id, found, "array"));
      return null;
    }
  }

  public final String id() {
    return id;
  }

  public static final class Config {
    public final String id;
    public final ROList<BackSpec> back;
    public final ROList<FrontSpec> front;

    public Config(String id, ROList<BackSpec> back, ROList<FrontSpec> front) {
      this.id = id;
      this.back = back;
      this.front = front;
    }
  }

  public static class NodeBackParent extends BackSpec.Parent {
    public int index;

    public NodeBackParent(final int index) {
      this.index = index;
    }
  }
}
