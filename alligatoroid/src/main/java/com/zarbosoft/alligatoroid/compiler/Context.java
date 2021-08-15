package com.zarbosoft.alligatoroid.compiler;

public class Context {
    public final ModuleContext module;
    public final TargetModuleContext target;
    public final Scope scope;

    public Context(ModuleContext module, TargetModuleContext target, Scope scope) {
        this.module = module;
        this.target = target;
        this.scope = scope;
    }

    public Context pushScope() {
        return new Context(module, target, new Scope(scope));
    }
}
