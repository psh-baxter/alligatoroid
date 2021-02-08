package com.zarbosoft.merman.webview.display;

import com.zarbosoft.merman.editor.display.Blank;
import com.zarbosoft.merman.editor.display.Display;
import com.zarbosoft.merman.editor.display.DisplayNode;
import com.zarbosoft.merman.editor.display.Drawing;
import com.zarbosoft.merman.editor.display.Font;
import com.zarbosoft.merman.editor.display.Group;
import com.zarbosoft.merman.editor.display.Image;
import com.zarbosoft.merman.editor.display.Text;
import com.zarbosoft.merman.editor.hid.HIDEvent;
import com.zarbosoft.merman.extensions.hotkeys.Key;
import com.zarbosoft.merman.syntax.Direction;
import com.zarbosoft.merman.syntax.style.ModelColor;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.ROSet;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSSet;
import def.dom.HTMLDivElement;
import def.dom.KeyboardEvent;
import def.dom.MouseEvent;
import def.dom.WheelEvent;
import jsweet.util.StringTypes;

import java.util.function.Consumer;

import static jsweet.util.Lang.$insert;

public class JSDisplay extends Display {
  private final HTMLDivElement base;
  int width = -1;
  int height = -1;
  double wheelXPixels = 0;
  double wheelYPixels = 0;
  double wheelZPixels = 0;
  TSList<Consumer<java.lang.String>> typingListeners = new TSList<>();
  TSList<Consumer<HIDEvent>> hidEventListeners = new TSList<>();

