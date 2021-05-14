package com.zarbosoft.merman.jfxcore.display;

import com.zarbosoft.merman.core.display.Blank;
import com.zarbosoft.merman.core.display.Display;
import com.zarbosoft.merman.core.display.DisplayNode;
import com.zarbosoft.merman.core.display.Drawing;
import com.zarbosoft.merman.core.display.Font;
import com.zarbosoft.merman.core.display.Group;
import com.zarbosoft.merman.core.display.Image;
import com.zarbosoft.merman.core.display.Text;
import com.zarbosoft.merman.core.hid.ButtonEvent;
import com.zarbosoft.merman.core.hid.Key;
import com.zarbosoft.merman.core.syntax.Direction;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.core.syntax.style.ModelColor;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.DeadCode;
import com.zarbosoft.rendaw.common.TSSet;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;

import java.util.ArrayList;
import java.util.List;

import static com.zarbosoft.merman.core.hid.Key.A;
import static com.zarbosoft.merman.core.hid.Key.ACCEPT;
import static com.zarbosoft.merman.core.hid.Key.ADD;
import static com.zarbosoft.merman.core.hid.Key.AGAIN;
import static com.zarbosoft.merman.core.hid.Key.ALL_CANDIDATES;
import static com.zarbosoft.merman.core.hid.Key.ALPHANUMERIC;
import static com.zarbosoft.merman.core.hid.Key.ALT;
import static com.zarbosoft.merman.core.hid.Key.ALT_GRAPH;
import static com.zarbosoft.merman.core.hid.Key.AMPERSAND;
import static com.zarbosoft.merman.core.hid.Key.ASTERISK;
import static com.zarbosoft.merman.core.hid.Key.AT;
import static com.zarbosoft.merman.core.hid.Key.B;
import static com.zarbosoft.merman.core.hid.Key.BACK_QUOTE;
import static com.zarbosoft.merman.core.hid.Key.BACK_SLASH;
import static com.zarbosoft.merman.core.hid.Key.BACK_SPACE;
import static com.zarbosoft.merman.core.hid.Key.BEGIN;
import static com.zarbosoft.merman.core.hid.Key.BRACELEFT;
import static com.zarbosoft.merman.core.hid.Key.BRACERIGHT;
import static com.zarbosoft.merman.core.hid.Key.C;
import static com.zarbosoft.merman.core.hid.Key.CANCEL;
import static com.zarbosoft.merman.core.hid.Key.CAPS;
import static com.zarbosoft.merman.core.hid.Key.CHANNEL_DOWN;
import static com.zarbosoft.merman.core.hid.Key.CHANNEL_UP;
import static com.zarbosoft.merman.core.hid.Key.CIRCUMFLEX;
import static com.zarbosoft.merman.core.hid.Key.CLEAR;
import static com.zarbosoft.merman.core.hid.Key.CLOSE_BRACKET;
import static com.zarbosoft.merman.core.hid.Key.CODE_INPUT;
import static com.zarbosoft.merman.core.hid.Key.COLON;
import static com.zarbosoft.merman.core.hid.Key.COLORED_KEY_0;
import static com.zarbosoft.merman.core.hid.Key.COLORED_KEY_1;
import static com.zarbosoft.merman.core.hid.Key.COLORED_KEY_2;
import static com.zarbosoft.merman.core.hid.Key.COLORED_KEY_3;
import static com.zarbosoft.merman.core.hid.Key.COMMA;
import static com.zarbosoft.merman.core.hid.Key.COMMAND;
import static com.zarbosoft.merman.core.hid.Key.COMPOSE;
import static com.zarbosoft.merman.core.hid.Key.CONTEXT_MENU;
import static com.zarbosoft.merman.core.hid.Key.CONTROL;
import static com.zarbosoft.merman.core.hid.Key.CONVERT;
import static com.zarbosoft.merman.core.hid.Key.COPY;
import static com.zarbosoft.merman.core.hid.Key.CUT;
import static com.zarbosoft.merman.core.hid.Key.D;
import static com.zarbosoft.merman.core.hid.Key.DEAD_ABOVEDOT;
import static com.zarbosoft.merman.core.hid.Key.DEAD_ABOVERING;
import static com.zarbosoft.merman.core.hid.Key.DEAD_ACUTE;
import static com.zarbosoft.merman.core.hid.Key.DEAD_BREVE;
import static com.zarbosoft.merman.core.hid.Key.DEAD_CARON;
import static com.zarbosoft.merman.core.hid.Key.DEAD_CEDILLA;
import static com.zarbosoft.merman.core.hid.Key.DEAD_CIRCUMFLEX;
import static com.zarbosoft.merman.core.hid.Key.DEAD_DIAERESIS;
import static com.zarbosoft.merman.core.hid.Key.DEAD_DOUBLEACUTE;
import static com.zarbosoft.merman.core.hid.Key.DEAD_GRAVE;
import static com.zarbosoft.merman.core.hid.Key.DEAD_IOTA;
import static com.zarbosoft.merman.core.hid.Key.DEAD_MACRON;
import static com.zarbosoft.merman.core.hid.Key.DEAD_OGONEK;
import static com.zarbosoft.merman.core.hid.Key.DEAD_SEMIVOICED_SOUND;
import static com.zarbosoft.merman.core.hid.Key.DEAD_TILDE;
import static com.zarbosoft.merman.core.hid.Key.DEAD_VOICED_SOUND;
import static com.zarbosoft.merman.core.hid.Key.DECIMAL;
import static com.zarbosoft.merman.core.hid.Key.DELETE;
import static com.zarbosoft.merman.core.hid.Key.DIGIT0;
import static com.zarbosoft.merman.core.hid.Key.DIGIT1;
import static com.zarbosoft.merman.core.hid.Key.DIGIT2;
import static com.zarbosoft.merman.core.hid.Key.DIGIT3;
import static com.zarbosoft.merman.core.hid.Key.DIGIT4;
import static com.zarbosoft.merman.core.hid.Key.DIGIT5;
import static com.zarbosoft.merman.core.hid.Key.DIGIT6;
import static com.zarbosoft.merman.core.hid.Key.DIGIT7;
import static com.zarbosoft.merman.core.hid.Key.DIGIT8;
import static com.zarbosoft.merman.core.hid.Key.DIGIT9;
import static com.zarbosoft.merman.core.hid.Key.DIVIDE;
import static com.zarbosoft.merman.core.hid.Key.DOLLAR;
import static com.zarbosoft.merman.core.hid.Key.DOWN;
import static com.zarbosoft.merman.core.hid.Key.E;
import static com.zarbosoft.merman.core.hid.Key.EJECT_TOGGLE;
import static com.zarbosoft.merman.core.hid.Key.END;
import static com.zarbosoft.merman.core.hid.Key.ENTER;
import static com.zarbosoft.merman.core.hid.Key.EQUALS;
import static com.zarbosoft.merman.core.hid.Key.ESCAPE;
import static com.zarbosoft.merman.core.hid.Key.EURO_SIGN;
import static com.zarbosoft.merman.core.hid.Key.EXCLAMATION_MARK;
import static com.zarbosoft.merman.core.hid.Key.F;
import static com.zarbosoft.merman.core.hid.Key.F1;
import static com.zarbosoft.merman.core.hid.Key.F10;
import static com.zarbosoft.merman.core.hid.Key.F11;
import static com.zarbosoft.merman.core.hid.Key.F12;
import static com.zarbosoft.merman.core.hid.Key.F13;
import static com.zarbosoft.merman.core.hid.Key.F14;
import static com.zarbosoft.merman.core.hid.Key.F15;
import static com.zarbosoft.merman.core.hid.Key.F16;
import static com.zarbosoft.merman.core.hid.Key.F17;
import static com.zarbosoft.merman.core.hid.Key.F18;
import static com.zarbosoft.merman.core.hid.Key.F19;
import static com.zarbosoft.merman.core.hid.Key.F2;
import static com.zarbosoft.merman.core.hid.Key.F20;
import static com.zarbosoft.merman.core.hid.Key.F21;
import static com.zarbosoft.merman.core.hid.Key.F22;
import static com.zarbosoft.merman.core.hid.Key.F23;
import static com.zarbosoft.merman.core.hid.Key.F24;
import static com.zarbosoft.merman.core.hid.Key.F3;
import static com.zarbosoft.merman.core.hid.Key.F4;
import static com.zarbosoft.merman.core.hid.Key.F5;
import static com.zarbosoft.merman.core.hid.Key.F6;
import static com.zarbosoft.merman.core.hid.Key.F7;
import static com.zarbosoft.merman.core.hid.Key.F8;
import static com.zarbosoft.merman.core.hid.Key.F9;
import static com.zarbosoft.merman.core.hid.Key.FAST_FWD;
import static com.zarbosoft.merman.core.hid.Key.FINAL;
import static com.zarbosoft.merman.core.hid.Key.FIND;
import static com.zarbosoft.merman.core.hid.Key.FULL_WIDTH;
import static com.zarbosoft.merman.core.hid.Key.G;
import static com.zarbosoft.merman.core.hid.Key.GAME_A;
import static com.zarbosoft.merman.core.hid.Key.GAME_B;
import static com.zarbosoft.merman.core.hid.Key.GAME_C;
import static com.zarbosoft.merman.core.hid.Key.GAME_D;
import static com.zarbosoft.merman.core.hid.Key.GREATER;
import static com.zarbosoft.merman.core.hid.Key.H;
import static com.zarbosoft.merman.core.hid.Key.HALF_WIDTH;
import static com.zarbosoft.merman.core.hid.Key.HELP;
import static com.zarbosoft.merman.core.hid.Key.HIRAGANA;
import static com.zarbosoft.merman.core.hid.Key.HOME;
import static com.zarbosoft.merman.core.hid.Key.I;
import static com.zarbosoft.merman.core.hid.Key.INFO;
import static com.zarbosoft.merman.core.hid.Key.INPUT_METHOD_ON_OFF;
import static com.zarbosoft.merman.core.hid.Key.INSERT;
import static com.zarbosoft.merman.core.hid.Key.INVERTED_EXCLAMATION_MARK;
import static com.zarbosoft.merman.core.hid.Key.J;
import static com.zarbosoft.merman.core.hid.Key.JAPANESE_HIRAGANA;
import static com.zarbosoft.merman.core.hid.Key.JAPANESE_KATAKANA;
import static com.zarbosoft.merman.core.hid.Key.JAPANESE_ROMAN;
import static com.zarbosoft.merman.core.hid.Key.K;
import static com.zarbosoft.merman.core.hid.Key.KANA;
import static com.zarbosoft.merman.core.hid.Key.KANA_LOCK;
import static com.zarbosoft.merman.core.hid.Key.KANJI;
import static com.zarbosoft.merman.core.hid.Key.KATAKANA;
import static com.zarbosoft.merman.core.hid.Key.KP_DOWN;
import static com.zarbosoft.merman.core.hid.Key.KP_LEFT;
import static com.zarbosoft.merman.core.hid.Key.KP_RIGHT;
import static com.zarbosoft.merman.core.hid.Key.KP_UP;
import static com.zarbosoft.merman.core.hid.Key.L;
import static com.zarbosoft.merman.core.hid.Key.LEFT;
import static com.zarbosoft.merman.core.hid.Key.LEFT_PARENTHESIS;
import static com.zarbosoft.merman.core.hid.Key.LESS;
import static com.zarbosoft.merman.core.hid.Key.M;
import static com.zarbosoft.merman.core.hid.Key.META;
import static com.zarbosoft.merman.core.hid.Key.MINUS;
import static com.zarbosoft.merman.core.hid.Key.MODECHANGE;
import static com.zarbosoft.merman.core.hid.Key.MOUSE_1;
import static com.zarbosoft.merman.core.hid.Key.MOUSE_2;
import static com.zarbosoft.merman.core.hid.Key.MOUSE_3;
import static com.zarbosoft.merman.core.hid.Key.MULTIPLY;
import static com.zarbosoft.merman.core.hid.Key.MUTE;
import static com.zarbosoft.merman.core.hid.Key.N;
import static com.zarbosoft.merman.core.hid.Key.NONCONVERT;
import static com.zarbosoft.merman.core.hid.Key.NUMBER_SIGN;
import static com.zarbosoft.merman.core.hid.Key.NUMPAD0;
import static com.zarbosoft.merman.core.hid.Key.NUMPAD1;
import static com.zarbosoft.merman.core.hid.Key.NUMPAD2;
import static com.zarbosoft.merman.core.hid.Key.NUMPAD3;
import static com.zarbosoft.merman.core.hid.Key.NUMPAD4;
import static com.zarbosoft.merman.core.hid.Key.NUMPAD5;
import static com.zarbosoft.merman.core.hid.Key.NUMPAD6;
import static com.zarbosoft.merman.core.hid.Key.NUMPAD7;
import static com.zarbosoft.merman.core.hid.Key.NUMPAD8;
import static com.zarbosoft.merman.core.hid.Key.NUMPAD9;
import static com.zarbosoft.merman.core.hid.Key.NUM_LOCK;
import static com.zarbosoft.merman.core.hid.Key.O;
import static com.zarbosoft.merman.core.hid.Key.OPEN_BRACKET;
import static com.zarbosoft.merman.core.hid.Key.P;
import static com.zarbosoft.merman.core.hid.Key.PAGE_DOWN;
import static com.zarbosoft.merman.core.hid.Key.PAGE_UP;
import static com.zarbosoft.merman.core.hid.Key.PASTE;
import static com.zarbosoft.merman.core.hid.Key.PAUSE;
import static com.zarbosoft.merman.core.hid.Key.PERIOD;
import static com.zarbosoft.merman.core.hid.Key.PLAY;
import static com.zarbosoft.merman.core.hid.Key.PLUS;
import static com.zarbosoft.merman.core.hid.Key.POUND;
import static com.zarbosoft.merman.core.hid.Key.POWER;
import static com.zarbosoft.merman.core.hid.Key.PREVIOUS_CANDIDATE;
import static com.zarbosoft.merman.core.hid.Key.PRINTSCREEN;
import static com.zarbosoft.merman.core.hid.Key.PROPS;
import static com.zarbosoft.merman.core.hid.Key.Q;
import static com.zarbosoft.merman.core.hid.Key.QUOTE;
import static com.zarbosoft.merman.core.hid.Key.QUOTEDBL;
import static com.zarbosoft.merman.core.hid.Key.R;
import static com.zarbosoft.merman.core.hid.Key.RECORD;
import static com.zarbosoft.merman.core.hid.Key.REWIND;
import static com.zarbosoft.merman.core.hid.Key.RIGHT;
import static com.zarbosoft.merman.core.hid.Key.RIGHT_PARENTHESIS;
import static com.zarbosoft.merman.core.hid.Key.ROMAN_CHARACTERS;
import static com.zarbosoft.merman.core.hid.Key.S;
import static com.zarbosoft.merman.core.hid.Key.SCROLL_LOCK;
import static com.zarbosoft.merman.core.hid.Key.SEMICOLON;
import static com.zarbosoft.merman.core.hid.Key.SEPARATOR;
import static com.zarbosoft.merman.core.hid.Key.SHIFT;
import static com.zarbosoft.merman.core.hid.Key.SHORTCUT;
import static com.zarbosoft.merman.core.hid.Key.SLASH;
import static com.zarbosoft.merman.core.hid.Key.SOFTKEY_0;
import static com.zarbosoft.merman.core.hid.Key.SOFTKEY_1;
import static com.zarbosoft.merman.core.hid.Key.SOFTKEY_2;
import static com.zarbosoft.merman.core.hid.Key.SOFTKEY_3;
import static com.zarbosoft.merman.core.hid.Key.SOFTKEY_4;
import static com.zarbosoft.merman.core.hid.Key.SOFTKEY_5;
import static com.zarbosoft.merman.core.hid.Key.SOFTKEY_6;
import static com.zarbosoft.merman.core.hid.Key.SOFTKEY_7;
import static com.zarbosoft.merman.core.hid.Key.SOFTKEY_8;
import static com.zarbosoft.merman.core.hid.Key.SOFTKEY_9;
import static com.zarbosoft.merman.core.hid.Key.SPACE;
import static com.zarbosoft.merman.core.hid.Key.STAR;
import static com.zarbosoft.merman.core.hid.Key.STOP;
import static com.zarbosoft.merman.core.hid.Key.SUBTRACT;
import static com.zarbosoft.merman.core.hid.Key.T;
import static com.zarbosoft.merman.core.hid.Key.TAB;
import static com.zarbosoft.merman.core.hid.Key.TRACK_NEXT;
import static com.zarbosoft.merman.core.hid.Key.TRACK_PREV;
import static com.zarbosoft.merman.core.hid.Key.U;
import static com.zarbosoft.merman.core.hid.Key.UNDEFINED;
import static com.zarbosoft.merman.core.hid.Key.UNDERSCORE;
import static com.zarbosoft.merman.core.hid.Key.UNDO;
import static com.zarbosoft.merman.core.hid.Key.UP;
import static com.zarbosoft.merman.core.hid.Key.V;
import static com.zarbosoft.merman.core.hid.Key.VOLUME_DOWN;
import static com.zarbosoft.merman.core.hid.Key.VOLUME_UP;
import static com.zarbosoft.merman.core.hid.Key.W;
import static com.zarbosoft.merman.core.hid.Key.WINDOWS;
import static com.zarbosoft.merman.core.hid.Key.X;
import static com.zarbosoft.merman.core.hid.Key.Y;
import static com.zarbosoft.merman.core.hid.Key.Z;

