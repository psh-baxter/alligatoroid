package com.zarbosoft.merman.helper;

import com.zarbosoft.merman.syntax.FreeAtomType;
import com.zarbosoft.merman.syntax.alignments.AbsoluteAlignmentDefinition;
import com.zarbosoft.merman.syntax.alignments.ConcensusAlignmentDefinition;
import com.zarbosoft.merman.syntax.alignments.RelativeAlignmentDefinition;
import com.zarbosoft.merman.syntax.back.BackSpec;
import com.zarbosoft.merman.syntax.front.FrontAtomSpec;
import com.zarbosoft.merman.syntax.front.FrontFixedArraySpec;
import com.zarbosoft.merman.syntax.front.FrontPrimitiveSpec;
import com.zarbosoft.merman.syntax.front.FrontSpec;
import com.zarbosoft.merman.syntax.front.FrontSymbol;
import com.zarbosoft.merman.syntax.symbol.SymbolSpaceSpec;
import com.zarbosoft.merman.syntax.symbol.SymbolTextSpec;

import java.util.ArrayList;

public class TypeBuilder {
  private final FreeAtomType type;

  public TypeBuilder(final String id) {
    this.type = new FreeAtomType();
    type.id = id;
    type.name = id;
    type.back = new ArrayList<>();
    type.front = new ArrayList<>();
  }

  public TypeBuilder back(final BackSpec back) {
    type.back.add(back);
    return this;
  }

  public FreeAtomType build() {
    return type;
  }

  public TypeBuilder front(final FrontSpec front) {
    type.front.add(front);
    return this;
  }

  public TypeBuilder autoComplete(final int x) {
    type.autoChooseAmbiguity = x;
    return this;
  }

  public TypeBuilder frontDataNode(final String middle) {
    final FrontAtomSpec part = new FrontAtomSpec();
    part.middle = middle;
    type.front.add(part);
    return this;
  }

  public TypeBuilder frontDataArray(final String middle) {
    final FrontFixedArraySpec part = new FrontFixedArraySpec();
    part.middle = middle;
    type.front.add(part);
    return this;
  }

  public TypeBuilder frontDataPrimitive(final String middle) {
    final FrontPrimitiveSpec part = new FrontPrimitiveSpec();
    part.field = middle;
    type.front.add(part);
    return this;
  }

  public TypeBuilder frontMark(final String value) {
    final FrontSymbol part = new FrontSymbol();
    part.type = new SymbolTextSpec(value);
    type.front.add(part);
    return this;
  }

  public TypeBuilder frontSpace() {
    final FrontSymbol part = new FrontSymbol();
    part.type = new SymbolSpaceSpec();
    type.front.add(part);
    return this;
  }

  public TypeBuilder precedence(final int precedence) {
    this.type.precedence = precedence;
    return this;
  }

  public TypeBuilder associateForward() {
    this.type.associateForward = true;
    return this;
  }

  public TypeBuilder associateBackward() {
    this.type.associateForward = false;
    return this;
  }

  public TypeBuilder depthScore(final int i) {
    this.type.depthScore = i;
    return this;
  }

  public TypeBuilder absoluteAlignment(final String name, final int offset) {
    final AbsoluteAlignmentDefinition definition = new AbsoluteAlignmentDefinition();
    definition.offset = offset;
    this.type.alignments.put(name, definition);
    return this;
  }

  public TypeBuilder relativeAlignment(final String name, final String base, final int offset) {
    final RelativeAlignmentDefinition definition = new RelativeAlignmentDefinition();
    definition.offset = offset;
    definition.base = base;
    this.type.alignments.put(name, definition);
    return this;
  }

  public TypeBuilder concensusAlignment(final String name) {
    this.type.alignments.put(name, new ConcensusAlignmentDefinition());
    return this;
  }
}
