package com.zarbosoft.alligatoroid.compiler.cache;

import com.zarbosoft.alligatoroid.compiler.Error;
import com.zarbosoft.alligatoroid.compiler.LocalModuleId;
import com.zarbosoft.alligatoroid.compiler.ModuleId;
import com.zarbosoft.alligatoroid.compiler.deserialize.BaseState;
import com.zarbosoft.alligatoroid.compiler.deserialize.Deserializer;
import com.zarbosoft.alligatoroid.compiler.deserialize.ObjectInfo;
import com.zarbosoft.alligatoroid.compiler.deserialize.State;
import com.zarbosoft.alligatoroid.compiler.deserialize.StateObject;
import com.zarbosoft.alligatoroid.compiler.deserialize.StatePrototype;
import com.zarbosoft.alligatoroid.compiler.deserialize.StatePrototypeInt;
import com.zarbosoft.alligatoroid.compiler.deserialize.StatePrototypeString;
import com.zarbosoft.luxem.read.path.LuxemPath;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.nio.file.Path;

public class ModuleIdDeserializer {
  private final StatePrototype valuePrototype;
  private final TSMap<String, ObjectInfo> typeInfos = new TSMap<>();

  public ModuleIdDeserializer() {
    final Class[] idTypes =
        new Class[] {
          LocalModuleId.class,
        };
    valuePrototype =
        new StatePrototype() {
          @Override
          public State create(TSList<Error> errors, LuxemPath luxemPath, TSList<State> stack) {
            return new BaseState() {
              State child;

              @Override
              public void eatType(
                  TSList<Error> errors, TSList<State> stack, LuxemPath luxemPath, String name) {
                ObjectInfo info = typeInfos.getOpt(name);
                if (info == null) {
                  errors.add(
                      Error.deserializeUnknownType(luxemPath, name, typeInfos.keys().toList()));
                  ok = false;
                  return;
                }
                child = new StateObject(info);
                stack.add(child);
              }

              @Override
              public Object build(TSList<Error> errors) {
                return child.build(errors);
              }
            };
          }
        };

    for (Class klass : idTypes) {
      String type = toUnderscore(klass.getSimpleName());
      typeInfos.put(type, new ObjectInfo(type));
    }
    for (Class klass : idTypes) {
      Constructor constructor = klass.getConstructors()[0];
      TSMap<String, StatePrototype> fields = new TSMap<>();
      TSMap<String, Integer> argOrder = new TSMap<>();
      for (int i = 0; i < constructor.getParameters().length; i++) {
        Parameter parameter = constructor.getParameters()[i];
        argOrder.put(parameter.getName(), i);
        StatePrototype prototype;
        if (parameter.getType() == int.class) {
          prototype = StatePrototypeInt.instance;
        } else if (parameter.getType() == String.class) {
          prototype = StatePrototypeString.instance;
        } else throw new Assertion();
        fields.put(parameter.getName(), prototype);
      }
      String type = toUnderscore(klass.getSimpleName());
      ObjectInfo prototype = typeInfos.get(type);
      prototype.constructor = constructor;
      prototype.argOrder = argOrder;
      prototype.fields = fields;
    }
  }

  private static String toUnderscore(String name) {
    StringBuilder out = new StringBuilder();
    for (int i = 0; i < name.length(); ++i) {
      if (Character.isUpperCase(name.codePointAt(i))) {
        if (i > 0) {
          out.append('_');
        }
        out.appendCodePoint(Character.toLowerCase(name.codePointAt(i)));
      } else {
        out.appendCodePoint(name.codePointAt(i));
      }
    }
    return out.toString();
  }

  /**
   * Returns null if there's an external issue
   *
   * @param errors
   * @param path
   * @return
   */
  public ModuleId deserialize(TSList<Error> errors, Path path) {
    TSList<State> stack = new TSList<>();
    State rootNode = valuePrototype.create(errors, null, stack);
    Deserializer.deserialize(errors, path, stack);
    return (ModuleId) rootNode.build(errors);
  }
}
