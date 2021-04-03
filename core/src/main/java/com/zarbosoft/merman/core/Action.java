package com.zarbosoft.merman.core;

public interface Action {
  void run(Context context);

  String id();
}
