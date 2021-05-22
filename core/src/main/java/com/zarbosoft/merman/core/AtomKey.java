package com.zarbosoft.merman.core;

import com.zarbosoft.merman.core.syntax.AtomType;
import com.zarbosoft.pidgoon.nodes.Reference;

import java.util.Objects;

public final class AtomKey extends Reference.Key<AtomType.AtomParseResult> {
  public final String type;

  public AtomKey(String type) {
    this.type = type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AtomKey atomKey = (AtomKey) o;
    return Objects.equals(type, atomKey.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type);
  }
}
