package def.js;

import def.dom.Element;

public class ResizeObserver {
  public ResizeObserver(Runnable r) {}

  public native void observe(Element e);
}
