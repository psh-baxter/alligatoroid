package com.zarbosoft.alligatoroid.compiler.deserialize;

import com.zarbosoft.alligatoroid.compiler.Error;
import com.zarbosoft.alligatoroid.compiler.ErrorValue;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.ModuleContext;
import com.zarbosoft.alligatoroid.compiler.ModuleId;
import com.zarbosoft.alligatoroid.compiler.Value;
import com.zarbosoft.alligatoroid.compiler.language.Access;
import com.zarbosoft.alligatoroid.compiler.language.Bind;
import com.zarbosoft.alligatoroid.compiler.language.Block;
import com.zarbosoft.alligatoroid.compiler.language.Builtin;
import com.zarbosoft.alligatoroid.compiler.language.Call;
import com.zarbosoft.alligatoroid.compiler.language.LiteralString;
import com.zarbosoft.alligatoroid.compiler.language.Local;
import com.zarbosoft.luxem.read.BufferedReader;
import com.zarbosoft.luxem.read.path.LuxemArrayPath;
import com.zarbosoft.luxem.read.path.LuxemPath;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.zarbosoft.rendaw.common.Common.uncheck;

public class Deserializer {
  private static final Class[] language =
      new Class[] {
        Access.class,
        Bind.class,
        Block.class,
        Builtin.class,
        Call.class,
        LiteralString.class,
        Local.class
      };
  private final StatePrototype valuePrototype;
  private final TSMap<String, ClassInfo> languageNodeInfos = new TSMap<>();

