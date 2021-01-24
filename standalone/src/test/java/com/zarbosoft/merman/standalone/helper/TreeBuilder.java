package com.zarbosoft.merman.standalone.helper;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.Value;
import com.zarbosoft.merman.document.values.ValueArray;
import com.zarbosoft.merman.document.values.ValueAtom;
import com.zarbosoft.merman.document.values.ValuePrimitive;
import com.zarbosoft.merman.syntax.AtomType;
import com.zarbosoft.merman.syntax.back.BaseBackArraySpec;
import com.zarbosoft.merman.syntax.back.BaseBackAtomSpec;
import com.zarbosoft.merman.syntax.back.BaseBackPrimitiveSpec;
import com.zarbosoft.rendaw.common.TSMap;

import java.util.Arrays;
import java.util.List;

public class TreeBuilder {
  private final AtomType type;
  private final TSMap<String, Value> data = new TSMap<>();

  public TreeBuilder(final AtomType type) {
    this.type = type;
  }

  public TreeBuilder add(final String key, final TreeBuilder builder) {
    data.putNew(key, new ValueAtom((BaseBackAtomSpec) type.fields.get(key), builder.build()));
    return this;
  }

  public Atom build() {
    return new Atom(type, data);
  }

  public TreeBuilder add(final String key, final Atom atom) {
    data.putNew(key, new ValueAtom((BaseBackAtomSpec) type.fields.get(key), atom));
    return this;
  }

  public TreeBuilder add(final String key, final String text) {
    data.putNew(key, new ValuePrimitive((BaseBackPrimitiveSpec) type.fields.get(key), text));
    return this;
  }

  public TreeBuilder addArray(final String key, final List<Atom> values) {
    data.putNew(key, new ValueArray((BaseBackArraySpec) type.fields.get(key), values));
    return this;
  }

  public TreeBuilder addArray(final String key, final Atom... values) {
    data.putNew(key, new ValueArray((BaseBackArraySpec) type.fields.get(key), Arrays.asList(values)));
    return this;
  }

  public TreeBuilder addRecord(final String key, final Atom... values) {
    data.putNew(key, new ValueArray((BaseBackArraySpec) type.fields.get(key), Arrays.asList(values)));
    return this;
  }

  public Value buildArray() {
    return new ValueArray(null, Arrays.asList(new Atom(type, data)));
  }
}
