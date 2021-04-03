package com.zarbosoft.merman.core.display;

import com.zarbosoft.merman.core.hid.HIDEvent;
import com.zarbosoft.merman.core.visual.Vector;
import com.zarbosoft.merman.core.syntax.Direction;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.core.syntax.style.ModelColor;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.TSList;

import java.util.function.Consumer;

public abstract class Display {
  public final DisplayAbsoluteConvert convert;
  public final DisplayHalfConvert halfConvert;
  private final TSList<Consumer<Vector>> mouseMoveListeners = new TSList<>();
  private final TSList<Runnable> mouseLeaveListeners = new TSList<>();
  private final TSList<DoubleListener> converseEdgeListeners = new TSList<>();
  private final TSList<DoubleListener> transverseEdgeListeners = new TSList<>();
  private double width;
  private double height;
  private double converseEdge = Integer.MAX_VALUE;
  private double transverseEdge = Integer.MAX_VALUE;
    protected Consumer<HIDEvent> hidEventListener;
    protected Consumer<String> typingListener;

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
                      public Vector convert(double left, double right, double top, double bottom) {
                        return new Vector((-bottom), (-right));
                      }

                      @Override
                      public UnconvertVector unconvert(
                          double converse, double transverse, double xSpan, double ySpan) {
                        return new UnconvertVector(-converse - ySpan, -transverse - xSpan);
                      }

                      @Override
                      public UnconvertAxis unconvertConverse(
                          double converse, double xSpan, double ySpan) {
                        return new UnconvertAxis(false, -converse - ySpan);
                      }

