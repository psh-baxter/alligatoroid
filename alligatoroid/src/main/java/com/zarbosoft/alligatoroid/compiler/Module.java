package com.zarbosoft.alligatoroid.compiler;

import com.zarbosoft.alligatoroid.compiler.deserialize.Deserializer;
import com.zarbosoft.alligatoroid.compiler.jvm.MultiError;
import com.zarbosoft.alligatoroid.compiler.jvmshared.DynamicClassLoader;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMDescriptor;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMSharedClass;
import com.zarbosoft.alligatoroid.compiler.language.Block;
import com.zarbosoft.alligatoroid.compiler.mortar.MortarCode;
import com.zarbosoft.alligatoroid.compiler.mortar.MortarTargetModuleContext;
import com.zarbosoft.alligatoroid.compiler.mortar.NullValue;
import com.zarbosoft.rendaw.common.Common;
import com.zarbosoft.rendaw.common.Format;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import static com.zarbosoft.rendaw.common.Common.uncheck;
import static org.objectweb.asm.Opcodes.RETURN;

public class Module {
  public static final String METHOD_NAME = "enter";
  public static final String METHOD_DESCRIPTOR = JVMDescriptor.func(JVMDescriptor.voidDescriptor());
  public static long uniqueClass = 0;
  public final ModuleId id;
  public final ModuleContext context;
  public final CompletableFuture<Value> result;

  public Module(ModuleId id, ModuleContext context, CompletableFuture<Value> result) {
    this.id = id;
    this.context = context;
    this.result = result;
  }

  private static Value loadRootInner(LocalModuleId module, ModuleContext moduleContext) {
    String className = Format.format("com.zarbosoft.alligatoroidmortar.Generated%s", uniqueClass++);

    // Do first pass flat evaluation
    MortarTargetModuleContext targetContext =
        new MortarTargetModuleContext(JVMDescriptor.jvmName(className));
    Context context = new Context(moduleContext, targetContext, new Scope(null));
    ROList<Value> rootStatements = Deserializer.deserialize(moduleContext, module, module.path);
    EvaluateResult.Context ectx = new EvaluateResult.Context(context, null);
    ectx.recordPre(
        ectx.record(
                new com.zarbosoft.alligatoroid.compiler.language.Scope(
                        null, new Block(null, rootStatements))
                    .evaluate(context))
            .drop(context, null));
    EvaluateResult evaluateResult = ectx.build(null);
    MortarCode code =
        (MortarCode)
            targetContext.merge(
                context, null, new TSList<>(evaluateResult.preEffect, evaluateResult.postEffect));
    if (moduleContext.errors.some()) {
      return ErrorValue.error;
    }

    // Do 2nd pass jvm evaluation
    JVMSharedClass preClass = new JVMSharedClass(className);
    for (ROPair<Object, String> e : Common.iterable(targetContext.transfers.iterator())) {
      preClass.defineStaticField(e.second, e.first.getClass());
    }
    preClass.defineFunction(
        METHOD_NAME, METHOD_DESCRIPTOR, new MortarCode().add(code).add(RETURN), new TSList<>());
    Class klass =
        DynamicClassLoader.loadTree(
            className, new TSMap<String, byte[]>().put(className, preClass.render()));
    for (ROPair<Object, String> e : Common.iterable(targetContext.transfers.iterator())) {
      uncheck(() -> klass.getDeclaredField(e.second).set(null, e.first));
    }
    uncheck(() -> klass.getMethod(METHOD_NAME).invoke(null));
    return NullValue.value;
  }

  private static void processError(Module module, Throwable e) {
    if (e instanceof Common.UncheckedException) {
      processError(module, e.getCause());
    } else if (e instanceof InvocationTargetException) {
      processError(module,e.getCause());
    } else if (e instanceof MultiError) {
      module.context.errors.addAll(((MultiError)e).errors);
    } else {
      module.context.errors.add(Error.unexpected(e));
    }
  }

  public static Future<Value> loadRoot(GlobalContext globalContext, Path path) {
    LocalModuleId id = new LocalModuleId(path);
    return globalContext.requestModule(
        id,
        module -> {
          try {
            module.result.complete(loadRootInner(id, module.context));
          } catch (Throwable e) {
            processError(module, e);
            module.result.complete(ErrorValue.error);
          }
        });
  }
}
