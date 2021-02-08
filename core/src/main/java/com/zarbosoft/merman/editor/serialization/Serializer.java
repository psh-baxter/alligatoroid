package com.zarbosoft.merman.editor.serialization;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.rendaw.common.ROList;

public interface Serializer {
  byte[] write(Atom atom);

  byte[] write(ROList<Atom> atom);

  /**
   * Per clipboard, data is bytes or string depending on execution environment (js vs java)
   * @param syntax
   * @param type
   * @param data
   * @return
   */
  ROList<Atom> loadFromClipboard(Syntax syntax, String type, Object data);
}
