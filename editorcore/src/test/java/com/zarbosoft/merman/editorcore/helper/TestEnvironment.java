package com.zarbosoft.merman.editorcore.helper;

import com.zarbosoft.merman.core.Environment;
import com.zarbosoft.rendaw.common.TSList;

import java.util.function.Consumer;

public class TestEnvironment implements Environment {
  byte[] data = null;
  String string = null;

  @Override
  public Time now() {
    return new Time() {
      @Override
      public boolean isBefore(Time other) {
        return false;
      }

      @Override
      public Time plusMillis(int count) {
        return this;
      }
    };
  }

  @Override
  public HandleDelay delay(long ms, Runnable r) {
    // nop
    return new HandleDelay() {
      @Override
      public void cancel() {}
    };
  }

  @Override
  public void clipboardSet(String mime, Object bytes) {
    data = (byte[]) bytes;
  }

  @Override
  public void clipboardSetString(String string) {
    this.string = string;
  }

  @Override
  public void clipboardGet(String mime, Consumer<Object> cb) {
    cb.accept(data);
  }

  @Override
  public void clipboardGetString(Consumer<String> cb) {
    cb.accept(string);
  }

  @Override
  public I18nWalker glyphWalker(String s) {
    return new I18nWalker() {
      @Override
      public int precedingStart(int offset) {
        if (offset == 0) return I18N_DONE;
        return offset - 1;
      }

      @Override
      public int precedingEnd(int offset) {
        return precedingStart(offset);
      }

      @Override
      public int followingStart(int offset) {
        if (offset == s.length()) return I18N_DONE;
        return offset + 1;
      }

      @Override
      public int followingEnd(int offset) {
        return followingStart(offset);
      }
    };
  }

  @Override
  public I18nWalker wordWalker(String s) {
    return new I18nWalker() {
      private boolean isSpace(int i) {
        if (i < 0 || i >= s.length()) return true;
        return s.charAt(i) == ' ' || s.charAt(i) == '\n';
      }

      @Override
      public int precedingStart(int offset) {
        if (offset == 0) return I18N_DONE;
        for (int i = offset - 1; i > 0; --i) {
          if (isSpace(i - 1) && !isSpace(i)) return i;
        }
        return 0;
      }

      @Override
      public int precedingEnd(int offset) {
        if (offset == 0) return I18N_DONE;
        for (int i = offset - 1; i > 0; --i) {
          if (!isSpace(i - 1) && isSpace(i)) return i;
        }
        return 0;
      }

      @Override
      public int followingStart(int offset) {
        if (offset == s.length()) return I18N_DONE;
        for (int i = offset + 1; i <= s.length(); ++i) {
          if (isSpace(i - 1) && !isSpace(i)) return i;
        }
        return s.length();
      }

      @Override
      public int followingEnd(int offset) {
        if (offset == s.length()) return I18N_DONE;
        for (int i = offset + 1; i <= s.length(); ++i) {
          if (!isSpace(i - 1) && isSpace(i)) return i;
        }
        return s.length();
      }
    };
  }

  @Override
  public I18nWalker lineWalker(String s) {
    return wordWalker(s);
  }

  @Override
  public void destroy() {
  }
}
