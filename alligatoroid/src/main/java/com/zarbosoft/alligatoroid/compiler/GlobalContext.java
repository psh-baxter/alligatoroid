package com.zarbosoft.alligatoroid.compiler;

import com.zarbosoft.rendaw.common.TSMap;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.zarbosoft.rendaw.common.Common.uncheck;

public class GlobalContext {
  public final ExecutorService executor = Executors.newWorkStealingPool();
  private final TSMap<ModuleId, Module> modules = new TSMap<>();
  private final ConcurrentLinkedDeque<Future> newPending = new ConcurrentLinkedDeque<>();

  public synchronized Future<Value> requestModule(ModuleId id, Consumer<Module> execute) {
    Module module = modules.getOpt(id);
    if (module == null) {
      CompletableFuture<Value> result = new CompletableFuture<>();
      module = new Module(id, new Context(this), result);
      modules.put(id, module);
      Module finalModule = module;
      executor.submit(() -> execute.accept(finalModule));
      newPending.add(module.result);
    }
    return module.result;
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
}
