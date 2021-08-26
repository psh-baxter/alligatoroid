package com.zarbosoft.alligatoroid.compiler.mortar;

import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.EvaluateResult;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.ModuleContext;
import com.zarbosoft.alligatoroid.compiler.Value;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMDescriptor;
import org.objectweb.asm.tree.MethodInsnNode;

import static com.zarbosoft.alligatoroid.compiler.mortar.MortarTargetModuleContext.convertFunctionArgument;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public class BuiltinModuleFunction implements SimpleValue {
  public static final String jbcInternalClass = JVMDescriptor.jvmName(ModuleContext.class);
  private final String name;
  private final String jbcDesc;
  private final MortarHalfDataType returnType;

  public BuiltinModuleFunction(String name, String jbcDesc, MortarHalfDataType returnType) {
    this.name = name;
    this.jbcDesc = jbcDesc;
    this.returnType = returnType;
  }

  @Override
  public EvaluateResult call(Context context, Location location, Value argument) {
    MortarCode code = new MortarCode();
    code.add(((MortarTargetModuleContext) context.target).transfer(context.module));
    convertFunctionArgument(context, code, argument);
    code.line(context.module.sourceLocation(location))
        .add(new MethodInsnNode(INVOKEVIRTUAL, jbcInternalClass, name, jbcDesc, false));
    if (returnType == null) return new EvaluateResult(code, null, NullValue.value);
    else return EvaluateResult.pure(returnType.stackAsValue(code));
  }
}
