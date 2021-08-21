package com.zarbosoft.alligatoroid.compiler.mortar;

import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.EvaluateResult;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.TargetCode;
import com.zarbosoft.alligatoroid.compiler.Value;
import org.objectweb.asm.tree.MethodInsnNode;

import static org.objectweb.asm.Opcodes.INVOKESPECIAL;

public class MortarMethodField implements SimpleValue {
  private final MortarProtocode lower;
  private final MortarMethodFieldType type;

  public MortarMethodField(MortarProtocode lower, MortarMethodFieldType type) {
    this.lower = lower;
    this.type = type;
  }

  @Override
  public TargetCode drop(Context context, Location location) {
    return lower.drop(context, location);
  }

  @Override
  public EvaluateResult call(Context context, Location location, Value argument) {
    return EvaluateResult.pure(
        type.returnType.stackAsValue(
            new MortarCode()
                .add(lower.lower())
                .add(((MortarLowerableValue) argument).lower())
                .line(context.module.sourceLocation(location))
                .add(
                    new MethodInsnNode(
                        INVOKESPECIAL,
                        type.base.jvmInternalClass,
                        type.name,
                        type.jbcDesc,
                        false))));
  }
}
