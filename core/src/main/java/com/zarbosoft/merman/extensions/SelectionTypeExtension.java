package com.zarbosoft.merman.extensions;

import com.google.common.collect.ImmutableMap;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.Cursor;
import com.zarbosoft.merman.editor.banner.BannerMessage;
import com.zarbosoft.merman.editor.visual.visuals.VisualArray;
import com.zarbosoft.merman.editor.visual.visuals.VisualAtom;
import com.zarbosoft.merman.editor.visual.visuals.VisualNestedBase;
import com.zarbosoft.merman.editor.visual.visuals.VisualPrimitive;
import com.zarbosoft.merman.syntax.format.Format;

public class SelectionTypeExtension extends Extension {

  public Format format;

  @Override
  public State create(final ExtensionContext context) {
    return new ModuleState(context);
  }

  private class ModuleState extends State {
    private BannerMessage message;
    private final Context.SelectionListener listener =
        new Context.SelectionListener() {
          @Override
          public void selectionChanged(final Context context, final Cursor cursor) {
            BannerMessage oldMessage = message;
            message = new BannerMessage();
            message.priority = 100;
            final String outerId;
            final String outerName;
            {
              final VisualAtom nodeType = cursor.getVisual().parent().atomVisual();
              outerId = nodeType.type().id();
              outerName = nodeType.type().name();
            }
            final String part;
            final String innerId;
            final String innerName;
            {
              if (cursor instanceof VisualArray.ArrayCursor) {
                part = "array";
                final VisualArray.ArrayCursor selection1 =
                    (VisualArray.ArrayCursor) cursor;
                final Atom child =
                    selection1.self.value.data.get(
                        selection1.leadFirst ? selection1.beginIndex : selection1.endIndex);
                innerId = child.type.id();
                innerName = child.type.name();
              } else if (cursor instanceof VisualNestedBase.NestedCursor) {
                part = "nested";
                final Atom child = ((VisualNestedBase) cursor.getVisual()).atomGet();
                innerId = child.type.id();
                innerName = child.type.name();
              } else if (cursor instanceof VisualPrimitive.PrimitiveCursor) {
                part = "primitive";
                innerId = outerId;
                innerName = outerName;
              } else throw new AssertionError();
            }
            message.text =
                format.format(
                    new ImmutableMap.Builder()
                        .put("outer_id", outerId)
                        .put("outer_name", outerName)
                        .put("part", part)
                        .put("inner_id", innerId)
                        .put("inner_name", innerName)
                        .build());
            context.banner.addMessage(context, message);
            if (oldMessage != null) {
              context.banner.removeMessage(
                  context, oldMessage); // TODO oldMessage callback on finish?
              oldMessage = null;
            }
          }
        };

    public ModuleState(final Context context) {
      context.addSelectionListener(listener);
    }

    @Override
    public void destroy(final ExtensionContext context) {
      context.removeSelectionListener(listener);
    }
  }
}
