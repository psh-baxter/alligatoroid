package com.zarbosoft.merman.editor.serialization;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.rendaw.common.ROList;

public interface Serializer {
  byte[] write(Atom atom);

  byte[] write(ROList<Atom> atom);

  ROList<Atom> load(Syntax syntax, String type, byte[] data);
}
