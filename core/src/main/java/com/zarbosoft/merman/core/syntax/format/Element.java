package com.zarbosoft.merman.core.syntax.format;

import com.zarbosoft.rendaw.common.ROMap;

interface Element {
  String format(ROMap<String, Object> data);
}