  private Deserializer(ModuleId module) {
    valuePrototype =
        new BaseStatePrototype() {
          @Override
          public State createRecord(ModuleContext context, LuxemPath luxemPath, String type) {
            ClassInfo info = languageNodeInfos.getOpt(type);
            if (info == null) {
              context.errors.add(
                  Error.deserializeUnknownType(
                      luxemPath, type, new TSList<String>(languageNodeInfos.keys().toList())));
              return StateErrorNonPrimitive.state;
            }
            return new BaseNonPrimitiveState() {
              public final TSMap<String, State> values = new TSMap<>();

              @Override
              public void createdState(String key, State state) {
                values.put(key, state);
              }

              @Override
              public void eatKey(
                  ModuleContext context, TSList<State> stack, LuxemPath luxemPath, String name) {
                proto = info.fields.getOpt(name);
                if (proto == null) {
                  context.errors.add(
                      Error.deserializeUnknownField(
                          luxemPath, info.luxemType, name, info.argOrder));
                  ok = false;
                }
                super.eatKey(context, stack, luxemPath, name);
              }

              @Override
              public Object build(ModuleContext context) {
                if (!ok) return ErrorValue.error;
                Object[] args = new Object[info.argOrder.size()];
                for (int i = 0; i < args.length; ++i) {
                  String field = info.argOrder.get(i);
                  State state = values.getOpt(field);
                  Object value = state.build(context);
                  if (value == null) {
                    context.errors.add(
                        Error.deserializeMissingField(luxemPath, info.luxemType, field));
                    return ErrorValue.error;
                  }
                  args[i] = value;
                }
                return uncheck(() -> info.constructor.newInstance(args));
              }
            };
          }
        };

    StatePrototype intPrototype =
        new BaseStatePrototype() {
          @Override
          public State createPrimitive(ModuleContext context, LuxemPath luxemPath, String type) {
            return StateInt.state;
          }
        };
    StatePrototype stringPrototype =
        new BaseStatePrototype() {
          @Override
          public State createPrimitive(ModuleContext context, LuxemPath luxemPath, String type) {
            return new BasePrimitiveState() {
              private String value;

              @Override
              public void eatPrimitive(
                  ModuleContext context, TSList<State> stack, LuxemPath luxemPath, String value) {
                this.value = value;
                stack.removeLast();
              }

              @Override
              public Object build(ModuleContext context) {
                if (!ok || value == null) return null; // was not primitive, error
                return value;
              }
            };
          }
        };

    for (Class klass : language) {
      String type = toUnderscore(klass.getSimpleName());
      languageNodeInfos.put(type, new ClassInfo(type));
    }
    for (Class klass : language) {
      Constructor constructor = klass.getConstructors()[0];
      TSMap<String, StatePrototype> fields = new TSMap<>();
      TSList<String> argOrder = new TSList<>();
      for (Parameter parameter : constructor.getParameters()) {
        argOrder.add(parameter.getName());
        StatePrototype prototype;
        if (parameter.getType() == Location.class) {
          prototype = new LocationPrototype(module);
        } else if (parameter.getType() == Value.class) {
          prototype = valuePrototype;
        } else if (parameter.getType() == int.class) {
          prototype = intPrototype;
        } else if (parameter.getType() == String.class) {
          prototype = stringPrototype;
        } else if (parameter.getType() == ROList.class) {
          Type paramType =
              ((ParameterizedType) parameter.getParameterizedType()).getActualTypeArguments()[0];
          if (paramType == Value.class) {
            prototype = new ArrayStatePrototype(valuePrototype);
          } else throw new Assertion();
        } else throw new Assertion();
        fields.put(parameter.getName(), prototype);
      }
      String type = toUnderscore(klass.getSimpleName());
      ClassInfo prototype = (ClassInfo) languageNodeInfos.get(type);
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

  public static ROList<Value> deserialize(ModuleContext context, ModuleId module, Path path) {
    return new Deserializer(module).deserialize(context, path);
  }

  private ROList<Value> deserialize(ModuleContext context, Path path) {
    State languageState =
        new BaseNonPrimitiveState() {
          State inner;

          @Override
          public void createdState(String key, State state) {
            inner = state;
          }

          @Override
          public void eatType(
              ModuleContext context, TSList<State> stack, LuxemPath luxemPath, String version) {
            switch (version) {
              case "alligatoroid:0.0.1":
                {
                  proto = new ArrayStatePrototype(valuePrototype);
                  break;
                }
              default:
                {
                  context.errors.add(Error.deserializeUnknownLanguageVersion(luxemPath, version));
                  ok = false;
                  break;
                }
            }
          }

          @Override
          public Object build(ModuleContext context) {
            if (!ok) return ErrorValue.error;
            if (inner == null) {
              context.errors.add(Error.deserializeMissingVersion());
              return ErrorValue.error;
            }
            return inner.build(context);
          }
        };
    TSList<State> stack = new TSList<State>(languageState);
    // TODO luxem path
    BufferedReader reader =
        new BufferedReader() {
          LuxemPath luxemPath = new LuxemArrayPath(null);

          @Override
          protected void eatRecordBegin() {
            luxemPath = luxemPath.pushRecordOpen();
            stack.last().eatRecordBegin(context, stack, luxemPath);
          }

          @Override
          protected void eatArrayBegin() {
            luxemPath = luxemPath.pushArrayOpen();
            stack.last().eatArrayBegin(context, stack, luxemPath);
          }

          @Override
          protected void eatArrayEnd() {
            luxemPath = luxemPath.pop();
            stack.last().eatArrayEnd(context, stack, luxemPath);
          }

          @Override
          protected void eatRecordEnd() {
            luxemPath = luxemPath.pop();
            stack.last().eatRecordEnd(context, stack, luxemPath);
          }

          @Override
          protected void eatType(String value) {
            luxemPath = luxemPath.type();
            stack.last().eatType(context, stack, luxemPath, value);
          }

          @Override
          protected void eatKey(String value) {
            luxemPath = luxemPath.key(value);
            stack.last().eatKey(context, stack, luxemPath, value);
          }

          @Override
          protected void eatPrimitive(String value) {
            luxemPath = luxemPath.value();
            stack.last().eatPrimitive(context, stack, luxemPath, value);
          }
        };
    uncheck(
        () -> {
          try (InputStream stream = Files.newInputStream(path)) {
            reader.feed(stream);
          }
        });
    return (ROList<Value>) languageState.build(context);
  }
}
