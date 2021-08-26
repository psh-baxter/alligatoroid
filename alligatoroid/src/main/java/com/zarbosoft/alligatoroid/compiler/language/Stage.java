package com.zarbosoft.alligatoroid.compiler.language;

import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.EvaluateResult;
import com.zarbosoft.alligatoroid.compiler.LanguageValue;
import com.zarbosoft.alligatoroid.compiler.Location;
import com.zarbosoft.alligatoroid.compiler.TargetCode;
import com.zarbosoft.alligatoroid.compiler.Value;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMDescriptor;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMSharedCode;
import com.zarbosoft.alligatoroid.compiler.mortar.MortarCode;
import com.zarbosoft.alligatoroid.compiler.mortar.MortarHalfValue;
import com.zarbosoft.alligatoroid.compiler.mortar.MortarProtocode;
import com.zarbosoft.alligatoroid.compiler.mortar.MortarTargetModuleContext;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ReverseIterable;
import com.zarbosoft.rendaw.common.TSList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;

import static com.zarbosoft.rendaw.common.Common.uncheck;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.NEW;

public class Stage extends LanguageValue {
  public final Value child;

  public Stage(Location id, Value child) {
    super(id, hasLowerInSubtree(child));
    this.child = child;
  }

  public static StageLowerResult stageLower(Context context, Location location, Value value) {
    TSList<TargetCode> post = new TSList<>();
    MortarCode pre;
    if (value instanceof Lower) {
      EvaluateResult evalRes = ((Lower) value).child.evaluate(context);
      JVMSharedCode lowerRes = MortarTargetModuleContext.lower(context, evalRes.value);
      pre =
          (MortarCode)
              context.target.merge(context, location, new TSList<>(evalRes.preEffect, lowerRes));
      post.add(evalRes.postEffect);
    } else if (value instanceof LanguageValue && ((LanguageValue) value).hasLowerInSubtree) {
      Class<? extends Value> klass = value.getClass();

      pre = new MortarCode();
      pre.add(new TypeInsnNode(NEW, JVMDescriptor.jvmName(klass))).add(DUP);

      Constructor<?> constructor = klass.getConstructors()[0];
      Parameter[] parameters = constructor.getParameters();
      String[] argDesc = new String[parameters.length];
      for (int i = 0; i < parameters.length; i++) {
        Parameter parameter = parameters[i];
        if (parameter.getType() == Location.class) {
          argDesc[i] = JVMDescriptor.objDescriptorFromReal(Location.class);
          pre.add(
              MortarTargetModuleContext.lowerRaw(
                  context, uncheck(() -> klass.getField("location").get(value)), false));
        } else if (parameter.getType() == ROList.class) {
          argDesc[i] = JVMDescriptor.objDescriptorFromReal(ROList.class);
          pre.add(MortarTargetModuleContext.newTSListCode);
          Object parameterValue = uncheck(() -> klass.getField(parameter.getName()).get(value));
          for (Object o : ((TSList) parameterValue)) {
            StageLowerResult stageRes = stageLower(context, location, (Value) o);
            pre.add((JVMSharedCode) stageRes.pre);
            pre.add(MortarTargetModuleContext.tsListAddCode);
            post.add(stageRes.post);
          }
        } else if (parameter.getType() == Value.class) {
          argDesc[i] = JVMDescriptor.objDescriptorFromReal(Value.class);
          StageLowerResult stageRes =
              stageLower(
                  context,
                  location,
                  (Value) uncheck(() -> klass.getField(parameter.getName()).get(value)));
          pre.add((JVMSharedCode) stageRes.pre);
          post.add(stageRes.post);
        } else throw new Assertion();
      }

      pre.add(
          new MethodInsnNode(
              INVOKESPECIAL,
              JVMDescriptor.jvmName(klass),
              "<init>",
              JVMDescriptor.func("V", argDesc),
              false));
    } else {
      pre = ((MortarTargetModuleContext) context.target).transfer(value);
    }
    return new StageLowerResult(
        pre, context.target.merge(context, location, new ReverseIterable<>(post)));
  }

  @Override
  public EvaluateResult evaluate(Context context) {
    StageLowerResult stageRes = stageLower(context, location, child);
    return new EvaluateResult(
        null,
        stageRes.post,
        new MortarHalfValue(
            Builtin.wrapClass(Value.class),
            new MortarProtocode() {
              @Override
              public MortarCode lower() {
                return (MortarCode) stageRes.pre;
              }

              @Override
              public TargetCode drop(Context context, Location location) {
                return null;
              }
            }));
  }

  public static class StageLowerResult {
    public final TargetCode pre;
    public final TargetCode post;

    public StageLowerResult(TargetCode pre, TargetCode post) {
      this.pre = pre;
      this.post = post;
    }
  }
}
