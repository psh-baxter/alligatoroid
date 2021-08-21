package com.zarbosoft.alligatoroid.compiler.mortar;

import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.TargetCode;
import com.zarbosoft.alligatoroid.compiler.Value;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMSharedCode;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMRWSharedCode;

import static org.objectweb.asm.Opcodes.POP;

public class MortarHalfStringType implements MortarHalfType {
  @Override
  public Value asValue(MortarProtocode lower) {
    return new MortarHalfString(lower);
  }

  @Override
  public Value stackAsValue(JVMRWSharedCode code) {
    return new MortarHalfString(
        new MortarProtocode() {
          @Override
          public JVMSharedCode lower() {
            return code;
          }

          @Override
          public TargetCode drop(Context context, Location location) {
            return new MortarCode().add(POP);
          }
        });
  }
}
