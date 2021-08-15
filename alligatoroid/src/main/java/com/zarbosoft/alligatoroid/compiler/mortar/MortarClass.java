package com.zarbosoft.alligatoroid.compiler.mortar;

import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.Error;
import com.zarbosoft.alligatoroid.compiler.EvaluateResult;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.TargetCode;
import com.zarbosoft.alligatoroid.compiler.Value;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMCode;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMRWCode;
import com.zarbosoft.rendaw.common.ROMap;

import static org.objectweb.asm.Opcodes.POP;

public class MortarClass implements MortarHalfType, MortarValue {
  public final String jbcInternalClass;
  public ROMap<Object, MortarHalfType> fields;

  public MortarClass(String jbcInternalClass) {
    this.jbcInternalClass = jbcInternalClass;
  }

  @Override
  public EvaluateResult access(Context context, Location location, Value field) {
    WholeValue key = WholeValue.getWhole(context, location, field);
    if (key == null) return EvaluateResult.error;
    Value out = fields.getOpt(key);
    if (out == null) {
      context.module.errors.add(Error.noField(location, key));
      return EvaluateResult.error;
    }
    return EvaluateResult.pure(out);
  }

  @Override
  public Value asValue(MortarProtocode lower) {
    return new MortarClassValue(lower, this);
  }

  @Override
  public Value stackAsValue(JVMRWCode code) {
    return new MortarClassValue(
        new MortarProtocode() {
          @Override
          public JVMCode lower() {
            return code;
          }

          @Override
          public TargetCode drop(Context context, Location location) {
            return new MortarCode().add(code).add(POP);
          }
        },
        this);
  }
}
