package com.zarbosoft.alligatoroid.compiler;

import com.zarbosoft.alligatoroid.compiler.cache.CacheSerializer;
import com.zarbosoft.alligatoroid.compiler.cache.ModuleIdDeserializer;
import com.zarbosoft.alligatoroid.compiler.jvm.MultiError;
import com.zarbosoft.alligatoroid.compiler.jvmshared.DynamicClassLoader;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMDescriptor;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMSharedClass;
import com.zarbosoft.alligatoroid.compiler.language.Block;
import com.zarbosoft.alligatoroid.compiler.mortar.MortarCode;
import com.zarbosoft.alligatoroid.compiler.mortar.MortarTargetModuleContext;
import com.zarbosoft.alligatoroid.compiler.mortar.NullValue;
import com.zarbosoft.alligatoroid.compiler.sourcedeserialize.SourceDeserializer;
import com.zarbosoft.luxem.write.Writer;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.Common;
import com.zarbosoft.rendaw.common.Format;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROMap;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;

import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.zarbosoft.rendaw.common.Common.uncheck;
import static org.objectweb.asm.Opcodes.RETURN;

public class CompilationContext {
  public static final ROMap<String, Object> builtinMap;
  public static final ROMap<Object, String> builtinMapReverse;
  public static final String METHOD_NAME = "enter";
  public static final String METHOD_DESCRIPTOR = JVMDescriptor.func(JVMDescriptor.voidDescriptor());
  public static final String CACHE_ID_FILENAME = "id.luxem";
  public static final String CACHE_OUTPUT_FILENAME = "output.luxem";
  public static final String CACHE_OBJECTS_DIRECTORY = "objects";
  public static final ModuleIdDeserializer moduleIdDeserializer = new ModuleIdDeserializer();
  public static long uniqueClass = 0;
  public final Path cachePath;
  public final ExecutorService executor = Executors.newWorkStealingPool();
  public final Object cacheLock = new Object();
  /**
   * cacheLock
   *
   * <p>Maps cache paths to deserialized cached objects
   */
  public final TSMap<String, Object> loadedCache = new TSMap<>();
  /**
   * cacheLock
   *
   * <p>Maps de/serialized cacheable objects to cache paths
   */
  public final TSMap<Object, String> loadedCacheReverse = new TSMap<>();
  /**
   * cacheLock
   *
   * <p>Maps mortar-defined classes to cache paths
   */
  public final TSMap<Class, String> typeSpcs = new TSMap<>();

  public final Object cacheDirLock = new Object();
  public final Object modulesLock = new Object();
  private final TSMap<ModuleId, Module> modules = new TSMap<>();
  private final ConcurrentLinkedDeque<Future> newPending = new ConcurrentLinkedDeque<>();

  private final TSMap<String, Object> cache = new TSMap<>();

  public CompilationContext(Path cachePath) {
    this.cachePath = cachePath;
  }

  private static void processError(Module module, Throwable e) {
    if (e instanceof Common.UncheckedException) {
      processError(module, e.getCause());
    } else if (e instanceof InvocationTargetException) {
      processError(module, e.getCause());
    } else if (e instanceof MultiError) {
      module.context.errors.addAll(((MultiError) e).errors);
    } else {
      module.context.errors.add(Error.unexpected(e));
    }
  }

  public Path nextObjectCachePath(Path moduleCachePath, boolean binary) {
    Path objectsDir = moduleCachePath.resolve("objects");
    uncheck(() -> Files.createDirectories(objectsDir));
    for (int i = 0; i < Integer.MAX_VALUE; ++i) {
      Path luxemPath = objectsDir.resolve(Format.format("%s.luxem"));
      if (Files.exists(luxemPath)) continue;
      Path binaryPath = objectsDir.resolve(Format.format("%s.bin"));
      if (Files.exists(binaryPath)) continue;
      if (binary) return binaryPath;
      else return luxemPath;
    }
  }

