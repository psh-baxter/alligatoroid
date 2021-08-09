package com.zarbosoft.alligatoroid.compiler.mortar;

import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.Error;
import com.zarbosoft.alligatoroid.compiler.ErrorValue;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.TargetValue;
import com.zarbosoft.alligatoroid.compiler.Value;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMCode;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMRWCode;

public class MortarTargetValue extends TargetValue implements LowerableValue {
  public final JVMCode code;
  public final Value value;
  private final Location location;

  public MortarTargetValue(Location location, JVMCode code, Value value) {
    this.location = location;
    this.code = code;
    this.value = value;
  }

  @Override
  public JVMCode lower() {
    return new JVMRWCode().add(code).add(((LowerableValue) value).lower());
  }

  @Override
  public Value drop(Context context) {
    Value dropped = value.drop(context);
    if (dropped instanceof TargetValue) {
      if (dropped instanceof MortarTargetValue) {
        return new MortarTargetValue(
            location,
            new JVMRWCode().add(code).add(((MortarTargetValue) dropped).code),
            ((MortarTargetValue) dropped).value);
      } else {
        context.errors.add(
            Error.incompatibleTargetValues(
                location, targetName(), ((TargetValue) dropped).targetName()));
        return ErrorValue.error;
      }
    } else return new MortarTargetValue(location, code, dropped);
  }

  @Override
  public Value mergePrevious(Context context, Value previous) {
    if (previous instanceof TargetValue) {
      if (previous instanceof MortarTargetValue) {
        return new MortarTargetValue(
            location, new JVMRWCode().add(((MortarTargetValue) previous).code).add(code), value);
      } else {
        context.errors.add(
            Error.incompatibleTargetValues(
                location, targetName(), ((TargetValue) previous).targetName()));
        return ErrorValue.error;
      }
    } else {
      return this;
    }
  }

  @Override
  public Value mergeNext(Context context, Value next) {
    if (next instanceof TargetValue) {
      if (next instanceof MortarTargetValue) {
        MortarTargetValue mortarNext = (MortarTargetValue) next;
        return new MortarTargetValue(
            location, new JVMRWCode().add(code).add(mortarNext.code), mortarNext.value);
      } else {
        context.errors.add(
            Error.incompatibleTargetValues(
                location, targetName(), ((TargetValue) next).targetName()));
        return ErrorValue.error;
      }
    } else {
      return new MortarTargetValue(location, code, next);
    }
  }

  @Override
  public String targetName() {
    return "mortar";
  }
}
