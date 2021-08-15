package com.zarbosoft.alligatoroid.compiler;

import com.zarbosoft.luxem.write.Writer;
import com.zarbosoft.rendaw.common.TSMap;

import java.nio.file.Paths;

public class Main {
  public static void main(String[] args) {
    if (args.length != 1) {
      throw new RuntimeException("Need one argument, path to root module");
    }
    GlobalContext globalContext = new GlobalContext();
    TSMap<ModuleId, Module> modules;
    try {
      Module.loadRoot(globalContext, Paths.get(args[0]));
    } finally {
      modules = globalContext.join();
    }

    Writer outWriter = new Writer(System.out, (byte) ' ', 4);
    outWriter.recordBegin();

    outWriter.key("modules").arrayBegin(); // TODO complex luxem keys
    for (Module value : modules.values()) {
      outWriter.recordBegin().key("id");
      value.id.serialize(outWriter);
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
