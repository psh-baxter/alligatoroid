package com.zarbosoft.alligatoroid.compiler.language;

import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.EvaluateResult;
import com.zarbosoft.alligatoroid.compiler.LanguageValue;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.ModuleContext;
import com.zarbosoft.alligatoroid.compiler.Value;
import com.zarbosoft.alligatoroid.compiler.jvm.JVMBuiltin;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMDescriptor;
import com.zarbosoft.alligatoroid.compiler.mortar.BuiltinModuleFunction;
import com.zarbosoft.alligatoroid.compiler.mortar.CreatedFile;
import com.zarbosoft.alligatoroid.compiler.mortar.Function;
import com.zarbosoft.alligatoroid.compiler.mortar.LooseRecord;
import com.zarbosoft.alligatoroid.compiler.mortar.MortarClass;
import com.zarbosoft.alligatoroid.compiler.mortar.MortarHalfArrayType;
import com.zarbosoft.alligatoroid.compiler.mortar.MortarHalfByteType;
import com.zarbosoft.alligatoroid.compiler.mortar.MortarHalfDataType;
import com.zarbosoft.alligatoroid.compiler.mortar.MortarHalfObjectType;
import com.zarbosoft.alligatoroid.compiler.mortar.MortarHalfType;
import com.zarbosoft.alligatoroid.compiler.mortar.MortarMethodFieldType;
import com.zarbosoft.alligatoroid.compiler.mortar.NullValue;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSMap;
import com.zarbosoft.rendaw.common.TSOrderedMap;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;

public class Builtin extends LanguageValue {
  public static TSMap<Class, MortarClass> wrappedClasses = new TSMap<>();
  public static LooseRecord builtin =
      new LooseRecord(
          new TSOrderedMap()
              .put("print", EvaluateResult.pure(wrapModuleFunction("builtinLog")))
              .put("jvm", EvaluateResult.pure(JVMBuiltin.builtin))
              .put("null", EvaluateResult.pure(NullValue.value))
              .put(
                  "createFile",
                  EvaluateResult.pure(wrapFunction(Builtin.class, "builtinCreateFile"))));

  public Builtin(Location id) {
    super(id, false);
  }

  private static Value wrapModuleFunction(String methodName) {
    Method method = null;
    for (Method c : ModuleContext.class.getDeclaredMethods()) {
      if (c.getName().equals(methodName)) {
        method = c;
        break;
      }
    }
    if (method == null) throw new Assertion();
    ROPair<String, MortarHalfDataType> desc = funcDescriptor(method);
    return new BuiltinModuleFunction(methodName, desc.first, desc.second);
  }

  public static ROPair<String, MortarHalfDataType> funcDescriptor(Method method) {
    // Arguments
    String[] argDescriptor = new String[method.getParameters().length];
    for (int i = 0; i < method.getParameters().length; ++i) {
      Parameter parameter = method.getParameters()[i];
      ROPair<String, MortarHalfDataType> paramDesc = dataDescriptor(parameter.getType());
      argDescriptor[i] = paramDesc.first;
    }

    ROPair<String, MortarHalfDataType> retDesc = dataDescriptor(method.getReturnType());

    return new ROPair<>(JVMDescriptor.func(retDesc.first, argDescriptor), retDesc.second);
  }

  public static ROPair<String, MortarHalfDataType> dataDescriptor(Class klass) {
    if (klass == void.class) {
      return new ROPair<>(JVMDescriptor.voidDescriptor(), null);
    } else if (klass == String.class) {
      return new ROPair<>(
          JVMDescriptor.objDescriptorFromReal(String.class), MortarHalfObjectType.type);
    } else if (klass == byte[].class) {
      return new ROPair<>(
          JVMDescriptor.arrayDescriptor(JVMDescriptor.byteDescriptor()),
          new MortarHalfArrayType(new MortarHalfByteType()));
    } else {
      return new ROPair<>(JVMDescriptor.objDescriptorFromReal(klass), wrapClass(klass));
    }
  }

  public static MortarHalfDataType wrapClass(Class klass) {
    /*
    if (klass == Value.class) {
      throw new Assertion();
    }
     */
    MortarClass out = wrappedClasses.getOpt(klass);
    if (out == null) {
      out = new MortarClass(JVMDescriptor.jvmName(klass));
      wrappedClasses.put(klass, out);
      TSMap<Object, MortarHalfType> fields = new TSMap<>();
      if (klass != Value.class)
        for (Method method : klass.getDeclaredMethods()) {
          if (!Modifier.isPublic(method.getModifiers())) continue;
          ROPair<String, MortarHalfDataType> desc = funcDescriptor(method);
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
    }
    return out;
  }

  public static Function wrapFunction(Class klass, String name) {
    Method method = null;
    for (Method checkMethod : klass.getMethods()) {
      if (!checkMethod.getName().equals(name)) continue;
      method = checkMethod;
      break;
    }
    ROPair<String, MortarHalfDataType> desc = funcDescriptor(method);
    return new Function(
        JVMDescriptor.jvmName(klass.getCanonicalName()), name, desc.first, desc.second);
  }

  public CreatedFile builtinCreateFile(String path) {
    return new CreatedFile(path);
  }

  @Override
  public EvaluateResult evaluate(Context context) {
    return EvaluateResult.pure(builtin);
  }
}
