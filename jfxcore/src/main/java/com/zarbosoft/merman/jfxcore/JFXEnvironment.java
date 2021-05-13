package com.zarbosoft.merman.jfxcore;

import com.zarbosoft.merman.core.Environment;
import javafx.application.Platform;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;

import java.nio.charset.StandardCharsets;
import java.text.BreakIterator;
import java.time.Instant;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class JFXEnvironment implements Environment {
  private final Locale locale;
  private final Timer timer = new Timer();
  private Clipboard clipboard;

  public JFXEnvironment(Locale locale) {
    this.locale = locale;
  }

  @Override
  public Time now() {
    return new JavaTime(Instant.now());
  }

  @Override
  public Environment.HandleDelay delay(long ms, Runnable r) {
    AtomicBoolean alive = new AtomicBoolean(true);
    try {
      timer.schedule(
          new TimerTask() {
            @Override
            public void run() {
              if (!alive.get()) return;
              Platform.runLater(r);
            }
          },
          ms);
    } catch (IllegalStateException ignore) {
      // When trying to schedule while shutting down
    }
    return new HandleDelay(alive);
  }

  @Override
  public void clipboardSet(String mime, Object bytes) {
    if (clipboard == null) {
      this.clipboard = Clipboard.getSystemClipboard();
    }
    final ClipboardContent content = new ClipboardContent();
    content.put(new DataFormat(mime), bytes);
    content.putString(new String((byte[]) bytes, StandardCharsets.UTF_8));
    clipboard.setContent(content);
  }

  @Override
  public void clipboardSetString(String string) {
    if (clipboard == null) {
      this.clipboard = Clipboard.getSystemClipboard();
    }
    final ClipboardContent content = new ClipboardContent();
    content.putString(string);
    clipboard.setContent(content);
  }

  @Override
  public void clipboardGet(String mime, Consumer<Object> cb) {
    if (clipboard == null) {
      this.clipboard = Clipboard.getSystemClipboard();
    }
    byte[] out = (byte[]) clipboard.getContent(new DataFormat(mime));
    if (out == null) {
      final String temp = clipboard.getString();
      if (temp != null) {
        out = temp.getBytes(StandardCharsets.UTF_8);
      }
    }
    cb.accept(out);
  }

  @Override
  public void clipboardGetString(Consumer<String> cb) {
    if (clipboard == null) {
      this.clipboard = Clipboard.getSystemClipboard();
    }
    cb.accept(clipboard.getString());
  }

  @Override
  public Environment.I18nWalker glyphWalker(String s) {
    return new I18nWalker(BreakIterator.getCharacterInstance(locale), s);
  }

  @Override
  public Environment.I18nWalker wordWalker(String s) {
    return new I18nWalker(BreakIterator.getWordInstance(locale), s);
  }

  @Override
  public Environment.I18nWalker lineWalker(String s) {
    return new I18nWalker(BreakIterator.getLineInstance(locale), s);
  }

  @Override
  public void destroy() {
    timer.cancel();
  }

  public static class JavaTime implements Time {
    public final Instant data;

    private JavaTime(Instant data) {
      this.data = data;
    }

    @Override
    public boolean isBefore(Time other) {
      return data.isBefore(((JavaTime) other).data);
    }

    @Override
    public Time plusMillis(int count) {
      return new JavaTime(data.plusMillis(count));
    }
  }

  public static class HandleDelay implements Environment.HandleDelay {
    private AtomicBoolean alive;

    public HandleDelay(AtomicBoolean alive) {
      this.alive = alive;
    }

    @Override
    public void cancel() {
      alive.set(false);
    }
  }

  private static class I18nWalker implements Environment.I18nWalker {
    private final BreakIterator i;
    private final String text;

    private I18nWalker(BreakIterator i, String s) {
      this.i = i;
      this.text = s;
      i.setText(s);
    }

    @Override
    public int precedingStart(int offset) {
      int out = i.preceding(offset);
      if (out != -1 && Character.isWhitespace(text.codePointAt(out))) out = i.preceding(out);
      return out;
    }

    @Override
    public int precedingEnd(int offset) {
      int out = i.preceding(offset);
      if (out != -1 && !Character.isWhitespace(text.codePointAt(out))) out = i.preceding(out);
      if (out == -1 && offset > 0) return 0;
      return out;
    }

    @Override
    public int followingStart(int offset) {
      int out = i.following(offset);
      if (out != -1 && out < text.length() && Character.isWhitespace(text.codePointAt(out)))
        out = i.following(out);
      return out;
    }

    @Override
    public int followingEnd(int offset) {
      int out = i.following(offset);
      if (out != -1 && out < text.length() && !Character.isWhitespace(text.codePointAt(out)))
        out = i.following(out);
      return out;
    }
  }
}
