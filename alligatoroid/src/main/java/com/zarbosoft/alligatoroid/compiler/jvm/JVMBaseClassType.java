package com.zarbosoft.alligatoroid.compiler.jvm;

import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.Error;
import com.zarbosoft.alligatoroid.compiler.EvaluateResult;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.Value;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMDescriptor;
import com.zarbosoft.alligatoroid.compiler.mortar.WholeValue;
import com.zarbosoft.rendaw.common.TSMap;

public class JVMBaseClassType extends JVMObjectType {
  public final String jvmInternalClass;
  public final TSMap<Object, JVMType> fields = new TSMap<>();

  public JVMBaseClassType(String jvmExternalClass, TSMap<Object, JVMType> fields) {
    this.jvmInternalClass = JVMDescriptor.jvmName(jvmExternalClass);
  }

  @Override
  public EvaluateResult valueAccess(
      Context context, Location location, Value field0, JVMProtocode lower) {
    WholeValue key = WholeValue.getWhole(context, location, field0);
    JVMType field = fields.getOpt(key.concreteValue());
    if (field == null) {
      context.module.errors.add(Error.noField(location, key));
      return EvaluateResult.error;
    }
    return EvaluateResult.pure(field.asValue(lower));
  }

  @Override
  public String jvmDesc() {
    return JVMDescriptor.objDescriptorFromJvmName(jvmInternalClass);
  }
}
