package com.zarbosoft.alligatoroid.compiler.mortar;

import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.EvaluateResult;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.Value;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMRWSharedCode;
import org.objectweb.asm.tree.MethodInsnNode;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

public class Function implements SimpleValue {
  private final String name;
  private final String jbcDesc;
  private final String jbcInternalClass;
  private final MortarHalfType returnType;

  public Function(String jbcInternalClass, String name, String jbcDesc, MortarHalfType returnType) {
    this.name = name;
    this.jbcDesc = jbcDesc;
    this.jbcInternalClass = jbcInternalClass;
    this.returnType = returnType;
  }

  @Override
  public EvaluateResult call(Context context, Location location, Value argument) {
    JVMRWSharedCode code =
        new MortarCode()
            .add(((MortarLowerableValue) argument).lower())
            .line(context.module.sourceLocation(location))
            .add(new MethodInsnNode(INVOKESTATIC, jbcInternalClass, name, jbcDesc, false));
    if (returnType == null) return new EvaluateResult(code, NullValue.value);
    else return EvaluateResult.pure(returnType.stackAsValue(code));
  }
}
