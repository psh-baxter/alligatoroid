package com.zarbosoft.merman.document;

import com.zarbosoft.merman.document.values.Field;
import com.zarbosoft.merman.document.values.FieldArray;
import com.zarbosoft.merman.document.values.FieldAtom;
import com.zarbosoft.merman.document.values.FieldPrimitive;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.editor.serialization.WriteState;
import com.zarbosoft.merman.editor.serialization.WriteStateBack;
import com.zarbosoft.merman.editor.visual.Visual;
import com.zarbosoft.merman.editor.visual.VisualParent;
import com.zarbosoft.merman.editor.visual.visuals.VisualAtom;
import com.zarbosoft.merman.syntax.AtomType;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;

import java.util.Map;

public class Atom {
  public final TSMap<String, Field> fields;
  /**
   * Null if root
   */
  public Field.Parent<?> valueParentRef;
  public final AtomType type;
  public VisualAtom visual;

  public Atom(final AtomType type, final TSMap<String, Field> fields) {
    this.type = type;
    this.fields = fields;
    for (Map.Entry<String, Field> entry : fields.entries()) {
      Field v = entry.getValue();
      v.setAtomParentRef(
          new Parent() {
            @Override
            public Atom atom() {
              return Atom.this;
            }

            @Override
            public boolean selectAtomParent(final Context context) {
              if (valueParentRef == null) return false;
              return Atom.this.valueParentRef.selectValue(context);
            }

            @Override
            public Path getSyntaxPath() {
              Path out;
              if (Atom.this.valueParentRef == null) out = new Path();
              else out = Atom.this.valueParentRef.getSyntaxPath();
              return out.add(entry.getKey());
            }
          });
    }
  }

  public Path getSyntaxPath() {
    if (valueParentRef == null) return new Path();
    else return valueParentRef.path();
  }

  public Visual ensureVisual(
          final Context context,
          final VisualParent parent,
          final int depth,
          final int depthScore) {
    if (visual != null) {
      visual.root(context, parent, depth, depthScore);
    } else {
      this.visual = new VisualAtom(context, parent, this, depth, depthScore);
    }
    return visual;
  }

  public void setValueParentRef(final Field.Parent<?> valueParentRef) {
    this.valueParentRef = valueParentRef;
  }

  public Object syntaxLocateStep(String segment) {
    return fields.getOpt(segment);
  }

  public void write(TSList<WriteState> stack) {
    TSMap<String, Object> childData = new TSMap<>();
    for (Map.Entry<String, Field> entry : fields.entries()) {
      if (entry.getValue() instanceof FieldAtom) {
        childData.put(entry.getKey(), ((FieldAtom) entry.getValue()).data);
      } else if (entry.getValue() instanceof FieldArray) {
        childData.put(entry.getKey(), ((FieldArray) entry.getValue()).data);
      } else if (entry.getValue() instanceof FieldPrimitive) {
        childData.put(entry.getKey(), ((FieldPrimitive) entry.getValue()).data);
      } else throw new Assertion();
    }
    stack.add(new WriteStateBack(childData, type.back().iterator()));
  }

  public abstract static class Parent {
    public abstract Atom atom();

    public abstract boolean selectAtomParent(final Context context);

    /**
     * Get path relative to syntax structure
     *
     * @return
     */
    public abstract Path getSyntaxPath();
  }
}
