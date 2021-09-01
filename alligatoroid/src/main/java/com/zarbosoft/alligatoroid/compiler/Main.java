package com.zarbosoft.alligatoroid.compiler;

import com.zarbosoft.appdirsj.AppDirs;
import com.zarbosoft.luxem.write.Writer;
import com.zarbosoft.rendaw.common.TSMap;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
  public static final AppDirs appDirs =
      new AppDirs().set_appname("alligatoroid").set_appauthor("zarbosoft");

  public static void main(String[] args) {
    if (args.length != 1) {
      throw new RuntimeException("Need one argument, path to root module");
    }
    Path cachePath;
    String cachePath0 = System.getenv("ALLIGATOROID_CACHE");
    if (cachePath0.isEmpty()) {
      cachePath = appDirs.user_cache_dir(false);
    } else {
      cachePath = Paths.get(cachePath0);
    }
    CompilationContext compilationContext = new CompilationContext(cachePath);
    TSMap<ModuleId, Module> modules;
    try {
      compilationContext.loadModule(new LocalModuleId(Paths.get(args[0]))); // TODO handle result
    } finally {
      modules = compilationContext.join();
    }

    Writer outWriter = new Writer(System.out, (byte) ' ', 4);
    outWriter.recordBegin();

    outWriter.key("modules").arrayBegin(); // TODO complex luxem keys
    for (Module value : modules.values()) {
      outWriter.recordBegin().key("id");
      value.id.serialize(outWriter);

      outWriter.key("log");
      outWriter.arrayBegin();
      for (String message : value.context.log) {
        outWriter.primitive(message);
      }
      outWriter.arrayEnd();

      outWriter.key("errors");
      outWriter.arrayBegin();
      for (Error error : value.context.errors) {
        error.serialize(outWriter);
      }
      outWriter.arrayEnd();

      outWriter.recordEnd();
    }
    outWriter.arrayEnd();

    // TODO output value

    outWriter.recordEnd();
    System.out.flush();
  }
}
