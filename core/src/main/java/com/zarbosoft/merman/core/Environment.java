package com.zarbosoft.merman.core;

import com.zarbosoft.merman.core.syntax.primitivepattern.CharacterEvent;
import com.zarbosoft.pidgoon.events.Event;
import com.zarbosoft.rendaw.common.TSList;

import java.util.function.Consumer;

/** Abstraction over the runtime environment (javascript, javafx, etc) */
public interface Environment {
  public static final int I18N_DONE = -1;

  Time now();

  HandleDelay delay(long ms, Runnable r);

  void clipboardSet(String mime, Object bytes);

  void clipboardSetString(String string);

  void clipboardGet(String mime, Consumer<Object> cb);

  void clipboardGetString(Consumer<String> cb);

  /**
   * @param text
   * @return list of CharacterEvent
   */
  default TSList<Event> splitGlyphs(String text) {
    I18nWalker walker = glyphWalker(text);
    TSList<String> glyphs1 = TSList.of();
    int end = 0;
    while (true) {
      int start = end;
      end = walker.followingStart(end);
      if (end == I18N_DONE) break;
      glyphs1.add(text.substring(start, end));
    }
    TSList<String> pre = glyphs1;
    TSList<Event> glyphs = TSList.of();
    for (String s : pre) {
      glyphs.add(new CharacterEvent(s));
    }
    return glyphs;
  }

  default String joinGlyphs(TSList<CharacterEvent> glyphs) {
    int size = 0;
    for (CharacterEvent glyph : glyphs) {
      size += glyph.value.length();
    }
    StringBuilder builder = new StringBuilder(size);
    for (CharacterEvent glyph : glyphs) {
      builder.append(glyph.value);
    }
    return builder.toString();
  }

  public I18nWalker glyphWalker(String s);

  public I18nWalker wordWalker(String s);

  /**
   * Suitable places to break lines
   * @param s
   * @return
   */
  public I18nWalker lineWalker(String s);

  public interface Time {
    boolean isBefore(Time other);

    Time plusMillis(int count);
  }

  public interface HandleDelay {
    void cancel();
  }

  public static interface I18nWalker {
    /**
     * Offset of start of next element starting before offset, I18N_DONE if at start
     *
     * <p>Includes 0
     *
     * @param offset
     * @return
     */
    int precedingStart(int offset);

    /**
     * Offset of end of next element ending before offset, I18N_DONE if at start
     *
     * <p>Includes 0
     *
     * @param offset
     * @return
     */
    int precedingEnd(int offset);

    /**
     * Offset of start of next element starting after offset, I18N_DONE if at end
     *
     * <p>Includes string.size()
     *
     * @param offset
     * @return
     */
    int followingStart(int offset);

    /**
     * Offset of end of next element ending after offset, I18N_DONE if at end
     *
     * <p>Includes string.size()
     *
     * @param offset
     * @return
     */
    int followingEnd(int offset);
  }
}