  public JSDisplay(
      Direction converseDirection,
      Direction transverseDirection,
      double wheelPixelThreshold,
      HTMLDivElement base) {
    super(converseDirection, transverseDirection);
    this.base = base;
    //noinspection unused
    Runnable resizeCb = () -> widthHeightChanged(width(), height());
    $insert("new (window as any).ResizeObserver(resizeCb).observe(base);");
    this.base.addEventListener(
        StringTypes.mousemove,
        mouseEvent -> {
          mouseMoved(mouseEvent.offsetX, mouseEvent.offsetY, width(), height());
          return null;
        },
        true);
    this.base.addEventListener(
        StringTypes.mouseleave,
        mouseEvent -> {
          mouseExited();
          return null;
        },
        true);
    this.base.addEventListener(
        StringTypes.mousedown, mouseEvent -> mouseHandler(mouseEvent, true), true);
    this.base.addEventListener(
        StringTypes.mousedown, mouseEvent -> mouseHandler(mouseEvent, false), true);
    this.base.addEventListener(
        StringTypes.wheel,
        (WheelEvent event) -> {
          TSSet<Key> modifiers0 = new TSSet<>();
          if (event.altKey) modifiers0.add(Key.ALT);
          if (event.ctrlKey) modifiers0.add(Key.CONTROL);
          if (event.shiftKey) modifiers0.add(Key.SHIFT);
          if (event.metaKey) modifiers0.add(Key.META);
          ROSet<Key> modifiers = modifiers0.ro();
          if (event.deltaMode == 0) {
            if (event.deltaX > 0 != wheelXPixels > 0) {
              wheelXPixels = 0;
            }
            wheelXPixels += event.deltaX;
            {
              HIDEvent sendEvent = new HIDEvent(Key.MOUSE_SCROLL_LEFT, true, modifiers);
              while (wheelXPixels < -wheelPixelThreshold) {
                for (Consumer<HIDEvent> l : hidEventListeners) l.accept(sendEvent);
                wheelXPixels += wheelPixelThreshold;
              }
            }
            {
              HIDEvent sendEvent = new HIDEvent(Key.MOUSE_SCROLL_RIGHT, true, modifiers);
              while (wheelXPixels > wheelPixelThreshold) {
                for (Consumer<HIDEvent> l : hidEventListeners) l.accept(sendEvent);
                wheelXPixels -= wheelPixelThreshold;
              }
            }

            if (event.deltaY > 0 != wheelYPixels > 0) {
              wheelYPixels = 0;
            }
            wheelYPixels += event.deltaY;
            {
              HIDEvent sendEvent = new HIDEvent(Key.MOUSE_SCROLL_UP, true, modifiers);
              while (wheelYPixels < -wheelPixelThreshold) {
                for (Consumer<HIDEvent> l : hidEventListeners) l.accept(sendEvent);
                wheelYPixels += wheelPixelThreshold;
              }
            }
            {
              HIDEvent sendEvent = new HIDEvent(Key.MOUSE_SCROLL_DOWN, true, modifiers);
              while (wheelYPixels > wheelPixelThreshold) {
                for (Consumer<HIDEvent> l : hidEventListeners) l.accept(sendEvent);
                wheelYPixels -= wheelPixelThreshold;
              }
            }

            if (event.deltaZ > 0 != wheelZPixels > 0) {
              wheelZPixels = 0;
            }
            wheelZPixels += event.deltaZ;
            {
              HIDEvent sendEvent = new HIDEvent(Key.MOUSE_SCROLL_IN, true, modifiers);
              while (wheelZPixels < -wheelPixelThreshold) {
                for (Consumer<HIDEvent> l : hidEventListeners) l.accept(sendEvent);
                wheelZPixels += wheelPixelThreshold;
              }
            }
            {
              HIDEvent sendEvent = new HIDEvent(Key.MOUSE_SCROLL_OUT, true, modifiers);
              while (wheelZPixels > wheelPixelThreshold) {
                for (Consumer<HIDEvent> l : hidEventListeners) l.accept(sendEvent);
                wheelZPixels -= wheelPixelThreshold;
              }
            }
          } else {
            {
              HIDEvent sendEvent = new HIDEvent(Key.MOUSE_SCROLL_LEFT, true, modifiers);
              for (int i = 0; i > event.deltaX; --i) {
                for (Consumer<HIDEvent> l : hidEventListeners) l.accept(sendEvent);
              }
            }
            {
              HIDEvent sendEvent = new HIDEvent(Key.MOUSE_SCROLL_RIGHT, true, modifiers);
              for (int i = 0; i < event.deltaX; ++i) {
                for (Consumer<HIDEvent> l : hidEventListeners) {
                  l.accept(sendEvent);
                }
              }
            }
            {
              HIDEvent sendEvent = new HIDEvent(Key.MOUSE_SCROLL_UP, true, modifiers);
              for (int i = 0; i > event.deltaY; --i) {
                for (Consumer<HIDEvent> l : hidEventListeners) {
                  l.accept(sendEvent);
                }
              }
            }
            {
              HIDEvent sendEvent = new HIDEvent(Key.MOUSE_SCROLL_DOWN, true, modifiers);
              for (int i = 0; i < event.deltaY; ++i) {
                for (Consumer<HIDEvent> l : hidEventListeners) {
                  l.accept(sendEvent);
                }
              }
            }
            {
              HIDEvent sendEvent = new HIDEvent(Key.MOUSE_SCROLL_IN, true, modifiers);
              for (int i = 0; i > event.deltaZ; --i) {
                for (Consumer<HIDEvent> l : hidEventListeners) {
                  l.accept(sendEvent);
                }
              }
            }
            {
              HIDEvent sendEvent = new HIDEvent(Key.MOUSE_SCROLL_OUT, true, modifiers);
              for (int i = 0; i < event.deltaZ; ++i) {
                for (Consumer<HIDEvent> l : hidEventListeners) {
                  l.accept(sendEvent);
                }
              }
            }
          }
          return null;
        },
        true);
    this.base.addEventListener(StringTypes.keydown, event -> keydownHandler(event, true), true);
    this.base.addEventListener(StringTypes.keyup, e -> keydownHandler(e, false), true);
  }

  public static String cssColor(ModelColor color) {
    if (color instanceof ModelColor.RGB) {
      return java.lang.String.format(
          "rgb(%d, %d, %d)",
          ((ModelColor.RGB) color).r * 255,
          ((ModelColor.RGB) color).g * 255,
          ((ModelColor.RGB) color).b * 255);
    } else if (color instanceof ModelColor.RGBA) {
      return java.lang.String.format(
          "rgba(%d, %d, %d, %f)",
          ((ModelColor.RGBA) color).r * 255,
          ((ModelColor.RGBA) color).g * 255,
          ((ModelColor.RGBA) color).b * 255,
          ((ModelColor.RGBA) color).a);
    } else throw new Assertion();
  }

  private Object mouseHandler(MouseEvent event, boolean press) {
    Key key = null;
    switch (event.button) {
      case 0:
        {
          key = Key.MOUSE_1;
          break;
        }
      case 1:
        {
          key = Key.MOUSE_2;
          break;
        }
      case 2:
        {
          key = Key.MOUSE_3;
          break;
        }
      case 3:
        {
          key = Key.MOUSE_4;
          break;
        }
      case 4:
        {
          key = Key.MOUSE_5;
          break;
        }
    }
    if (key != null) {
      TSSet<Key> modifiers = new TSSet<>();
      if (event.altKey) modifiers.add(Key.ALT);
      if (event.ctrlKey) modifiers.add(Key.CONTROL);
      if (event.shiftKey) modifiers.add(Key.SHIFT);
      if (event.metaKey) modifiers.add(Key.META);
      HIDEvent sendEvent = new HIDEvent(key, press, modifiers.ro());
      for (Consumer<HIDEvent> l : hidEventListeners) {
        l.accept(sendEvent);
      }
    }
    return null;
  }

