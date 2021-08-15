package com.zarbosoft.alligatoroid.compiler;

import com.zarbosoft.alligatoroid.compiler.deserialize.Deserializer;
import com.zarbosoft.alligatoroid.compiler.jvmshared.DynamicClassLoader;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMCode;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMDescriptor;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMRWCode;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMSharedClass;
import com.zarbosoft.alligatoroid.compiler.language.Block;
import com.zarbosoft.alligatoroid.compiler.mortar.MortarCode;
import com.zarbosoft.alligatoroid.compiler.mortar.NullValue;
import com.zarbosoft.rendaw.common.Format;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import static com.zarbosoft.alligatoroid.compiler.mortar.MortarCode.MORTAR_TARGET_NAME;
import static com.zarbosoft.rendaw.common.Common.uncheck;
import static org.objectweb.asm.Opcodes.RETURN;

public class Module {
  public static final String METHOD_NAME = "enter";
  public static final String METHOD_DESCRIPTOR = JVMDescriptor.func(JVMDescriptor.void_());
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
    // Do first pass flat evaluation
    Context context =
        new Context(
            moduleContext,
            new Target() {
              @Override
              public TargetCode mergeScoped(
                  Context context, Location location, Iterable<TargetCode> chunks) {
                JVMCode inner = (JVMCode) merge(context, location, chunks);
                return new MortarCode().addScoped(inner);
              }

              @Override
              public TargetCode merge(
                  Context context, Location location, Iterable<TargetCode> chunks) {
                JVMRWCode code = new MortarCode();
                for (TargetCode chunk : chunks) {
                  if (chunk == null) continue;
                  if (!(chunk instanceof MortarCode)) {
                    context.module.errors.add(
                        Error.incompatibleTargetValues(
                            location, MORTAR_TARGET_NAME, chunk.targetName()));
                    return null;
                  }
                  code.add((MortarCode) chunk);
                }
                return code;
              }
            },
            new Scope(null));
    ROList<Value> rootStatements = Deserializer.deserialize(moduleContext, module, module.path);
    EvaluateResult.Context ectx = new EvaluateResult.Context(context, null);
    ectx.record(ectx.record(Block.evaluate(context, null, rootStatements)).drop(context, null));
    MortarCode code = (MortarCode) ectx.build(null).sideEffect;
    if (moduleContext.errors.some()) {
      return ErrorValue.error;
    }

    // Do 2nd pass jvm evaluation
    String className = Format.format("com.zarbosoft.alligatoroidmortar.Generated%s", uniqueClass++);
    JVMSharedClass preClass = new JVMSharedClass(className);
    preClass.defineFunction(
        METHOD_NAME, METHOD_DESCRIPTOR, new MortarCode().add(code).add(RETURN), new TSList<>());
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
