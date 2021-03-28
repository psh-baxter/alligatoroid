package com.zarbosoft.merman.core.extension;

import com.zarbosoft.merman.core.editor.Context;
import com.zarbosoft.merman.core.editor.Hoverable;
import com.zarbosoft.merman.core.editor.banner.BannerMessage;
import com.zarbosoft.merman.core.editor.visual.Visual;
import com.zarbosoft.merman.core.editor.visual.visuals.VisualAtom;
import com.zarbosoft.merman.core.editor.visual.visuals.VisualFrontArray;
import com.zarbosoft.merman.core.editor.visual.visuals.VisualFrontAtomBase;
import com.zarbosoft.merman.core.editor.visual.visuals.VisualFrontPrimitive;

public class HoverTypeExtension {
  private BannerMessage message;

  public HoverTypeExtension(Context context, boolean node, boolean part) {
    context.addHoverListener(
        new Context.HoverListener() {

          @Override
          public void hoverChanged(final Context context, final Hoverable hover) {
            BannerMessage oldMessage = message;
            message = null;
            if (hover != null) {
              message = new BannerMessage();
              message.priority = 100;
              final StringBuilder text = new StringBuilder();
              if (node) {
                final VisualAtom nodeType = hover.atom();
                if (nodeType == null) text.append("Root Element");
                else text.append((hover.atom()).type().name());
              }
              if (part) {
                final Visual part = hover.visual();
                final String temp;
                if (part instanceof VisualFrontArray) {
                  temp = "array";
                } else if (part instanceof VisualFrontPrimitive) {
                  temp = "primitive";
                } else if (part instanceof VisualFrontAtomBase) {
                  temp = "nested";
                } else temp = part.getClass().getSimpleName();
                if (text.length() > 0) text.append(" (" + temp + ")");
                else text.append(temp);
              }
              message.text = text.toString();
              context.banner.addMessage(context, message);
            }
            if (oldMessage != null) {
              context.banner.removeMessage(
                  context, oldMessage); // TODO oldMessage callback on finish?
              // oldMessage = null;
            }
          }
        });
  }
}
