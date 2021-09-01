package com.zarbosoft.alligatoroid.compiler;

import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;

public class ModuleContext {
  public final CompilationContext global;
  public final TSList<Error> errors = new TSList<>();
  public final TSList<String> log = new TSList<>(); // TODO
  public final TSList<Location> sourceMapReverse = new TSList<>();
  public final TSMap<Location, Integer> sourceMapForward = new TSMap<>();

  public ModuleContext(CompilationContext global) {
    this.global = global;
  }

  public final void builtinLog(String message) {
    log.add(message);
  }

  public int sourceLocation(Location location) {
    return sourceMapForward.getCreate(
        location,
        () -> {
          int out = sourceMapReverse.size();
          sourceMapReverse.add(location);
          return out;
        });
  }
}