public class JavaFXDisplay extends Display {
  public final Pane node = new Pane();
  private final javafx.scene.Group origin = new javafx.scene.Group();

  TSSet<Key> modifiers = new TSSet<>();
  List<DoubleListener> converseEdgeListeners = new ArrayList<>();
  List<DoubleListener> transverseEdgeListeners = new ArrayList<>();
  int oldConverseEdge = Integer.MAX_VALUE;
  int oldTransverseEdge = Integer.MAX_VALUE;

  public JavaFXDisplay(final Syntax syntax) {
    super(syntax.converseDirection, syntax.transverseDirection);
    node.setSnapToPixel(true);
    node.setFocusTraversable(true);
    node.getChildren().add(origin);
    node.setOnMouseEntered(event -> {
        node.requestFocus();
    });
    node.setOnMouseExited(
        event -> {
          mouseExited();
        });
    node.addEventFilter(
        MouseEvent.MOUSE_MOVED,
        event -> {
          mouseMoved(event.getX(), event.getY());
        });
    node.addEventFilter(
        MouseEvent.MOUSE_DRAGGED,
        event -> {
          mouseMoved(event.getX(), event.getY());
        });
    node.addEventFilter(
        MouseEvent.MOUSE_PRESSED,
        e -> {
          node.requestFocus();
          if (this.keyEventListener.apply(buildHIDEvent(convertButton(e.getButton()), true))) {
            e.consume();
          }
        });
    node.addEventFilter(
        MouseEvent.MOUSE_RELEASED,
        e -> {
          node.requestFocus();
          if (this.keyEventListener.apply(buildHIDEvent(convertButton(e.getButton()), false))) {
            e.consume();
          }
        });
    node.setOnScroll(
        e -> {
          node.requestFocus();
          if (this.keyEventListener.apply(
              buildHIDEvent(
                  e.getDeltaY() > 0 ? Key.MOUSE_SCROLL_UP : Key.MOUSE_SCROLL_DOWN, true))) {
            e.consume();
          }
        });
    node.setOnKeyPressed(
        e -> {
          if (this.keyEventListener.apply(buildHIDEvent(convertButton(e.getCode()), true))) {
            e.consume();
          } else if (e.getCode() == KeyCode.ENTER) {
            if (this.typingListener.apply("\n")) {
              e.consume();
            }
          }
        });
    node.setOnKeyReleased(
        e -> {
          if (this.keyEventListener.apply(buildHIDEvent(convertButton(e.getCode()), false))) {
            e.consume();
          }
        });
    node.setOnKeyTyped(
        e -> {
          if (this.typingListener.apply(e.getCharacter())) {
            e.consume();
          }
        });
    node.heightProperty()
        .addListener(
            new ChangeListener<Number>() {
              @Override
              public void changed(
                  ObservableValue<? extends Number> observableValue,
                  Number number,
                  Number newValue) {
                heightChanged(node.getHeight());
              }
            });
    node.widthProperty()
        .addListener(
            new ChangeListener<Number>() {
              @Override
              public void changed(
                  ObservableValue<? extends Number> observableValue,
                  Number number,
                  Number newValue) {
                widthChanged(node.getWidth());
              }
            });
    final ChangeListener<Number> clipListener =
        new ChangeListener<Number>() {
          @Override
          public void changed(
              final ObservableValue<? extends Number> observable,
              final Number oldValue,
              final Number newValue) {
            node.setClip(new Rectangle(node.getWidth(), node.getHeight()));
          }
        };
    node.heightProperty().addListener(clipListener);
    node.widthProperty().addListener(clipListener);
    if (syntax.converseDirection == Direction.LEFT
        || syntax.transverseDirection == Direction.LEFT) {
      node.widthProperty()
          .addListener(
              new ChangeListener<Number>() {
                @Override
                public void changed(
                    final ObservableValue<? extends Number> observable,
                    final Number oldValue,
                    final Number newValue) {
                  origin.setLayoutX(newValue.doubleValue());
                }
              });
    }
    if (syntax.converseDirection == Direction.UP || syntax.transverseDirection == Direction.UP) {
      node.heightProperty()
          .addListener(
              new ChangeListener<Number>() {
                @Override
                public void changed(
                    final ObservableValue<? extends Number> observable,
                    final Number oldValue,
                    final Number newValue) {
                  origin.setLayoutY(newValue.doubleValue());
                }
              });
    }
  }

