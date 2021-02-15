package com.zarbosoft.rendaw.common;

import java.util.Objects;

public class Format {
  public static String format(String pattern, Object... args) {
    StringBuilder out = new StringBuilder();
    int at = 0;
    int index = 0;
    while (true) {
      int newAt = pattern.indexOf("%s", at);
      if (newAt == -1) {
        break;
      }
      out.append(pattern.substring(at, newAt));
      out.append(args[index]);
      at = newAt + 2;
      index += 1;
    }
    if (index < args.length) {
      throw new RuntimeException();
    }
    out.append(pattern.substring(at, pattern.length()));
    return out.toString();
  }
}
