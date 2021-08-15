package com.zarbosoft.alligatoroid.compiler.mortar;

import com.zarbosoft.alligatoroid.compiler.OkValue;
import com.zarbosoft.alligatoroid.compiler.jvmshared.JVMCode;
import com.zarbosoft.luxem.write.Writer;

public class WholeString implements WholeValue, OkValue {
  public final String value;

  public WholeString(String value) {
    this.value = value;
  }

  @Override
  public JVMCode lower() {
    return new MortarCode().addString(value);
  }

  @Override
  public Object concreteValue() {
    return value;
  }

  @Override
  public void serialize(Writer writer) {
    writer.type("string").primitive(value);
  }
}
