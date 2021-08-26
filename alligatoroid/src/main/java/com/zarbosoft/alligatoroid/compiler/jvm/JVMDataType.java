package com.zarbosoft.alligatoroid.compiler.jvm;

import com.zarbosoft.alligatoroid.compiler.Binding;
import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.Error;
import com.zarbosoft.alligatoroid.compiler.EvaluateResult;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.TargetCode;
import com.zarbosoft.alligatoroid.compiler.Value;
import com.zarbosoft.alligatoroid.compiler.mortar.NullValue;
import com.zarbosoft.rendaw.common.ROPair;

import static org.objectweb.asm.Opcodes.POP;

public interface JVMDataType extends JVMType {
  default EvaluateResult valueAccess(
      Context context, Location location, Value field, JVMProtocode lower) {
    context.module.errors.add(Error.accessNotSupported(location));
    return EvaluateResult.error;
  }

  default ROPair<EvaluateResult, Binding> valueBind(JVMProtocode lower) {
    Object key = new Object();
    return new ROPair<>(
        new EvaluateResult(
            new JVMCode().add(lower.lower()).addVarInsn(storeOpcode(), key), null, NullValue.value),
        new JVMBinding(key, this));
  }

  default Value asValue(JVMProtocode lower) {
    return new JVMValue(this, lower);
  }

  default Value stackAsValue(JVMCode code) {
    return new JVMValue(
        this,
        new JVMProtocode() {
          @Override
          public JVMCode lower() {
            return code;
          }

          @Override
          public TargetCode drop(Context context, Location location) {
            return new JVMCode().add(POP);
          }
        });
  }

  int storeOpcode();

  int loadOpcode();

  String jvmDesc();
}
