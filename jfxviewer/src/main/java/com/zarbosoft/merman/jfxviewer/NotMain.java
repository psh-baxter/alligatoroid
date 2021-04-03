package com.zarbosoft.merman.jfxviewer;

import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.document.Document;
import com.zarbosoft.merman.core.document.values.FieldArray;
import com.zarbosoft.merman.core.document.values.FieldPrimitive;
import com.zarbosoft.merman.core.editor.Context;
import com.zarbosoft.merman.core.editor.Cursor;
import com.zarbosoft.merman.core.editor.Hoverable;
import com.zarbosoft.merman.core.editor.IterationContext;
import com.zarbosoft.merman.core.editor.IterationTask;
import com.zarbosoft.merman.core.editor.Path;
import com.zarbosoft.merman.core.editor.hid.HIDEvent;
import com.zarbosoft.merman.core.editor.hid.Key;
import com.zarbosoft.merman.core.editor.visual.visuals.VisualFrontArray;
import com.zarbosoft.merman.core.editor.visual.visuals.VisualFrontAtomBase;
import com.zarbosoft.merman.core.editor.visual.visuals.VisualFrontPrimitive;
import com.zarbosoft.merman.core.example.JsonSyntax;
import com.zarbosoft.merman.core.syntax.BackType;
import com.zarbosoft.merman.core.syntax.style.Padding;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.jfxcore.JavaI18nEngine;
import com.zarbosoft.merman.jfxcore.JfxDelayEngine;
import com.zarbosoft.merman.jfxcore.SimpleClipboardEngine;
import com.zarbosoft.merman.jfxcore.jfxdisplay.JavaFXDisplay;
import com.zarbosoft.merman.jfxcore.jfxserialization.JavaSerializer;
import com.zarbosoft.pidgoon.errors.GrammarTooUncertain;
import com.zarbosoft.pidgoon.errors.InvalidStream;
import com.zarbosoft.pidgoon.errors.NoResults;
import com.zarbosoft.pidgoon.events.Position;
import com.zarbosoft.pidgoon.model.MismatchCause;
import com.zarbosoft.pidgoon.model.Parse;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.Format;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.TSList;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.PriorityQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class NotMain extends Application {
  private final ScheduledThreadPoolExecutor worker = new ScheduledThreadPoolExecutor(1);
  private final PriorityQueue<IterationTask> iterationQueue = new PriorityQueue<>();
  public DragSelectState dragSelect;
  private boolean iterationPending = false;
  private ScheduledFuture<?> iterationTimer = null;
  private IterationContext iterationContext = null;
  private Stage stage;

  public static void main(String[] args) {
    NotMain.launch(args);
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    try {
      this.stage = primaryStage;
      List<String> args = getParameters().getUnnamed();
      if (args.isEmpty())
        throw new RuntimeException("need to specify one file to open on the command line");
      String path = args.get(0);
      JavaI18nEngine i18n = new JavaI18nEngine(Locale.getDefault());
      Syntax syntax = JsonSyntax.create(i18n, new Padding(5, 5, 5, 5));
      JavaSerializer serializer;
      Document document;
      if (path.endsWith(".json")) {
        serializer = new JavaSerializer(BackType.JSON);
        document = serializer.loadDocument(syntax, Files.readAllBytes(Paths.get(path)));
      } else {
        throw new RuntimeException("unknown file type (using file extension)");
      }
      JavaFXDisplay display = new JavaFXDisplay(syntax);
      Context context =
          new Context(
              new Context.InitialConfig().startSelected(false),
              syntax,
              document,
              display,
              this::addIteration,
              this::flushIteration,
              new JfxDelayEngine(),
              new SimpleClipboardEngine(syntax.backType),
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
                  // If hover paths diverge, it's either
                  // - at two depths in a single tree (parent and child): both paths are for an
                  // atom,
                  // so longest match == parent == atom
                  // - at two subtrees of an array/primitives: longest submatch == array/primitive
                  // ==
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
                    // If end/start paths are the same then the longest match includes the index
                    // vs if they're different, then it includes the primitive but not index
                    // Adjust so the index is the next element in both cases
                    if (longestMatch == startPathList.size()) longestMatch -= 1;
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
                        System.out.format("in cursor\n");
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
      primaryStage.setScene(new Scene(display.node, 800, 600));
      primaryStage.show();
      primaryStage.setOnCloseRequest(
          windowEvent -> {
            worker.shutdown();
          });
    } catch (GrammarTooUncertain e) {
      StringBuilder message = new StringBuilder();
      for (Parse.State leaf : e.context.leaves) {
        message.append(Format.format(" * %s (%s)\n", leaf, leaf.color()));
      }
      throw new RuntimeException(
          Format.format(
              "Too much uncertainty while parsing!\nat %s %s\n%s branches:\n%s",
              ((Position) e.position).at, ((Position) e.position).event, message.toString()));
    } catch (InvalidStream e) {
      StringBuilder message = new StringBuilder();
      for (MismatchCause error : e.state.errors) {
        message.append(Format.format(" * %s\n", error));
      }
      throw new RuntimeException(
          Format.format(
              "Document doesn't conform to syntax tree\nat %s %s\nmismatches at final stream element:\n%s",
              ((Position) e.position).at, ((Position) e.position).event, message.toString()));
    } catch (NoResults e) {
      StringBuilder message = new StringBuilder();
      for (MismatchCause error : e.state.errors) {
        message.append(Format.format(" * %s\n", error));
      }
      throw new RuntimeException(
          Format.format("Document incomplete\nexpected:\n%s", message.toString()));
    } catch (RuntimeException e) {
      StringWriter writer = new StringWriter();
      e.printStackTrace(new PrintWriter(writer));
      throw new RuntimeException("\n" + writer.toString());
    }
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
      try {
        iterationTimer =
            worker.scheduleWithFixedDelay(
                () -> {
                  if (iterationPending) return;
                  iterationPending = true;
                  Platform.runLater(
                      () -> {
                        wrap(
                            stage.getOwner(),
                            () -> {
                              try {
                                flushIteration(1000);
                              } finally {
                                iterationPending = false;
                              }
                            });
                      });
                },
                0,
                50,
                TimeUnit.MILLISECONDS);
      } catch (final RejectedExecutionException e) {
        // Happens on unhover when window closes to shutdown
      }
    }
  }

  private void wrap(final Window top, final Wrappable runnable) {
    try {
      runnable.run();
    } catch (final Exception e) {
      StringWriter writer = new StringWriter();
      e.printStackTrace(new PrintWriter(writer));
      System.out.format("Exception passed sieve: %s\n%s\n", e, writer.toString());
      final Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage());
      alert.initModality(Modality.APPLICATION_MODAL);
      alert.initOwner(top);
      alert.setResizable(true);
      alert.getDialogPane().getChildren().stream()
          .filter(node -> node instanceof Label)
          .forEach(node -> ((Label) node).setMinHeight(Region.USE_PREF_SIZE));
      alert.showAndWait();
    }
  }

  @FunctionalInterface
  private interface Wrappable {
    void run() throws Exception;
  }

  public static class DragSelectState {
    public final Path start;
    public Path end;

    public DragSelectState(Path start) {
      this.start = start;
    }
  }
}
