package com.zarbosoft.alligatoroid.compiler.jvm;

import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.EvaluateResult;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.TargetCode;
import com.zarbosoft.alligatoroid.compiler.Value;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMDescriptor;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMSharedCode;
import com.zarbosoft.alligatoroid.compiler.mortar.SimpleValue;
import org.objectweb.asm.tree.FieldInsnNode;

import static org.objectweb.asm.Opcodes.GETSTATIC;

public class JVMExternStaticField implements SimpleValue {
  public final String jvmParentInternalClass;
  public final String name;
  public final JVMDataType type;

  public JVMExternStaticField(String jvmExternalClass, String fieldName, JVMDataType spec) {
    this.jvmParentInternalClass = JVMDescriptor.jvmName(jvmExternalClass);
    this.name = fieldName;
    this.type = spec;
  }

  @Override
  public EvaluateResult access(Context context, Location location, Value field0) {
    return type.valueAccess(
        context,
        location,
        field0,
        new JVMProtocode() {
          @Override
          public TargetCode drop(Context context, Location location) {
            return null;
          }

          @Override
          public JVMSharedCode lower() {
            return new JVMCode()
                .add(new FieldInsnNode(GETSTATIC, jvmParentInternalClass, name, type.jvmDesc()));
          }
        });
  }
}
