package com.zarbosoft.alligatoroid.compiler;

import com.zarbosoft.rendaw.common.TSList;

public class EvaluateResult {
  public static final EvaluateResult error = new EvaluateResult(null, ErrorValue.error);
  /** TargetCode or null if no effect */
  public final TargetCode sideEffect;

  public final Value value;

  public EvaluateResult(TargetCode sideEffect, Value value) {
    this.sideEffect = sideEffect;
    this.value = value;
  }

  public static EvaluateResult pure(Value value) {
    return new EvaluateResult(null, value);
  }

  public static class Context {
    public final TSList<TargetCode> sideEffect = new TSList<>();
    public final com.zarbosoft.alligatoroid.compiler.Context context;
    private final Location location;

    public Context(com.zarbosoft.alligatoroid.compiler.Context context, Location location) {
      this.context = context;
      this.location = location;
    }

    public Value evaluate(Value value) {
      return record(value.evaluate(context));
    }

    public void record(TargetCode sideEffect) {
      this.sideEffect.add(sideEffect);
    }

    public Value record(EvaluateResult res) {
      sideEffect.add(res.sideEffect);
      return res.value;
    }

    public EvaluateResult build(Value value) {
      return new EvaluateResult(context.target.merge(context, location, sideEffect), value);
    }
  }
}
