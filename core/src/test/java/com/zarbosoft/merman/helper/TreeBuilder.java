package com.zarbosoft.merman.helper;

import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.document.values.Field;
import com.zarbosoft.merman.core.document.values.FieldArray;
import com.zarbosoft.merman.core.document.values.FieldAtom;
import com.zarbosoft.merman.core.document.values.FieldPrimitive;
import com.zarbosoft.merman.core.syntax.AtomType;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.core.syntax.back.BaseBackArraySpec;
import com.zarbosoft.merman.core.syntax.back.BaseBackAtomSpec;
import com.zarbosoft.merman.core.syntax.back.BaseBackPrimitiveSpec;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.ROSet;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;

public class TreeBuilder {
  private final AtomType type;
  private final TSMap<String, Field> data = new TSMap<>();

  public TreeBuilder(final AtomType type) {
    this.type = type;
  }

  public TreeBuilder(Syntax syntax, String type) {
    ROSet<AtomType> t = syntax.splayedTypes.get(type);
    if (t.size() != 1) throw new Assertion();
    this.type = t.iterator().next();
  }

  public TreeBuilder add(final String key, final TreeBuilder builder) {
    FieldAtom v = new FieldAtom((BaseBackAtomSpec) type.fields.get(key));
    v.initialSet(builder.build());
    data.putNew(key, v);
    return this;
  }

  public Atom build() {
    Atom atom = new Atom(type);
    atom.initialSet(data);
    return atom;
  }

  public TreeBuilder add(final String key, final Atom atom) {
    FieldAtom v = new FieldAtom((BaseBackAtomSpec) type.fields.get(key));
    v.initialSet(atom);
    data.putNew(key, v);
    return this;
  }

  public TreeBuilder add(final String key, final String text) {
    data.putNew(key, new FieldPrimitive((BaseBackPrimitiveSpec) type.fields.get(key), text));
    return this;
  }

  public TreeBuilder addArray(final String key, final Atom... values) {
    FieldArray v = new FieldArray((BaseBackArraySpec) type.fields.get(key));
    v.initialSet(TSList.of(values));
    data.putNew(key, v);
    return this;
  }

  public TreeBuilder addRecord(final String key, final Atom... values) {
    FieldArray v = new FieldArray((BaseBackArraySpec) type.fields.get(key));
    v.initialSet(TSList.of(values));
    data.putNew(key, v);
    return this;
  }

  public Field buildArray() {
    FieldArray fieldArray = new FieldArray(null);
    Atom atom = new Atom(type);
    atom.initialSet(data);
    fieldArray.initialSet(TSList.of(atom));
    return fieldArray;
  }
}
