package com.zarbosoft.merman.jfxviewer;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.CursorFactory;
import com.zarbosoft.merman.core.Environment;
import com.zarbosoft.merman.core.Hoverable;
import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.document.Document;
import com.zarbosoft.merman.core.document.fields.FieldArray;
import com.zarbosoft.merman.core.document.fields.FieldPrimitive;
import com.zarbosoft.merman.core.example.JsonSyntax;
import com.zarbosoft.merman.core.hid.ButtonEvent;
import com.zarbosoft.merman.core.hid.Key;
import com.zarbosoft.merman.core.syntax.BackType;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.core.syntax.style.Padding;
import com.zarbosoft.merman.core.visual.visuals.FieldArrayCursor;
import com.zarbosoft.merman.core.visual.visuals.FieldAtomCursor;
import com.zarbosoft.merman.core.visual.visuals.VisualFieldArray;
import com.zarbosoft.merman.core.visual.visuals.VisualFrontAtomBase;
import com.zarbosoft.merman.core.visual.visuals.VisualFrontPrimitive;
import com.zarbosoft.merman.jfxcore.JFXEnvironment;
import com.zarbosoft.merman.jfxcore.display.JavaFXDisplay;
import com.zarbosoft.merman.jfxcore.serialization.JavaSerializer;
import com.zarbosoft.pidgoon.errors.GrammarTooUncertainAt;
import com.zarbosoft.pidgoon.errors.InvalidStreamAt;
import com.zarbosoft.pidgoon.errors.NoResults;
import com.zarbosoft.pidgoon.events.Position;
import com.zarbosoft.pidgoon.model.MismatchCause;
import com.zarbosoft.pidgoon.model.Step;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.Format;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.TSList;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;

public class NotMain extends Application {
  public DragSelectState dragSelect;

  public static void main(String[] args) {
    NotMain.launch(args);
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    try {
      List<String> args = getParameters().getUnnamed();
      if (args.isEmpty())
        throw new RuntimeException("need to specify one file to open on the command line");
      String path = args.get(0);
      Environment env = new JFXEnvironment(Locale.getDefault());
      Syntax syntax = JsonSyntax.create(env, new Padding(5, 5, 5, 5));
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
              new Context.InitialConfig(),
              syntax,
              document,
              display,
              env,
              serializer,
              new CursorFactory() {
                boolean handleCommon(Context context, ButtonEvent e) {
                  if (e.press) {
                    switch (e.key) {
                      case C:
                        {
                          if (context.cursor != null
                              && (e.modifiers.contains(Key.CONTROL)
                                  || e.modifiers.contains(Key.CONTROL_LEFT)
                                  || e.modifiers.contains(Key.CONTROL_RIGHT))) {
                            context.cursor.dispatch(
                                new com.zarbosoft.merman.core.Cursor.Dispatcher() {
                                  @Override
                                  public void handle(FieldArrayCursor cursor) {
                                    cursor.actionCopy(context);
                                  }

                                  @Override
                                  public void handle(FieldAtomCursor cursor) {
                                    cursor.actionCopy(context);
                                  }

                                  @Override
                                  public void handle(VisualFrontPrimitive.Cursor cursor) {
                                    cursor.actionCopy(context);
                                  }
                                });
                          }
                          return true;
                        }
                    }
                  }
                  return false;
                }

                @Override
                public VisualFrontPrimitive.Cursor createPrimitiveCursor(
                    Context context,
                    VisualFrontPrimitive visualPrimitive,
                    boolean leadFirst,
                    int beginOffset,
                    int endOffset) {
                  return new VisualFrontPrimitive.Cursor(
                      context, visualPrimitive, leadFirst, beginOffset, endOffset) {
                    @Override
                    public boolean handleKey(Context context, ButtonEvent hidEvent) {
                      return handleCommon(context, hidEvent);
                    }
                  };
                }

                @Override
                public FieldArrayCursor createArrayCursor(
                    Context context,
                    VisualFieldArray visual,
                    boolean leadFirst,
                    int start,
                    int end) {
                  return new FieldArrayCursor(context, visual, leadFirst, start, end) {
                    @Override
                    public boolean handleKey(Context context, ButtonEvent hidEvent) {
                      return handleCommon(context, hidEvent);
                    }
                  };
                }

                @Override
                public FieldAtomCursor createAtomCursor(
                    Context context, VisualFrontAtomBase base) {
                  return new FieldAtomCursor(context, base) {
                    @Override
                    public boolean handleKey(Context context, ButtonEvent hidEvent) {
                      return handleCommon(context, hidEvent);
                    }
                  };
                }

                @Override
                public boolean prepSelectEmptyArray(Context context, FieldArray value) {
                  return false;
                }
              });
      context.addHoverListener(
          new Context.HoverListener() {
            @Override
            public void hoverChanged(Context context, Hoverable hover) {
              if (hover != null && dragSelect != null) {
                SyntaxPath endPath = hover.getSyntaxPath();
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
                  Object base =
                      context.syntaxLocate(new SyntaxPath(endPathList.subUntil(longestMatch)));
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
                    ((Atom) base).fieldParentRef.selectValue(context);
                  } else throw new Assertion();
                }
              }
            }
          });
      context.mouseButtonEventListener =
          new Context.KeyListener() {
            @Override
            public boolean handleKey(Context context, ButtonEvent e) {
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
                        SyntaxPath path = context.hover.getSyntaxPath();
                        context.hover.select(context);
                        dragSelect = new DragSelectState(path);
                        return true;
                      } else if (context.cursor != null) {
                        SyntaxPath path = context.cursor.getSyntaxPath();
                        dragSelect = new DragSelectState(path);
                        return true;
                      }
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
            env.destroy();
          });
    } catch (GrammarTooUncertainAt e) {
      StringBuilder message = new StringBuilder();
      for (Step.Branch leaf : (TSList<Step.Branch>) e.e.step.branches) {
        message.append(Format.format(" * %s (%s)\n", leaf, leaf.color()));
      }
      throw new RuntimeException(
          Format.format(
              "Too much uncertainty while parsing!\nat %s %s\n%s branches:\n%s",
              ((Position) e.at).at, ((Position) e.at).event, message.toString()));
    } catch (InvalidStreamAt e) {
      StringBuilder message = new StringBuilder();
      for (MismatchCause error : (TSList<MismatchCause>) e.step.errors) {
        message.append(Format.format(" * %s\n", error));
      }
      throw new RuntimeException(
          Format.format(
              "Document doesn't conform to syntax tree\nat %s %s\nmismatches at final stream element:\n%s",
              ((Position) e.at).at, ((Position) e.at).event, message.toString()));
    } catch (NoResults e) {
      StringBuilder message = new StringBuilder();
      for (MismatchCause error : (TSList<MismatchCause>) e.state.errors) {
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

  public static class DragSelectState {
    public final SyntaxPath start;
    public SyntaxPath end;

    public DragSelectState(SyntaxPath start) {
      this.start = start;
    }
  }
}
