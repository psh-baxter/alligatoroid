package com.zarbosoft.alligatoroid.compiler.jvm;

import com.zarbosoft.alligatoroid.compiler.Context;
import com.zarbosoft.alligatoroid.compiler.EvaluateResult;
import com.zarbosoft.alligatoroid.compiler.ModuleContext;
import com.zarbosoft.alligatoroid.compiler.Scope;
import com.zarbosoft.alligatoroid.compiler.Value;
import com.zarbosoft.alligatoroid.compiler.mortar.Record;
import com.zarbosoft.alligatoroid.compiler.mortar.SimpleValue;
import com.zarbosoft.rendaw.common.TSList;

import static org.objectweb.asm.Opcodes.RETURN;

public class JVMMethod implements SimpleValue {
  private final JVMClassType base;
  private final String name;
  private String externName;
  private JVMCode built;

  public JVMMethod(JVMClassType base, String name) {
    this.base = base;
    this.name = name;
    this.externName = name;
  }

  public void setJvmName(String name) {
    this.externName = name;
  }

  public void define(Record spec, Value body) {
    JVMShallowMethodFieldType.MethodSpecDetails specDetails =
        JVMShallowMethodFieldType.specDetails(spec);

    base.fields.putNew(
        name,
        new JVMShallowMethodFieldType(
            base, specDetails.returnType, externName, specDetails.jvmSigDesc));
    ModuleContext moduleContext = new ModuleContext(null);
    JVMTargetModuleContext targetContext = new JVMTargetModuleContext();
    Context context = new Context(moduleContext, targetContext, new Scope(null));

    EvaluateResult.Context ectx = new EvaluateResult.Context(context, null);
    ectx.record(body.evaluate(context));
    built = (JVMCode) ectx.build(null).sideEffect;
    if (moduleContext.errors.some()) {
      throw new MultiError(moduleContext.errors);
    }
    base.jvmClass.defineFunction(
        externName, specDetails.jvmSigDesc, new JVMCode().add(built).add(RETURN), new TSList<>());
    base.incompleteMethods.remove(name);
  }
}
