package com.zarbosoft.merman.editorcore.hotkeys;

import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROMap;
import com.zarbosoft.rendaw.common.ROSet;

public class HotkeyRule {
  public static final boolean DEFAULT_FREE_TYPING = true;
  public final ROSet<String> with;
  public final ROSet<String> without;
  public final ROMap<String, ROList<Node>> hotkeys;
  /**
   * Keys that don't match any actions are passed to the underlying atom/field if this is true in
   * any active rule.
   */
  public final boolean freeTyping;

  public HotkeyRule(
      final ROSet<String> with,
      final ROSet<String> without,
      ROMap<String, ROList<Node>> hotkeys,
      boolean freeTyping) {
    this.with = with;
    this.without = without;
    this.hotkeys = hotkeys;
    this.freeTyping = freeTyping;
  }
}
