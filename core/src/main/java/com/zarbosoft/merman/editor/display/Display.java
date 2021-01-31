package com.zarbosoft.merman.editor.display;

import com.zarbosoft.merman.editor.hid.HIDEvent;
import com.zarbosoft.merman.editor.visual.Vector;
import com.zarbosoft.merman.syntax.Direction;
import com.zarbosoft.merman.syntax.style.ModelColor;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.TSList;

import java.util.function.Consumer;

public abstract class Display {
  private final TSList<Consumer<Vector>> mouseMoveListeners = new TSList<>();
  private final TSList<Runnable> mouseLeaveListeners = new TSList<>();

  public interface DisplayAbsoluteConvert {
    public Vector convert(
        double left, double right, double rightEdge, double top, double bottom, double bottomEdge);

    public UnconvertVector unconvert(
        int converse, int transverse, double xSpan, double ySpan, double xEdge, double yEdge);

    public UnconvertAxis unconvertConverse(
        int converse, double xSpan, double ySpan, double xEdge, double yEdge);

    public UnconvertAxis unconvertTransverse(
        int transverse, double xSpan, double ySpan, double xEdge, double yEdge);
  }

  public interface DisplayHalfConvert {
    public Vector convert(double width, double height);

    public UnconvertVector unconvertSpan(int converse, int transverse);

    public UnconvertAxis unconvertConverseSpan(int span);

    public UnconvertAxis unconvertTransverseSpan(int span);
  }

  public static class UnconvertAxis {
    public final boolean x;
    public final double amount;

    public UnconvertAxis(boolean x, double amount) {
      this.x = x;
      this.amount = amount;
    }
  }

  public static class UnconvertVector {
    public final double x;
    public final double y;

    public UnconvertVector(double x, double y) {
      this.x = x;
      this.y = y;
    }
  }

  public final DisplayAbsoluteConvert convert;
  public final DisplayHalfConvert halfConvert;
  private final TSList<IntListener> converseEdgeListeners = new TSList<>();
  private final TSList<IntListener> transverseEdgeListeners = new TSList<>();
  private double width;
  private double height;
  private int converseEdge;
  private int transverseEdge;

