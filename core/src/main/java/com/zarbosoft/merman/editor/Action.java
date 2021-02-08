package com.zarbosoft.merman.editor;

public interface Action {
  void run(Context context);

  String id();
}
