package com.zarbosoft.merman.webview;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.I18nEngine;
import com.zarbosoft.merman.editor.IterationContext;
import com.zarbosoft.merman.editor.IterationTask;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.merman.webview.display.JSDisplay;
import def.dom.Globals;
import def.dom.HTMLDivElement;
import jsweet.util.StringTypes;

import java.util.PriorityQueue;
import java.util.function.Consumer;

public class JSSourceView {
  public final HTMLDivElement element;
  private final PriorityQueue<IterationTask> iterationQueue = new PriorityQueue<>();
  private boolean iterationPending = false;
  private Double iterationTimer = null;
  private IterationContext iterationContext = null;

  public JSSourceView(Syntax syntax, I18nEngine i18n, String rawDoc) {
    element = Globals.window.document.createElement(StringTypes.div);
    JSSerializer serializer = new JSSerializer(syntax.backType);
    new Context(
        new Context.InitialConfig(),
        syntax,
        serializer.loadDocument(syntax, rawDoc),
        new JSDisplay(syntax.converseDirection, syntax.transverseDirection, 15, element),
        this::addIteration,
        this::flushIteration,
        new JSDelayEngine(),
        new JSClipboardEngine(syntax.backType),
        serializer,
        false,
        i18n);
  }

  private void flushIteration(final int limit) {
    final long start = System.currentTimeMillis();
    // TODO measure pending event backlog, adjust batch size to accomodate
    // by proxy? time since last invocation?
    for (int i = 0; i < limit; ++i) {
      {
        long now = start;
        if (i % 100 == 0) {
          now = System.currentTimeMillis();
        }
        if (now - start > 500) {
          iterationContext = null;
          break;
        }
      }
      final IterationTask top = iterationQueue.poll();
      if (top == null) {
        iterationContext = null;
        break;
      } else {
        if (iterationContext == null) iterationContext = new IterationContext();
        if (top.run(iterationContext)) addIteration(top);
      }
    }
  }

  private void addIteration(final IterationTask task) {
    iterationQueue.add(task);
    if (iterationTimer == null) {
      iterationTimer = Globals.window.setTimeout((Consumer<Void>)(ignored) -> handleTimer(), 50);
    }
  }

  private void handleTimer() {
    if (iterationPending) return;
    iterationPending = true;
    try {
      flushIteration(1000);
    } finally {
      iterationPending = false;
      iterationTimer = null;
    }
  }
}
