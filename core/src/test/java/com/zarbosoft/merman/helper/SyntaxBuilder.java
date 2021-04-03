package com.zarbosoft.merman.helper;

import com.zarbosoft.merman.core.misc.MultiError;
import com.zarbosoft.merman.core.syntax.AtomType;
import com.zarbosoft.merman.core.syntax.FreeAtomType;
import com.zarbosoft.merman.core.syntax.style.Padding;
import com.zarbosoft.merman.core.syntax.RootAtomType;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.core.syntax.alignments.AlignmentSpec;
import com.zarbosoft.merman.core.syntax.alignments.ConcensusAlignmentSpec;
import com.zarbosoft.merman.core.syntax.alignments.RelativeAlignmentSpec;
import com.zarbosoft.merman.core.syntax.front.FrontSymbol;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROSet;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;

public class SyntaxBuilder {
  private final TSList<AtomType> types = new TSList<>();
  private final TSMap<String, ROList<String>> groups = new TSMap<String, ROList<String>>();
  private final TSMap<String, AlignmentSpec> alignments = new TSMap<>();
  private final String rootChildType;
  private final FrontDataArrayBuilder front = new FrontDataArrayBuilder("value");
  private Padding padding = Padding.empty;

  public SyntaxBuilder(final String root) {
    rootChildType = root;
  }

  public SyntaxBuilder type(final FreeAtomType type) {
    types.add(type);
    return this;
  }

  public Syntax build() {
    MultiError splayErrors = new MultiError();
    TSMap<String, ROSet<AtomType>> splayed = Syntax.splayGroups(splayErrors, types, groups);
    splayErrors.raise();

    RootAtomType root =
        new RootAtomType(
            new RootAtomType.Config(
                TSList.of(Helper.buildBackDataRootArray("value", rootChildType)),
                TSList.of(front.build()),
                alignments));

    Syntax.Config config = new Syntax.Config(types, splayed, root);
    config.pad = this.padding;
    Syntax syntax = new Syntax(Helper.i18n, config);

    return syntax;
  }

  public SyntaxBuilder group(final String name, final ROList<String> subtypes) {
    groups.putNew(name, subtypes);
    return this;
  }

  public SyntaxBuilder relativeAlignment(final String name, final int offset) {
    final RelativeAlignmentSpec definition =
        new RelativeAlignmentSpec(new RelativeAlignmentSpec.Config(null, offset, false));
    alignments.put(name, definition);
    return this;
  }

  public SyntaxBuilder concensusAlignment(final String name) {
    alignments.put(name, new ConcensusAlignmentSpec());
    return this;
  }

  public SyntaxBuilder addRootFrontSeparator(final FrontSymbol part) {
    front.addSeparator(part);
    return this;
  }

  public SyntaxBuilder addRootFrontPrefix(final FrontSymbol part) {
    front.addPrefix(part);
    return this;
  }

  public SyntaxBuilder pad(Padding padding) {
    this.padding = padding;
    return this;
  }
}
