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
    data.putNew(key, new FieldAtom((BaseBackAtomSpec) type.fields.get(key), builder.build()));
    return this;
  }

  public Atom build() {
    return new Atom(type, data);
  }

  public TreeBuilder add(final String key, final Atom atom) {
    data.putNew(key, new FieldAtom((BaseBackAtomSpec) type.fields.get(key), atom));
    return this;
  }

  public TreeBuilder add(final String key, final String text) {
    data.putNew(key, new FieldPrimitive((BaseBackPrimitiveSpec) type.fields.get(key), text));
    return this;
  }

  public TreeBuilder addArray(final String key, final Atom... values) {
    data.putNew(
        key, new FieldArray((BaseBackArraySpec) type.fields.get(key), TSList.of(values)));
    return this;
  }

  public TreeBuilder addRecord(final String key, final Atom... values) {
    data.putNew(
        key, new FieldArray((BaseBackArraySpec) type.fields.get(key), TSList.of(values)));
    return this;
  }

  public Field buildArray() {
    return new FieldArray(null, TSList.of(new Atom(type, data)));
  }
}
