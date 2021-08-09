package com.zarbosoft.alligatoroid.compiler.mortar;

import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.OkValue;
import com.zarbosoft.alligatoroid.compiler.Value;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMRWCode;
import org.objectweb.asm.tree.MethodInsnNode;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

public class Function extends MortarValue {
  private final String name;
  private final String jbcDesc;
  private final String jbcInternalClass;
  private final Value returnType;

  public Function(String jbcInternalClass, String name, String jbcDesc, Value returnType) {
    this.name = name;
    this.jbcDesc = jbcDesc;
    this.jbcInternalClass = jbcInternalClass;
    this.returnType = returnType;
  }

  @Override
  public Value call(Context context, Location location,Value argument) {
    return new MortarTargetValue(location,
        new JVMRWCode()
            .add(((LowerableValue) argument).lower())
            .add(new MethodInsnNode(INVOKESTATIC, jbcInternalClass, name, jbcDesc, false)),
            returnType);
  }
}
