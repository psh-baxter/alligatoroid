package com.zarbosoft.merman.extensions.hotkeys.grammar;

import com.zarbosoft.merman.editor.hid.HIDEvent;
import com.zarbosoft.merman.extensions.hotkeys.Key;
import com.zarbosoft.merman.misc.ROSet;
import com.zarbosoft.pidgoon.Store;
import com.zarbosoft.pidgoon.events.Event;
import com.zarbosoft.pidgoon.events.stores.StackStore;
import com.zarbosoft.pidgoon.nodes.Operator;

import java.util.HashSet;

public class Terminal implements Node {

  public final Key key;

  public final boolean press;

  public final ROSet<Key> modifiers;

  public Terminal(Key key, boolean press, ROSet<Key> modifiers) {
    this.key = key;
    this.press = press;
    this.modifiers = modifiers;
  }

  public com.zarbosoft.pidgoon.Node build() {
    return new Operator<StackStore>(
        new com.zarbosoft.pidgoon.events.nodes.Terminal() {
          @Override
          protected boolean matches(Event event, Store store) {
            final HIDEvent event1 = (HIDEvent) event;
            final boolean a = key.equals(event1.key);
            final boolean b = press == event1.press;
            final boolean c = event1.modifiers.containsAll(modifiers);
            return a && b && c;
          }
        }) {
      @Override
      protected StackStore process(StackStore store) {
        return store.stackSingleElement(1 + modifiers.size());
      }
    };
  }

  @Override
  public String toString() {
    return String.valueOf(key);
  }
}
