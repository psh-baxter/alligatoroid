package com.zarbosoft.merman.core.document;

import com.zarbosoft.merman.core.syntax.Syntax;

public class Document {
  public final Syntax syntax;
  public final Atom root;

  public Document(final Syntax syntax, final Atom root) {
    this.syntax = syntax;
    this.root = root;
  }
}