                      @Override
                      public UnconvertAxis unconvertTransverse(
                          double transverse, double xSpan, double ySpan) {
                        return new UnconvertAxis(true, -transverse - xSpan);
                      }
                    };
                break;
              }
            case RIGHT:
              {
                convert =
                    new DisplayAbsoluteConvert() {
                      @Override
                      public Vector convert(double left, double right, double top, double bottom) {
                        return new Vector((-bottom), left);
                      }

                      @Override
                      public UnconvertVector unconvert(
                          double converse, double transverse, double xSpan, double ySpan) {
                        return new UnconvertVector(-transverse - xSpan, converse);
                      }

                      @Override
                      public UnconvertAxis unconvertConverse(
                          double converse, double xSpan, double ySpan) {
                        return new UnconvertAxis(false, converse);
                      }

                      @Override
                      public UnconvertAxis unconvertTransverse(
                          double transverse, double xSpan, double ySpan) {
                        return new UnconvertAxis(true, -transverse - xSpan);
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
                      public Vector convert(double left, double right, double top, double bottom) {
                        return new Vector(top, (-right));
                      }

                      @Override
                      public UnconvertVector unconvert(
                          double converse, double transverse, double xSpan, double ySpan) {
                        return new UnconvertVector(-transverse - xSpan, converse);
                      }

                      @Override
                      public UnconvertAxis unconvertConverse(
                          double converse, double xSpan, double ySpan) {
                        return new UnconvertAxis(false, converse);
                      }

                      @Override
                      public UnconvertAxis unconvertTransverse(
                          double transverse, double xSpan, double ySpan) {
                        return new UnconvertAxis(true, -transverse - xSpan);
                      }
                    };
                break;
              }
            case RIGHT:
              {
                convert =
                    new DisplayAbsoluteConvert() {
                      @Override
                      public Vector convert(double left, double right, double top, double bottom) {
                        return new Vector(top, left);
                      }

                      @Override
                      public UnconvertVector unconvert(
                          double converse, double transverse, double xSpan, double ySpan) {
                        return new UnconvertVector(transverse, converse);
                      }

                      @Override
                      public UnconvertAxis unconvertConverse(
                          double converse, double xSpan, double ySpan) {
                        return new UnconvertAxis(false, converse);
                      }

                      @Override
                      public UnconvertAxis unconvertTransverse(
                          double transverse, double xSpan, double ySpan) {
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
                      public Vector convert(double left, double right, double top, double bottom) {
                        return new Vector((-right), (-bottom));
                      }

                      @Override
                      public UnconvertVector unconvert(
                          double converse, double transverse, double xSpan, double ySpan) {
                        return new UnconvertVector(-converse - xSpan, -transverse - ySpan);
                      }

                      @Override
                      public UnconvertAxis unconvertConverse(
                          double converse, double xSpan, double ySpan) {
                        return new UnconvertAxis(true, -converse - xSpan);
                      }

                      @Override
                      public UnconvertAxis unconvertTransverse(
                          double transverse, double xSpan, double ySpan) {
                        return new UnconvertAxis(false, -transverse - ySpan);
                      }
                    };
                break;
              }
            case DOWN:
              {
                convert =
                    new DisplayAbsoluteConvert() {
                      @Override
                      public Vector convert(double left, double right, double top, double bottom) {
                        return new Vector((-right), top);
                      }

                      @Override
                      public UnconvertVector unconvert(
                          double converse, double transverse, double xSpan, double ySpan) {
                        return new UnconvertVector(-converse - xSpan, transverse);
                      }

                      @Override
                      public UnconvertAxis unconvertConverse(
                          double converse, double xSpan, double ySpan) {
                        return new UnconvertAxis(true, -converse - xSpan);
                      }

                      @Override
                      public UnconvertAxis unconvertTransverse(
                          double transverse, double xSpan, double ySpan) {
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
                      public Vector convert(double left, double right, double top, double bottom) {
                        return new Vector(left, (-bottom));
                      }

                      @Override
                      public UnconvertVector unconvert(
                          double converse, double transverse, double xSpan, double ySpan) {
                        return new UnconvertVector(converse, -transverse - ySpan);
                      }

                      @Override
                      public UnconvertAxis unconvertConverse(
                          double converse, double xSpan, double ySpan) {
                        return new UnconvertAxis(true, converse);
                      }

                      @Override
                      public UnconvertAxis unconvertTransverse(
                          double transverse, double xSpan, double ySpan) {
                        return new UnconvertAxis(false, -transverse - ySpan);
                      }
                    };
                break;
              }
            case DOWN:
              {
                convert =
                    new DisplayAbsoluteConvert() {
                      @Override
                      public Vector convert(double left, double right, double top, double bottom) {
                        return new Vector(left, top);
                      }

                      @Override
                      public UnconvertVector unconvert(
                          double converse, double transverse, double xSpan, double ySpan) {
                        return new UnconvertVector(converse, transverse);
                      }

                      @Override
                      public UnconvertAxis unconvertConverse(
                          double converse, double xSpan, double ySpan) {
                        return new UnconvertAxis(true, converse);
                      }

                      @Override
                      public UnconvertAxis unconvertTransverse(
                          double transverse, double xSpan, double ySpan) {
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
                return new Vector(height, width);
              }

              @Override
              public UnconvertVector unconvertSpan(double converse, double transverse) {
                return new UnconvertVector(transverse, converse);
              }

              @Override
              public UnconvertAxis unconvertConverseSpan(double span) {
                return new UnconvertAxis(false, span);
              }

              @Override
              public UnconvertAxis unconvertTransverseSpan(double span) {
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
                return new Vector(width, height);
              }

              @Override
              public UnconvertVector unconvertSpan(double converse, double transverse) {
                return new UnconvertVector(converse, transverse);
              }

              @Override
              public UnconvertAxis unconvertConverseSpan(double span) {
                return new UnconvertAxis(true, span);
              }

              @Override
              public UnconvertAxis unconvertTransverseSpan(double span) {
                return new UnconvertAxis(false, span);
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

  public abstract Font font(String font, double fontSize);

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

  protected void mouseMoved(double x, double y) {
    Vector vector = convert.convert(x, x, y, y);
    for (Consumer<Vector> l : mouseMoveListeners.mut()) {
      l.accept(vector);
    }
  }

  public final void addMouseMoveListener(Consumer<Vector> listener) {
    this.mouseMoveListeners.add(listener);
  }

  public final void setHIDEventListener(Consumer<HIDEvent> listener) {
      if (hidEventListener != null) throw new Assertion("hid event listener already set");
      this.hidEventListener = listener;
  }

  public final void setTypingListener(Consumer<String> listener) {
      if (this.typingListener != null) throw new Assertion("typing listener already set");
      this.typingListener = listener;
  }

  public final double width() {
      return width;
  }

  public final double height() {
      return height;
  }

  public final double edge() {
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
    double oldConverseEdge = converseEdge;
    double oldTransverseEdge = transverseEdge;
    converseEdge = converted.converse;
    transverseEdge = converted.transverse;
    if (converseEdge != oldConverseEdge)
      for (DoubleListener l : converseEdgeListeners) l.changed(oldConverseEdge, converseEdge);
    if (transverseEdge != oldTransverseEdge)
      for (DoubleListener l : transverseEdgeListeners) l.changed(oldTransverseEdge, transverseEdge);
  }

  public final void addConverseEdgeListener(DoubleListener listener) {
    converseEdgeListeners.add(listener);
  }

  public final double transverseEdge() {
    return halfConvert.convert(width(), height()).transverse;
  }

  public final void addTransverseEdgeListener(DoubleListener listener) {
    transverseEdgeListeners.add(listener);
  }

  public abstract void add(int index, DisplayNode node);

  public void add(final DisplayNode node) {
    add(childCount(), node);
  }

  public abstract int childCount();

  public abstract void remove(DisplayNode node);

  public abstract void setBackgroundColor(ModelColor color);

    public abstract double toPixels(Syntax.DisplayUnit displayUnit);

    public interface DisplayAbsoluteConvert {
    public Vector convert(double left, double right, double top, double bottom);

    public UnconvertVector unconvert(
        double converse, double transverse, double xSpan, double ySpan);

    public UnconvertAxis unconvertConverse(double converse, double xSpan, double ySpan);

    public UnconvertAxis unconvertTransverse(double transverse, double xSpan, double ySpan);
  }

  public interface DisplayHalfConvert {
    public Vector convert(double width, double height);

    public UnconvertVector unconvertSpan(double converse, double transverse);

    public UnconvertAxis unconvertConverseSpan(double span);

    public UnconvertAxis unconvertTransverseSpan(double span);
  }

  @FunctionalInterface
  public interface DoubleListener {
    void changed(double oldValue, double newValue);
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
}
