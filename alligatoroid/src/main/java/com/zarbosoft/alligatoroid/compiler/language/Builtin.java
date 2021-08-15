package com.zarbosoft.alligatoroid.compiler.language;

import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.EvaluateResult;
import com.zarbosoft.alligatoroid.compiler.LanguageValue;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.Value;
import com.zarbosoft.alligatoroid.compiler.jvm.JVMBuiltin;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMDescriptor;
import com.zarbosoft.alligatoroid.compiler.mortar.Function;
import com.zarbosoft.alligatoroid.compiler.mortar.MortarClass;
import com.zarbosoft.alligatoroid.compiler.mortar.MortarHalfStringType;
import com.zarbosoft.alligatoroid.compiler.mortar.MortarHalfType;
import com.zarbosoft.alligatoroid.compiler.mortar.MortarMethodFieldType;
import com.zarbosoft.alligatoroid.compiler.mortar.Record;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSMap;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class Builtin extends LanguageValue {
  public static TSMap<Class, Value> wrappedClasses = new TSMap<>();
  public static Record builtin =
      new Record(
          new TSMap()
              .put("print", wrapFunction(Builtin.class, "builtinPrint"))
              .put("jvm", JVMBuiltin.builtin));

  public Builtin(Location id) {
    super(id);
  }

  public static void builtinPrint(String text) {
    System.out.println(text);
  }

  public static ROPair<String, MortarHalfType> funcDescriptor(Method method) {
    // Arguments
    String[] argDescriptor = new String[method.getParameters().length];
    for (int i = 0; i < method.getParameters().length; ++i) {
      Parameter parameter = method.getParameters()[i];
      if (Object.class.isAssignableFrom(parameter.getType())) {
        argDescriptor[i] = JVMDescriptor.objReal(parameter.getType());
      } else throw new Assertion();
    }

    ROPair<String, MortarHalfType> retDesc = dataDescriptor(method.getReturnType());

    return new ROPair<>(JVMDescriptor.func(retDesc.first, argDescriptor), retDesc.second);
  }

  public static ROPair<String, MortarHalfType> dataDescriptor(Class klass) {
    if (klass == void.class) {
      return new ROPair<>(JVMDescriptor.void_(), null);
    } else if (klass == String.class) {
      return new ROPair<>(JVMDescriptor.objReal(String.class), new MortarHalfStringType());
    } else if (Value.class.isAssignableFrom(klass)) {
      throw new Assertion(); // TODO
    } else {
      // fallthrough (classes) -- TODO primitives above
      return new ROPair<>(JVMDescriptor.objReal(klass), wrapClass(klass));
    }
  }

  public static MortarHalfType wrapClass(Class klass) {
    MortarClass out = new MortarClass(JVMDescriptor.internalName(klass));
    TSMap<Object, MortarHalfType> fields = new TSMap<>();
    for (Method method : klass.getDeclaredMethods()) {
      ROPair<String, MortarHalfType> desc = funcDescriptor(method);
      fields.putNew(
          method.getName(),
          new MortarMethodFieldType(out, method.getName(), desc.first, desc.second));
    }
    /*
    TODO
    for (Field field : klass.getDeclaredFields()) {
      ROPair<String, MortarHalfType> desc = dataDescriptor(field.getType());
      fields.putNew()
    }
     */
    out.fields = fields;
    return out;
  }

  public static Function wrapFunction(Class klass, String name) {
    Method method = null;
    for (Method checkMethod : klass.getMethods()) {
      if (!checkMethod.getName().equals(name)) continue;
      method = checkMethod;
      break;
    }
    ROPair<String, MortarHalfType> desc = funcDescriptor(method);
    return new Function(
        JVMDescriptor.internalName(klass.getCanonicalName()), name, desc.first, desc.second);
  }

  @Override
  public EvaluateResult evaluate(Context context) {
    return EvaluateResult.pure(builtin);
  }
}