  protected Display(Direction converseDirection, Direction transverseDirection) {
    switch (converseDirection) {
      case UP:
        {
          switch (transverseDirection) {
            case LEFT:
              {
                convert =
                    new DisplayAbsoluteConvert() {
                      @Override
                      public Vector convert(
                          double left,
                          double right,
                          double rightEdge,
                          double top,
                          double bottom,
                          double bottomEdge) {
                        return new Vector(
                            (int) (double) (bottomEdge - bottom),
                            (int) (double) (rightEdge - right));
                      }

                      @Override
                      public UnconvertVector unconvert(
                          int converse,
                          int transverse,
                          double xSpan,
                          double ySpan,
                          double xEdge,
                          double yEdge) {
                        return new UnconvertVector(
                            yEdge - (double) converse - ySpan, xEdge - (double) transverse - xSpan);
                      }

                      @Override
                      public UnconvertAxis unconvertConverse(
                          int converse, double xSpan, double ySpan, double xEdge, double yEdge) {
                        return new UnconvertAxis(false, yEdge - (double) converse - ySpan);
                      }

                      @Override
                      public UnconvertAxis unconvertTransverse(
                          int transverse, double xSpan, double ySpan, double xEdge, double yEdge) {
                        return new UnconvertAxis(true, xEdge - (double) transverse - xSpan);
                      }
                    };
                break;
              }
            case RIGHT:
              {
                convert =
                    new DisplayAbsoluteConvert() {
                      @Override
                      public Vector convert(
                          double left,
                          double right,
                          double rightEdge,
                          double top,
                          double bottom,
                          double bottomEdge) {
                        return new Vector((int) (double) (bottomEdge - bottom), (int) left);
                      }

                      @Override
                      public UnconvertVector unconvert(
                          int converse,
                          int transverse,
                          double xSpan,
                          double ySpan,
                          double xEdge,
                          double yEdge) {
                        return new UnconvertVector(xEdge - (double) transverse - xSpan, converse);
                      }

                      @Override
                      public UnconvertAxis unconvertConverse(
                          int converse, double xSpan, double ySpan, double xEdge, double yEdge) {
                        return new UnconvertAxis(false, converse);
                      }

                      @Override
                      public UnconvertAxis unconvertTransverse(
                          int transverse, double xSpan, double ySpan, double xEdge, double yEdge) {
                        return new UnconvertAxis(true, xEdge - (double) transverse - xSpan);
                      }
                    };
                break;
              }
            default:
              throw new Assertion();
          }
          break;
        }
      case DOWN:
        {
          switch (transverseDirection) {
            case LEFT:
              {
                convert =
                    new DisplayAbsoluteConvert() {
                      @Override
                      public Vector convert(
                          double left,
                          double right,
                          double rightEdge,
                          double top,
                          double bottom,
                          double bottomEdge) {
                        return new Vector((int) top, (int) (double) (rightEdge - right));
                      }

                      @Override
                      public UnconvertVector unconvert(
                          int converse,
                          int transverse,
                          double xSpan,
                          double ySpan,
                          double xEdge,
                          double yEdge) {
                        return new UnconvertVector(transverse, yEdge - (double) converse - ySpan);
                      }

                      @Override
                      public UnconvertAxis unconvertConverse(
                          int converse, double xSpan, double ySpan, double xEdge, double yEdge) {
                        return new UnconvertAxis(false, yEdge - (double) converse - ySpan);
                      }

                      @Override
                      public UnconvertAxis unconvertTransverse(
                          int transverse, double xSpan, double ySpan, double xEdge, double yEdge) {
                        return new UnconvertAxis(true, transverse);
                      }
                    };
                break;
              }
            case RIGHT:
              {
                convert =
                    new DisplayAbsoluteConvert() {
                      @Override
                      public Vector convert(
                          double left,
                          double right,
                          double rightEdge,
                          double top,
                          double bottom,
                          double bottomEdge) {
                        return new Vector((int) top, (int) left);
                      }

                      @Override
                      public UnconvertVector unconvert(
                          int converse,
                          int transverse,
                          double xSpan,
                          double ySpan,
                          double xEdge,
                          double yEdge) {
                        return new UnconvertVector(transverse, converse);
                      }

                      @Override
                      public UnconvertAxis unconvertConverse(
                          int converse, double xSpan, double ySpan, double xEdge, double yEdge) {
                        return new UnconvertAxis(false, converse);
                      }

                      @Override
                      public UnconvertAxis unconvertTransverse(
                          int transverse, double xSpan, double ySpan, double xEdge, double yEdge) {
                        return new UnconvertAxis(true, transverse);
                      }
                    };
                break;
              }
            default:
              throw new Assertion();
          }
          break;
        }
      case LEFT:
        {
          switch (transverseDirection) {
            case UP:
              {
                convert =
                    new DisplayAbsoluteConvert() {
                      @Override
                      public Vector convert(
                          double left,
                          double right,
                          double rightEdge,
                          double top,
                          double bottom,
                          double bottomEdge) {
                        return new Vector((int) (rightEdge - right), (int) (bottomEdge - bottom));
                      }

                      @Override
                      public UnconvertVector unconvert(
                          int converse,
                          int transverse,
                          double xSpan,
                          double ySpan,
                          double xEdge,
                          double yEdge) {
                        return new UnconvertVector(
                            xEdge - converse - xSpan, yEdge - transverse - ySpan);
                      }

                      @Override
                      public UnconvertAxis unconvertConverse(
                          int converse, double xSpan, double ySpan, double xEdge, double yEdge) {
                        return new UnconvertAxis(true, xEdge - converse - xSpan);
                      }

                      @Override
                      public UnconvertAxis unconvertTransverse(
                          int transverse, double xSpan, double ySpan, double xEdge, double yEdge) {
                        return new UnconvertAxis(false, yEdge - transverse - ySpan);
                      }
                    };
                break;
              }
            case DOWN:
              {
                convert =
                    new DisplayAbsoluteConvert() {
                      @Override
                      public Vector convert(
                          double left,
                          double right,
                          double rightEdge,
                          double top,
                          double bottom,
                          double bottomEdge) {
                        return new Vector((int) (rightEdge - right), (int) top);
                      }

                      @Override
                      public UnconvertVector unconvert(
                          int converse,
                          int transverse,
                          double xSpan,
                          double ySpan,
                          double xEdge,
                          double yEdge) {
                        return new UnconvertVector(xEdge - converse - xSpan, transverse);
                      }

                      @Override
                      public UnconvertAxis unconvertConverse(
                          int converse, double xSpan, double ySpan, double xEdge, double yEdge) {
                        return new UnconvertAxis(true, xEdge - converse - xSpan);
                      }

                      @Override
                      public UnconvertAxis unconvertTransverse(
                          int transverse, double xSpan, double ySpan, double xEdge, double yEdge) {
                        return new UnconvertAxis(false, transverse);
                      }
                    };
                break;
              }
            default:
              throw new Assertion();
          }
          break;
        }
      case RIGHT:
        {
          switch (transverseDirection) {
            case UP:
              {
                convert =
                    new DisplayAbsoluteConvert() {
                      @Override
                      public Vector convert(
                          double left,
                          double right,
                          double rightEdge,
                          double top,
                          double bottom,
                          double bottomEdge) {
                        return new Vector((int) left, (int) (bottomEdge - bottom));
                      }

                      @Override
                      public UnconvertVector unconvert(
                          int converse,
                          int transverse,
                          double xSpan,
                          double ySpan,
                          double xEdge,
                          double yEdge) {
                        return new UnconvertVector(converse, yEdge - transverse - ySpan);
                      }

                      @Override
                      public UnconvertAxis unconvertConverse(
                          int converse, double xSpan, double ySpan, double xEdge, double yEdge) {
                        return new UnconvertAxis(true, converse);
                      }

                      @Override
                      public UnconvertAxis unconvertTransverse(
                          int transverse, double xSpan, double ySpan, double xEdge, double yEdge) {
                        return new UnconvertAxis(false, yEdge - transverse - ySpan);
                      }
                    };
                break;
              }
            case DOWN:
              {
                convert =
                    new DisplayAbsoluteConvert() {
                      @Override
                      public Vector convert(
                          double left,
                          double right,
                          double rightEdge,
                          double top,
                          double bottom,
                          double bottomEdge) {
                        return new Vector((int) left, (int) top);
                      }

                      @Override
                      public UnconvertVector unconvert(
                          int converse,
                          int transverse,
                          double xSpan,
                          double ySpan,
                          double xEdge,
                          double yEdge) {
                        return new UnconvertVector(converse, transverse);
                      }

                      @Override
                      public UnconvertAxis unconvertConverse(
                          int converse, double xSpan, double ySpan, double xEdge, double yEdge) {
                        return new UnconvertAxis(true, converse);
                      }

                      @Override
                      public UnconvertAxis unconvertTransverse(
                          int transverse, double xSpan, double ySpan, double xEdge, double yEdge) {
                        return new UnconvertAxis(false, transverse);
                      }
                    };
                break;
              }
            default:
              throw new Assertion();
          }
          break;
        }
      default:
        throw new Assertion();
    }
    switch (converseDirection) {
      case UP:
      case DOWN:
        halfConvert =
            new DisplayHalfConvert() {
              @Override
              public Vector convert(double width, double height) {
                return new Vector((int) (double) height, (int) (double) width);
              }

              @Override
              public UnconvertVector unconvertSpan(int converse, int transverse) {
                return new UnconvertVector(transverse, converse);
              }

              @Override
              public UnconvertAxis unconvertConverseSpan(int span) {
                return new UnconvertAxis(false, span);
              }

              @Override
              public UnconvertAxis unconvertTransverseSpan(int span) {
                return new UnconvertAxis(true, span);
              }
            };
        break;
      case LEFT:
      case RIGHT:
        halfConvert =
            new DisplayHalfConvert() {
              @Override
              public Vector convert(double width, double height) {
                return new Vector((int) width, (int) height);
              }

              @Override
              public UnconvertVector unconvertSpan(int converse, int transverse) {
                return new UnconvertVector(converse, transverse);
              }

              @Override
              public UnconvertAxis unconvertConverseSpan(int span) {
                return new UnconvertAxis(true, span);
              }

              @Override
              public UnconvertAxis unconvertTransverseSpan(int span) {
                return new UnconvertAxis(true, span);
              }
            };
        break;
      default:
        throw new Assertion();
    }
  }

