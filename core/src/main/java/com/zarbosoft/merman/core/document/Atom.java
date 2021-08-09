package com.zarbosoft.merman.core.document;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.document.fields.Field;
import com.zarbosoft.merman.core.document.fields.FieldArray;
import com.zarbosoft.merman.core.document.fields.FieldAtom;
import com.zarbosoft.merman.core.document.fields.FieldId;
import com.zarbosoft.merman.core.document.fields.FieldPrimitive;
import com.zarbosoft.merman.core.serialization.WriteState;
import com.zarbosoft.merman.core.serialization.WriteStateBack;
import com.zarbosoft.merman.core.syntax.AtomType;
import com.zarbosoft.merman.core.visual.Visual;
import com.zarbosoft.merman.core.visual.VisualParent;
import com.zarbosoft.merman.core.visual.visuals.VisualAtom;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROMap;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;

import java.util.HashMap;
import java.util.Map;

public class Atom {
  public final AtomType type;
  public ROList<Field> unnamedFields;
  public ROMap<String, Field> namedFields;
  /** Null if root */
  public Field.Parent<?> fieldParentRef;

  public VisualAtom visual;

  public Atom(final AtomType type) {
    this.type = type;
  }

  public void initialSet(TSList<Field> unnamedFields, final TSMap<String, Field> fields) {
    this.unnamedFields = unnamedFields;
    this.namedFields = fields;
    for (int i = 0; i < unnamedFields.size(); ++i) {
      Field field = unnamedFields.get(i);
      String strI = Integer.toString(i);
      field.setAtomParentRef(
          new Parent() {
            @Override
            public Atom atom() {
              return Atom.this;
            }

            @Override
            public boolean selectParent(Context context) {
              return false;
            }

            @Override
            public SyntaxPath getSyntaxPath() {
              SyntaxPath out;
              if (Atom.this.fieldParentRef == null) out = new SyntaxPath();
              else out = Atom.this.fieldParentRef.getSyntaxPath();
              return out.add("unnamed").add(strI);
            }
          });
    }
    for (Map.Entry<String, Field> entry : fields.entries()) {
      entry
          .getValue()
          .setAtomParentRef(
              new Parent() {
                @Override
                public Atom atom() {
                  return Atom.this;
                }

                @Override
                public boolean selectParent(final Context context) {
                  if (visual.needIntermediateCursor) {
                    visual.selectById(context, entry.getKey());
                    return true;
                  } else {
                    if (fieldParentRef == null) return false;
                    return Atom.this.fieldParentRef.selectField(context);
                  }
                }

                @Override
                public SyntaxPath getSyntaxPath() {
                  SyntaxPath out;
                  if (Atom.this.fieldParentRef == null) out = new SyntaxPath();
                  else out = Atom.this.fieldParentRef.getSyntaxPath();
                  return out.add("named").add(entry.getKey());
                }
              });
    }
  }

  public SyntaxPath getSyntaxPath() {
    if (fieldParentRef == null) return new SyntaxPath();
    else return fieldParentRef.path();
  }

  public boolean selectInto(final Context context) {
    if (context.window) context.windowAdjustMinimalTo(this);
    return visual.selectIntoAnyChild(context);
  }

  public Visual ensureVisual(
      final Context context, final VisualParent parent, final int depth, final int depthScore) {
    if (visual != null) {
      visual.root(context, parent, depth, depthScore);
    } else {
      this.visual = new VisualAtom(context, parent, this, depth, depthScore);
    }
    return visual;
  }

  public void setFieldParentRef(final Field.Parent<?> fieldParentRef) {
    this.fieldParentRef = fieldParentRef;
  }

  public Object syntaxLocateStep(String segment) {
    return namedFields.getOpt(segment);
  }

  public void write(TSList<WriteState> stack) {
    Map<Object, Object> childData = new HashMap<>();
    for (Field field : unnamedFields) {
      if (field instanceof FieldId) {
        childData.put(field.back(), ((FieldId) field).id );
    } else throw new Assertion();
  }
    for (Map.Entry<String, Field> entry : namedFields) {
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

    public abstract boolean selectParent(final Context context);

    /**
     * Get path relative to syntax structure
     *
     * @return
     */
    public abstract SyntaxPath getSyntaxPath();
  }
}
