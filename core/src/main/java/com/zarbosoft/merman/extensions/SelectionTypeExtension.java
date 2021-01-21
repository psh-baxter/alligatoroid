package com.zarbosoft.merman.extensions;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.Cursor;
import com.zarbosoft.merman.editor.banner.BannerMessage;
import com.zarbosoft.merman.editor.visual.visuals.VisualFrontArray;
import com.zarbosoft.merman.editor.visual.visuals.VisualAtom;
import com.zarbosoft.merman.editor.visual.visuals.VisualFrontAtomBase;
import com.zarbosoft.merman.editor.visual.visuals.VisualFrontPrimitive;
import com.zarbosoft.merman.misc.TSMap;
import com.zarbosoft.merman.syntax.format.Format;

public class SelectionTypeExtension {
  private final Format format;
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
            if (cursor instanceof VisualFrontArray.ArrayCursor) {
              part = "array";
              final VisualFrontArray.ArrayCursor selection1 = (VisualFrontArray.ArrayCursor) cursor;
              final Atom child =
                  selection1.self.value.data.get(
                      selection1.leadFirst ? selection1.beginIndex : selection1.endIndex);
              innerId = child.type.id();
              innerName = child.type.name();
            } else if (cursor instanceof VisualFrontAtomBase.NestedCursor) {
              part = "nested";
              final Atom child = ((VisualFrontAtomBase) cursor.getVisual()).atomGet();
              innerId = child.type.id();
              innerName = child.type.name();
            } else if (cursor instanceof VisualFrontPrimitive.PrimitiveCursor) {
              part = "primitive";
              innerId = outerId;
              innerName = outerName;
            } else throw new AssertionError();
          }
          message.text =
              format.format(
                  new TSMap<String, Object>()
                      .putChain("outer_id", outerId)
                      .putChain("outer_name", outerName)
                      .putChain("part", part)
                      .putChain("inner_id", innerId)
                      .putChain("inner_name", innerName));
          context.banner.addMessage(context, message);
          if (oldMessage != null) {
            context.banner.removeMessage(
                context, oldMessage); // TODO oldMessage callback on finish?
          }
        }
      };

  public SelectionTypeExtension(Context context, Format format) {
    this.format = format;
    context.addSelectionListener(listener);
  }
}