  public abstract Group group();

  public abstract Image image();

  public abstract Text text();

  public abstract Font font(String font, int fontSize);

  public abstract Drawing drawing();

  public abstract Blank blank();

  public final void addMouseExitListener(Runnable listener) {
    mouseLeaveListeners.add(listener);
  }

  protected final void mouseExited() {
    for (Runnable l : mouseLeaveListeners) {
      l.run();
    }
  }

  protected void mouseMoved(double x, double y, double re, double be) {
    Vector vector = convert.convert(x, x, re, y, y, be);
    for (Consumer<Vector> l : mouseMoveListeners.mut()) {
      l.accept(vector);
    }
  }

  public final void addMouseMoveListener(Consumer<Vector> listener) {
    this.mouseMoveListeners.add(listener);
  }

  public abstract void addHIDEventListener(Consumer<HIDEvent> listener);

  public abstract void addTypingListener(Consumer<String> listener);

  @FunctionalInterface
  public interface IntListener {
    void changed(int oldValue, int newValue);
  }

  protected abstract double width();

  protected abstract double height();

  public final int edge() {
    return halfConvert.convert(width(), height()).converse;
  }

  protected void widthChanged(double newWidth) {
    widthHeightChanged(newWidth, height);
  }

  protected void heightChanged(double newHeight) {
    widthHeightChanged(width, newHeight);
  }

  protected void widthHeightChanged(double newWidth, double newHeight) {
    width = newWidth;
    height = newHeight;
    Vector converted = halfConvert.convert(newWidth, newHeight);
    int oldConverseEdge = converseEdge;
    int oldTransverseEdge = transverseEdge;
    converseEdge = converted.converse;
    transverseEdge = converted.transverse;
    if (converseEdge != oldConverseEdge)
      for (IntListener l : converseEdgeListeners) l.changed(oldConverseEdge, converseEdge);
    if (transverseEdge != oldTransverseEdge)
      for (IntListener l : transverseEdgeListeners) l.changed(oldTransverseEdge, transverseEdge);
  }

  public final void addConverseEdgeListener(IntListener listener) {
    converseEdgeListeners.add(listener);
  }

  public final int transverseEdge() {
    return halfConvert.convert(width(), height()).transverse;
  }

  public final void addTransverseEdgeListener(IntListener listener) {
    transverseEdgeListeners.add(listener);
  }

  public abstract void add(int index, DisplayNode node);

  public void add(final DisplayNode node) {
    add(childCount(), node);
  }

  public abstract int childCount();

  public abstract void remove(DisplayNode node);

  public abstract void setBackgroundColor(ModelColor color);
}