  private Object keydownHandler(KeyboardEvent event, boolean press) {
    boolean isText = false;
    String text = null;
    Key key = null;
    switch (event.key) {
      case "Again":
        {
          key = Key.AGAIN;
          break;
        }
      case "AltLeft":
        {
          key = Key.ALT_LEFT;
          break;
        }
      case "AltRight":
        {
          key = Key.ALT_RIGHT;
          break;
        }
      case "ArrowDown":
        {
          key = Key.DOWN;
          break;
        }
      case "ArrowLeft":
        {
          key = Key.LEFT;
          break;
        }
      case "ArrowRight":
        {
          key = Key.RIGHT;
          break;
        }
      case "ArrowUp":
        {
          key = Key.UP;
          break;
        }
      case "AudioVolumeDown":
        {
          key = Key.VOLUME_DOWN;
          break;
        }
      case "AudioVolumeMute":
        {
          key = Key.MUTE;
          break;
        }
      case "AudioVolumeUp":
        {
          key = Key.VOLUME_UP;
          break;
        }
      case "Backquote":
        {
          key = Key.BACK_QUOTE;
          isText = true;
          break;
        }
      case "Backslash":
        {
          key = Key.BACK_SLASH;
          isText = true;
          break;
        }
      case "Backspace":
        {
          key = Key.BACK_SPACE;
          isText = true;
          break;
        }
      case "BracketLeft":
        {
          key = Key.BRACELEFT;
          isText = true;
          break;
        }
      case "BracketRight":
        {
          key = Key.BRACERIGHT;
          isText = true;
          break;
        }
      case "BrowserBack":
        {
          key = Key.BROWSER_BACK;
          break;
        }
      case "BrowserFavorites":
        {
          key = Key.BROWSER_FAVORITES;
          break;
        }
      case "BrowserForward":
        {
          key = Key.BROWSER_FORWARD;
          break;
        }
      case "BrowserHome":
        {
          key = Key.BROWSER_HOME;
          break;
        }
      case "BrowserRefresh":
        {
          key = Key.BROWSER_REFRESH;
          break;
        }
      case "BrowserSearch":
        {
          key = Key.BROWSER_SEARCH;
          break;
        }
      case "BrowserStop":
        {
          key = Key.BROWSER_STOP;
          break;
        }
      case "Cancel":
        {
          key = Key.CANCEL;
          break;
        }
      case "CapsLock":
        {
          key = Key.CAPS;
          break;
        }
      case "Comma":
        {
          key = Key.COMMA;
          isText = true;
          break;
        }
      case "ContextMenu":
        {
          key = Key.CONTEXT_MENU;
          break;
        }
      case "ControlLeft":
        {
          key = Key.CONTROL_LEFT;
          break;
        }
      case "ControlRight":
        {
          key = Key.CONTROL_RIGHT;
          break;
        }
      case "Convert":
        {
          key = Key.CONVERT;
          break;
        }
      case "Copy":
        {
          key = Key.COPY;
          break;
        }
      case "Cut":
        {
          key = Key.CUT;
          break;
        }
      case "Delete":
        {
          key = Key.DELETE;
          isText = true;
          break;
        }
      case "Digit0":
        {
          key = Key.DIGIT0;
          isText = true;
          break;
        }
      case "Digit1":
        {
          key = Key.DIGIT1;
          isText = true;
          break;
        }
      case "Digit2":
        {
          key = Key.DIGIT2;
          isText = true;
          break;
        }
      case "Digit3":
        {
          key = Key.DIGIT3;
          isText = true;
          break;
        }
      case "Digit4":
        {
          key = Key.DIGIT4;
          isText = true;
          break;
        }
      case "Digit5":
        {
          key = Key.DIGIT5;
          isText = true;
          break;
        }
      case "Digit6":
        {
          key = Key.DIGIT6;
          isText = true;
          break;
        }
      case "Digit7":
        {
          key = Key.DIGIT7;
          isText = true;
          break;
        }
      case "Digit8":
        {
          key = Key.DIGIT8;
          isText = true;
          break;
        }
      case "Digit9":
        {
          key = Key.DIGIT9;
          isText = true;
          break;
        }
      case "Eject":
        {
          key = Key.EJECT_TOGGLE;
          break;
        }
      case "End":
        {
          key = Key.END;
          break;
        }
      case "Enter":
        {
          key = Key.ENTER;
          isText = true;
          text = "\n";
          break;
        }
      case "Equal":
        {
          key = Key.EQUALS;
          isText = true;
          break;
        }
      case "Escape":
        {
          key = Key.ESCAPE;
          break;
        }
      case "F1":
        {
          key = Key.F1;
          break;
        }
      case "F10":
        {
          key = Key.F10;
          break;
        }
      case "F11":
        {
          key = Key.F11;
          break;
        }
      case "F12":
        {
          key = Key.F12;
          break;
        }
      case "F13":
        {
          key = Key.F13;
          break;
        }
      case "F14":
        {
          key = Key.F14;
          break;
        }
      case "F15":
        {
          key = Key.F15;
          break;
        }
      case "F16":
        {
          key = Key.F16;
          break;
        }
      case "F17":
        {
          key = Key.F17;
          break;
        }
      case "F18":
        {
          key = Key.F18;
          break;
        }
      case "F19":
        {
          key = Key.F19;
          break;
        }
      case "F2":
        {
          key = Key.F2;
          break;
        }
      case "F20":
        {
          key = Key.F20;
          break;
        }
      case "F21":
        {
          key = Key.F21;
          break;
        }
      case "F22":
        {
          key = Key.F22;
          break;
        }
      case "F23":
        {
          key = Key.F23;
          break;
        }
      case "F24":
        {
          key = Key.F24;
          break;
        }
      case "F3":
        {
          key = Key.F3;
          break;
        }
      case "F4":
        {
          key = Key.F4;
          break;
        }
      case "F5":
        {
          key = Key.F5;
          break;
        }
      case "F6":
        {
          key = Key.F6;
          break;
        }
      case "F7":
        {
          key = Key.F7;
          break;
        }
      case "F8":
        {
          key = Key.F8;
          break;
        }
      case "F9":
        {
          key = Key.F9;
          break;
        }
      case "Find":
        {
          key = Key.FIND;
          break;
        }
      case "HangulMode":
        {
          key = Key.INTL_HANGUL_MODE;
          break;
        }
      case "Hanja":
        {
          key = Key.INTL_HANJA;
          break;
        }
      case "Help":
        {
          key = Key.HELP;
          break;
        }
      case "Home":
        {
          key = Key.HOME;
          break;
        }
      case "Insert":
        {
          key = Key.INSERT;
          break;
        }
      case "IntlBackslash":
        {
          key = Key.INTL_BACK_SLASH;
          isText = true;
          break;
        }
      case "IntlRo":
        {
          key = Key.INTL_RO;
          isText = true;
          break;
        }
      case "IntlYen":
        {
          key = Key.INTL_YEN;
          isText = true;
          break;
        }
      case "KanaMode":
        {
          key = Key.KANA;
          break;
        }
      case "KeyA":
        {
          key = Key.A;
          isText = true;
          break;
        }
      case "KeyB":
        {
          key = Key.B;
          isText = true;
          break;
        }
      case "KeyC":
        {
          key = Key.C;
          isText = true;
          break;
        }
      case "KeyD":
        {
          key = Key.D;
          isText = true;
          break;
        }
      case "KeyE":
        {
          key = Key.E;
          isText = true;
          break;
        }
      case "KeyF":
        {
          key = Key.F;
          isText = true;
          break;
        }
      case "KeyG":
        {
          key = Key.G;
          isText = true;
          break;
        }
      case "KeyH":
        {
          key = Key.H;
          isText = true;
          break;
        }
      case "KeyI":
        {
          key = Key.I;
          isText = true;
          break;
        }
      case "KeyJ":
        {
          key = Key.J;
          isText = true;
          break;
        }
      case "KeyK":
        {
          key = Key.K;
          isText = true;
          break;
        }
      case "KeyL":
        {
          key = Key.L;
          isText = true;
          break;
        }
      case "KeyM":
        {
          key = Key.M;
          isText = true;
          break;
        }
      case "KeyN":
        {
          key = Key.N;
          isText = true;
          break;
        }
      case "KeyO":
        {
          key = Key.O;
          isText = true;
          break;
        }
      case "KeyP":
        {
          key = Key.P;
          isText = true;
          break;
        }
      case "KeyQ":
        {
          key = Key.Q;
          isText = true;
          break;
        }
      case "KeyR":
        {
          key = Key.R;
          isText = true;
          break;
        }
      case "KeyS":
        {
          key = Key.S;
          isText = true;
          break;
        }
      case "KeyT":
        {
          key = Key.T;
          isText = true;
          break;
        }
      case "KeyU":
        {
          key = Key.U;
          isText = true;
          break;
        }
      case "KeyV":
        {
          key = Key.V;
          isText = true;
          break;
        }
      case "KeyW":
        {
          key = Key.W;
          isText = true;
          break;
        }
      case "KeyX":
        {
          key = Key.X;
          isText = true;
          break;
        }
      case "KeyY":
        {
          key = Key.Y;
          isText = true;
          break;
        }
      case "KeyZ":
        {
          key = Key.Z;
          isText = true;
          break;
        }
      case "Lang1":
        {
          key = Key.LANG1;
          break;
        }
      case "Lang2":
        {
          key = Key.LANG2;
          break;
        }
      case "LaunchApp1":
        {
          key = Key.LAUNCH_APP1;
          break;
        }
      case "LaunchApp2":
        {
          key = Key.LAUNCH_APP2;
          break;
        }
      case "LaunchMail":
        {
          key = Key.LAUNCH_MAIL;
          break;
        }
      case "LaunchMediaPlayer":
        {
          key = Key.LAUNCH_MEDIA_PLAYER;
          break;
        }
      case "MediaPlayPause":
        {
          key = Key.MEDIA_PLAY_PAUSE;
          break;
        }
      case "MediaStop":
        {
          key = Key.MEDIA_STOP;
          break;
        }
      case "MediaTrackNext":
        {
          key = Key.MEDIA_NEXT;
          break;
        }
      case "MediaTrackPrevious":
        {
          key = Key.MEDIA_PREVIOUS;
          break;
        }
      case "MetaLeft":
        {
          key = Key.META_LEFT;
          break;
        }
      case "MetaRight":
        {
          key = Key.META_RIGHT;
          break;
        }
      case "Minus":
        {
          key = Key.MINUS;
          isText = true;
          break;
        }
      case "NonConvert":
        {
          key = Key.NONCONVERT;
          break;
        }
      case "NumLock":
        {
          key = Key.NUM_LOCK;
          break;
        }
      case "Numpad0":
        {
          key = Key.NUMPAD0;
          isText = true;
          break;
        }
      case "Numpad1":
        {
          key = Key.NUMPAD1;
          isText = true;
          break;
        }
      case "Numpad2":
        {
          key = Key.NUMPAD2;
          isText = true;
          break;
        }
      case "Numpad3":
        {
          key = Key.NUMPAD3;
          isText = true;
          break;
        }
      case "Numpad4":
        {
          key = Key.NUMPAD4;
          isText = true;
          break;
        }
      case "Numpad5":
        {
          key = Key.NUMPAD5;
          isText = true;
          break;
        }
      case "Numpad6":
        {
          key = Key.NUMPAD6;
          isText = true;
          break;
        }
      case "Numpad7":
        {
          key = Key.NUMPAD7;
          isText = true;
          break;
        }
      case "Numpad8":
        {
          key = Key.NUMPAD8;
          isText = true;
          break;
        }
      case "Numpad9":
        {
          key = Key.NUMPAD9;
          isText = true;
          break;
        }
      case "NumpadAdd":
        {
          key = Key.NUMPAD_ADD;
          isText = true;
          break;
        }
      case "NumpadChangeSign":
        {
          key = Key.NUMPAD_CHANGESIGN;
          break;
        }
      case "NumpadComma":
        {
          key = Key.NUMPAD_COMMA;
          isText = true;
          break;
        }
      case "NumpadDecimal":
        {
          key = Key.NUMPAD_DECIMAL;
          isText = true;
          break;
        }
      case "NumpadDivide":
        {
          key = Key.NUMPAD_DIVIDE;
          isText = true;
          break;
        }
      case "NumpadEnter":
        {
          key = Key.NUMPAD_ENTER;
          isText = true;
          break;
        }
      case "NumpadEqual":
        {
          key = Key.NUMPAD_EQUAL;
          isText = true;
          break;
        }
      case "NumpadMultiply":
        {
          key = Key.NUMPAD_MULTIPLY;
          isText = true;
          break;
        }
      case "NumpadParenLeft":
        {
          key = Key.NUMPAD_LEFTPAREN;
          isText = true;
          break;
        }
      case "NumpadParenRight":
        {
          key = Key.NUMPAD_RIGHTPAREN;
          isText = true;
          break;
        }
      case "NumpadSubtract":
        {
          key = Key.NUMPAD_SUBTRACT;
          isText = true;
          break;
        }
      case "Open":
        {
          key = Key.OPEN;
          break;
        }
      case "OSLeft":
        {
          key = Key.OS_LEFT;
          break;
        }
      case "OSRight":
        {
          key = Key.OS_RIGHT;
          break;
        }
      case "PageDown":
        {
          key = Key.PAGE_DOWN;
          break;
        }
      case "PageUp":
        {
          key = Key.PAGE_UP;
          break;
        }
      case "Paste":
        {
          key = Key.PASTE;
          break;
        }
      case "Pause":
        {
          key = Key.PAUSE;
          break;
        }
      case "Period":
        {
          key = Key.PERIOD;
          isText = true;
          break;
        }
      case "Power":
        {
          key = Key.POWER;
          break;
        }
      case "PrintScreen":
        {
          key = Key.PRINTSCREEN;
          break;
        }
      case "Props":
        {
          key = Key.PROPS;
          break;
        }
      case "Quote":
        {
          key = Key.QUOTE;
          isText = true;
          break;
        }
      case "ScrollLock":
        {
          key = Key.SCROLL_LOCK;
          break;
        }
      case "Select":
        {
          key = Key.SELECT;
          break;
        }
      case "Semicolon":
        {
          key = Key.SEMICOLON;
          isText = true;
          break;
        }
      case "ShiftLeft":
        {
          key = Key.SHIFT_LEFT;
          break;
        }
      case "ShiftRight":
        {
          key = Key.SHIFT_RIGHT;
          break;
        }
      case "Slash":
        {
          key = Key.SLASH;
          isText = true;
          break;
        }
      case "Space":
        {
          key = Key.SPACE;
          isText = true;
          break;
        }
      case "Tab":
        {
          key = Key.TAB;
          isText = true;
          text = "\t";
          break;
        }
      case "Undo":
        {
          key = Key.UNDO;
          break;
        }
      case "WakeUp":
        {
          key = Key.WAKE;
          break;
        }
      case "Unidentified":
        {
          isText = true;
          break;
        }
    }
    if (key != null) {
      TSSet<Key> modifiers = new TSSet<>();
      if (event.altKey) modifiers.add(Key.ALT);
      if (event.ctrlKey) modifiers.add(Key.CONTROL);
      if (event.shiftKey) modifiers.add(Key.SHIFT);
      if (event.metaKey) modifiers.add(Key.META);
      HIDEvent sendEvent = new HIDEvent(key, press, modifiers.ro());
      for (Consumer<HIDEvent> l : hidEventListeners) {
        l.accept(sendEvent);
      }
    }
    if (isText) {
      if (text == null) text = event.key;
      for (Consumer<String> l : typingListeners) {
        l.accept(text);
      }
    }
    return null;
  }

