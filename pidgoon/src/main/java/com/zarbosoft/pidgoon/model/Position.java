package com.zarbosoft.pidgoon.model;

public interface Position {
  Position advance();

  boolean isEOF();
}
