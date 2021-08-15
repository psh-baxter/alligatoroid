package com.zarbosoft.alligatoroid.compiler;

public class Context {
    public final ModuleContext module;
    public final Target target;
    public final Scope scope;

    public Context(ModuleContext module, Target target, Scope scope) {
        this.module = module;
        this.target = target;
        this.scope = scope;
    }

    public Context pushScope() {
        return new Context(module, target, new Scope(scope));
    }
}
