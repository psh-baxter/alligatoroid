package com.zarbosoft.merman.editor.hid;

import com.google.common.collect.ImmutableSet;
import com.zarbosoft.merman.extensions.hotkeys.Key;
import com.zarbosoft.merman.misc.ROSet;
import com.zarbosoft.pidgoon.events.Event;

import java.util.Set;
import java.util.stream.Collectors;

public class HIDEvent implements Event {
  public final Key key;
  public final boolean press;
  public final ROSet<Key> modifiers;

  public HIDEvent(final Key key, final boolean press, final ROSet<Key> modifiers) {
    this.key = key;
    this.press = press;
    this.modifiers = modifiers;
  }

  @Override
  public String toString() {
    final StringBuilder out = new StringBuilder();
    if (!press) out.append("↑");
    out.append(key.name().toLowerCase());
    if (!modifiers.isEmpty()) {
      out.append(" [");
      boolean first = true;
      for (Key modifier : modifiers) {
        if (first) {
          out.append(" ");
        }
        out.append("+" + modifier.name().toLowerCase());
      }
      out.append("]");
    }
    return out.toString();
  }
}
