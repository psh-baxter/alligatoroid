package com.zarbosoft.merman.extensions.hotkeys;

import com.zarbosoft.merman.editor.visual.tags.Tag;
import com.zarbosoft.merman.extensions.hotkeys.grammar.Node;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HotkeyRule {
  public Set<Tag> with = new HashSet<>();
  public Set<Tag> without = new HashSet<>();
  public Map<String, List<Node>> hotkeys = new HashMap<>();
  public boolean freeTyping = true;

  public HotkeyRule() {}

  public HotkeyRule(final Set<Tag> with, final Set<Tag> without) {
    this.with.addAll(with);
    this.without.addAll(without);
  }
}
