package com.zarbosoft.alligatoroid.compiler.jvm;

import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.Error;
import com.zarbosoft.alligatoroid.compiler.EvaluateResult;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.TargetCode;
import com.zarbosoft.alligatoroid.compiler.Value;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMDescriptor;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMSharedCode;
import com.zarbosoft.alligatoroid.compiler.mortar.SimpleValue;
import com.zarbosoft.alligatoroid.compiler.mortar.WholeValue;
import org.objectweb.asm.tree.FieldInsnNode;

import static org.objectweb.asm.Opcodes.GETSTATIC;

public class JVMExternStaticField implements SimpleValue {
  public final String jvmParentInternalClass;
  public final String name;
  public final JVMDataType type;

  public JVMExternStaticField(String jvmExternalClass, String fieldName, JVMDataType spec) {
    this.jvmParentInternalClass = JVMDescriptor.internalName(jvmExternalClass);
    this.name = fieldName;
    this.type = spec;
  }

  @Override
  public EvaluateResult access(Context context, Location location, Value field0) {
    WholeValue key = WholeValue.getWhole(context, location, field0);
    JVMType field = type.getField(key.concreteValue());
    if (field == null) {
      context.module.errors.add(Error.noField(location, key));
      return EvaluateResult.error;
    }
    EvaluateResult pure =
        EvaluateResult.pure(
            field.asValue(
                new JVMProtocode() {
                  @Override
                  public TargetCode drop(Context context, Location location) {
                    return null;
                  }

                  @Override
                  public JVMSharedCode lower() {
                    return new JVMCode()
                        .add(
                            new FieldInsnNode(
                                GETSTATIC, jvmParentInternalClass, name, type.jvmDesc()));
                  }
                }));
    return pure;
  }
}
