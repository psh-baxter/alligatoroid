package com.zarbosoft.merman.helper;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.merman.syntax.FreeAtomType;
import com.zarbosoft.merman.syntax.RootAtomType;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.merman.syntax.alignments.AbsoluteAlignmentDefinition;
import com.zarbosoft.merman.syntax.alignments.ConcensusAlignmentDefinition;
import com.zarbosoft.merman.syntax.alignments.RelativeAlignmentDefinition;
import com.zarbosoft.merman.syntax.back.BackSpec;
import com.zarbosoft.merman.syntax.front.FrontFixedArraySpec;
import com.zarbosoft.merman.syntax.front.FrontSymbol;
import com.zarbosoft.merman.syntax.style.Style;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SyntaxBuilder {

  private final Syntax syntax;

  public SyntaxBuilder(final String root) {
    this.syntax = new Syntax();
    syntax.root = new RootAtomType();
    BackSpec back = Helper.buildBackDataRootArray("value", root);
    syntax.root.back = ImmutableList.of(back);
    syntax.root.front = ImmutableList.of(new FrontDataArrayBuilder("value").build());
  }

  public SyntaxBuilder type(final FreeAtomType type) {
    syntax.types.putNew(type.id, type);
    return this;
  }

  public Syntax build() {
    List<Object> errors = new ArrayList<>();
    syntax.finish(errors);
    if (!errors.isEmpty())
      throw new RuntimeException(
          String.format(
              "\n%s\n", errors.stream().map(e -> e.toString()).collect(Collectors.joining("\n"))));
    return syntax;
  }

  public SyntaxBuilder group(final String name, final List<String> subtypes) {
    syntax.groups.putNew(name, subtypes);
    return this;
  }

  public SyntaxBuilder style(final Style style) {
    syntax.styles.add(style);
    return this;
  }

  public SyntaxBuilder absoluteAlignment(final String name, final int offset) {
    final AbsoluteAlignmentDefinition definition = new AbsoluteAlignmentDefinition();
    definition.offset = offset;
    this.syntax.root.alignments.put(name, definition);
    return this;
  }

  public SyntaxBuilder relativeAlignment(final String name, final int offset) {
    final RelativeAlignmentDefinition definition = new RelativeAlignmentDefinition();
    definition.offset = offset;
    this.syntax.root.alignments.put(name, definition);
    return this;
  }

  public SyntaxBuilder concensusAlignment(final String name) {
    this.syntax.root.alignments.put(name, new ConcensusAlignmentDefinition());
    return this;
  }

  public SyntaxBuilder addRootFrontSeparator(final FrontSymbol part) {
    ((FrontFixedArraySpec) syntax.root.front.get(0)).separator.add(part);
    return this;
  }

  public SyntaxBuilder addRootFrontPrefix(final FrontSymbol part) {
    ((FrontFixedArraySpec) syntax.root.front.get(0)).prefix.add(part);
    return this;
  }
}
