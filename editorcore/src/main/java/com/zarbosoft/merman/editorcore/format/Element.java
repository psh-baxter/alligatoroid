package com.zarbosoft.merman.editorcore.format;

import com.zarbosoft.rendaw.common.ROMap;

interface Element {
  String format(ROMap<String, Object> data);
}
