package com.zarbosoft.merman.editorcore.editing;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.Cursor;
import com.zarbosoft.merman.editor.visual.visuals.VisualFrontArray;
import com.zarbosoft.merman.editor.visual.visuals.VisualFrontAtomBase;
import com.zarbosoft.merman.editor.visual.visuals.VisualFrontPrimitive;
import com.zarbosoft.merman.editorcore.history.History;
import com.zarbosoft.merman.editorcore.editing.actions.ArrayActionSuffix;
import com.zarbosoft.merman.syntax.style.BoxStyle;
import com.zarbosoft.merman.syntax.symbol.Symbol;
import com.zarbosoft.merman.syntax.symbol.SymbolTextSpec;
import com.zarbosoft.rendaw.common.Assertion;

import java.util.Arrays;

public class GapExtension {
    public Symbol gapPlaceholder = new SymbolTextSpec("•");
    public BoxStyle gapChoiceStyle = new BoxStyle(padding, roundStart, roundEnd, roundOuterEdges, roundRadius, line, lineColor, lineThickness, fill, fillColor);

    public static void install(Context context, History history) {
        context.addSelectionListener(
                new Context.SelectionListener() {
                    @Override
                    public void selectionChanged(Context context, Cursor cursor0) {
                        context.removeActions(this);
                        if (cursor0 instanceof VisualFrontArray.ArrayCursor) {
                            VisualFrontArray.ArrayCursor cursor = (VisualFrontArray.ArrayCursor) cursor0;
                            context.addActions(this, Arrays.asList(
                                    new ArrayActionSuffix(history, cursor))
                            );
                        } else if (cursor0 instanceof VisualFrontAtomBase.NestedCursor) {
                            VisualFrontAtomBase.NestedCursor cursor = (VisualFrontAtomBase.NestedCursor) cursor0;
                        } else if (cursor0 instanceof VisualFrontPrimitive.PrimitiveCursor) {
                            VisualFrontPrimitive.PrimitiveCursor cursor = (VisualFrontPrimitive.PrimitiveCursor) cursor0;
                        } else throw new Assertion();
                    }
                });
    }
}
