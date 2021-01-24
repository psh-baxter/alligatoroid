package com.zarbosoft.merman.standalone;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.merman.document.Document;
import com.zarbosoft.merman.editor.Action;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.Editor;
import com.zarbosoft.merman.editor.IterationContext;
import com.zarbosoft.merman.editor.IterationTask;
import com.zarbosoft.merman.editor.history.History;
import com.zarbosoft.merman.standalone.display.JavaFXDisplay;
import com.zarbosoft.merman.syntax.Syntax;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.PriorityQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.zarbosoft.rendaw.common.Common.uncheck;

public class Main extends Application {

  private final Logger logger = LoggerFactory.getLogger("main");
  private final ScheduledThreadPoolExecutor worker = new ScheduledThreadPoolExecutor(1);
  private final PriorityQueue<IterationTask> iterationQueue = new PriorityQueue<>();
  private boolean iterationPending = false;
  private ScheduledFuture<?> iterationTimer = null;
  private IterationContext iterationContext = null;
  private Path filename;
  private Stage stage;
  private JavaFXDisplay display;
  private Editor editor;

  public static void main(final String[] args) {
    launch(args);
  }

  public static void save(Path path) {
          context.document.write(dest);
          context.history.clearModified(context);
  }

  public static boolean confirmDialog(final Stage stage, final String text) {
    final Alert confirm =
        new Alert(Alert.AlertType.CONFIRMATION, text, ButtonType.NO, ButtonType.YES);
    confirm.initOwner(stage.getOwner());
    confirm.initModality(Modality.APPLICATION_MODAL);
    confirm.setResizable(true);
    confirm.getDialogPane().getChildren().stream()
        .filter(node -> node instanceof Label)
        .forEach(node -> ((Label) node).setMinHeight(Region.USE_PREF_SIZE));
    confirm.showAndWait();
    return confirm.getResult() == ButtonType.YES;
  }

