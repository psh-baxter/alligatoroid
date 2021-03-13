package com.zarbosoft.merman.webview;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.FieldArray;
import com.zarbosoft.merman.document.values.FieldPrimitive;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.Cursor;
import com.zarbosoft.merman.editor.Hoverable;
import com.zarbosoft.merman.editor.I18nEngine;
import com.zarbosoft.merman.editor.IterationContext;
import com.zarbosoft.merman.editor.IterationTask;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.editor.hid.HIDEvent;
import com.zarbosoft.merman.editor.hid.Key;
import com.zarbosoft.merman.editor.visual.visuals.VisualFrontArray;
import com.zarbosoft.merman.editor.visual.visuals.VisualFrontAtomBase;
import com.zarbosoft.merman.editor.visual.visuals.VisualFrontPrimitive;
import com.zarbosoft.merman.misc.MultiError;
import com.zarbosoft.merman.syntax.Direction;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.merman.syntax.error.UnsupportedDirections;
import com.zarbosoft.merman.syntax.style.ModelColor;
import com.zarbosoft.merman.webview.display.JSDisplay;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.Format;
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
          + "    position: relative;\n"
          + "    width: 100%;\n"
          + "    height: 100%;\n"
          + "}\n"
          + ".merman-block-view-container-inner:focus {\n"
          + "    border: none;\n"
          + "    outline: none;\n"
          + "}\n"
          + ".merman-block-view-container-origin {\n"
          + "    position: absolute;\n"
          + "}\n"
          + ".merman-block-view-container * {\n"
          + "    margin: 0;\n"
          + "    padding: 0;\n"
          + "}\n"
          + ".merman-block-view-container {\n"
          + "    width: 100%;\n"
          + "    min-width: 1em;\n"
          + "    min-height: 1em;\n"
          // + "    overflow-x: auto;\n"
          // + "    overflow-y: visible;\n"
          // + "    display: flex;\n"
          + "}\n"
          + ".merman-display {\n"
          + "    position: absolute;\n"
          + "    pointer-events: none;\n"
          + "}\n"
          + ".merman-display-blank {}\n"
          + ".merman-display-img {}\n"
          + ".merman-display-drawing {}\n"
          + ".merman-display-group {}\n"
          + ".merman-display-text {\n"
          + "    white-space: pre;\n"
          + "}\n"
          + ".merman-dir-dl .merman-display-text {\n"
          + "    writing-mode: vertical-rl;\n"
          + "}\n"
          + ".merman-dir-dr .merman-display-text {\n"
          + "    writing-mode: vertical-lr;\n"
          + "}\n";
  private final PriorityQueue<IterationTask> iterationQueue = new PriorityQueue<>();
  public DragSelectState dragSelect;
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
    /** Shifts origin for negative */
    HTMLDivElement elementOrigin = (HTMLDivElement) DomGlobal.document.createElement("div");
    elementOrigin.classList.add("merman-block-view-container-origin");

    /** Establishes size + overflow, roots event handling */
    HTMLDivElement elementInner = (HTMLDivElement) DomGlobal.document.createElement("div");
    elementInner.classList.add("merman-block-view-container-inner");
    elementInner.tabIndex = 0;
    if (syntax.converseDirection == Direction.RIGHT || syntax.converseDirection == Direction.LEFT) {
      // nop
    } /*else if (syntax.converseDirection == Direction.DOWN) {
        elementInner.style.minHeight = CSSProperties.MinHeightUnionType.of("20em");
        if (syntax.transverseDirection == Direction.LEFT) {
          elementInner.classList.add("merman-dir-dl");
          elementInner.style.flexDirection = "column-reverse";
        } else if (syntax.transverseDirection == Direction.RIGHT) {
          elementInner.classList.add("merman-dir-dr");
        }
      }*/ else
      new MultiError()
          .add(new UnsupportedDirections(syntax.converseDirection, syntax.transverseDirection))
          .raise();
    elementInner.appendChild(elementOrigin);

    /** The block element that joins the html doc flow */
    HTMLDivElement element = (HTMLDivElement) DomGlobal.document.createElement("div");
    element.classList.add("merman-block-view-container");
    element.appendChild(elementInner);

    JSSerializer serializer = new JSSerializer(syntax.backType, prioritizeKeys);
    JSDisplay display =
        new JSDisplay(
            syntax.converseDirection, syntax.transverseDirection, 15, elementInner, elementOrigin);
    Context context =
        new Context(
            new Context.InitialConfig()
                .startSelected(false)
                .wallTransverseUsageListener(
                    (min, max) -> {
                      if (max.x) {
                        elementInner.style.width =
                            CSSProperties.WidthUnionType.of(max.amount + "px");
                        if (syntax.transverseDirection == Direction.LEFT)
                          elementOrigin.style.left = max.amount + "px";
                      } else {
                        elementInner.style.height =
                            CSSProperties.HeightUnionType.of(max.amount + "px");
                        if (syntax.transverseDirection == Direction.UP)
                          elementOrigin.style.top = max.amount + "px";
                      }
                    })
                .hoverStyle(
                    c ->
                        c.obbox
                            .roundEnd(true)
                            .roundStart(true)
                            .roundRadius(8)
                            .lineThickness(1.5)
                            .lineColor(ModelColor.RGBA.polarOKLab(0.3, 0, 0, 0.4)))
                .cursorStyle(
                    c ->
                        c.obbox
                            .roundStart(true)
                            .roundEnd(true)
                            .lineThickness(1.5)
                            .roundRadius(8)
                            .lineColor(ModelColor.RGBA.polarOKLab(0.3, 0.5, 180, 0.8))),
            syntax,
            serializer.loadDocument(syntax, rawDoc),
            display,
            this::addIteration,
            this::flushIteration,
            new JSDelayEngine(),
            new JSClipboardEngine(syntax.backType),
            serializer,
            i18n);
    context.addHoverListener(
        new Context.HoverListener() {
          @Override
          public void hoverChanged(Context context, Hoverable hover) {
            if (hover != null && dragSelect != null) {
              Path endPath = hover.getSyntaxPath();
              if (!endPath.equals(dragSelect.end)) {
                dragSelect.end = endPath;
                ROList<String> endPathList = endPath.toList();
                ROList<String> startPathList = dragSelect.start.toList();
                int longestMatch = startPathList.longestMatch(endPathList);
                if (longestMatch == startPathList.size() && longestMatch == endPathList.size())
                  longestMatch -= 1;
                // If hover paths diverge, it's either
                // - at two depths in a single tree (parent and child): both paths are for an atom,
                // so longest match == parent == atom
                // - at two subtrees of an array/primitives: longest submatch == array/primitive ==
                // field, next segment == int
                Object base = context.syntaxLocate(new Path(endPathList.subUntil(longestMatch)));
                if (base instanceof FieldArray) {
                  int startIndex = Integer.parseInt(startPathList.get(longestMatch));
                  int endIndex = Integer.parseInt(endPathList.get(longestMatch));
                  if (endIndex < startIndex) {
                    ((FieldArray) base).selectInto(context, true, endIndex, startIndex);
                  } else {
                    ((FieldArray) base).selectInto(context, false, startIndex, endIndex);
                  }
                } else if (base instanceof FieldPrimitive) {
                  int startIndex = Integer.parseInt(startPathList.get(longestMatch));
                  int endIndex = Integer.parseInt(endPathList.get(longestMatch));
                  if (endIndex < startIndex) {
                    ((FieldPrimitive) base).selectInto(context, true, endIndex, startIndex);
                  } else {
                    ((FieldPrimitive) base).selectInto(context, false, startIndex, endIndex);
                  }
                } else if (base instanceof Atom) {
                  ((Atom) base).valueParentRef.selectValue(context);
                } else throw new Assertion();
              }
            }
          }
        });
    context.keyListener =
        new Context.KeyListener() {
          @Override
          public boolean handleKey(Context context, HIDEvent e) {
            if (!e.press) {
              switch (e.key) {
                case MOUSE_1:
                  {
                    if (dragSelect != null) {
                      dragSelect = null;
                      return true;
                    }
                  }
                default:
                  return false;
              }
            } else {
              switch (e.key) {
                case MOUSE_1:
                  {
                    if (context.hover != null) {
                      Path path = context.hover.getSyntaxPath();
                      context.hover.select(context);
                      dragSelect = new DragSelectState(path);
                      return true;
                    } else if (context.cursor != null) {
                      Path path = context.cursor.getSyntaxPath();
                      dragSelect = new DragSelectState(path);
                      return true;
                    }
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
            }
            return false;
          }
        };
    display.addMouseExitListener(
        () -> {
          if (context.hover != null) {
            context.clearHover();
          }
          if (dragSelect != null) {
            dragSelect = null;
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

  public static class DragSelectState {
    public final Path start;
    public Path end;

    public DragSelectState(Path start) {
      this.start = start;
    }
  }
}
