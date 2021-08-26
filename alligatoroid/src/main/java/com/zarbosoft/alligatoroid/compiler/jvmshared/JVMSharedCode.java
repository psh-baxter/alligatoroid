package com.zarbosoft.alligatoroid.compiler.jvmshared;

import com.zarbosoft.alligatoroid.compiler.TargetCode;
import com.zarbosoft.rendaw.common.TSList;
import org.objectweb.asm.MethodVisitor;

public abstract class JVMSharedCode implements TargetCode {
  public abstract void render(MethodVisitor out, TSList<Object> initialIndexes);
}
