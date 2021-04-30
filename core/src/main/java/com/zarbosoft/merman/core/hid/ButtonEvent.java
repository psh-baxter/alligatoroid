package com.zarbosoft.merman.core.hid;

import com.zarbosoft.pidgoon.events.Event;
import com.zarbosoft.rendaw.common.ROSet;

public class ButtonEvent implements Event {
  public final Key key;
  public final boolean press;
  public final ROSet<Key> modifiers;

  public ButtonEvent(final Key key, final boolean press, final ROSet<Key> modifiers) {
    this.key = key;
    this.press = press;
    this.modifiers = modifiers;
  }
}
