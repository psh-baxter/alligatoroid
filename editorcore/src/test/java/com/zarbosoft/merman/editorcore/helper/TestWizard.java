package com.zarbosoft.merman.editorcore.helper;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.hid.ButtonEvent;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.editorcore.Editor;
import com.zarbosoft.merman.editorcore.display.MockeryDisplay;

import static com.zarbosoft.merman.editorcore.helper.Helper.buildDoc;

public class TestWizard {
  public final Editor editor;
  private final MockeryDisplay display;

  public TestWizard(final Syntax syntax, boolean startWindowed, final Atom... initial) {
    this.editor =
        buildDoc(new Context.InitialConfig().startWindowed(startWindowed), syntax, initial);
    this.display = (MockeryDisplay) editor.context.display;
    flushIteration();
  }

  public void flushIteration() {
    editor.context.flushIteration(1000);
    if (!editor.context.iterationQueue.isEmpty())
      throw new AssertionError("Too much idle activity");
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

  public TestWizard resize(final int size) {
    display.setWidth(size);
    flushIteration();
    return this;
  }

  public TestWizard resizeTransitive(final int size) {
    display.setHeight(size);
    flushIteration();
    return this;
  }

  public TestWizard sendHIDEvent(final ButtonEvent event) {
    display.sendHIDEvent(event);
    return this;
  }
}
