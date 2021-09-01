package com.zarbosoft.alligatoroid.compiler.cache;

import com.zarbosoft.alligatoroid.compiler.Error;
import com.zarbosoft.alligatoroid.compiler.LocalModuleId;
import com.zarbosoft.alligatoroid.compiler.ModuleId;
import com.zarbosoft.alligatoroid.compiler.Value;
import com.zarbosoft.alligatoroid.compiler.deserialize.BaseState;
import com.zarbosoft.alligatoroid.compiler.deserialize.Deserializer;
import com.zarbosoft.alligatoroid.compiler.deserialize.ObjectInfo;
import com.zarbosoft.alligatoroid.compiler.deserialize.State;
import com.zarbosoft.alligatoroid.compiler.deserialize.StateErrorSingle;
import com.zarbosoft.alligatoroid.compiler.deserialize.StateInt;
import com.zarbosoft.alligatoroid.compiler.deserialize.StateObject;
import com.zarbosoft.alligatoroid.compiler.deserialize.StatePrototype;
import com.zarbosoft.alligatoroid.compiler.deserialize.StatePrototypeArray;
import com.zarbosoft.alligatoroid.compiler.deserialize.StatePrototypeInt;
import com.zarbosoft.alligatoroid.compiler.deserialize.StatePrototypeString;
import com.zarbosoft.alligatoroid.compiler.deserialize.StateString;
import com.zarbosoft.luxem.read.path.LuxemPath;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Path;

public class CacheDeserializer {
  public static final String BUILTIN_TYPE_PREFIX = "builtin";
  public static final String CACHE_TYPE_PREFIX = "cache";
  private final StatePrototype valuePrototype;
  private final TSMap<String, ObjectInfo> typeInfos = new TSMap<>();

  public CacheDeserializer() {
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
        } else if (parameter.getType() == ROList.class) {
          Type paramType =
              ((ParameterizedType) parameter.getParameterizedType()).getActualTypeArguments()[0];
          if (paramType == Value.class) {
            prototype = new StatePrototypeArray(valuePrototype);
          } else throw new Assertion();
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
    State rootNode =
        new BaseState() {
          State out;

          @Override
          public void eatType(
              TSList<Error> errors, TSList<State> stack, LuxemPath luxemPath, String name) {
            stack.removeLast();
            String[] parts = name.split(":", 1);
            switch (parts[0]) {
              case "string":
                {
                  if (parts.length != 1) {
                    errors.add(Error.deserializeCacheInvalidTypeFormat(luxemPath, name, 1));
                    break;
                  }
                  stack.add(out = new StateString());
                  return;
                }
              case "int":
                {
                  if (parts.length != 1) {
                    errors.add(Error.deserializeCacheInvalidTypeFormat(luxemPath, name, 1));
                    break;
                  }
                  stack.add(out = new StateInt());
                  return;
                }
              case BUILTIN_TYPE_PREFIX:
                {
                  if (parts.length != 2) {
                    errors.add(Error.deserializeCacheInvalidTypeFormat(luxemPath, name, 2));
                    break;
                  }
                  StatePrototype statePrototype = builtin.getOpt(parts[1]);
                  if (statePrototype == null) {
                    errors.add(Error.deserializeCacheInvalidBuiltinType(luxemPath, parts[1]));
                    break;
                  }
                  out = statePrototype.create(errors, luxemPath, stack);
                  return;
                }
              case CACHE_TYPE_PREFIX:
                {
                  if (parts.length != 2) {
                    errors.add(
                        Error.deserializeCacheInvalidTypeFormat(luxemPath, name, "cache:..."));
                    break;
                  }
                  String cachePath = parts[1];
                  // classObj = cacheGet(cachePath);
                  // TODO
                }
              default:
                {
                  errors.add(Error.deserializeCacheInvalidType(luxemPath, name));
                }
            }
            stack.add(StateErrorSingle.state);
          }
        };
    Deserializer.deserialize(errors, path, stack);
    return (ModuleId) rootNode.build(errors);
  }
}