  public static Key convertButton(final MouseButton button) {
    switch (button) {
      case NONE:
        throw new DeadCode();
      case PRIMARY:
        return MOUSE_1;
      case MIDDLE:
        return MOUSE_3;
      case SECONDARY:
        return MOUSE_2;
    }
    throw new DeadCode();
  }

  public static Key convertButton(final KeyCode code) {
    switch (code) {
      case ENTER:
        return ENTER;
      case BACK_SPACE:
        return BACK_SPACE;
      case TAB:
        return TAB;
      case CANCEL:
        return CANCEL;
      case CLEAR:
        return CLEAR;
      case SHIFT:
        return SHIFT;
      case CONTROL:
        return CONTROL;
      case ALT:
        return ALT;
      case PAUSE:
        return PAUSE;
      case CAPS:
        return CAPS;
      case ESCAPE:
        return ESCAPE;
      case SPACE:
        return SPACE;
      case PAGE_UP:
        return PAGE_UP;
      case PAGE_DOWN:
        return PAGE_DOWN;
      case END:
        return END;
      case HOME:
        return HOME;
      case LEFT:
        return LEFT;
      case UP:
        return UP;
      case RIGHT:
        return RIGHT;
      case DOWN:
        return DOWN;
      case COMMA:
        return COMMA;
      case MINUS:
        return MINUS;
      case PERIOD:
        return PERIOD;
      case SLASH:
        return SLASH;
      case DIGIT0:
        return DIGIT0;
      case DIGIT1:
        return DIGIT1;
      case DIGIT2:
        return DIGIT2;
      case DIGIT3:
        return DIGIT3;
      case DIGIT4:
        return DIGIT4;
      case DIGIT5:
        return DIGIT5;
      case DIGIT6:
        return DIGIT6;
      case DIGIT7:
        return DIGIT7;
      case DIGIT8:
        return DIGIT8;
      case DIGIT9:
        return DIGIT9;
      case SEMICOLON:
        return SEMICOLON;
      case EQUALS:
        return EQUALS;
      case A:
        return A;
      case B:
        return B;
      case C:
        return C;
      case D:
        return D;
      case E:
        return E;
      case F:
        return F;
      case G:
        return G;
      case H:
        return H;
      case I:
        return I;
      case J:
        return J;
      case K:
        return K;
      case L:
        return L;
      case M:
        return M;
      case N:
        return N;
      case O:
        return O;
      case P:
        return P;
      case Q:
        return Q;
      case R:
        return R;
      case S:
        return S;
      case T:
        return T;
      case U:
        return U;
      case V:
        return V;
      case W:
        return W;
      case X:
        return X;
      case Y:
        return Y;
      case Z:
        return Z;
      case OPEN_BRACKET:
        return OPEN_BRACKET;
      case BACK_SLASH:
        return BACK_SLASH;
      case CLOSE_BRACKET:
        return CLOSE_BRACKET;
      case NUMPAD0:
        return NUMPAD0;
      case NUMPAD1:
        return NUMPAD1;
      case NUMPAD2:
        return NUMPAD2;
      case NUMPAD3:
        return NUMPAD3;
      case NUMPAD4:
        return NUMPAD4;
      case NUMPAD5:
        return NUMPAD5;
      case NUMPAD6:
        return NUMPAD6;
      case NUMPAD7:
        return NUMPAD7;
      case NUMPAD8:
        return NUMPAD8;
      case NUMPAD9:
        return NUMPAD9;
      case MULTIPLY:
        return MULTIPLY;
      case ADD:
        return ADD;
      case SEPARATOR:
        return SEPARATOR;
      case SUBTRACT:
        return SUBTRACT;
      case DECIMAL:
        return DECIMAL;
      case DIVIDE:
        return DIVIDE;
      case DELETE:
        return DELETE;
      case NUM_LOCK:
        return NUM_LOCK;
      case SCROLL_LOCK:
        return SCROLL_LOCK;
      case F1:
        return F1;
      case F2:
        return F2;
      case F3:
        return F3;
      case F4:
        return F4;
      case F5:
        return F5;
      case F6:
        return F6;
      case F7:
        return F7;
      case F8:
        return F8;
      case F9:
        return F9;
      case F10:
        return F10;
      case F11:
        return F11;
      case F12:
        return F12;
      case F13:
        return F13;
      case F14:
        return F14;
      case F15:
        return F15;
      case F16:
        return F16;
      case F17:
        return F17;
      case F18:
        return F18;
      case F19:
        return F19;
      case F20:
        return F20;
      case F21:
        return F21;
      case F22:
        return F22;
      case F23:
        return F23;
      case F24:
        return F24;
      case PRINTSCREEN:
        return PRINTSCREEN;
      case INSERT:
        return INSERT;
      case HELP:
        return HELP;
      case META:
        return META;
      case BACK_QUOTE:
        return BACK_QUOTE;
      case QUOTE:
        return QUOTE;
      case KP_UP:
        return KP_UP;
      case KP_DOWN:
        return KP_DOWN;
      case KP_LEFT:
        return KP_LEFT;
      case KP_RIGHT:
        return KP_RIGHT;
      case DEAD_GRAVE:
        return DEAD_GRAVE;
      case DEAD_ACUTE:
        return DEAD_ACUTE;
      case DEAD_CIRCUMFLEX:
        return DEAD_CIRCUMFLEX;
      case DEAD_TILDE:
        return DEAD_TILDE;
      case DEAD_MACRON:
        return DEAD_MACRON;
      case DEAD_BREVE:
        return DEAD_BREVE;
      case DEAD_ABOVEDOT:
        return DEAD_ABOVEDOT;
      case DEAD_DIAERESIS:
        return DEAD_DIAERESIS;
      case DEAD_ABOVERING:
        return DEAD_ABOVERING;
      case DEAD_DOUBLEACUTE:
        return DEAD_DOUBLEACUTE;
      case DEAD_CARON:
        return DEAD_CARON;
      case DEAD_CEDILLA:
        return DEAD_CEDILLA;
      case DEAD_OGONEK:
        return DEAD_OGONEK;
      case DEAD_IOTA:
        return DEAD_IOTA;
      case DEAD_VOICED_SOUND:
        return DEAD_VOICED_SOUND;
      case DEAD_SEMIVOICED_SOUND:
        return DEAD_SEMIVOICED_SOUND;
      case AMPERSAND:
        return AMPERSAND;
      case ASTERISK:
        return ASTERISK;
      case QUOTEDBL:
        return QUOTEDBL;
      case LESS:
        return LESS;
      case GREATER:
        return GREATER;
      case BRACELEFT:
        return BRACELEFT;
      case BRACERIGHT:
        return BRACERIGHT;
      case AT:
        return AT;
      case COLON:
        return COLON;
      case CIRCUMFLEX:
        return CIRCUMFLEX;
      case DOLLAR:
        return DOLLAR;
      case EURO_SIGN:
        return EURO_SIGN;
      case EXCLAMATION_MARK:
        return EXCLAMATION_MARK;
      case INVERTED_EXCLAMATION_MARK:
        return INVERTED_EXCLAMATION_MARK;
      case LEFT_PARENTHESIS:
        return LEFT_PARENTHESIS;
      case NUMBER_SIGN:
        return NUMBER_SIGN;
      case PLUS:
        return PLUS;
      case RIGHT_PARENTHESIS:
        return RIGHT_PARENTHESIS;
      case UNDERSCORE:
        return UNDERSCORE;
      case WINDOWS:
        return WINDOWS;
      case CONTEXT_MENU:
        return CONTEXT_MENU;
      case FINAL:
        return FINAL;
      case CONVERT:
        return CONVERT;
      case NONCONVERT:
        return NONCONVERT;
      case ACCEPT:
        return ACCEPT;
      case MODECHANGE:
        return MODECHANGE;
      case KANA:
        return KANA;
      case KANJI:
        return KANJI;
      case ALPHANUMERIC:
        return ALPHANUMERIC;
      case KATAKANA:
        return KATAKANA;
      case HIRAGANA:
        return HIRAGANA;
      case FULL_WIDTH:
        return FULL_WIDTH;
      case HALF_WIDTH:
        return HALF_WIDTH;
      case ROMAN_CHARACTERS:
        return ROMAN_CHARACTERS;
      case ALL_CANDIDATES:
        return ALL_CANDIDATES;
      case PREVIOUS_CANDIDATE:
        return PREVIOUS_CANDIDATE;
      case CODE_INPUT:
        return CODE_INPUT;
      case JAPANESE_KATAKANA:
        return JAPANESE_KATAKANA;
      case JAPANESE_HIRAGANA:
        return JAPANESE_HIRAGANA;
      case JAPANESE_ROMAN:
        return JAPANESE_ROMAN;
      case KANA_LOCK:
        return KANA_LOCK;
      case INPUT_METHOD_ON_OFF:
        return INPUT_METHOD_ON_OFF;
      case CUT:
        return CUT;
      case COPY:
        return COPY;
      case PASTE:
        return PASTE;
      case UNDO:
        return UNDO;
      case AGAIN:
        return AGAIN;
      case FIND:
        return FIND;
      case PROPS:
        return PROPS;
      case STOP:
        return STOP;
      case COMPOSE:
        return COMPOSE;
      case ALT_GRAPH:
        return ALT_GRAPH;
      case BEGIN:
        return BEGIN;
      case UNDEFINED:
        return UNDEFINED;
      case SOFTKEY_0:
        return SOFTKEY_0;
      case SOFTKEY_1:
        return SOFTKEY_1;
      case SOFTKEY_2:
        return SOFTKEY_2;
      case SOFTKEY_3:
        return SOFTKEY_3;
      case SOFTKEY_4:
        return SOFTKEY_4;
      case SOFTKEY_5:
        return SOFTKEY_5;
      case SOFTKEY_6:
        return SOFTKEY_6;
      case SOFTKEY_7:
        return SOFTKEY_7;
      case SOFTKEY_8:
        return SOFTKEY_8;
      case SOFTKEY_9:
        return SOFTKEY_9;
      case GAME_A:
        return GAME_A;
      case GAME_B:
        return GAME_B;
      case GAME_C:
        return GAME_C;
      case GAME_D:
        return GAME_D;
      case STAR:
        return STAR;
      case POUND:
        return POUND;
      case POWER:
        return POWER;
      case INFO:
        return INFO;
      case COLORED_KEY_0:
        return COLORED_KEY_0;
      case COLORED_KEY_1:
        return COLORED_KEY_1;
      case COLORED_KEY_2:
        return COLORED_KEY_2;
      case COLORED_KEY_3:
        return COLORED_KEY_3;
      case EJECT_TOGGLE:
        return EJECT_TOGGLE;
      case PLAY:
        return PLAY;
      case RECORD:
        return RECORD;
      case FAST_FWD:
        return FAST_FWD;
      case REWIND:
        return REWIND;
      case TRACK_PREV:
        return TRACK_PREV;
      case TRACK_NEXT:
        return TRACK_NEXT;
      case CHANNEL_UP:
        return CHANNEL_UP;
      case CHANNEL_DOWN:
        return CHANNEL_DOWN;
      case VOLUME_UP:
        return VOLUME_UP;
      case VOLUME_DOWN:
        return VOLUME_DOWN;
      case MUTE:
        return MUTE;
      case COMMAND:
        return COMMAND;
      case SHORTCUT:
        return SHORTCUT;
    }
    throw new DeadCode();
  }

