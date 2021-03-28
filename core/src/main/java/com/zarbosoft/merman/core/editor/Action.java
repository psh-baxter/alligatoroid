package com.zarbosoft.merman.core.editor;

public interface Action {
  void run(Context context);

  String id();
}
