package com.zarbosoft.merman.jfxcore;

import com.zarbosoft.merman.core.Environment;
import com.zarbosoft.rendaw.common.TSMap;
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
  public static TSMap<String, DataFormat> dataFormats = new TSMap<>();
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
    content.put(dataFormats.getCreate(mime, () -> new DataFormat(mime)), bytes);
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
    return new I18nWalker() {
      private final BreakIterator iter;

      {
        iter = BreakIterator.getCharacterInstance(locale);
      }

      @Override
      public int precedingStart(int offset) {
        return iter.preceding(offset);
      }

      @Override
      public int precedingEnd(int offset) {
        return iter.preceding(offset);
      }

      @Override
      public int followingStart(int offset) {
        return iter.following(offset);
      }

      @Override
      public int followingEnd(int offset) {
        return iter.following(offset);
      }
    };
  }

  @Override
  public Environment.I18nWalker wordWalker(String s) {
    return new FixedWordI18nWalker() {
      private final String text;
      private final BreakIterator iter;

      {
        iter = BreakIterator.getWordInstance(locale);
        text = s;
      }

      @Override
      public int precedingAny(int offset) {
        return iter.preceding(offset);
      }

      @Override
      public int followingAny(int offset) {
        return iter.following(offset);
      }

      @Override
      public boolean isWhitespace(String glyph) {
        return Character.isWhitespace(glyph.codePointAt(0));
      }

      @Override
      public String charAt(int offset) {
        return text.substring(offset, 1);
      }

      @Override
      public int length() {
        return text.length();
      }
    };
  }

  @Override
  public Environment.I18nWalker lineWalker(String s) {
    return new I18nWalker() {
      private final BreakIterator iter;

      {
        iter = BreakIterator.getLineInstance(locale);
      }

      @Override
      public int precedingStart(int offset) {
        return iter.preceding(offset);
      }

      @Override
      public int precedingEnd(int offset) {
        return iter.preceding(offset);
      }

      @Override
      public int followingStart(int offset) {
        return iter.following(offset);
      }

      @Override
      public int followingEnd(int offset) {
        return iter.following(offset);
      }
    };
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
    private final AtomicBoolean alive;

    public HandleDelay(AtomicBoolean alive) {
      this.alive = alive;
    }

    @Override
    public void cancel() {
      alive.set(false);
    }
  }
}
