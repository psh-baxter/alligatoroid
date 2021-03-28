package com.zarbosoft.merman.core.editor;

import com.zarbosoft.rendaw.common.TSList;

public class BackPath {
  public static final BackPath root = new BackPath(null, -1);
  public final BackPath parent;
  public final int index;

  public BackPath(BackPath parent, int index) {
    this.parent = parent;
    this.index = index;
  }

  public BackPath add(int index) {
    return new BackPath(this, index);
  }

  public String toString() {
    TSList<String> list = new TSList<>();
    BackPath at = this;
    while (at != null && at != root) {
      list.add(Integer.toString(at.index));
      at = at.parent;
    }
    list.reverse();
    StringBuilder out = new StringBuilder();
    for (String i : list) {
      out.append("/");
      out.append(i);
    }
    if (out.length() == 0) out.append("/");
    return out.toString();
  }
}
