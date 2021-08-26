package com.zarbosoft.alligatoroid.compiler.jvm;

import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.EvaluateResult;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.TargetCode;
import com.zarbosoft.alligatoroid.compiler.Value;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMRWSharedCode;
import com.zarbosoft.alligatoroid.compiler.mortar.NullValue;
import com.zarbosoft.alligatoroid.compiler.mortar.SimpleValue;
import org.objectweb.asm.tree.MethodInsnNode;

import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public class JVMMethodField implements SimpleValue {
  private final JVMProtocode lower;
  private final JVMShallowMethodFieldType type;

  public JVMMethodField(JVMProtocode lower, JVMShallowMethodFieldType type) {
    this.lower = lower;
    this.type = type;
  }

  @Override
  public TargetCode drop(Context context, Location location) {
    return lower.drop(context, location);
  }

  @Override
  public EvaluateResult call(Context context, Location location, Value argument) {
    JVMRWSharedCode code = new JVMCode().add(lower.lower());
    JVMTargetModuleContext.convertFunctionArgument(context, code, argument);
    code.line(context.module.sourceLocation(location))
        .add(
            new MethodInsnNode(
                INVOKEVIRTUAL, type.base.jvmInternalClass, type.name, type.jvmDesc, false));
    if (type.returnType == null) return new EvaluateResult(code, null, NullValue.value);
    else return EvaluateResult.pure(type.returnType.stackAsValue((JVMCode) code));
  }
}