  /**
   * Finds an existing or creates a new cache directory for a module.
   *
   * @param id
   * @return
   */
  public Path ensureCachePath(TSList<Error> errors, ModuleId id) {
    synchronized (cacheDirLock) {
      String hash = id.hash();
      Path tryCacheModulePath = cachePath.resolve("modules");
      int cuts = 3;
      for (int i = 0; i < cuts; ++i) {
        tryCacheModulePath =
            tryCacheModulePath.resolve(
                hash.substring(i * 2, i == cuts - 1 ? hash.length() : (i + 1) * 2));
      }
      Path useCacheModulePath0 = null;
      for (int i = 0; i < 1000; ++i) {
        tryCacheModulePath =
            tryCacheModulePath.resolve(Format.format("%s-%s", hash.substring(cuts * 2), i));
        ModuleId tryId =
            moduleIdDeserializer.deserialize(errors, tryCacheModulePath.resolve("id.luxem"));
        if (tryId != null && tryId.equal1(id)) {
          useCacheModulePath0 = tryCacheModulePath;
          break;
        }
      }
      if (useCacheModulePath0 == null)
        throw new Assertion(); // Something's probably wrong with the hashing code if this is
      // reached
      Path useCacheModulePath = useCacheModulePath0;
      uncheck(
          () -> {
            Files.createDirectories(useCacheModulePath);
            try (OutputStream s =
                Files.newOutputStream(useCacheModulePath.resolve(CACHE_ID_FILENAME))) {
              Writer writer = new Writer(s, (byte) ' ', 4);
              id.serialize(writer);
            }
          });
      return useCacheModulePath;
    }
  }

  public Future<Value> loadLocalModule(Path path) {
    if (!path.isAbsolute()) throw new Assertion();
    LocalModuleId moduleId = new LocalModuleId(path);
    return loadModule(
        moduleId,
        new LoadModuleInner() {
          @Override
          public Value load(Module module) {
            Path cachePath = ensureCachePath(module.context.errors, moduleId);
            Value out = null;
            try {
              out = loadCacheValue(cachePath.resolve("output.luxem"));
            } catch (Exception e) {
              module.context.log.add(
                  Format.format("Error loading existing build output of %s: %s", cachePath, e));
            }
            if (out != null) {
              return out;
            }
            Utils.recursiveDelete(cachePath.resolve(CACHE_OUTPUT_FILENAME));
            Utils.recursiveDelete(cachePath.resolve(CACHE_OBJECTS_DIRECTORY));

            String className =
                Format.format("com.zarbosoft.alligatoroidmortar.Generated%s", uniqueClass++);

            // Do first pass flat evaluation
            MortarTargetModuleContext targetContext =
                new MortarTargetModuleContext(JVMDescriptor.jvmName(className));
            Context context = new Context(module.context, targetContext, new Scope(null));
            ROList<Value> rootStatements =
                new SourceDeserializer(moduleId).deserialize(module.context.errors, moduleId.path);
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
                        context,
                        null,
                        new TSList<>(evaluateResult.preEffect, evaluateResult.postEffect));
            if (module.context.errors.some()) {
              return ErrorValue.error;
            }

            // Do 2nd pass jvm evaluation
            JVMSharedClass preClass = new JVMSharedClass(className);
            for (ROPair<Object, String> e : Common.iterable(targetContext.transfers.iterator())) {
              preClass.defineStaticField(e.second, e.first.getClass());
            }
            preClass.defineFunction(
                METHOD_NAME,
                METHOD_DESCRIPTOR,
                new MortarCode().add(code).add(RETURN),
                new TSList<>());
            Class klass =
                DynamicClassLoader.loadTree(
                    className, new TSMap<String, byte[]>().put(className, preClass.render()));
            for (ROPair<Object, String> e : Common.iterable(targetContext.transfers.iterator())) {
              uncheck(() -> klass.getDeclaredField(e.second).set(null, e.first));
            }
            uncheck(() -> klass.getMethod(METHOD_NAME).invoke(null));

            out = NullValue.value;

            Value finalOut = out;
            uncheck(
                () -> {
                  try (OutputStream stream =
                      Files.newOutputStream(cachePath.resolve("output.luxem"))) {
                    Writer writer = new Writer(stream, (byte) ' ', 4);
                    new CacheSerializer()
                        .serializeSubValue(CompilationContext.this, writer, finalOut);
                  }
                });
            return out;
          }
        });
  }

  private Future<Value> loadModule(ModuleId id, LoadModuleInner inner) {
    synchronized (modulesLock) {
      Module module = modules.getOpt(id);
      if (module == null) {
        CompletableFuture<Value> result = new CompletableFuture<>();
        module = new Module(id, new ModuleContext(this), result);
        modules.put(id, module);
        Module finalModule = module;
        executor.submit(
            () -> {
              try {
                Value value = inner.load(finalModule);
                finalModule.result.complete(value);
              } catch (Throwable e) {
                processError(finalModule, e);
                finalModule.result.complete(ErrorValue.error);
              }
            });
        newPending.add(module.result);
      }
      return module.result;
    }
  }

  public TSMap<ModuleId, Module> join() {
    uncheck(
        () -> {
          while (true) {
            Future got = newPending.pollLast();
            if (got == null) break;
            got.get();
          }
          executor.shutdown();
          executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        });
    return modules;
  }

  @FunctionalInterface
  interface LoadModuleInner {
    Value load(Module module);
  }
}
