package com.zarbosoft.merman.core.serialization;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.Environment;
import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.document.Document;
import com.zarbosoft.merman.core.syntax.AtomType;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.TSList;

public interface Serializer {
  /** @return byte[] in java, string in js */
  Object writeDocument(Environment env, Document document);

  /**
   *
   * @param env
   * @param copyContext
   * @return byte[] in java, string in js
   */
  Object writeForClipboard(Environment env, Context.CopyContext copyContext, TSList<WriteState> stack);

  /**
   * Per clipboard, data is bytes or string depending on execution environment (js vs java)
   *
   * @param syntax
   * @param copyContext
   * @param type
   * @param data
   * @return
   */
  ROList<Atom> loadFromClipboard(
          Syntax syntax, Context.CopyContext copyContext, Node<ROList<AtomType.AtomParseResult>> type, Object data);
}
