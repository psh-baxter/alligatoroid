package com.zarbosoft.alligatoroid.compiler.jvmshared;

public class JVMDescriptor {
  public static String func(String returnDescriptor, String... argDescriptors) {
    StringBuilder builder = new StringBuilder();
    builder.append('(');
    for (String d : argDescriptors) {
      builder.append(d);
    }
    builder.append(')');
    builder.append(returnDescriptor);
    return builder.toString();
  }

  public static String void_() {
    return "V";
  }

  public static String bool_() {
    return "Z";
  }

  public static String short_() {
    return "S";
  }

  public static String char_() {
    return "C";
  }

  public static String byte_() {
    return "B";
  }

  public static String double_() {
    return "D";
  }

  public static String float_() {
    return "F";
  }

  public static String int_() {
    return "I";
  }

  public static String long_() {
    return "J";
  }

  public static String objReal(Class klass) {
    return obj_(klass.getCanonicalName());
  }

  public static String obj_(String externalName) {
    return "L" + internalName(externalName) + ";";
  }

  /**
   * Converts a.b.c to a/b/c
   *
   * @param externalName
   * @return
   */
  public static String internalName(String externalName) {
    return externalName.replace('.', '/');
  }

  public static String array(String child) {
    return "[" + child;
  }
}
