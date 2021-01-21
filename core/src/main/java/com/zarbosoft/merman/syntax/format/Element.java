package com.zarbosoft.merman.syntax.format;

import com.zarbosoft.merman.misc.ROMap;

import java.util.Map;

interface Element {
  String format(ROMap<String, Object> data);
}
