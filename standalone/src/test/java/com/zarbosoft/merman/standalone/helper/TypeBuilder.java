package com.zarbosoft.merman.standalone.helper;

import com.zarbosoft.merman.syntax.AtomType;
import com.zarbosoft.merman.syntax.FreeAtomType;
import com.zarbosoft.merman.syntax.alignments.AlignmentSpec;
import com.zarbosoft.merman.syntax.alignments.ConcensusAlignmentSpec;
import com.zarbosoft.merman.syntax.alignments.RelativeAlignmentSpec;
import com.zarbosoft.merman.syntax.back.BackSpec;
import com.zarbosoft.merman.syntax.front.FrontArraySpec;
import com.zarbosoft.merman.syntax.front.FrontArraySpecBase;
import com.zarbosoft.merman.syntax.front.FrontAtomSpec;
import com.zarbosoft.merman.syntax.front.FrontPrimitiveSpec;
import com.zarbosoft.merman.syntax.front.FrontSpec;
import com.zarbosoft.merman.syntax.front.FrontSymbol;
import com.zarbosoft.merman.syntax.symbol.SymbolSpaceSpec;
import com.zarbosoft.merman.syntax.symbol.SymbolTextSpec;
import com.zarbosoft.rendaw.common.ROSet;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;

public class TypeBuilder {
  private final String id;
  private final TSList<FrontSpec> front = new TSList<>();
  private final TSList<BackSpec> back = new TSList<>();
  private final FreeAtomType.Config config = new FreeAtomType.Config(name);
  private final TSMap<String, AlignmentSpec> alignments = new TSMap<>();

  public TypeBuilder(final String id) {
    this.id = id;
    config.name = id;
  }

  public TypeBuilder back(final BackSpec back) {
    this.back.add(back);
    return this;
  }

  public FreeAtomType build() {
    config.base = new AtomType.Config(id, ROSet.empty, back, front);
    config.alignments = alignments;
    return new FreeAtomType(config);
  }

  public TypeBuilder front(final FrontSpec front) {
    this.front.add(front);
    return this;
  }

  public TypeBuilder autoComplete(final int x) {
    config.autoChooseAmbiguity = x;
    return this;
  }

  public TypeBuilder frontDataNode(final String middle) {
    this.front.add(new FrontAtomSpec(new FrontAtomSpec.Config(middle)));
    return this;
  }

  public TypeBuilder frontDataArray(final String middle) {
    this.front.add(
        new FrontArraySpec(new FrontArraySpec.Config(middle, new FrontArraySpecBase.Config())));
    return this;
  }

  public TypeBuilder frontDataPrimitive(final String middle) {
    this.front.add(new FrontPrimitiveSpec(new FrontPrimitiveSpec.Config(middle)));
    return this;
  }

  public TypeBuilder frontMark(final String value) {
    this.front.add(
        new FrontSymbol(
            new FrontSymbol.Config(new SymbolTextSpec(value, style))));
    return this;
  }

  public TypeBuilder frontSpace() {
    this.front.add(
        new FrontSymbol(new FrontSymbol.Config(new SymbolSpaceSpec(style))));
    return this;
  }

  public TypeBuilder precedence(final int precedence) {
    config.precedence = precedence;
    return this;
  }

  public TypeBuilder associateForward() {
    config.associateForward = true;
    return this;
  }

  public TypeBuilder associateBackward() {
    config.associateForward = false;
    return this;
  }

  public TypeBuilder depthScore(final int i) {
    config.depthScore = i;
    return this;
  }

  public TypeBuilder absoluteAlignment(final String name, final int offset) {
    alignments.put(name, new AbsoluteAlignmentSpec(offset));
    return this;
  }

  public TypeBuilder relativeAlignment(final String name, final String base, final int offset) {
    alignments.put(name, new RelativeAlignmentSpec(new RelativeAlignmentSpec.Config(base, offset, false)));
    return this;
  }

  public TypeBuilder concensusAlignment(final String name) {
    alignments.put(name, new ConcensusAlignmentSpec());
    return this;
  }
}
