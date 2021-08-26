package com.zarbosoft.alligatoroid.compiler.mortar;

import com.zarbosoft.alligatoroid.compiler.Binding;
import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.EvaluateResult;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.TargetCode;
import com.zarbosoft.alligatoroid.compiler.Value;
import com.zarbosoft.rendaw.common.ROPair;

import static org.objectweb.asm.Opcodes.POP;

public interface MortarHalfDataType extends MortarHalfType {
  default Value asValue(MortarProtocode lower) {
    return new MortarHalfValue(this, lower);
  }

  default Value stackAsValue(MortarCode code) {
    return new MortarHalfValue(
        this,
        new MortarProtocode() {
          @Override
          public MortarCode lower() {
            return code;
          }

          @Override
          public TargetCode drop(Context context, Location location) {
            return new MortarCode().add(POP);
          }
        });
  }

  int storeOpcode();

  int loadOpcode();

  default ROPair<EvaluateResult, Binding> valueBind(MortarProtocode lower) {
    Object key = new Object();
    return new ROPair<>(
        new EvaluateResult(
            new MortarCode().add(lower.lower()).addVarInsn(storeOpcode(), key),
            null,
            NullValue.value),
        new MortarHalfBinding(key, this));
  }
}
