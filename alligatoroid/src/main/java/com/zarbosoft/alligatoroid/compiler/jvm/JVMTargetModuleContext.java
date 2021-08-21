package com.zarbosoft.alligatoroid.compiler.jvm;

import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.Error;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.TargetCode;
import com.zarbosoft.alligatoroid.compiler.TargetModuleContext;
import com.zarbosoft.alligatoroid.compiler.Value;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMRWSharedCode;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMSharedCode;
import com.zarbosoft.alligatoroid.compiler.mortar.MortarHalfStringType;
import com.zarbosoft.alligatoroid.compiler.mortar.WholeString;
import com.zarbosoft.rendaw.common.Assertion;

import static com.zarbosoft.alligatoroid.compiler.mortar.MortarCode.MORTAR_TARGET_NAME;

public class JVMTargetModuleContext implements TargetModuleContext {
    public static JVMSharedCode lowerValue(Value argument) {
        if (argument instanceof WholeString) {
            return new JVMCode().addString(((WholeString) argument).value);
        } else throw new Assertion();
    }

    @Override
    public TargetCode mergeScoped(Context context, Location location, Iterable<TargetCode> chunks) {
        JVMSharedCode inner = (JVMSharedCode) merge(context, location, chunks);
        return new JVMCode().addScoped(inner);
    }

    @Override
    public TargetCode merge(Context context, Location location, Iterable<TargetCode> chunks) {
        JVMRWSharedCode code = new JVMCode();
        for (TargetCode chunk : chunks) {
            if (chunk == null) continue;
            if (!(chunk instanceof JVMCode)) {
                context.module.errors.add(
                        Error.incompatibleTargetValues(location, MORTAR_TARGET_NAME, chunk.targetName()));
                return null;
            }
            code.add((JVMCode) chunk);
        }
        return code;
    }
}
