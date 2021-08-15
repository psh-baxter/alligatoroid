package com.zarbosoft.alligatoroid.compiler.mortar;

import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.EvaluateResult;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.ModuleContext;
import com.zarbosoft.alligatoroid.compiler.Value;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMDescriptor;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMRWCode;
import org.objectweb.asm.tree.MethodInsnNode;

import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public class BuiltinModuleFunction implements MortarValue {
  public static final String jbcInternalClass = JVMDescriptor.internalName(ModuleContext.class);
  private final String name;
  private final String jbcDesc;
  private final MortarHalfType returnType;

  public BuiltinModuleFunction(String name, String jbcDesc, MortarHalfType returnType) {
    this.name = name;
    this.jbcDesc = jbcDesc;
    this.returnType = returnType;
  }

  @Override
  public EvaluateResult call(Context context, Location location, Value argument) {
    JVMRWCode code =
        new MortarCode()
            .add(((MortarTargetModuleContext) context.target).transfer(context.module))
            .add(((MortarLowerableValue) argument).lower())
            .line(context.module.sourceLocation(location))
            .add(new MethodInsnNode(INVOKEVIRTUAL, jbcInternalClass, name, jbcDesc, false));
    if (returnType == null) return new EvaluateResult(code, NullValue.value);
    else return EvaluateResult.pure(returnType.stackAsValue(code));
  }
}
