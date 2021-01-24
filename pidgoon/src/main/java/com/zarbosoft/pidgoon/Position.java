package com.zarbosoft.pidgoon;

public interface Position {
  Position advance();

  boolean isEOF();
}
