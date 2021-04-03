package com.zarbosoft.merman.helper;

import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.editor.display.MockeryDisplay;
import com.zarbosoft.merman.core.hid.HIDEvent;
import com.zarbosoft.merman.core.syntax.Syntax;

import static com.zarbosoft.merman.helper.Helper.buildDoc;

public class TestWizard {
  public final IterationRunner runner;
  public final Context context;
  private final MockeryDisplay display;

  public TestWizard(final Syntax syntax, boolean startWindowed, final Atom... initial) {
    this.runner = new IterationRunner();
    this.context =
        buildDoc(
            new Context.InitialConfig().startWindowed(startWindowed),
            runner::addIteration,
            runner::flushIteration,
            syntax,
            initial);
    this.display = (MockeryDisplay) context.display;
    runner.flush();
  }

  public TestWizard displayWidth(final int size) {
    display.setWidth(size);
    runner.flush();
    return this;
  }

  public TestWizard displayHeight(final int size) {
    display.setHeight(size);
    runner.flush();
    return this;
  }

  public TestWizard sendHIDEvent(final HIDEvent event) {
    display.sendHIDEvent(event);
    return this;
  }
}
