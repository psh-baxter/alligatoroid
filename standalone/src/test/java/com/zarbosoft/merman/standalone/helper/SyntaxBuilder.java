package com.zarbosoft.merman.standalone.helper;

import com.zarbosoft.merman.misc.MultiError;
import com.zarbosoft.merman.syntax.AtomType;
import com.zarbosoft.merman.syntax.FreeAtomType;
import com.zarbosoft.merman.syntax.Padding;
import com.zarbosoft.merman.syntax.RootAtomType;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.merman.syntax.alignments.AbsoluteAlignmentSpec;
import com.zarbosoft.merman.syntax.alignments.AlignmentSpec;
import com.zarbosoft.merman.syntax.alignments.ConcensusAlignmentSpec;
import com.zarbosoft.merman.syntax.alignments.RelativeAlignmentSpec;
import com.zarbosoft.merman.syntax.front.FrontSymbol;
import com.zarbosoft.merman.syntax.style.Style;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROSet;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;

import java.util.Set;

public class SyntaxBuilder {
  private final TSList<AtomType> types = new TSList<>();
  private final TSMap<String, ROList<String>> groups = new TSMap<String, ROList<String>>();
  private final TSList<Style.Spec> styles = new TSList<>();
  private final TSMap<String, AlignmentSpec> alignments = new TSMap<>();
  private final String rootChildType;
  private final FrontDataArrayBuilder front = new FrontDataArrayBuilder("value");
  private Padding padding;

  public SyntaxBuilder(final String root) {
    rootChildType = root;
  }

  public SyntaxBuilder pad(Padding padding) {
    this.padding = padding;
    return this;
  }

  public SyntaxBuilder type(final FreeAtomType type) {
    types.add(type);
    return this;
  }

  public Syntax build() {
    MultiError splayErrors = new MultiError();
    TSMap<String, Set<AtomType>> splayed = Syntax.splayGroups(splayErrors, types, groups);
    splayErrors.raise();

    RootAtomType root =
        new RootAtomType(
            new RootAtomType.Config(
                ROSet.empty,
                TSList.of(Helper.buildBackDataRootArray("value", rootChildType)),
                TSList.of(front.build()),
                alignments));

    Syntax.Config config = new Syntax.Config(types, splayed, root);
    config.pad = this.padding;
    config.styles = styles;
    Syntax syntax = new Syntax(config);
    MultiError errors = new MultiError();
    syntax.finish(errors);
    errors.raise();

    return syntax;
  }

  public SyntaxBuilder group(final String name, final ROList<String> subtypes) {
    groups.putNew(name, subtypes);
    return this;
  }

  public SyntaxBuilder style(final Style.Spec style) {
    styles.add(style);
    return this;
  }

  public SyntaxBuilder absoluteAlignment(final String name, final int offset) {
    alignments.put(name, new AbsoluteAlignmentSpec(offset));
    return this;
  }

  public SyntaxBuilder relativeAlignment(final String name, final int offset) {
    final RelativeAlignmentSpec definition =
        new RelativeAlignmentSpec(new RelativeAlignmentSpec.Config(null, offset));
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
}
