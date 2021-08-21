package com.zarbosoft.alligatoroid.compiler.jvm;

import com.zarbosoft.alligatoroid.compiler.Value;
import com.zarbosoft.alligatoroid.compiler.mortar.Record;
import com.zarbosoft.rendaw.common.TSMap;

import static com.zarbosoft.alligatoroid.compiler.language.Builtin.wrapFunction;

public class JVMBuiltin {
  public static final Record builtin =
      new Record(
          new TSMap()
              .put("newClass", wrapFunction(JVMBuiltin.class, "builtinNewClass"))
              .put("externClass", wrapFunction(JVMBuiltin.class, "builtinExternClass"))
              .put("externStaticField", wrapFunction(JVMBuiltin.class, "builtinExternStaticField"))
              .put("string", JVMStringType.value));

  public static JVMClassType builtinNewClass(String qualifiedName) {
    return new JVMClassType(qualifiedName);
  }

  public static JVMExternClassType builtinExternClass(String qualifiedName) {
    return new JVMExternClassType(qualifiedName);
  }

  public static Value builtinExternStaticField(
      String qualifiedClassName, String fieldName, Value spec) {
    JVMDataType spec1 = (JVMDataType) spec;
    return new JVMExternStaticField(qualifiedClassName, fieldName, spec1);
  }
}