  @Override
  public Group group() {
    return new JavaFXGroup(this);
  }

  @Override
  public Image image() {
    return new JavaFXImage(this);
  }

  @Override
  public Text text() {
    return new JavaFXText(this);
  }

  @Override
  public Font font(final String font, final double fontSize) {
    return new JavaFXFont(font, fontSize);
  }

  @Override
  public Drawing drawing() {
    return new JavaFXDrawing(this);
  }

  @Override
  public Blank blank() {
    return new JavaFXBlank();
  }

  @Override
  public void add(final int index, final DisplayNode node) {
    Node node1 = (Node) node.inner_();
    if (node1 != null) this.origin.getChildren().add(index, node1);
  }

  @Override
  public void remove(final DisplayNode node) {
    Node node1 = (Node) node.inner_();
    if (node1 != null) this.origin.getChildren().remove(node1);
  }

  @Override
  public int childCount() {
    return origin.getChildren().size();
  }

  @Override
  public void setBackgroundColor(final ModelColor color) {
    node.setBackground(new Background(new BackgroundFill(Helper.convert(color), null, null)));
  }

  @Override
  public double toPixels(Syntax.DisplayUnit displayUnit) {
    if (displayUnit == Syntax.DisplayUnit.PX) return 1;
    Screen screen = Screen.getPrimary();
    double dpi = screen.getDpi();
    switch (displayUnit) {
      case MM:
        return dpi / 2.54 / 10;
      default:
        throw new Assertion();
    }
  }

  public ButtonEvent buildHIDEvent(final Key key, final boolean press) {
    final ButtonEvent out = new ButtonEvent(key, press, modifiers.roCopy());
    switch (key) {
      case MOUSE_SCROLL_DOWN:
      case MOUSE_SCROLL_UP:
      case MOUSE_1:
      case MOUSE_2:
      case MOUSE_3:
        break;
      default:
        if (press) modifiers.add(key);
        else modifiers.remove(key);
    }
    return out;
  }
}