  private void wrap(final Window top, final Wrappable runnable) {
    try {
      runnable.run();
    } catch (final Exception e) {
      logger.error("Exception passed sieve.", e);
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

  private boolean confirmOverwrite(final Window top) {
    return confirmDialog(stage, "File exists, overwrite?");
  }

  private void setTitle() {
    stage.setTitle(String.format("%s - merman", filename.getFileName().toString()));
  }

  @Override
  public void start(final Stage primaryStage) {
    stage = primaryStage;
    if (getParameters().getUnnamed().isEmpty())
      throw new IllegalArgumentException("Must specify a filename as first argument.");
    filename = Paths.get(getParameters().getUnnamed().get(0));
    if (!Files.exists(filename.toAbsolutePath().normalize().getParent()))
      throw new IllegalArgumentException(
          String.format(
              "Directory of specified file must exist.\nAbsolute path: %s",
              filename.toAbsolutePath().normalize()));
    setTitle();
    final History history = new History();
    final Path path = Paths.get(getParameters().getUnnamed().get(0));
    final Syntax syntax = new Syntax();
    final Document doc;
    if (Files.exists(path)) doc = uncheck(() -> syntax.load(path));
    else doc = syntax.create();
    this.display = new JavaFXDisplay(syntax);
    editor =
        new Editor(
            syntax,
            doc,
            display,
            this::addIteration,
            this::flushIteration,
            history,
            new SimpleClipboardEngine());
    editor.addActions(
        this, ImmutableList.of(new ActionSave(), new ActionQuit(), new ActionDebug()));
    final HBox filesystemLayout = new HBox();
    filesystemLayout.setPadding(new Insets(3, 2, 3, 2));
    filesystemLayout.setSpacing(5);
    final TextField filenameEntry = new TextField(getParameters().getUnnamed().get(0));
    filesystemLayout.getChildren().add(filenameEntry);
    HBox.setHgrow(filenameEntry, Priority.ALWAYS);
    final Button save = new Button("Save");
    final Button rename = new Button("Rename");
    final Button saveAs = new Button("Save as");
    final Button backup = new Button("Back-up");
    history.addModifiedStateListener(
        modified -> {
          if (modified) {
            save.getStyleClass().add("modified");
            rename.getStyleClass().add("modified");
            saveAs.getStyleClass().add("modified");
            backup.getStyleClass().add("modified");
          } else {
            save.getStyleClass().remove("modified");
            rename.getStyleClass().remove("modified");
            saveAs.getStyleClass().remove("modified");
            backup.getStyleClass().remove("modified");
          }
        });
    final ChangeListener<String> alignFilesystemLayout =
        new ChangeListener<>() {
          @Override
          public void changed(
              final ObservableValue<? extends String> observable,
              final String oldValue,
              final String newValue) {
            wrap(
                stage.getOwner(),
                () -> {
                  if (newValue.equals(filename.toString())) {
                    filesystemLayout
                        .getChildren()
                        .removeAll(ImmutableList.of(rename, saveAs, backup));
                    filesystemLayout.getChildren().add(save);
                  } else {
                    filesystemLayout.getChildren().remove(save);
                    filesystemLayout.getChildren().addAll(ImmutableList.of(rename, saveAs, backup));
                  }
                });
          }
        };
    filenameEntry.textProperty().addListener(alignFilesystemLayout);
    alignFilesystemLayout.changed(null, null, filename.toString());
    filenameEntry.setOnAction(
        new EventHandler<>() {
          @Override
          public void handle(final ActionEvent event) {
            if (save.isVisible()) save.getOnAction().handle(null);
          }
        });
    save.setOnAction(
        new EventHandler<>() {
          @Override
          public void handle(final ActionEvent event) {
            wrap(
                stage.getOwner(),
                () -> {
                  editor.save(filename);
                  editor.focus();
                });
          }
        });
    rename.setOnAction(
        new EventHandler<>() {
          @Override
          public void handle(final ActionEvent event) {
            wrap(
                stage.getOwner(),
                () -> {
                  final Path dest = Paths.get(filenameEntry.getText());
                  if (Files.exists(dest) && !confirmOverwrite(stage.getOwner())) return;
                  editor.save(filename);
                  Files.move(filename, dest);
                  filename = Paths.get(filenameEntry.getText());
                  setTitle();
                  alignFilesystemLayout.changed(null, null, filename.toString());
                  editor.focus();
                });
          }
        });
    saveAs.setOnAction(
        new EventHandler<>() {
          @Override
          public void handle(final ActionEvent event) {
            wrap(
                stage.getOwner(),
                () -> {
                  final Path dest = Paths.get(filenameEntry.getText());
                  if (Files.exists(dest) && !confirmOverwrite(stage.getOwner())) return;
                  filename = dest;
                  setTitle();
                  editor.save(dest);
                  alignFilesystemLayout.changed(null, null, filename.toString());
                  editor.focus();
                });
          }
        });
    backup.setOnAction(
        new EventHandler<>() {
          @Override
          public void handle(final ActionEvent event) {
            wrap(
                stage.getOwner(),
                () -> {
                  final Path dest = Paths.get(filenameEntry.getText());
                  if (Files.exists(dest) && !confirmOverwrite(stage.getOwner())) return;
                  editor.save(dest);
                  filenameEntry.setText(filename.toString());
                  editor.focus();
                });
          }
        });
    final VBox mainLayout = new VBox();
    mainLayout.getChildren().add(filesystemLayout);
    mainLayout.getChildren().add(display.node);
    VBox.setVgrow(display.node, Priority.ALWAYS);
    final Scene scene = new Scene(mainLayout, 700, 500);
    stage.setScene(scene);
    stage.setOnCloseRequest(
        new EventHandler<>() {
          @Override
          public void handle(final WindowEvent t) {
            if (history.isModified()) {
              if (!confirmDialog(stage, "File has unsaved changes. Do you still want to quit?")) {
                t.consume();
                return;
              }
            }
            editor.destroy();
            worker.shutdown();
          }
        });
    stage
        .getIcons()
        .add(
            new Image(
                getClass().getResourceAsStream("/com/zarbosoft/merman/resources/icon48.png")));
    stage.show();
    editor.focus();
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

  @FunctionalInterface
  private interface Wrappable {
    void run() throws Exception;
  }

  private abstract static class ActionBase extends Action {
    public static String group() {
      return "application";
    }
  }

  @Action.StaticID(id = "quit")
  private static class ActionQuit extends ActionBase {
    @Override
    public boolean run(final Context context) {
      Platform.exit();
      return true;
    }
  }

  @Action.StaticID(id = "debug")
  private static class ActionDebug extends ActionBase {
    @Override
    public boolean run(final Context context) {
      System.out.format("This is a convenient place to put a breakpoint.\n");
      return true;
    }
  }

  @Action.StaticID(id = "save")
  private class ActionSave extends ActionBase {
    @Override
    public boolean run(final Context context) {
      editor.save(filename);
      return true;
    }
  }
}
