package com.zarbosoft.alligatoroid.compiler.jvm;

import com.zarbosoft.alligatoroid.compiler.mortar.Record;
import com.zarbosoft.rendaw.common.TSMap;

import static com.zarbosoft.alligatoroid.compiler.language.Builtin.wrapFunction;

public class JVMBuiltin {
  public static final Record builtin =
      new Record(new TSMap().put("newClass", wrapFunction(JVMBuiltin.class, "builtinNewClass")));

  public static JVMClass builtinNewClass(String qualifiedName) {
    return new JVMClass(qualifiedName);
  }
}
