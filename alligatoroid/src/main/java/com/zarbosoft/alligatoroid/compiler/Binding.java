package com.zarbosoft.alligatoroid.compiler;

public interface Binding {
    /**
     * Forks a bound value (part remains bound, returned value is temporary/on-stack)
     * @param context
     * @param location
     * @return
     */
    EvaluateResult fork(Context context, Location location);

    TargetCode drop(Context context, Location location);
}