  @Override
  public Group group() {
    return new JSGroup(this);
  }

  @Override
  public Image image() {
    return new JSImage(this);
  }

  @Override
  public Text text() {
    return new JSText(this);
  }

  @Override
  public Font font(String font, int fontSize) {
    return new JSFont(font, fontSize);
  }

  @Override
  public Drawing drawing() {
    return new JSDrawing(this);
  }

  @Override
  public Blank blank() {
    return new JSBlank(this);
  }

  @Override
  public void addHIDEventListener(Consumer<HIDEvent> listener) {
    this.hidEventListeners.add(listener);
  }

  @Override
  public void addTypingListener(Consumer<java.lang.String> listener) {
    this.typingListeners.add(listener);
  }

  @Override
  public double width() {
    return (int) base.clientWidth;
  }

  @Override
  public double height() {
    return (int) base.clientHeight;
  }

  @Override
  public void add(int index, DisplayNode node) {
    if (index < base.childNodes.length) base.appendChild(((JSDisplayNode) node).js());
    else base.insertBefore(((JSDisplayNode) node).js(), base.childNodes.$get(index));
  }

  @Override
  public int childCount() {
    return base.childNodes.length;
  }

  @Override
  public void remove(DisplayNode node) {
    ((JSDisplayNode) node).js().remove();
  }

  @Override
  public void setBackgroundColor(ModelColor color) {
    base.style.backgroundColor = cssColor(color);
  }
}
