package com.zarbosoft.merman.document;

import com.zarbosoft.merman.document.values.Value;
import com.zarbosoft.merman.document.values.ValueArray;
import com.zarbosoft.merman.document.values.ValueAtom;
import com.zarbosoft.merman.document.values.ValuePrimitive;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.editor.serialization.WriteState;
import com.zarbosoft.merman.editor.serialization.WriteStateBack;
import com.zarbosoft.merman.editor.visual.Visual;
import com.zarbosoft.merman.editor.visual.VisualParent;
import com.zarbosoft.merman.editor.visual.tags.TagsChange;
import com.zarbosoft.merman.editor.visual.visuals.VisualAtom;
import com.zarbosoft.merman.syntax.AtomType;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.ROSetRef;
import com.zarbosoft.rendaw.common.TSMap;
import com.zarbosoft.rendaw.common.TSSet;

import java.util.Deque;
import java.util.Map;

public class Atom {
  public final TSMap<String, Value> fields;
  /**
   * Null if root
   */
  public Value.Parent<?> valueParentRef;
  public final AtomType type;
  public VisualAtom visual;
  private final TSSet<String> tags;

  public Atom(final AtomType type, final TSMap<String, Value> fields) {
    this.type = type;
    this.fields = fields;
    tags = new TSSet<>();
    for (Map.Entry<String, Value> entry : fields.entries()) {
      Value v = entry.getValue();
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

  public void setValueParentRef(final Value.Parent<?> valueParentRef) {
    this.valueParentRef = valueParentRef;
  }

  public Object syntaxLocateStep(String segment) {
    return fields.getOpt(segment);
  }

  public void write(Deque<WriteState> stack) {
    TSMap<String, Object> childData = new TSMap<>();
    for (Map.Entry<String, Value> entry : fields.entries()) {
      if (entry.getValue() instanceof ValueAtom) {
        childData.put(entry.getKey(), ((ValueAtom) entry.getValue()).data);
      } else if (entry.getValue() instanceof ValueArray) {
        childData.put(entry.getKey(), ((ValueArray) entry.getValue()).data);
      } else if (entry.getValue() instanceof ValuePrimitive) {
        childData.put(entry.getKey(), ((ValuePrimitive) entry.getValue()).data);
      } else throw new Assertion();
    }
    stack.addLast(new WriteStateBack(childData, type.back().iterator()));
  }

  public ROSetRef<String> getTags() {
    return tags;
  }

  public void changeTags(Context context, final TagsChange change) {
    if (change.apply(tags) && visual != null) visual.tagsChanged(context);
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
