package com.zarbosoft.merman.core.syntax;

import com.zarbosoft.merman.core.AtomKey;
import com.zarbosoft.merman.core.Environment;
import com.zarbosoft.merman.core.MultiError;
import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.document.fields.Field;
import com.zarbosoft.merman.core.document.fields.FieldArray;
import com.zarbosoft.merman.core.document.fields.FieldAtom;
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
import com.zarbosoft.merman.core.syntax.error.AtomTypeErrors;
import com.zarbosoft.merman.core.syntax.error.AtomTypeNoBack;
import com.zarbosoft.merman.core.syntax.error.BackFieldWrongType;
import com.zarbosoft.merman.core.syntax.error.DuplicateBackId;
import com.zarbosoft.merman.core.syntax.error.MissingBack;
import com.zarbosoft.merman.core.syntax.error.NonexistentDefaultSelection;
import com.zarbosoft.merman.core.syntax.error.UnusedBackData;
import com.zarbosoft.merman.core.syntax.front.FrontArraySpec;
import com.zarbosoft.merman.core.syntax.front.FrontAtomSpec;
import com.zarbosoft.merman.core.syntax.front.FrontSpec;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.nodes.Color;
import com.zarbosoft.pidgoon.nodes.MergeSequence;
import com.zarbosoft.pidgoon.nodes.Operator;
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
  public final String id;
  public final AtomKey key;
  private final ROList<BackSpec> back;
  private final ROList<FrontSpec> front;
  public final String defaultSelection;

  public AtomType(Config config) {
    id = config.id;
    key = new AtomKey(this.id);
    back = config.back;
    front = config.front;
    defaultSelection = config.defaultSelection;
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
              if (s instanceof BaseBackArraySpec) return false;
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
    final Atom testAtom = test.field.atomParentRef.atom();

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
      if (front instanceof FrontAtomSpec) id = ((FrontAtomSpec) front).fieldId();
      else if (front instanceof FrontArraySpec) id = ((FrontArraySpec) front).fieldId();
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
      e.finish(subErrors, syntax, new SyntaxPath("back").add(Integer.toString(i)), false, false);
      e.parent = new NodeBackParent(i);
    }
    {
      final TSSet<String> fieldsUsedFront = new TSSet<>();
      for (int i = 0; i < front().size(); ++i) {
        FrontSpec e = front().get(i);
        e.finish(
            subErrors, new SyntaxPath("front").add(Integer.toString(i)), this, fieldsUsedFront);
      }
      final TSSet<String> missing = fields.keys().difference(fieldsUsedFront);
      if (!missing.isEmpty()) {
        subErrors.add(new UnusedBackData(missing.ro()));
      }
    }
    if (defaultSelection!=null && !fields.has(defaultSelection)) {
      subErrors.add(new NonexistentDefaultSelection(defaultSelection));
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

  public Node<AtomParseResult> buildBackRule(Environment env, final Syntax syntax) {
    final MergeSequence<FieldParseResult> seq = new MergeSequence<>();
    for (BackSpec p : back()) {
      seq.add(p.buildBackRule(env, syntax));
    }
    return new Color<AtomParseResult>(
        "atom " + id,
        new Operator<ROList<FieldParseResult>, AtomParseResult>(seq) {
          @Override
          protected AtomParseResult process(ROList<FieldParseResult> value) {
            return new AtomParseResult(new Atom(AtomType.this), value);
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
      MultiError errors, SyntaxPath typePath, final String key, String forName) {
    BackSpecData found = getBack(errors, typePath, key, forName);
    try {
      return (BaseBackPrimitiveSpec) found;
    } catch (ClassCastException e) {
      errors.add(new BackFieldWrongType(typePath, id, found, "primitive", forName));
      return null;
    }
  }

  public BackSpecData getBack(MultiError errors, SyntaxPath typePath, final String id, String forName) {
    final BackSpecData found = fields.getOpt(id);
    if (found == null) {
      errors.add(new MissingBack(typePath, id, forName));
      return null;
    }
    return found;
  }

  public BaseBackAtomSpec getDataAtom(MultiError errors, SyntaxPath typePath, final String key, String forName) {
    BackSpecData found = getBack(errors, typePath, key, forName);
    try {
      return (BaseBackAtomSpec) found;
    } catch (ClassCastException e) {
      errors.add(new BackFieldWrongType(typePath, id, found, "atom", forName));
      return null;
    }
  }

  public BaseBackArraySpec getDataArray(MultiError errors, SyntaxPath typePath, final String key, String forName) {
    BackSpecData found = getBack(errors, typePath, key, forName);
    try {
      return (BaseBackArraySpec) found;
    } catch (ClassCastException e) {
      errors.add(new BackFieldWrongType(typePath, id, found, "array", forName));
      return null;
    }
  }

  public final String id() {
    return id;
  }

  public abstract static class FieldParseResult {
    public final String key;

    protected FieldParseResult(String key) {
      this.key = key;
    }

    public abstract Field field();

    public abstract void finish();
  }

  public static class PrimitiveFieldParseResult extends FieldParseResult {
    final Field field;

    public PrimitiveFieldParseResult(String key, Field field) {
      super(key);
      this.field = field;
    }

    @Override
    public Field field() {
      return field;
    }

    @Override
    public void finish() {}
  }

  public static class AtomFieldParseResult extends FieldParseResult {
    public final AtomParseResult data;
    final FieldAtom field;

    public AtomFieldParseResult(String key, FieldAtom field, AtomParseResult data) {
      super(key);
      this.field = field;
      this.data = data;
    }

    @Override
    public Field field() {
      return field;
    }

    @Override
    public void finish() {
      field.initialSet(data.finish());
    }
  }

  public static class ArrayFieldParseResult extends FieldParseResult {
    final FieldArray field;
    final ROList<AtomParseResult> data;

    public ArrayFieldParseResult(String key, FieldArray field, ROList<AtomParseResult> data) {
      super(key);
      this.field = field;
      this.data = data;
    }

    @Override
    public Field field() {
      return field;
    }

    @Override
    public void finish() {
      TSList<Atom> fieldData = new TSList<>();
      for (AtomParseResult element : data) {
        fieldData.add(element.finish());
      }
      field.initialSet(fieldData);
    }
  }

  public static class AtomParseResult {
    public final Atom atom;
    public final ROList<FieldParseResult> fields;

    /**
     * @param atom
     * @param fields FieldParseResult or null if no field parsed for a back element
     */
    public AtomParseResult(Atom atom, ROList<FieldParseResult> fields) {
      this.atom = atom;
      this.fields = fields;
    }

    public Atom finish() {
      TSMap<String, Field> initialFields = new TSMap<>();
      for (FieldParseResult field : fields) {
        if (field == null) continue;
        field.finish();
        initialFields.put(field.key, field.field());
      }
      atom.initialSet(initialFields);
      return atom;
    }
  }

  public static final class Config {
    public final String id;
    public final ROList<BackSpec> back;
    public final ROList<FrontSpec> front;
    /**
     * If this has multiple selectable front elements, this is the default selection when selecting in.  If not specified, defaults to first one.
     */
    public String defaultSelection;

    public Config(String id, ROList<BackSpec> back, ROList<FrontSpec> front) {
      this.id = id;
      this.back = back;
      this.front = front;
    }

    public Config defaultSelection(String id) {
      this.defaultSelection = id;
      return this;
    }
  }

  public static class NodeBackParent extends BackSpec.Parent {
    public int index;

    public NodeBackParent(final int index) {
      this.index = index;
    }
  }
}
