package com.zarbosoft.merman.webview;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.Cursor;
import com.zarbosoft.merman.editor.I18nEngine;
import com.zarbosoft.merman.editor.IterationContext;
import com.zarbosoft.merman.editor.IterationTask;
import com.zarbosoft.merman.editor.hid.HIDEvent;
import com.zarbosoft.merman.editor.hid.Key;
import com.zarbosoft.merman.editor.visual.visuals.VisualFrontArray;
import com.zarbosoft.merman.editor.visual.visuals.VisualFrontAtomBase;
import com.zarbosoft.merman.editor.visual.visuals.VisualFrontPrimitive;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.merman.syntax.style.ModelColor;
import com.zarbosoft.merman.webview.display.JSDisplay;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.TSList;
import elemental2.dom.CSSProperties;
import elemental2.dom.DomGlobal;
import elemental2.dom.Event;
import elemental2.dom.EventListener;
import elemental2.dom.FocusEvent;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLStyleElement;
import jsinterop.annotations.JsMethod;

import java.util.PriorityQueue;

public class WebView {
  private static final String style =
      ".merman-block-view-container-inner {\n"
          + "    margin: 0;\n"
          + "    padding: 0;\n"
          + "    position: relative;\n"
          + "    width: 100%;\n"
          + "    height: 100%;\n"
          + "}\n"
          + ".merman-block-view-container-inner:focus {\n"
          + "    border: none;\n"
          + "    outline: none;\n"
          + "}\n"
          + ".merman-block-view-container {\n"
          + "    width: 100%;\n"
          + "    min-width: 1em;\n"
          + "    min-height: 1em;\n"
          + "}\n"
          + ".merman-display {\n"
          + "    position: absolute;\n"
          + "}\n"
          + ".merman-display-blank {}\n"
          + ".merman-display-img {}\n"
          + ".merman-display-drawing {}\n"
          + ".merman-display-group {}\n"
          + ".merman-display-text {\n"
          + "    white-space: pre;\n"
          + "}\n";
  private final PriorityQueue<IterationTask> iterationQueue = new PriorityQueue<>();
  private boolean iterationPending = false;
  private Double iterationTimer = null;
  private IterationContext iterationContext = null;

  public WebView() {
    HTMLStyleElement style = (HTMLStyleElement) DomGlobal.document.createElement("style");
    style.textContent = WebView.style;
    DomGlobal.document.head.appendChild(style);
  }

  @JsMethod
  public HTMLElement block(
      Syntax syntax, I18nEngine i18n, String rawDoc, ROList<String> prioritizeKeys) {
    HTMLDivElement elementInner = (HTMLDivElement) DomGlobal.document.createElement("div");
    elementInner.classList.add("merman-block-view-container-inner");
    elementInner.tabIndex = 0;
    HTMLDivElement element = (HTMLDivElement) DomGlobal.document.createElement("div");
    element.classList.add("merman-block-view-container");
    element.appendChild(elementInner);
    JSSerializer serializer = new JSSerializer(syntax.backType, prioritizeKeys);
    JSDisplay display =
        new JSDisplay(syntax.converseDirection, syntax.transverseDirection, 15, elementInner);
    Context context =
        new Context(
            new Context.InitialConfig()
                .startSelected(false)
                .wallUsageListener(
                    (min, max) -> {
                      if (max.x) {
                        elementInner.style.width =
                            CSSProperties.WidthUnionType.of(max.amount + "px");
                      } else {
                        elementInner.style.height =
                            CSSProperties.HeightUnionType.of(max.amount + "px");
                      }
                    })
                .hoverStyle(
                    c ->
                        c.obbox
                            .roundEnd(true)
                            .roundRadius(8)
                            .lineThickness(1.5)
                            .lineColor(ModelColor.RGBA.polarOKLab(0.4, 0, 0, 0.5)))
                .cursorStyle(
                    c ->
                        c.obbox
                            .roundEnd(true)
                            .lineThickness(1.5)
                            .roundRadius(8)
                            .lineColor(ModelColor.RGBA.polarOKLab(0.3, 0.5, 180, 0.5))),
            syntax,
            serializer.loadDocument(syntax, rawDoc),
            display,
            this::addIteration,
            this::flushIteration,
            new JSDelayEngine(),
            new JSClipboardEngine(syntax.backType),
            serializer,
            i18n);
    context.keyListener =
        new Context.KeyListener() {
          @Override
          public boolean handleKey(Context context, HIDEvent e) {
            if (!e.press) return false;
            switch (e.key) {
              case MOUSE_1:
                {
                  if (context.hover != null) {
                    context.hover.select(context);
                  }
                  return true;
                }
              case C:
                {
                  if (context.cursor != null
                      && (e.modifiers.contains(Key.CONTROL)
                          || e.modifiers.contains(Key.CONTROL_LEFT)
                          || e.modifiers.contains(Key.CONTROL_RIGHT))) {
                    context.cursor.dispatch(
                        new Cursor.Dispatcher() {
                          @Override
                          public void handle(VisualFrontArray.ArrayCursor cursor) {
                            context.copy(
                                cursor.visual.value.data.sublist(
                                    cursor.beginIndex, cursor.endIndex + 1));
                          }

                          @Override
                          public void handle(VisualFrontAtomBase.NestedCursor cursor) {
                            context.copy(TSList.of(cursor.base.atomGet()));
                          }

                          @Override
                          public void handle(VisualFrontPrimitive.PrimitiveCursor cursor) {
                            context.copy(
                                cursor.visualPrimitive.value.data.substring(
                                    cursor.range.beginOffset, cursor.range.endOffset));
                          }
                        });
                  }
                  return true;
                }
            }
            return false;
          }
        };
    display.addMouseExitListener(
        () -> {
          if (context.hover != null) {
            context.clearHover();
          }
        });
    DomGlobal.document.addEventListener(
        "focusin",
        new EventListener() {
          @Override
          public void handleEvent(Event evt0) {
            FocusEvent e = (FocusEvent) evt0;
            if (e.target == elementInner) return;
            context.clearSelection();
          }
        });
    return element;
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
      iterationTimer =
          DomGlobal.setTimeout(
              new DomGlobal.SetTimeoutCallbackFn() {
                @Override
                public void onInvoke(Object... p0) {
                  handleTimer();
                }
              },
              50);
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
