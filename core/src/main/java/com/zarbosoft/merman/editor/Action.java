package com.zarbosoft.merman.editor;

public interface Action {
  boolean run(Context context);

  String id();
}
