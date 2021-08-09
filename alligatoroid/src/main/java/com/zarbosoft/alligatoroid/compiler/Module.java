package com.zarbosoft.alligatoroid.compiler;

import com.zarbosoft.alligatoroid.compiler.deserialize.Deserializer;
import com.zarbosoft.alligatoroid.compiler.jvmshared.DynamicClassLoader;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMDescriptor;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMRWCode;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMSharedClass;
import com.zarbosoft.alligatoroid.compiler.language.Block;
import com.zarbosoft.alligatoroid.compiler.mortar.LowerableValue;
import com.zarbosoft.alligatoroid.compiler.mortar.NullValue;
import com.zarbosoft.rendaw.common.Format;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import static com.zarbosoft.rendaw.common.Common.uncheck;
import static org.objectweb.asm.Opcodes.RETURN;

public class Module {
  public static final String METHOD_NAME = "enter";
  public static final String METHOD_DESCRIPTOR = JVMDescriptor.func(JVMDescriptor.void_());
  public static long uniqueClass = 0;
  public final ModuleId id;
  public final Context context;
  public final CompletableFuture<Value> result;

  public Module(ModuleId id, Context context, CompletableFuture<Value> result) {
    this.id = id;
    this.context = context;
    this.result = result;
  }

  private static Value loadRootInner(LocalModuleId module, Context context) {
    ROList<Value> rootStatements = Deserializer.deserialize(context, module, module.path);
    Value value = Block.evaluate(context, rootStatements);
    if (context.errors.some()) {
      return ErrorValue.error;
    }
    String className = Format.format("com.zarbosoft.alligatoroidmortar.Generated%s", uniqueClass++);
    JVMSharedClass preClass = new JVMSharedClass(className);
    value = value.drop(context);
    if (value == ErrorValue.error) return ErrorValue.error;
    preClass.defineFunction(
        METHOD_NAME,
        METHOD_DESCRIPTOR,
        new JVMRWCode().add(((LowerableValue) value).lower()).add(RETURN),
        new TSList<>());
    Class klass =
        DynamicClassLoader.loadTree(
            className, new TSMap<String, byte[]>().put(className, preClass.render()));
    uncheck(() -> klass.getMethod(METHOD_NAME).invoke(null));
    return NullValue.value;
  }

  public static Future<Value> loadRoot(GlobalContext globalContext, Path path) {
    LocalModuleId id = new LocalModuleId(path);
    return globalContext.requestModule(
        id,
        module -> {
          try {
            module.result.complete(loadRootInner(id, module.context));
          } catch (Throwable e) {
            module.context.errors.add(Error.unexpected(e));
            module.result.complete(ErrorValue.error);
          }
        });
  }
}
