package com.zarbosoft.alligatoroid.compiler.sourcedeserialize;

import com.zarbosoft.alligatoroid.compiler.Error;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.ModuleId;
import com.zarbosoft.alligatoroid.compiler.Value;
import com.zarbosoft.alligatoroid.compiler.deserialize.BaseState;
import com.zarbosoft.alligatoroid.compiler.deserialize.Deserializer;
import com.zarbosoft.alligatoroid.compiler.deserialize.LocationPrototype;
import com.zarbosoft.alligatoroid.compiler.deserialize.ObjectInfo;
import com.zarbosoft.alligatoroid.compiler.deserialize.State;
import com.zarbosoft.alligatoroid.compiler.deserialize.StateObject;
import com.zarbosoft.alligatoroid.compiler.deserialize.StatePrototype;
import com.zarbosoft.alligatoroid.compiler.deserialize.StatePrototypeArray;
import com.zarbosoft.alligatoroid.compiler.deserialize.StatePrototypeInt;
import com.zarbosoft.alligatoroid.compiler.deserialize.StatePrototypeString;
import com.zarbosoft.alligatoroid.compiler.language.Access;
import com.zarbosoft.alligatoroid.compiler.language.Bind;
import com.zarbosoft.alligatoroid.compiler.language.Block;
import com.zarbosoft.alligatoroid.compiler.language.Builtin;
import com.zarbosoft.alligatoroid.compiler.language.Call;
import com.zarbosoft.alligatoroid.compiler.language.LiteralString;
import com.zarbosoft.alligatoroid.compiler.language.Local;
import com.zarbosoft.alligatoroid.compiler.language.Lower;
import com.zarbosoft.alligatoroid.compiler.language.Record;
import com.zarbosoft.alligatoroid.compiler.language.RecordElement;
import com.zarbosoft.alligatoroid.compiler.language.Stage;
import com.zarbosoft.alligatoroid.compiler.language.Tuple;
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

public class SourceDeserializer {
  private final StatePrototype valuePrototype;
  private final TSMap<String, ObjectInfo> languageNodeInfos = new TSMap<>();

  public SourceDeserializer(ModuleId module) {
    final Class[] language =
        new Class[] {
          Access.class,
          Bind.class,
          Block.class,
          Builtin.class,
          Call.class,
          LiteralString.class,
          Local.class,
          Record.class,
          RecordElement.class,
          Tuple.class,
          Stage.class,
          Lower.class,
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
                ObjectInfo info = languageNodeInfos.getOpt(name);
                if (info == null) {
                  errors.add(
                      Error.deserializeUnknownType(
                          luxemPath, name, languageNodeInfos.keys().toList()));
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

    for (Class klass : language) {
      String type = toUnderscore(klass.getSimpleName());
      languageNodeInfos.put(type, new ObjectInfo(type));
    }
    for (Class klass : language) {
      Constructor constructor = klass.getConstructors()[0];
      TSMap<String, StatePrototype> fields = new TSMap<>();
      TSMap<String, Integer> argOrder = new TSMap<>();
      for (int i = 0; i < constructor.getParameters().length; i++) {
        Parameter parameter = constructor.getParameters()[i];
        argOrder.put(parameter.getName(), i);
        StatePrototype prototype;
        if (parameter.getType() == Location.class) {
          prototype = new LocationPrototype(module);
        } else if (parameter.getType() == Value.class) {
          prototype = valuePrototype;
        } else if (parameter.getType() == int.class) {
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
      ObjectInfo prototype = languageNodeInfos.get(type);
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

  public ROList<Value> deserialize(TSList<Error> errors, Path path) {
    TSList<State> stack = new TSList<>();
    State rootNodes = new StatePrototypeArray(valuePrototype).create(errors, null, stack);
    stack.add(
        new BaseState() {
          @Override
          public void eatType(
              TSList<Error> errors, TSList<State> stack, LuxemPath luxemPath, String name) {
            String expected = "alligatoroid:0.0.1";
            if (!expected.equals(name)) {
              errors.add(Error.deserializeUnknownLanguageVersion(luxemPath, expected));
              ok = false;
            }
            stack.removeLast();
          }
        });
    Deserializer.deserialize(errors, path, stack);
    return (ROList<Value>) rootNodes.build(errors);
  }
}
