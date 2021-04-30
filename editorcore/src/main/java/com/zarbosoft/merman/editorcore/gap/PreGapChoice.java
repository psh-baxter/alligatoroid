package com.zarbosoft.merman.editorcore.gap;

import com.zarbosoft.merman.core.syntax.FreeAtomType;
import com.zarbosoft.merman.core.syntax.front.FrontSpec;
import com.zarbosoft.rendaw.common.ROList;

public class PreGapChoice {
  public final FreeAtomType type;
  public final int consumePreceding;
  public final ROList<EditGapCursor.PrepareAtomField> supplyFillAtoms;
  public final ROList<FrontSpec> keySpecs;
  public final FrontSpec following;

  public PreGapChoice(
      FreeAtomType type,
      int consumePreceding,
      ROList<EditGapCursor.PrepareAtomField> supplyFillAtoms,
      ROList<FrontSpec> keySpecs,
      FrontSpec following) {
    this.type = type;
    this.consumePreceding = consumePreceding;
    this.supplyFillAtoms = supplyFillAtoms;
    this.keySpecs = keySpecs;
    this.following = following;
  }
}
