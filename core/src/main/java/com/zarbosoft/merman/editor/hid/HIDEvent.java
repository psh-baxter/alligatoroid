package com.zarbosoft.merman.editor.hid;

import com.google.common.collect.ImmutableSet;
import com.zarbosoft.merman.extensions.hotkeys.Key;
import com.zarbosoft.pidgoon.events.Event;

import java.util.Set;
import java.util.stream.Collectors;

public class HIDEvent implements Event {
  public final Key key;
  public final boolean press;
  public final Set<Key> modifiers;
  public HIDEvent(final Key key, final boolean press, final Set<Key> modifiers) {
    this.key = key;
    this.press = press;
    this.modifiers = ImmutableSet.copyOf(modifiers);
  }

  @Override
  public String toString() {
    final StringBuilder out = new StringBuilder();
    if (!press) out.append("↑");
    out.append(key.name().toLowerCase());
    if (!modifiers.isEmpty())
      out.append(
          String.format(
              " [%s]",
              modifiers.stream()
                  .map(modifier -> "+" + modifier.name().toLowerCase())
                  .collect(Collectors.joining(" "))));
    return out.toString();
  }
}
