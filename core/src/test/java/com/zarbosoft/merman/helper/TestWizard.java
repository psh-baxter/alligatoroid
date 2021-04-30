package com.zarbosoft.merman.helper;

import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.editor.display.MockeryDisplay;
import com.zarbosoft.merman.core.hid.ButtonEvent;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.rendaw.common.Assertion;

import static com.zarbosoft.merman.helper.Helper.buildDoc;

public class TestWizard {
  public final Context context;
  private final MockeryDisplay display;

  public TestWizard(final Syntax syntax, boolean startWindowed, final Atom... initial) {
    this.context =
        buildDoc(
            new Context.InitialConfig().startWindowed(startWindowed),
            syntax,
            initial);
    this.display = (MockeryDisplay) context.display;
    this.flushIteration();
  }

  public void flushIteration() {
    context.flushIteration(1000);
    if (!context.iterationQueue.isEmpty())
      throw new Assertion();
  }

  public TestWizard displayWidth(final int size) {
    display.setWidth(size);
    flushIteration();
    return this;
  }

  public TestWizard displayHeight(final int size) {
    display.setHeight(size);
    flushIteration();
    return this;
  }

  public TestWizard sendHIDEvent(final ButtonEvent event) {
    display.sendHIDEvent(event);
    return this;
  }
}
