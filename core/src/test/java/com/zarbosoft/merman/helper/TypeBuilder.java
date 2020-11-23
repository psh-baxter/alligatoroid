package com.zarbosoft.merman.helper;

import com.zarbosoft.merman.syntax.FreeAtomType;
import com.zarbosoft.merman.syntax.alignments.AbsoluteAlignmentDefinition;
import com.zarbosoft.merman.syntax.alignments.ConcensusAlignmentDefinition;
import com.zarbosoft.merman.syntax.alignments.RelativeAlignmentDefinition;
import com.zarbosoft.merman.syntax.back.BackSpec;
import com.zarbosoft.merman.syntax.front.FrontDataAtom;
import com.zarbosoft.merman.syntax.front.FrontFixedArraySpec;
import com.zarbosoft.merman.syntax.front.FrontPrimitiveSpec;
import com.zarbosoft.merman.syntax.front.FrontSpec;
import com.zarbosoft.merman.syntax.front.FrontSymbol;
import com.zarbosoft.merman.syntax.middle.MiddleArraySpec;
import com.zarbosoft.merman.syntax.middle.MiddleAtomSpec;
import com.zarbosoft.merman.syntax.middle.MiddlePrimitiveSpec;
import com.zarbosoft.merman.syntax.middle.MiddleRecordSpec;
import com.zarbosoft.merman.syntax.primitivepattern.Digits;
import com.zarbosoft.merman.syntax.primitivepattern.Letters;
import com.zarbosoft.merman.syntax.primitivepattern.Repeat1;
import com.zarbosoft.merman.syntax.symbol.SymbolSpaceSpec;
import com.zarbosoft.merman.syntax.symbol.SymbolTextSpec;

import java.util.ArrayList;
import java.util.HashMap;

public class TypeBuilder {
  private final FreeAtomType type;

  public TypeBuilder(final String id) {
    this.type = new FreeAtomType();
    type.id = id;
    type.name = id;
    type.back = new ArrayList<>();
    type.middle = new HashMap<>();
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
    final FrontDataAtom part = new FrontDataAtom();
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
    part.middle = middle;
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

  public TypeBuilder middlePrimitive(final String id) {
    final MiddlePrimitiveSpec middle = new MiddlePrimitiveSpec();
    middle.id = id;
    this.type.middle.put(id, middle);
    return this;
  }

  public TypeBuilder middlePrimitiveLetters(final String id) {
    final MiddlePrimitiveSpec middle = new MiddlePrimitiveSpec();
    middle.id = id;
    middle.pattern = new Repeat1();
    ((Repeat1) middle.pattern).pattern = new Letters();
    this.type.middle.put(id, middle);
    return this;
  }

  public TypeBuilder middlePrimitiveDigits(final String id) {
    final MiddlePrimitiveSpec middle = new MiddlePrimitiveSpec();
    middle.id = id;
    middle.pattern = new Repeat1();
    ((Repeat1) middle.pattern).pattern = new Digits();
    this.type.middle.put(id, middle);
    return this;
  }

  public TypeBuilder middleAtom(final String id, final String type) {
    final MiddleAtomSpec middle = new MiddleAtomSpec();
    middle.type = type;
    middle.id = id;
    this.type.middle.put(id, middle);
    return this;
  }

  public TypeBuilder middleArray(final String id, final String type) {
    final MiddleArraySpec middle = new MiddleArraySpec();
    middle.type = type;
    middle.id = id;
    this.type.middle.put(id, middle);
    return this;
  }

  public TypeBuilder middleRecord(final String id, final String type) {
    final MiddleRecordSpec middle = new MiddleRecordSpec();
    middle.type = type;
    middle.id = id;
    this.type.middle.put(id, middle);
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
