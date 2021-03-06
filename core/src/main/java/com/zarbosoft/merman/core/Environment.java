package com.zarbosoft.merman.core;

import com.zarbosoft.merman.core.syntax.primitivepattern.CharacterEvent;
import com.zarbosoft.pidgoon.events.Event;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.TSList;

import java.util.function.Consumer;

/** Abstraction over the runtime environment (javascript, javafx, etc) */
public interface Environment {
  public static final int I18N_DONE = -1;

  public static String joinGlyphEvents(ROList<CharacterEvent> glyphs) {
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

  public static String joinGlyphs(ROList<String> glyphs) {
    int size = 0;
    for (String glyph : glyphs) {
      size += glyph.length();
    }
    StringBuilder builder = new StringBuilder(size);
    for (String glyph : glyphs) {
      builder.append(glyph);
    }
    return builder.toString();
  }

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
  default TSList<Event> splitGlyphEvents(String text) {
    TSList<String> pre = splitGlyphs(text);
    TSList<Event> glyphs = TSList.of();
    for (String s : pre) {
      glyphs.add(new CharacterEvent(s));
    }
    return glyphs;
  }

  /**
   * @param text
   * @return list of CharacterEvent
   */
  default TSList<String> splitGlyphs(String text) {
    I18nWalker walker = glyphWalker(text);
    TSList<String> glyphs = TSList.of();
    int end = 0;
    while (true) {
      int start = end;
      end = walker.followingStart(end);
      if (end == I18N_DONE) break;
      glyphs.add(text.substring(start, end));
    }
    return glyphs;
  }

  public I18nWalker glyphWalker(String s);

  public I18nWalker wordWalker(String s);

  /**
   * Suitable places to break lines
   *
   * @param s
   * @return
   */
  public I18nWalker lineWalker(String s);

  void destroy();

  public interface Time {
    boolean isBefore(Time other);

    Time plusMillis(int count);
  }

  public interface HandleDelay {
    void cancel();
  }

  /**
   * Moves in larger increments: doesn't flop before/after whitespace between words (1 glyph
   * movement)
   */
  public static interface FixedWordI18nWalker extends I18nWalker {
    int precedingAny(int offset);

    int followingAny(int offset);

    boolean isWhitespace(String glyph);

    String charAt(int offset);

    int length();

    @Override
    default int precedingStart(int offset) {
      int out = precedingAny(offset);
      if (out != -1 && isWhitespace(charAt(out))) out = precedingAny(out);
      return out;
    }

    @Override
    default int precedingEnd(int offset) {
      int out = precedingAny(offset);
      if (out != -1 && !isWhitespace(charAt(out))) out = precedingAny(out);
      if (out == -1 && offset > 0) return 0;
      return out;
    }

    @Override
    default int followingStart(int offset) {
      int out = followingAny(offset);
      if (out != -1 && out < length() && isWhitespace(charAt(out))) out = followingAny(out);
      return out;
    }

    @Override
    default int followingEnd(int offset) {
      int out = followingAny(offset);
      if (out != -1 && out < length() && !isWhitespace(charAt(out))) out = followingAny(out);
      return out;
    }
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
