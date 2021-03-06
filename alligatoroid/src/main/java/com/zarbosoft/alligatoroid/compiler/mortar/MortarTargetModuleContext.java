package com.zarbosoft.alligatoroid.compiler.mortar;

import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.Error;
import com.zarbosoft.alligatoroid.compiler.EvaluateResult;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.ModuleId;
import com.zarbosoft.alligatoroid.compiler.TargetCode;
import com.zarbosoft.alligatoroid.compiler.TargetModuleContext;
import com.zarbosoft.alligatoroid.compiler.Value;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMDescriptor;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMRWSharedCode;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMSharedCode;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.ROMap;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;
import com.zarbosoft.rendaw.common.TSOrderedMap;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

import static com.zarbosoft.alligatoroid.compiler.mortar.MortarCode.MORTAR_TARGET_NAME;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;

public class MortarTargetModuleContext implements TargetModuleContext {
  public static final String TRANSFER_PREFIX = "transfer";
  public static JVMRWSharedCode newTSListCode =
      new MortarCode()
          .add(new TypeInsnNode(NEW, JVMDescriptor.jvmName(TSList.class)))
          .add(DUP)
          .add(callConstructorCode(TSList.class));
  public static JVMRWSharedCode tsListAddCode =
      new MortarCode()
          .add(
              new MethodInsnNode(
                  INVOKEVIRTUAL,
                  JVMDescriptor.jvmName(TSList.class),
                  "add",
                  JVMDescriptor.func(
                      JVMDescriptor.objDescriptorFromReal(TSList.class),
                      JVMDescriptor.objDescriptorFromReal(Object.class)),
                  false));
  public static JVMRWSharedCode newTSMapCode =
      new MortarCode()
          .add(new TypeInsnNode(NEW, JVMDescriptor.jvmName(TSMap.class)))
          .add(DUP)
          .add(callConstructorCode(TSMap.class));
  public static JVMRWSharedCode newTupleCode1 =
      new MortarCode().add(new TypeInsnNode(NEW, JVMDescriptor.jvmName(Tuple.class))).add(DUP);
  public static JVMRWSharedCode newTupleCode2 = callConstructorCode(Tuple.class, TSList.class);
  public static JVMRWSharedCode newRecordCode1 =
      new MortarCode().add(new TypeInsnNode(NEW, JVMDescriptor.jvmName(Record.class))).add(DUP);
  public static JVMRWSharedCode newRecordCode2 = callConstructorCode(Record.class, ROMap.class);
  public static JVMRWSharedCode newLocationCode1 =
      new MortarCode().add(new TypeInsnNode(NEW, JVMDescriptor.jvmName(Location.class))).add(DUP);
  public static JVMRWSharedCode newLocationCode2 =
      callConstructorCode(Location.class, ModuleId.class, int.class);
  public final TSOrderedMap<Object, String> transfers = new TSOrderedMap<>();
  public final String moduleInternalName;

  public MortarTargetModuleContext(String moduleInternalName) {
    this.moduleInternalName = moduleInternalName;
  }

  public static JVMRWSharedCode callConstructorCode(Class klass, Class... args) {
    String[] argDesc = new String[args.length];
    for (int i = 0; i < args.length; i++) {
      Class arg = args[i];
      if (arg == int.class) {
        argDesc[i] = JVMDescriptor.intDescriptor();
        continue;
      }
      if (arg.isPrimitive()) throw new Assertion(); // todo?
      if (arg.isArray()) throw new Assertion();
      argDesc[i] = JVMDescriptor.objDescriptorFromReal(arg);
    }
    return new MortarCode()
        .add(
            new MethodInsnNode(
                INVOKESPECIAL,
                JVMDescriptor.jvmName(klass),
                "<init>",
                JVMDescriptor.func("V", argDesc),
                false));
  }

  public static JVMSharedCode lower(Context context, Value value) {
    if (value instanceof LooseTuple) {
      MortarCode out = new MortarCode();
      out.add(newTupleCode1);
      out.add(newTSListCode);
      for (EvaluateResult e : ((LooseTuple) value).data) {
        if (e.preEffect != null) out.add((JVMSharedCode) e.preEffect);
        out.add(lower(context, e.value));
        out.add(tsListAddCode);
      }
      out.add(newTupleCode2);
      return out;
    } else if (value instanceof LooseRecord) {
      MortarCode out = new MortarCode();
      out.add(newRecordCode1);
      out.add(newTSMapCode);
      for (ROPair<Object, EvaluateResult> e : ((LooseRecord) value).data) {
        if (e.second.preEffect != null) out.add((JVMSharedCode) e.second.preEffect);
        out.add(lowerRaw(context, e.first, true));
        out.add(lower(context, e.second.value));
        out.add(
            new MethodInsnNode(
                INVOKEVIRTUAL,
                JVMDescriptor.jvmName(TSMap.class),
                "put",
                JVMDescriptor.func(
                    JVMDescriptor.objDescriptorFromReal(TSMap.class),
                    JVMDescriptor.objDescriptorFromReal(Object.class),
                    JVMDescriptor.objDescriptorFromReal(Object.class)),
                false));
      }
      out.add(newRecordCode2);
      return out;
    } else if (value instanceof WholeString) {
      return new MortarCode().addString(((WholeString) value).value);
    } else if (value instanceof MortarHalfValue) {
      return ((MortarHalfValue) value).lower();
    } else {
      return ((MortarTargetModuleContext) context.target).transfer(value);
    }
  }

  public static JVMSharedCode lowerRaw(Context context, Object value, boolean boxed) {
    if (value.getClass() == String.class) {
      return new MortarCode().addString((String) value);
    } else if (value.getClass() == Integer.class && !boxed) {
      return new MortarCode().addInt((Integer) value);
    } else if (value.getClass() == Location.class) {
      MortarCode out = new MortarCode();
      out.add(MortarTargetModuleContext.newLocationCode1);
      Location location = (Location) value;
      out.add(((MortarTargetModuleContext) context.target).transfer(location.module));
      out.addInt(location.id);
      out.add(MortarTargetModuleContext.newLocationCode2);
      return out;
    } else throw new Assertion();
  }

  public static void convertFunctionArgument(
      Context context, JVMRWSharedCode code, Value argument) {
    if (argument instanceof LooseTuple) {
      for (EvaluateResult e : ((LooseTuple) argument).data) {
        if (e.preEffect != null) code.add((JVMSharedCode) e.preEffect);
        code.add(lower(context, e.value));
      }
    } else {
      code.add(lower(context, argument));
    }
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
                    JVMDescriptor.objDescriptorFromReal(object.getClass())));
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
