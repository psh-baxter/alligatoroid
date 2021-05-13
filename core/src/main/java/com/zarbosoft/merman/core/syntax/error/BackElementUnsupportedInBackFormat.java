package com.zarbosoft.merman.core.syntax.error;

import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.syntax.BackType;

public class BackElementUnsupportedInBackFormat extends BaseKVError {
  public BackElementUnsupportedInBackFormat(String element, BackType format, SyntaxPath typePath) {
    put("element", element);
    put("format", format);
    put("backPath", typePath);
  }

  @Override
  protected String description() {
    return "this element not supported in back format";
  }
}
