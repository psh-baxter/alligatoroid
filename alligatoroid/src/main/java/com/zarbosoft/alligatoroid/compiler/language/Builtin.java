package com.zarbosoft.alligatoroid.compiler.language;

import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.LanguageValue;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.Value;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMDescriptor;
import com.zarbosoft.alligatoroid.compiler.mortar.Function;
import com.zarbosoft.alligatoroid.compiler.mortar.Record;
import com.zarbosoft.alligatoroid.compiler.mortar.TypeNull;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.TSMap;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class Builtin extends LanguageValue {
  public static Record builtin =
      new Record(new TSMap().put("print", wrapFunction(Builtin.class, "builtinPrint")));

  public Builtin(Location id) {
    super(id);
  }

  public static void builtinPrint(String text) {
    System.out.println(text);
  }

  public static Function wrapFunction(Class klass, String name) {
    Method method = null;
    for (Method checkMethod : klass.getMethods()) {
      if (!checkMethod.getName().equals(name)) continue;
      method = checkMethod;
      break;
    }
    String[] argDescriptor = new String[method.getParameters().length];
    for (int i = 0; i < method.getParameters().length; ++i) {
      Parameter parameter = method.getParameters()[i];
      if (Object.class.isAssignableFrom(parameter.getType())) {
        argDescriptor[i] = JVMDescriptor.objReal(parameter.getType());
      } else throw new Assertion();
    }
    String retDescriptor;
    Value retTypeValue;
    if (method.getReturnType() == void.class) {
      retDescriptor = JVMDescriptor.void_();
      retTypeValue = TypeNull.value;
    } else throw new Assertion();
    return new Function(
        JVMDescriptor.internalName(klass.getCanonicalName()),
        name,
        JVMDescriptor.func(retDescriptor, argDescriptor),
        retTypeValue);
  }

  @Override
  public Value evaluate(Context context) {
    return builtin;
  }
}
