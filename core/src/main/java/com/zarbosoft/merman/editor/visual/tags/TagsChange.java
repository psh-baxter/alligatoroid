package com.zarbosoft.merman.editor.visual.tags;

import com.zarbosoft.rendaw.common.TSSet;

public class TagsChange {
  public final TSSet<String> add;
  public final TSSet<String> remove;

  public TagsChange(TSSet<String> add, TSSet<String> remove) {
    this.add = add;
    this.remove = remove;
  }

  public static TagsChange remove(String... tag) {
    return new TagsChange(new TSSet<>(), new TSSet<>(tag));
  }

  public static TagsChange add(String... tag) {
    return new TagsChange(new TSSet<>(tag), new TSSet<>());
  }

  public boolean apply(TSSet<String> target) {
    boolean a =target.removeAnyOld(remove);
    boolean b = target.addAnyNew(add);
    return a||b;
  }
}
