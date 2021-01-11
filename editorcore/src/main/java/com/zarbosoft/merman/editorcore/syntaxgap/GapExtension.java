package com.zarbosoft.merman.editorcore.syntaxgap;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.Cursor;
import com.zarbosoft.merman.editor.Editor;
import com.zarbosoft.merman.editor.visual.visuals.VisualArray;
import com.zarbosoft.merman.editor.visual.visuals.VisualNestedBase;
import com.zarbosoft.merman.editor.visual.visuals.VisualPrimitive;
import com.zarbosoft.merman.editorcore.history.History;
import com.zarbosoft.merman.editorcore.syntaxgap.actions.ArrayActionSuffix;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.merman.syntax.style.BoxStyle;
import com.zarbosoft.merman.syntax.symbol.Symbol;
import com.zarbosoft.merman.syntax.symbol.SymbolTextSpec;
import com.zarbosoft.rendaw.common.Assertion;

import java.util.Arrays;

public class GapExtension {
    public Symbol gapPlaceholder = new SymbolTextSpec("•");
    public BoxStyle gapChoiceStyle = new BoxStyle();

    public static void install(Context context, History history) {
        context.addSelectionListener(
                new Context.SelectionListener() {
                    @Override
                    public void selectionChanged(Context context, Cursor cursor0) {
                        context.removeActions(this);
                        if (cursor0 instanceof VisualArray.ArrayCursor) {
                            VisualArray.ArrayCursor cursor = (VisualArray.ArrayCursor) cursor0;
                            context.addActions(this, Arrays.asList(
                                    new ArrayActionSuffix(history, cursor))
                            );
                        } else if (cursor0 instanceof VisualNestedBase.NestedCursor) {
                            VisualNestedBase.NestedCursor cursor = (VisualNestedBase.NestedCursor) cursor0;
                        } else if (cursor0 instanceof VisualPrimitive.PrimitiveCursor) {
                            VisualPrimitive.PrimitiveCursor cursor = (VisualPrimitive.PrimitiveCursor) cursor0;
                        } else throw new Assertion();
                    }
                });
    }
}
