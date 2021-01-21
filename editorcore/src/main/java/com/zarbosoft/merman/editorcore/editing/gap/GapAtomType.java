package com.zarbosoft.merman.editorcore.editing.gap;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.Value;
import com.zarbosoft.merman.document.values.ValueArray;
import com.zarbosoft.merman.document.values.ValuePrimitive;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editorcore.editing.BaseGapAtomType;
import com.zarbosoft.merman.editorcore.editing.EditingExtension;
import com.zarbosoft.merman.misc.ROList;
import com.zarbosoft.merman.misc.TSMap;
import com.zarbosoft.merman.syntax.back.BackFixedTypeSpec;
import com.zarbosoft.merman.syntax.back.BackPrimitiveSpec;
import com.zarbosoft.merman.syntax.back.BackSpec;
import com.zarbosoft.merman.syntax.back.BaseBackPrimitiveSpec;
import com.zarbosoft.merman.editorcore.editing.FrontGapBase;
import com.zarbosoft.merman.syntax.front.FrontSpec;
import com.zarbosoft.merman.syntax.front.FrontSymbol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GapAtomType extends BaseGapAtomType {
  public static final String DEFAULT_TAG = "__gap";
  public static final String DEFAULT_ID = "__gap";
  protected final BaseBackPrimitiveSpec gapBack;
  private final List<BackSpec> back;
  protected List<FrontSpec> front;

  public GapAtomType(
      EditingExtension edit,
      String tag,
      String id,
      List<FrontSymbol> frontPrefix,
      List<FrontSymbol> frontSuffix) {
    super(edit, tag, id);
    gapBack = new BackPrimitiveSpec();
    gapBack.id = GAP_PRIMITIVE_KEY;
    final BackFixedTypeSpec gapTypeBack = new BackFixedTypeSpec();
    gapTypeBack.type = tag;
    gapTypeBack.value = gapBack;
    back = Arrays.asList(gapTypeBack);
    final FrontGapBase gapPrimitiveFront =
            new FrontGapBase() {
              @Override
              public void deselect(final Context context, final Atom self, final String string) {
                if (!string.isEmpty()) return;
                if (self.parent == null) return;
                final Value parentValue = self.parent.child;
                if (parentValue instanceof ValueArray) {
                  edit.arrayParentDelete((ValueArray.ArrayParent) self.parent);
                }
              }
            };
    front = new ArrayList<>();
    front.addAll(frontPrefix);
    front.add(gapPrimitiveFront);
    front.addAll(frontSuffix);
  }

  @Override
  public String name() {
    return "Gap";
  }

  public Atom create() {
    return new Atom(
        this, new TSMap<String, Value>().putChain("gap", new ValuePrimitive(gapBack, "")));
  }

  @Override
  public List<FrontSpec> front() {
    return front;
  }

  @Override
  public ROList<BackSpec> back() {
    return back;
  }
}
