package com.zarbosoft.merman.core.editor.hid;

public enum Key {
  MOUSE_1,
  MOUSE_2,
  MOUSE_3,
  MOUSE_SCROLL_UP,
  MOUSE_SCROLL_DOWN,
  ENTER,
  BACK_SPACE,
  TAB,
  CANCEL,
  CLEAR,
  SHIFT,
  CONTROL,
  ALT,
  PAUSE,
  CAPS,
  ESCAPE,
  SPACE,
  PAGE_UP,
  PAGE_DOWN,
  END,
  HOME,
  LEFT,
  UP,
  RIGHT,
  DOWN,
  COMMA,
  MINUS,
  PERIOD,
  SLASH,
  DIGIT0,
  DIGIT1,
  DIGIT2,
  DIGIT3,
  DIGIT4,
  DIGIT5,
  DIGIT6,
  DIGIT7,
  DIGIT8,
  DIGIT9,
  SEMICOLON,
  EQUALS,
  A,
  B,
  C,
  D,
  E,
  F,
  G,
  H,
  I,
  J,
  K,
  L,
  M,
  N,
  O,
  P,
  Q,
  R,
  S,
  T,
  U,
  V,
  W,
  X,
  Y,
  Z,
  OPEN_BRACKET,
  BACK_SLASH,
  CLOSE_BRACKET,
  NUMPAD0,
  NUMPAD1,
  NUMPAD2,
  NUMPAD3,
  NUMPAD4,
  NUMPAD5,
  NUMPAD6,
  NUMPAD7,
  NUMPAD8,
  NUMPAD9,
  NUMPAD_ADD,
  NUMPAD_CHANGESIGN,
  NUMPAD_COMMA,
  NUMPAD_DECIMAL,
  NUMPAD_DIVIDE,
  NUMPAD_ENTER,
  NUMPAD_EQUAL,
  NUMPAD_MULTIPLY,
  NUMPAD_LEFTPAREN,
  NUMPAD_RIGHTPAREN,
  NUMPAD_SUBTRACT,
  MULTIPLY,
  ADD,
  SEPARATOR,
  SUBTRACT,
  DECIMAL,
  DIVIDE,
  DELETE,
  NUM_LOCK,
  SCROLL_LOCK,
  F1,
  F2,
  F3,
  F4,
  F5,
  F6,
  F7,
  F8,
  F9,
  F10,
  F11,
  F12,
  F13,
  F14,
  F15,
  F16,
  F17,
  F18,
  F19,
  F20,
  F21,
  F22,
  F23,
  F24,
  PRINTSCREEN,
  INSERT,
  HELP,
  META,
  BACK_QUOTE,
  QUOTE,
  KP_UP,
  KP_DOWN,
  KP_LEFT,
  KP_RIGHT,
  DEAD_GRAVE,
  DEAD_ACUTE,
  DEAD_CIRCUMFLEX,
  DEAD_TILDE,
  DEAD_MACRON,
  DEAD_BREVE,
  DEAD_ABOVEDOT,
  DEAD_DIAERESIS,
  DEAD_ABOVERING,
  DEAD_DOUBLEACUTE,
  DEAD_CARON,
  DEAD_CEDILLA,
  DEAD_OGONEK,
  DEAD_IOTA,
  DEAD_VOICED_SOUND,
  DEAD_SEMIVOICED_SOUND,
  AMPERSAND,
  ASTERISK,
  QUOTEDBL,
  LESS,
  GREATER,
  BRACELEFT,
  BRACERIGHT,
  AT,
  COLON,
  CIRCUMFLEX,
  DOLLAR,
  EURO_SIGN,
  EXCLAMATION_MARK,
  INVERTED_EXCLAMATION_MARK,
  LEFT_PARENTHESIS,
  NUMBER_SIGN,
  PLUS,
  RIGHT_PARENTHESIS,
  UNDERSCORE,
  WINDOWS,
  CONTEXT_MENU,
  FINAL,
  CONVERT,
  NONCONVERT,
  ACCEPT,
  MODECHANGE,
  KANA,
  KANJI,
  ALPHANUMERIC,
  KATAKANA,
  HIRAGANA,
  FULL_WIDTH,
  HALF_WIDTH,
  ROMAN_CHARACTERS,
  ALL_CANDIDATES,
  PREVIOUS_CANDIDATE,
  CODE_INPUT,
  JAPANESE_KATAKANA,
  JAPANESE_HIRAGANA,
  JAPANESE_ROMAN,
  KANA_LOCK,
  INPUT_METHOD_ON_OFF,
  CUT,
  COPY,
  PASTE,
  UNDO,
  AGAIN,
  FIND,
  PROPS,
  STOP,
  COMPOSE,
  ALT_GRAPH,
  BEGIN,
  UNDEFINED,
  SOFTKEY_0,
  SOFTKEY_1,
  SOFTKEY_2,
  SOFTKEY_3,
  SOFTKEY_4,
  SOFTKEY_5,
  SOFTKEY_6,
  SOFTKEY_7,
  SOFTKEY_8,
  SOFTKEY_9,
  GAME_A,
  GAME_B,
  GAME_C,
  GAME_D,
  STAR,
  POUND,
  POWER,
  INFO,
  COLORED_KEY_0,
  COLORED_KEY_1,
  COLORED_KEY_2,
  COLORED_KEY_3,
  EJECT_TOGGLE,
  PLAY,
  RECORD,
  FAST_FWD,
  REWIND,
  TRACK_PREV,
  TRACK_NEXT,
  CHANNEL_UP,
  CHANNEL_DOWN,
  VOLUME_UP,
  VOLUME_DOWN,
  MUTE,
  COMMAND,
  SHORTCUT,
  ALT_LEFT, ALT_RIGHT, BROWSER_BACK, BROWSER_FAVORITES, BROWSER_FORWARD, BROWSER_HOME, BROWSER_REFRESH, BROWSER_SEARCH, BROWSER_STOP, CONTROL_LEFT, CONTROL_RIGHT, INTL_HANGUL_MODE, INTL_HANJA, INTL_BACK_SLASH, INTL_RO, INTL_YEN, MEDIA_PLAY_PAUSE, MEDIA_STOP, MEDIA_NEXT, MEDIA_PREVIOUS, META_LEFT, META_RIGHT, OPEN, SHIFT_LEFT, SHIFT_RIGHT, SELECT, WAKE, LANG1, LANG2, LAUNCH_MEDIA_PLAYER, LAUNCH_MAIL, LAUNCH_APP2, LAUNCH_APP1, OS_RIGHT, OS_LEFT, MOUSE_4, MOUSE_5, MOUSE_SCROLL_LEFT, MOUSE_SCROLL_RIGHT, MOUSE_SCROLL_IN, MOUSE_SCROLL_OUT;

  public static Key fromChar(final char at) {
    switch (at) {
      case 'a':
        return A;
      case 'b':
        return B;
      case 'c':
        return C;
      case 'd':
        return D;
      case 'e':
        return E;
      case 'f':
        return F;
      case 'g':
        return G;
      case 'h':
        return H;
      case 'i':
        return I;
      case 'j':
        return J;
      case 'k':
        return K;
      case 'l':
        return L;
      case 'm':
        return M;
      case 'n':
        return N;
      case 'o':
        return O;
      case 'p':
        return P;
      case 'q':
        return Q;
      case 'r':
        return R;
      case 's':
        return S;
      case 't':
        return T;
      case 'u':
        return U;
      case 'v':
        return V;
      case 'w':
        return W;
      case 'x':
        return X;
      case 'y':
        return Y;
      case 'z':
        return Z;
      case '-':
        return MINUS;
      case ':':
        return COLON;
    }
    throw new IllegalArgumentException();
  }
}
