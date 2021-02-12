package com.zarbosoft.rendaw.common;

public class Format {
    public static String format(String pattern, Object ...args) {
        String[] splits = pattern.split("%s");
        if (splits.length != args.length + 1) throw new RuntimeException();
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < args.length; ++i) {
            out.append(splits[i]);
            out.append(args[i].toString());
        }
        out.append(splits[splits.length - 1]);
        return out.toString();
    }
}
