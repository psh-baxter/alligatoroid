package com.zarbosoft.merman.syntax.format;

import java.util.Map;

interface Element {
  String format(Map<String, Object> data);
}
