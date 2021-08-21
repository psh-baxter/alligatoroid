package com.zarbosoft.alligatoroid.compiler.jvmshared;

import com.zarbosoft.alligatoroid.compiler.TargetCode;
import com.zarbosoft.rendaw.common.TSList;
import org.objectweb.asm.MethodVisitor;

public abstract class JVMSharedCode implements TargetCode {
  public void render(MethodVisitor out, TSList<Object> initialIndexes) {
    render(new Scope(initialIndexes), out);
  }

  protected abstract void render(Scope scope, MethodVisitor out);

  protected static class Scope {
    final Scope parent;
    TSList<Object> indexes;

    public Scope(TSList<Object> indexes) {
      parent = null;
      this.indexes = indexes.mut();
    }

    public Scope(Scope parent) {
      this.parent = parent;
      if (parent == null) indexes = new TSList<>();
      else indexes = parent.indexes.mut();
    }
  }
}
