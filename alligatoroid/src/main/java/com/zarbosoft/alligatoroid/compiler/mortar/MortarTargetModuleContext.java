package com.zarbosoft.alligatoroid.compiler.mortar;

import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.Error;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.TargetCode;
import com.zarbosoft.alligatoroid.compiler.TargetModuleContext;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMSharedCode;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMDescriptor;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMRWSharedCode;
import com.zarbosoft.rendaw.common.TSOrderedMap;
import org.objectweb.asm.tree.FieldInsnNode;

import static com.zarbosoft.alligatoroid.compiler.mortar.MortarCode.MORTAR_TARGET_NAME;
import static org.objectweb.asm.Opcodes.GETSTATIC;

public class MortarTargetModuleContext implements TargetModuleContext {
  public static final String TRANSFER_PREFIX = "transfer";
  public final TSOrderedMap<Object, String> transfers = new TSOrderedMap<>();
  public final String moduleInternalName;

  public MortarTargetModuleContext(String moduleInternalName) {
    this.moduleInternalName = moduleInternalName;
  }

  public MortarCode transfer(Object object) {
    String name = transfers.getOpt(object);
    if (name == null) {
      name = TRANSFER_PREFIX + transfers.size();
      transfers.put(object, name);
    }
    return (MortarCode)
        new MortarCode()
            .add(
                new FieldInsnNode(
                    GETSTATIC,
                    moduleInternalName,
                    name,
                    JVMDescriptor.objReal(object.getClass())));
  }

  @Override
  public TargetCode mergeScoped(Context context, Location location, Iterable<TargetCode> chunks) {
    JVMSharedCode inner = (JVMSharedCode) merge(context, location, chunks);
    return new MortarCode().addScoped(inner);
  }

  @Override
  public TargetCode merge(Context context, Location location, Iterable<TargetCode> chunks) {
    JVMRWSharedCode code = new MortarCode();
    for (TargetCode chunk : chunks) {
      if (chunk == null) continue;
      if (!(chunk instanceof MortarCode)) {
        context.module.errors.add(
            Error.incompatibleTargetValues(location, MORTAR_TARGET_NAME, chunk.targetName()));
        return null;
      }
      code.add((MortarCode) chunk);
    }
    return code;
  }
}
